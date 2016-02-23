package com.jeszka.dao;

import com.jeszka.domain.AppCredentials;
import com.jeszka.security.PasswordStore;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PosterDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public AppCredentials findDefaultLine() {
        return findByAppName(PasswordStore.DEFAULT_LINE);
    }

    public AppCredentials findByAppName(String appName) {
        String sql = "SELECT * FROM app_credentials WHERE appName = :appName";
        final Map<String, String> params = Collections.singletonMap(AppCredentials.APP_NAME, appName);
        AppCredentials defaultLine;
        try {
            defaultLine = namedParameterJdbcTemplate.queryForObject(sql, params, PosterDao::rowMapper);
        } catch (EmptyResultDataAccessException e) {
            defaultLine = null;
        }
        return defaultLine;
    }

    public List<String> findAllActiveAppNames() {
        String sql = "SELECT appName FROM app_credentials WHERE enabled = 1";
        return namedParameterJdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(AppCredentials.APP_NAME));
    }

    public int saveAppCredentials(AppCredentials appCredentials) {
        String sql = "INSERT INTO app_credentials " +
                "(appName, username, password, enabled)" +
                "VALUES (:appName, :username, :password, :enabled)";
        Map<String, Object> params = new HashMap<>();
        params.put(AppCredentials.APP_NAME, appCredentials.getAppName());
        params.put(AppCredentials.USERNAME, appCredentials.getUsername());
        params.put(AppCredentials.PASSWORD, appCredentials.getPassword());
        params.put(AppCredentials.ENABLED, appCredentials.isEnabled());
        return namedParameterJdbcTemplate.update(sql, params);
    }

    private static AppCredentials rowMapper(ResultSet rs, int rowNum) throws SQLException {
        return new AppCredentials.Builder().id(rs.getInt("id"))
                                   .appName(rs.getString(AppCredentials.APP_NAME))
                                   .username(rs.getString(AppCredentials.USERNAME))
                                   .password(rs.getString(AppCredentials.PASSWORD))
                                   // TODO appCredentials.setType();
                                   .enabled(rs.getBoolean(AppCredentials.ENABLED))
                                   .build();
    }
}
