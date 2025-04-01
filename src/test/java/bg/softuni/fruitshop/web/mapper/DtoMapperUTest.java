package bg.softuni.fruitshop.web.mapper;

import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DtoMapperUTest {

    @Test
    void givenHappyPath_whenMappingUserToUserEditRequest(){


        User user = User.builder()
                .firstName("Vik")
                .lastName("Aleksandrov")
                .email("vik123@abv.bg")
                .profilePicture("www.image.com")
                .build();


        UserEditRequest resultDto = DtoMapper.mapUserToUserEditRequest(user);


        assertEquals(user.getFirstName(), resultDto.getFirstName());
        assertEquals(user.getLastName(), resultDto.getLastName());
        assertEquals(user.getEmail(), resultDto.getEmail());
        assertEquals(user.getProfilePicture(), resultDto.getProfilePicture());
    }
}
