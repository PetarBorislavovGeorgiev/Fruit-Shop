package bg.softuni.fruitshop.weather;


import bg.softuni.fruitshop.config.WeatherApiConfig;
import bg.softuni.fruitshop.weather.entity.Weather;
import bg.softuni.fruitshop.weather.repository.WeatherRepository;
import bg.softuni.fruitshop.weather.service.WeatherService;
import bg.softuni.fruitshop.web.dto.WeatherRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceUTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private RestClient restClient;

    @Mock
    private WeatherApiConfig weatherApiConfig;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    void hasInitializedWeatherData_shouldReturnTrue_whenDataExists() {
        when(weatherRepository.count()).thenReturn(5L);
        boolean result = weatherService.hasInitializedWeatherData();
        assertThat(result).isTrue();
    }

    @Test
    void updateWeather_shouldSaveWeatherEntity() {

        WeatherRequest mockRequest = new WeatherRequest();

        var hourly = new WeatherRequest.HourlyData();
        hourly.setTime(List.of("2025-04-03T10:00"));
        hourly.setTemperature_2m(List.of(20.5));
        mockRequest.setTimezone("Europe/Sofia");
        mockRequest.setHourly(hourly);

        weatherService.updateWeather(mockRequest);

        ArgumentCaptor<Weather> captor = ArgumentCaptor.forClass(Weather.class);
        verify(weatherRepository).save(captor.capture());

        Weather saved = captor.getValue();
        assertThat(saved.getTimezone()).isEqualTo("Europe/Sofia");
        assertThat(saved.getTime()).containsExactly("2025-04-03T10:00");
        assertThat(saved.getTemperature()).containsExactly(20.5);
    }


    @Test
    void getLatestWeather_shouldReturnAllWeatherData() {
        List<Weather> mockList = List.of(new Weather(), new Weather());
        when(weatherRepository.findAll()).thenReturn(mockList);
        List<Weather> result = weatherService.getLatestWeather();
        assertThat(result).hasSize(2);
    }


}
