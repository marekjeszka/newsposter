package com.jeszka.dao;

import com.jeszka.domain.AppCredentials;
import com.jeszka.security.PasswordStore;
import org.junit.After;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.Assert.*;

public class PosterDaoTest {

    private EmbeddedDatabase embeddedDatabase;

    @After
    public void tearDown() throws Exception {
        embeddedDatabase.shutdown();
    }

    private PosterDao preparePosterDao() {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(embeddedDatabase);
        PosterDao posterDao = new PosterDao();
        posterDao.setNamedParameterJdbcTemplate(template);
        return posterDao;
    }

    @Test
    public void testFindAllActiveAppNames() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScripts("db/sql/create-db.sql", "db/sql/insert-data.sql")
                .build();
        PosterDao posterDao = preparePosterDao();

        final List<String> allActiveAppNames = posterDao.findAllActiveAppNames();

        assertEquals(2, allActiveAppNames.size());
    }

    @Test
    public void testFindDefault_fail() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScripts("db/sql/create-db.sql", "db/sql/insert-data-no_default.sql")
                .build();
        PosterDao posterDao = preparePosterDao();

        assertNull(posterDao.findDefaultLine());
    }


    @Test
    public void testFindDefault() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScripts("db/sql/create-db.sql", "db/sql/insert-data.sql")
                .build();
        PosterDao posterDao = preparePosterDao();

        final AppCredentials defaultLine = posterDao.findDefaultLine();

        assertNotNull(defaultLine);
        assertEquals(PasswordStore.DEFAULT_LINE, defaultLine.getAppName());
    }

    @Test
    public void testFindByAppName() {
        final String appName = "wordpress_1";
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScripts("db/sql/create-db.sql", "db/sql/insert-data.sql")
                .build();
        PosterDao posterDao = preparePosterDao();

        final AppCredentials wordpress_1 = posterDao.findByAppName(appName);

        assertEquals(2, wordpress_1.getId().intValue());
        assertEquals(appName, wordpress_1.getAppName());
        assertEquals("word_hash", wordpress_1.getUsername());
        assertEquals("word_pass_hash", wordpress_1.getPassword());
        assertEquals(true, wordpress_1.getEnabled());
    }

    @Test
    public void testSave() {
        final String appName = "testApp";
        final AppCredentials appCredentials =
                new AppCredentials.Builder().appName(appName)
                                            .id(10)
                                            .username("u")
                                            .password("p")
                                            .enabled(true)
                                            .build();
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScripts("db/sql/create-db.sql", "db/sql/insert-data.sql")
                .build();
        PosterDao posterDao = preparePosterDao();

        posterDao.saveAppCredentials(appCredentials);

        assertEquals(appName, posterDao.findByAppName(appName).getAppName());
    }
}
