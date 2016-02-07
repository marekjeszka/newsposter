package com.jeszka.security;

import org.junit.Test;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import static org.junit.Assert.assertEquals;

public class PasswordStoreTest {
    // https://commons.apache.org/proper/commons-codec/apidocs/src-html/org/apache/commons/codec/digest/DigestUtils.html#line.437
    String masterPassword = "512d60630ec3df27158620b1359173b884b0a551";
    String newPassword = "ddd123ddd";

    @Test
    public void encryptDecryptTest() throws GeneralSecurityException, IOException {
        final String encrypted = PasswordStore.encrypt(masterPassword, newPassword);
        assertEquals(newPassword, PasswordStore.decrypt(masterPassword, encrypted));
    }

    @Test(expected = BadPaddingException.class)
    public void badDecryptingPasswordTest() throws GeneralSecurityException, IOException {
        final String encrypted = PasswordStore.encrypt(masterPassword, newPassword);
        PasswordStore.decrypt("forgotten", encrypted);
    }

    @Test
    public void emptyMasterPasswordTest() throws GeneralSecurityException, IOException {
        final String encrypted = PasswordStore.encrypt("", newPassword);
        assertEquals(newPassword, PasswordStore.decrypt("", encrypted));
    }
}
