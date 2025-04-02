package bg.softuni.fruitshop;


import bg.softuni.fruitshop.address.model.Address;
import bg.softuni.fruitshop.address.repository.AddressRepository;
import bg.softuni.fruitshop.product.model.CategoryType;
import bg.softuni.fruitshop.product.model.Product;
import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.model.UserRole;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {
        return User.builder()
                .username("User_" + UUID.randomUUID())
                .password("123123")
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static User anInactiveUser() {
        return User.builder()
                .username("inactiveUser_" + UUID.randomUUID())
                .password("123123")
                .role(UserRole.USER)
                .isActive(false)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }


    public static Address aRandomAddress(User user, AddressRepository addressRepository) {

        Address address = new Address();
        address.setCity("Test City");
        address.setZipCode("12345");
        address.setStreet("Test Street 123");
        address.setUser(user);

        return addressRepository.save(address);

    }

    public static Product aRandomProduct() {
        return Product.builder()
                .name("Product_" + UUID.randomUUID())
                .price(BigDecimal.valueOf(4.50))
                .category(CategoryType.FRUIT)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static Address aTestAddress() {
        Address address = new Address();
        address.setCity("Sofia");
        address.setStreet("Test Street 42");
        address.setZipCode("1000");
        return address;
    }

}
