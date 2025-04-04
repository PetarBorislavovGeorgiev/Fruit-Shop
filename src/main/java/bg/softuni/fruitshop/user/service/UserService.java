package bg.softuni.fruitshop.user.service;

import bg.softuni.fruitshop.exception.DomainException;
import bg.softuni.fruitshop.exception.UsernameAlreadyExistException;
import bg.softuni.fruitshop.notification.service.NotificationService;
import bg.softuni.fruitshop.security.AuthenticationMetadata;
import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.model.UserRole;
import bg.softuni.fruitshop.user.repository.UserRepository;
import bg.softuni.fruitshop.web.dto.RegisterRequest;
import bg.softuni.fruitshop.web.dto.UserEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }


    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionUser = userRepository.findByUsername(registerRequest.getUsername());
        if (optionUser.isPresent()) {
            throw new UsernameAlreadyExistException("Username [%s] already exist.".formatted(registerRequest.getUsername()));
        }

        User user = userRepository.save(initializeUser(registerRequest));
        notificationService.saveNotificationPreference(user.getId(), false, null);

        log.info("Successfully create new user account for username [%s] and id [%s]".formatted(user.getUsername(), user.getId()));

        return user;
    }

    @CacheEvict(value = "users", allEntries = true)
    public void editUserDetails(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setEmail(userEditRequest.getEmail());
        user.setProfilePicture(userEditRequest.getProfilePicture());


        userRepository.save(user);
    }

    private User initializeUser(RegisterRequest registerRequest) {

        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    @Cacheable("users")
    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public User getById(UUID id) {

        Optional<User> user = userRepository.findById(id);

        user.orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(id)));

        return user.get();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new DomainException("User with this username does not exist."));

        return new AuthenticationMetadata(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }

    public User getUserById(UUID id) {
        Optional<User> user = userRepository.findById(id);
        user.orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(id)));
        return user.get();
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }


    public void updateUserFromAdmin(UUID id, User updatedData) {
        User existingUser = getUserById(id);

        existingUser.setFirstName(updatedData.getFirstName());
        existingUser.setLastName(updatedData.getLastName());
        existingUser.setEmail(updatedData.getEmail());
        existingUser.setUsername(updatedData.getUsername());
        existingUser.setRole(updatedData.getRole());
        existingUser.setActive(updatedData.isActive());
        existingUser.setUpdatedOn(LocalDateTime.now());

        userRepository.save(existingUser);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void deactivateUser(UUID userId) {
        User user = getUserById(userId);
        user.setActive(false);
        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void activateUser(UUID userId) {
        User user = getUserById(userId);
        user.setActive(true);
        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);
    }
}
