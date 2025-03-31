package bg.softuni.fruitshop.weather.repository;

import bg.softuni.fruitshop.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, UUID> {
    List<Weather> findTop10ByOrderByIdDesc();
}
