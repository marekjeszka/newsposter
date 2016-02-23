package com.jeszka.security;

import com.jeszka.dao.PosterDao;
import com.jeszka.domain.AppCredentials;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public class PasswordStore {
    public static final String DEFAULT_LINE = "defaultLine";

    private static final String ALGORITHM = "PBEWithMD5AndDES";
    private static final byte[] SALT = {
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
    };

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private SecretKeyFactory keyFactory;

    @Autowired
    PosterDao posterDao;

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
        final AppCredentials defaultLine = posterDao.findDefaultLine();
        if (defaultLine != null) {
            try {
                String password = defaultLine.getPassword();
                decrypt(token, password);
                return true;
            } catch (GeneralSecurityException | IOException e) {
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
     *
     * @param password application's master password
     * @return SHA1 token - generated from master password
     */
    public String login(char[] password) {
        if (posterDao.findDefaultLine() == null) {
            String sha1Token = getPasswordToken(password);
            // store some encrypted data,
            // to have something to decrypt during authorization check
            try {
                final AppCredentials build =
                        new AppCredentials.Builder().appName(DEFAULT_LINE)
                                                    .username(encrypt(sha1Token, UUID.randomUUID().toString()))
                                                    .password(encrypt(sha1Token, UUID.randomUUID().toString()))
                                                    .enabled(false)
                                                    .build();
                posterDao.saveAppCredentials(build);
            } catch (GeneralSecurityException | UnsupportedEncodingException e) {
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
        byte[] passwordBytes = new byte[password.length * 2];
        ByteBuffer.wrap(passwordBytes)
                  .asCharBuffer()
                  .put(password); // password stored as UTF-16
        return DigestUtils.sha1Hex(passwordBytes);
    }

    /**
     * @return true, if succeeded
     */
    public boolean storeCredentials(String appName, String hashedUser, String hashedPassword) {
        AppCredentials appCredentials = new AppCredentials.Builder().appName(appName)
                                                                    .username(hashedUser)
                                                                    .password(hashedPassword)
                                                                    .enabled(true)
                                                                    .build();
        final int result = posterDao.saveAppCredentials(appCredentials);
        System.out.println("Storing credentials result: " + result);
        return true;
    }

    public AppCredentials getCredentials(String appName, String masterPassword) {
        final AppCredentials appCredentials = posterDao.findByAppName(appName);

        try {
            return new AppCredentials.Builder().id(appCredentials.getId())
                                               .appName(appCredentials.getAppName())
                                               .username(decrypt(masterPassword, appCredentials.getUsername()))
                                               .password(decrypt(masterPassword, appCredentials.getPassword()))
                                               .enabled(appCredentials.getEnabled())
                                               .build();
        } catch (GeneralSecurityException | IOException e) {
            // IOException will not occur - file existence checked
            // wrong token will generate BadPaddingException
            System.out.println("Not authorized: " + e.getMessage());
        }
        return null;
    }

    public List<String> getStoredApps() {
        return posterDao.findAllActiveAppNames();
    }

    public static boolean isEmail(final String email) {
        return !StringUtils.isEmpty(email) && email.matches(EMAIL_PATTERN);
    }
}
