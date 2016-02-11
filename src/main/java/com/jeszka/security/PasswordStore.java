package com.jeszka.security;

import org.apache.commons.codec.digest.DigestUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class PasswordStore {
    private static final String PASSWORDS_FILENAME = "credentials";
    private static final String ALGORITHM = "PBEWithMD5AndDES";
    private static final byte[] SALT = {
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
    };
    private static final char SPLIT_CHAR = ':';

    private SecretKeyFactory keyFactory;

    // TODO not work with Strings

    public String encrypt(String masterPassword, String property) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKey key = getKeyFactory().generateSecret(new PBEKeySpec(masterPassword.toCharArray()));
        Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")));
    }

    private String base64Encode(byte[] bytes) {
        return new BASE64Encoder().encode(bytes);
    }

    public String decrypt(String masterPassword, String property) throws GeneralSecurityException, IOException {
        SecretKey key = getKeyFactory().generateSecret(new PBEKeySpec(masterPassword.toCharArray()));
        Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }

    private byte[] base64Decode(String property) throws IOException {
        return new BASE64Decoder().decodeBuffer(property);
    }

    private SecretKeyFactory getKeyFactory() throws NoSuchAlgorithmException {
        if (keyFactory == null) {
            keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        }
        return keyFactory;
    }

    public boolean isAuthorized(String token) {
        final Path passwordsPath = Paths.get(PASSWORDS_FILENAME);
        if (Files.exists(passwordsPath)) {
            try {
                final String defaultLine =
                        Files.lines(passwordsPath)
                             .findFirst()
                             .get();
                String password = getPassword(defaultLine);
                decrypt(token, password);
                return true;
            } catch (IOException | GeneralSecurityException e) {
                // IOException will not occur - file existence checked
                // wrong token will generate BadPaddingException
                System.out.println("Not authorized: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * Logs in with provided password generating token,
     * that has to be used (as cookie value) in all future requests.
     * @param password application's master password
     * @return SHA1 token - generated from master password
     */
    public String login(char[] password) {
//        http://howtodoinjava.com/core-java/io/how-to-create-a-new-file-in-java/
        final Path passwordsPath = Paths.get(PASSWORDS_FILENAME);
        if (!Files.exists(passwordsPath)) {
            String sha1Token = getPasswordToken(password);
            // in newly created file store some encrypted data,
            // to have something to decrypt during authorization check
            try {
                String defaultLine = "defaultLine" + SPLIT_CHAR +
                        encrypt(sha1Token, UUID.randomUUID().toString()) + SPLIT_CHAR +
                        encrypt(sha1Token, UUID.randomUUID().toString()) +
                        System.lineSeparator();
                Files.write(passwordsPath, defaultLine.getBytes());
            } catch (GeneralSecurityException | IOException e) {
                System.out.println("Error during creation of credentials file: " + e.getMessage());
                return null;
            }
            return sha1Token;
        } else {
            System.out.println("Checking provided password...");
            final String passwordToken = getPasswordToken(password);
            return isAuthorized(passwordToken) ? passwordToken : null;
        }
    }

    private String getPasswordToken(char[] password) {
        byte[] passwordBytes = new byte[password.length*2];
        ByteBuffer.wrap(passwordBytes).asCharBuffer().put(password); // password stored as UTF-16
        return DigestUtils.sha1Hex(passwordBytes);
    }

    String getPassword(String appUserPassword) {
        if (appUserPassword.length() > 2 && appUserPassword.indexOf(SPLIT_CHAR) > -1) {
            final String[] split = appUserPassword.split(Character.toString(SPLIT_CHAR));
            return split.length == 3 ?
                    split[2] :
                    "";
        }
        else return "";
    }

    /**
     * @return true, if succeeded
     */
    public boolean storeCredentials(String appName, String hashedUser, String hashedPassword) {
        final Path passwordsPath = Paths.get(PASSWORDS_FILENAME);
        if (Files.exists(passwordsPath)) {
            try {
                Files.write(passwordsPath,
                        (appName + SPLIT_CHAR + hashedUser + SPLIT_CHAR + hashedPassword + System.lineSeparator()).getBytes(),
                        StandardOpenOption.APPEND);
                return true;
            } catch (IOException e) {
                System.out.println("Error writing credentials: " + e);
            }
        }
        return false;
    }
}
