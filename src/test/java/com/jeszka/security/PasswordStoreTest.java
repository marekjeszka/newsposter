package com.jeszka.security;

import org.junit.Test;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PasswordStoreTest {
    // https://commons.apache.org/proper/commons-codec/apidocs/src-html/org/apache/commons/codec/digest/DigestUtils.html#line.437
    String masterPassword = "512d60630ec3df27158620b1359173b884b0a551";
    String newPassword = "ddd123ddd";

    @Test
    public void encryptDecryptTest() throws GeneralSecurityException, IOException {
        final PasswordStore passwordStore = new PasswordStore();
        final String encrypted = passwordStore.encrypt(masterPassword, newPassword);
        assertEquals(newPassword, passwordStore.decrypt(masterPassword, encrypted));
    }

    @Test(expected = BadPaddingException.class)
    public void badDecryptingPasswordTest() throws GeneralSecurityException, IOException {
        final PasswordStore passwordStore = new PasswordStore();
        final String encrypted = passwordStore.encrypt(masterPassword, newPassword);
        passwordStore.decrypt("forgotten", encrypted);
    }

    @Test
    public void emptyMasterPasswordTest() throws GeneralSecurityException, IOException {
        final PasswordStore passwordStore = new PasswordStore();
        final String encrypted = passwordStore.encrypt("", newPassword);
        assertEquals(newPassword, passwordStore.decrypt("", encrypted));
    }

    @Test
    public void getPasswordTest() {
        assertEquals("pass", new PasswordStore().getPassword("app:me:pass"));
    }

    @Test
    public void getPasswordWrongStringTest() {
        assertNotEquals("pass", new PasswordStore().getPassword("me:pass"));
    }
}
