package bg.softuni.fruitshop.user;

import bg.softuni.fruitshop.exception.DomainException;
import bg.softuni.fruitshop.exception.UsernameAlreadyExistException;
import bg.softuni.fruitshop.notification.service.NotificationService;
import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.model.UserRole;
import bg.softuni.fruitshop.user.repository.UserRepository;
import bg.softuni.fruitshop.user.service.UserService;
import bg.softuni.fruitshop.web.dto.RegisterRequest;
import bg.softuni.fruitshop.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;


    @Test
    void givenMissingUserFromDatabase_whenEditUserDetails_thenExceptionIsThrown() {

        UUID userId = UUID.randomUUID();
        UserEditRequest dto = UserEditRequest.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> userService.editUserDetails(userId, dto));
    }

    @Test
    void givenExistingUser_whenEditTheirProfileWithActualEmail_thenChangeTheirDetailsSaveNotificationPreferenceAndSaveToDatabase() {


        UUID userId = UUID.randomUUID();
        UserEditRequest dto = UserEditRequest.builder()
                .firstName("Pesho")
                .lastName("Pesho")
                .email("pesho@abv.bg")
                .profilePicture("www.image.com")
                .build();
        User user = User.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));


        userService.editUserDetails(userId, dto);


        assertEquals("Pesho", user.getFirstName());
        assertEquals("Pesho", user.getLastName());
        assertEquals("pesho@abv.bg", user.getEmail());
        assertEquals("www.image.com", user.getProfilePicture());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenExistingUsername_whenRegister_thenExceptionIsThrown() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Pesho")
                .password("Pesho")
                .build();
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));

        assertThrows(UsernameAlreadyExistException.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void givenHappyPath_whenRegister_thenUserIsSavedAndNotificationPreferenceCreated() {

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .password("encodedPass")
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);


        User result = userService.register(registerRequest);


        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(userCaptor.capture());
        verify(notificationService).saveNotificationPreference(savedUser.getId(), false, null);

        User capturedUser = userCaptor.getValue();

        assertEquals("testuser", capturedUser.getUsername());
        assertEquals("encodedPass", capturedUser.getPassword());
        assertTrue(capturedUser.isActive());
        assertEquals(UserRole.USER, capturedUser.getRole());
        assertNotNull(result.getId());
    }

    @Test
    void givenMissingUserFromDatabase_whenLoadUserByUsername_thenExceptionIsThrown() {


        String username = "Pesho";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());


        assertThrows(DomainException.class, () -> userService.loadUserByUsername(username));
    }

    @Test
    void givenExistingUsersInDatabase_whenGetAllUsers_thenReturnThemAll() {

        List<User> userList = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(userList);

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
    }

    @Test
    void givenExistingUsername_whenLoadUserByUsername_thenReturnsUserDetails() {


        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .username("Pesho")
                .password("123456")
                .role(UserRole.USER)
                .isActive(true)
                .build();

        when(userRepository.findByUsername("Pesho")).thenReturn(Optional.of(user));


        UserDetails result = userService.loadUserByUsername("Pesho");


        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getPassword(), result.getPassword());
        assertEquals(user.isActive(), result.isEnabled());
        assertTrue(result.getAuthorities().stream().anyMatch(
                a -> a.getAuthority().equals("ROLE_USER")
        ));

        verify(userRepository, times(1)).findByUsername("Pesho");
    }

    @Test
    void givenExistingId_whenGetUserById_thenReturnsUser() {

        UUID testId = UUID.randomUUID();

        User user = User.builder()
                .id(testId)
                .username("Pesho")
                .build();

        when(userRepository.findById(testId)).thenReturn(Optional.of(user));


        User result = userService.getUserById(testId);


        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("Pesho", result.getUsername());
        verify(userRepository, times(1)).findById(testId);
    }

    @Test
    void givenMissingId_whenGetUserById_thenThrowsException() {

        UUID testId = UUID.randomUUID();

        when(userRepository.findById(testId)).thenReturn(Optional.empty());


        DomainException exception = assertThrows(DomainException.class, () -> userService.getUserById(testId));
        assertTrue(exception.getMessage().contains("User with id"));
        verify(userRepository, times(1)).findById(testId);
    }


    @Test
    void givenExistingUsername_whenFindByUsername_thenReturnsUser() {

        String username = "Pesho";
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = userService.findByUsername(username);

        assertNotNull(result);
        assertEquals("Pesho", result.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void givenMissingUsername_whenFindByUsername_thenThrowsException() {

        String username = "Pesho";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());


        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.findByUsername(username));

        assertEquals("User not found: Pesho", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
    }


    @Test
    void givenValidUserData_whenUpdateUserFromAdmin_thenUserIsUpdated() {

        UUID userId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(userId)
                .username("oldUsername")
                .firstName("Old")
                .lastName("Name")
                .email("old@email.com")
                .role(UserRole.USER)
                .isActive(true)
                .build();

        User updatedUser = User.builder()
                .username("newUsername")
                .firstName("New")
                .lastName("Name")
                .email("new@email.com")
                .role(UserRole.ADMIN)
                .isActive(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));


        userService.updateUserFromAdmin(userId, updatedUser);


        verify(userRepository).save(argThat(savedUser ->
                savedUser.getUsername().equals("newUsername") &&
                        savedUser.getFirstName().equals("New") &&
                        savedUser.getLastName().equals("Name") &&
                        savedUser.getEmail().equals("new@email.com") &&
                        savedUser.getRole().equals(UserRole.ADMIN) &&
                        !savedUser.isActive()
        ));
    }

    @Test
    void givenUserId_whenDeactivateUser_thenUserIsDeactivatedAndSaved() {

        UUID userId = UUID.randomUUID();
        User activeUser = User.builder()
                .id(userId)
                .username("Pesho")
                .email("pesho@example.com")
                .isActive(true)
                .createdOn(LocalDateTime.now().minusDays(1))
                .updatedOn(LocalDateTime.now().minusDays(1))
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));

        userService.deactivateUser(userId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertFalse(savedUser.isActive());
        assertEquals(userId, savedUser.getId());
        assertNotNull(savedUser.getUpdatedOn());
    }

    @Test
    void givenValidUserId_whenActivateUser_thenUserIsActivatedAndSaved() {

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("Pesho")
                .email("pesho@example.com")
                .role(UserRole.USER)
                .isActive(false)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.activateUser(userId);

        assertTrue(user.isActive());

        verify(userRepository).save(user);
    }


}


