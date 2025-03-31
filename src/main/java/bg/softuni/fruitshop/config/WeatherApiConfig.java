package bg.softuni.fruitshop.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "weather.api")
public class WeatherApiConfig {

    private String url = "https://api.open-meteo.com/v1/forecast?latitude=42.6975&longitude=23.3241&hourly=temperature_2m&timezone=auto";

    @PostConstruct
    public void checkConfiguration() {
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("No URL is found");
        }
    }
}
