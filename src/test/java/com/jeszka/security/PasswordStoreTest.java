package com.jeszka.security;

import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.Assert.assertEquals;

public class PasswordStoreTest {
    @Test
    public void encryptDecryptTest() throws GeneralSecurityException, IOException {
        // https://commons.apache.org/proper/commons-codec/apidocs/src-html/org/apache/commons/codec/digest/DigestUtils.html#line.437
        String masterPassword = "512d60630ec3df27158620b1359173b884b0a551";
        String newPassword = "ddd123ddd";
        final String encrypted = PasswordStore.encrypt(masterPassword, newPassword);
        assertEquals(newPassword, PasswordStore.decrypt(masterPassword, encrypted));
    }
}
