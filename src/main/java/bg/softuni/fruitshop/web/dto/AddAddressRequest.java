package bg.softuni.fruitshop.web.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddAddressRequest {

    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "Postal code is required.")
    private String zipCode;

    @NotBlank(message = "Street is required.")
    private String street;

}
