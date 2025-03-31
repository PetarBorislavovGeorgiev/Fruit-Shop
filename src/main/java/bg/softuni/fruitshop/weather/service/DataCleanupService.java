package bg.softuni.fruitshop.weather.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataCleanupService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DataCleanupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void cleanWeatherData() {
        String deleteWeatherTimeQuery = "DELETE FROM weather_time";
        String deleteWeatherTemperatureQuery = "DELETE FROM weather_temperature";
        String deleteWeatherQuery = "DELETE FROM weather";

        jdbcTemplate.update(deleteWeatherTimeQuery);
        jdbcTemplate.update(deleteWeatherTemperatureQuery);
        jdbcTemplate.update(deleteWeatherQuery);
    }
}
