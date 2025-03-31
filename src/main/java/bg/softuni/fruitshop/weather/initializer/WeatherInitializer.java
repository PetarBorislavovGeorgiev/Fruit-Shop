package bg.softuni.fruitshop.weather.initializer;

import bg.softuni.fruitshop.weather.service.WeatherService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class WeatherInitializer implements CommandLineRunner {

    private final WeatherService weatherService;
    public WeatherInitializer(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!weatherService.hasInitializedWeatherData()){
            weatherService.updateWeather(weatherService.fetchWeather());
        }
    }
}
