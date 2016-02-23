package com.jeszka.security;

import com.jeszka.domain.AppCredentials;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PasswordStore {
    public static final String DEFAULT_LINE = "defaultLine";

    private static final String PASSWORDS_FILENAME = "credentials";
    private static final String ALGORITHM = "PBEWithMD5AndDES";
    private static final byte[] SALT = {
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
    };
    private static final char SPLIT_CHAR = ':';

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private SecretKeyFactory keyFactory;

    // TODO not work with Strings

    Path getPasswordPath() {
        return Paths.get(PASSWORDS_FILENAME);
    }

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
        final Path passwordPath = getPasswordPath();
        if (Files.exists(passwordPath)) {
            try {
                final String defaultLine =
                        Files.lines(passwordPath)
                             .findFirst()
                             .get();
                String password = getPassword(defaultLine);
                decrypt(token, password);
                return true;
            } catch (IOException | GeneralSecurityException e) {
                // IOException will not occur - file existence checked
                // wrong token will generate BadPaddingException
                System.out.println("Not authorized: " + e.getMessage());
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
        final Path passwordPath = getPasswordPath();
        if (!Files.exists(passwordPath)) {
            String sha1Token = getPasswordToken(password);
            // in newly created file store some encrypted data,
            // to have something to decrypt during authorization check
            try {
                String defaultLine = DEFAULT_LINE + SPLIT_CHAR +
                        encrypt(sha1Token, UUID.randomUUID().toString()) + SPLIT_CHAR +
                        encrypt(sha1Token, UUID.randomUUID().toString()) +
                        System.lineSeparator();
                Files.write(passwordPath, defaultLine.getBytes());
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
        final Path passwordPath = getPasswordPath();
        if (Files.exists(passwordPath)) {
            try {
                Files.write(passwordPath,
                        (appName + SPLIT_CHAR + hashedUser + SPLIT_CHAR + hashedPassword + System.lineSeparator()).getBytes(),
                        StandardOpenOption.APPEND);
                return true;
            } catch (IOException e) {
                System.out.println("Error writing credentials: " + e);
            }
        }
        return false;
    }

    public AppCredentials getCredentials(String appName, String masterPassword) {
        final Path passwordPath = getPasswordPath();
        if (Files.exists(passwordPath)) {
            try {
                final Optional<String> foundCredentials =
                        Files.lines(passwordPath)
                             .filter(s -> s.startsWith(appName))
                             .findFirst();
                if (foundCredentials.isPresent()) {
                    return mapLineToCredentials(foundCredentials.get(), masterPassword);
                }
            } catch (IOException | GeneralSecurityException e) {
                // IOException will not occur - file existence checked
                // wrong token will generate BadPaddingException
                System.out.println("Not authorized: " + e.getMessage());
            }
        }
        return null;
    }

    private AppCredentials mapLineToCredentials(String foundCredentials, String masterPassword) throws GeneralSecurityException, IOException {
        if (foundCredentials.length() > 2 && foundCredentials.indexOf(SPLIT_CHAR) > -1) {
            final String[] split = foundCredentials.split(Character.toString(SPLIT_CHAR));
            return split.length == 3 ?
                    new AppCredentials(split[0], decrypt(masterPassword, split[1]), decrypt(masterPassword, split[2])) :
                    null;
        }
        return null;
    }

    private boolean isCorrectLine() {
        // TODO implement regex
        return false;
    }

    public List<String> getStoredApps() {
        // TODO retrieve only active apps
        final Path passwordPath = getPasswordPath();
        if (Files.exists(passwordPath)) {
            try {
                return Files.lines(passwordPath)
                            .filter(s -> !s.startsWith(DEFAULT_LINE))
                            .map(s -> s.split(Character.toString(SPLIT_CHAR))[0]) // TODO NPE
                            .collect(Collectors.toList());
            } catch (IOException e) {
                System.out.println("Error reading stored apps: " + e.getMessage());
            }
        }
        return null;
    }

    public static boolean isEmail(final String email) {
        return !StringUtils.isEmpty(email) && email.matches(EMAIL_PATTERN);
    }
}
