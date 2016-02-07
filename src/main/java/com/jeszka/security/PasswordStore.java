package com.jeszka.security;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

public class PasswordStore {
    private static final String algorithm = "PBEWithMD5AndDES";
    private static final byte[] SALT = {
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
    };

    private static SecretKeyFactory keyFactory;

    static String encrypt(String masterPassword, String property) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKey key = getKeyFactory().generateSecret(new PBEKeySpec(masterPassword.toCharArray()));
        Cipher pbeCipher = Cipher.getInstance(algorithm);
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")));
    }

    private static String base64Encode(byte[] bytes) {
        return new BASE64Encoder().encode(bytes);
    }

    static String decrypt(String masterPassword, String property) throws GeneralSecurityException, IOException {
        SecretKey key = getKeyFactory().generateSecret(new PBEKeySpec(masterPassword.toCharArray()));
        Cipher pbeCipher = Cipher.getInstance(algorithm);
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }

    private static byte[] base64Decode(String property) throws IOException {
        return new BASE64Decoder().decodeBuffer(property);
    }

    private static SecretKeyFactory getKeyFactory() throws NoSuchAlgorithmException {
        if (keyFactory == null) {
            keyFactory = SecretKeyFactory.getInstance(algorithm);
        }
        return keyFactory;
    }
}
