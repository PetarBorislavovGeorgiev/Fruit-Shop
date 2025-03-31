package bg.softuni.fruitshop.weather.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "weather")
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String timezone;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "weather_time", joinColumns = @JoinColumn(name = "weather_id"))
    @Column(name = "time")
    private List<String> time;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "weather_temperature", joinColumns = @JoinColumn(name = "weather_id"))
    @Column(name = "temperature")
    private List<Double> temperature;
}
