package bg.softuni.fruitshop.weather.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTasks {

    private final DataCleanupService dataCleanupService;

    public ScheduledTasks(DataCleanupService dataCleanupService) {
        this.dataCleanupService = dataCleanupService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyCleanupTask() {
        dataCleanupService.cleanWeatherData();
    }
}
