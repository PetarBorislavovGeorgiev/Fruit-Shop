package bg.softuni.fruitshop.notification.client.dto;

import lombok.Data;

@Data
public class NotificationPreference {

    private String type;

    private boolean enabled;

    private String contactInfo;
}
