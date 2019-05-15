package com.nilangpatel.worldgdp.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.nilangpatel.worldgdp.model.CountryLanguage;

import org.springframework.jdbc.core.RowMapper;

public class CountryLanguageRowMapper implements RowMapper<CountryLanguage> {
    public CountryLanguage mapRow(ResultSet rs, int rowNum) throws SQLException {
        CountryLanguage countryLanguage = new CountryLanguage();
        countryLanguage.setCountryCode(rs.getString("countrycode"));
        countryLanguage.setIsOfficial(rs.getString("isofficial"));
        countryLanguage.setLanguage(rs.getString("language"));
        countryLanguage.setPercentage(rs.getDouble("percentage"));
        return countryLanguage;
    }
}
