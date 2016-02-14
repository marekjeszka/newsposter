package com.jeszka.security;

import com.jeszka.domain.AppCredentials;
import org.junit.Test;
import org.mockito.Mockito;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class PasswordStoreTest {
    private final static String CREDENTIALS_TEST_PASS = "TDD";
    private final static String CREDENTIALS_TEST_TOKEN = "fc51c109863cbba59bc32b949f736db737f4b30d";
    private final static String CREDENTIALS_TEST_PATH = "src/test/resources/credentials_test";

    private String masterPassword = "512d60630ec3df27158620b1359173b884b0a551";
    private String newPassword = "ddd123ddd";

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

    private PasswordStore getPasswordStoreSpied() {
        final PasswordStore passwordStore = Mockito.spy(new PasswordStore());
        when(passwordStore.getPasswordPath()).thenReturn(Paths.get(CREDENTIALS_TEST_PATH));
        return passwordStore;
    }

    @Test
    public void isAuthorizedTest() {
        final PasswordStore passwordStore = getPasswordStoreSpied();

        assertEquals(CREDENTIALS_TEST_TOKEN, passwordStore.login(CREDENTIALS_TEST_PASS.toCharArray()));
        assertNull(passwordStore.login("forgotten".toCharArray()));
    }

    @Test
    public void storeCredentialsTest() throws IOException {
        final PasswordStore passwordStore = Mockito.spy(new PasswordStore());
        final Path tempFile = Paths.get("src/test/resources/temp_credentials_test");
        Files.createFile(tempFile);
        when(passwordStore.getPasswordPath()).thenReturn(tempFile);

        assertTrue(passwordStore.storeCredentials("testApp", "1234", "5678"));
        final Optional<String> credentials = Files.lines(tempFile).findFirst();
        assertTrue(credentials.get().startsWith("testApp"));

        // cleanup
        try {
            Files.delete(tempFile);
        } catch (IOException e) {
            // ignore
        }
    }

    @Test
    public void getCredentialsTest() {
        final AppCredentials defaultLine =
                getPasswordStoreSpied().getCredentials("defaultLine", CREDENTIALS_TEST_TOKEN);
        assertEquals("defaultLine", defaultLine.getAppName());
        assertNotNull(defaultLine.getUsername());
        assertNotNull(defaultLine.getPassword());
    }
}
