package bg.softuni.fruitshop.weather.service;

import bg.softuni.fruitshop.config.WeatherApiConfig;
import bg.softuni.fruitshop.weather.entity.Weather;
import bg.softuni.fruitshop.weather.repository.WeatherRepository;
import bg.softuni.fruitshop.web.dto.WeatherRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class WeatherService {


    private final WeatherRepository weatherRepository;
    private final RestClient restClient;
    private final WeatherApiConfig weatherApiConfig;

    @Autowired
    public WeatherService(WeatherRepository weatherRepository, RestClient restClient, WeatherApiConfig weatherApiConfig) {
        this.weatherRepository = weatherRepository;
        this.restClient = restClient;
        this.weatherApiConfig = weatherApiConfig;
    }

    public boolean hasInitializedWeatherData() {

        return weatherRepository.count() > 0;
    }

    public WeatherRequest fetchWeather(){
        return restClient.get().uri(weatherApiConfig.getUrl()).accept(MediaType.APPLICATION_JSON).retrieve().body(WeatherRequest.class);
    }


    public void updateWeather(WeatherRequest weatherRequest) {

        Weather weather = new Weather();
        weather.setTimezone(weatherRequest.getTimezone());
        weather.setTime(weatherRequest.getHourly().getTime());
        weather.setTemperature(weatherRequest.getHourly().getTemperature_2m());

        weatherRepository.save(weather);


    }

    public List<Weather> getLatestWeather() {
        return weatherRepository.findAll();
    }

    public Optional<Weather> getCurrentHourWeather() {
        List<Weather> weatherData = weatherRepository.findTop10ByOrderByIdDesc();
        if (weatherData.isEmpty()) {
            return Optional.empty();
        }

        Weather latestWeather = weatherData.getFirst();
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00");

        String formattedCurrentTime = currentDateTime.format(formatter);

        for (int i = 0; i < latestWeather.getTime().size(); i++) {
            if (latestWeather.getTime().get(i).equals(formattedCurrentTime)) {
                Weather currentWeather = new Weather();
                currentWeather.setTimezone(latestWeather.getTimezone());
                currentWeather.setTime(List.of(latestWeather.getTime().get(i)));
                currentWeather.setTemperature(List.of(latestWeather.getTemperature().get(i)));
                return Optional.of(currentWeather);
            }
        }
        return Optional.empty();
    }
}
