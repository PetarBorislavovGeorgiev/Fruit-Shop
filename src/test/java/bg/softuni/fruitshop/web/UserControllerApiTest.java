package bg.softuni.fruitshop.web;


import bg.softuni.fruitshop.address.repository.AddressRepository;
import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.model.UserRole;
import bg.softuni.fruitshop.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AddressRepository addressRepository;

    @Autowired
    private MockMvc mockMvc;


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void activateUser_shouldRedirectAndCallService() throws Exception {
        UUID userId = UUID.randomUUID();

        User mockUser = User.builder()
                .id(userId)
                .username("admin")
                .role(UserRole.ADMIN)
                .isActive(true)
                .build();

        when(userService.findByUsername("admin")).thenReturn(mockUser);

        mockMvc.perform(post("/users/activate/" + userId)
                        .with(csrf()))
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).activateUser(userId);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deactivateUser_shouldRedirectAndCallService() throws Exception {
        UUID userId = UUID.randomUUID();

        User mockUser = User.builder()
                .id(userId)
                .username("admin")
                .role(UserRole.ADMIN)
                .isActive(true)
                .build();

        when(userService.findByUsername("admin")).thenReturn(mockUser);

        mockMvc.perform(post("/users/deactivate/" + userId)
                        .with(csrf()))
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).deactivateUser(userId);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void activateUser_shouldRedirectToAccessDenied_ifUserNotAdminOrNotSelf() throws Exception {
        UUID userId = UUID.randomUUID();

        User mockUser = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .role(UserRole.USER)
                .isActive(true)
                .build();

        when(userService.findByUsername("user")).thenReturn(mockUser);

        mockMvc.perform(post("/users/activate/" + userId).with(csrf()))
                .andExpect(redirectedUrl("/access-denied"));

        verify(userService, never()).activateUser(any());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deactivateUser_shouldRedirectToAccessDenied_ifUserNotAdminOrNotSelf() throws Exception {
        UUID userId = UUID.randomUUID();

        User mockUser = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .role(UserRole.USER)
                .isActive(true)
                .build();

        when(userService.findByUsername("user")).thenReturn(mockUser);

        mockMvc.perform(post("/users/deactivate/" + userId).with(csrf()))
                .andExpect(redirectedUrl("/access-denied"));

        verify(userService, never()).deactivateUser(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getProfileMenu_shouldReturnProfileMenu_forAdminAccessingAnotherUser() throws Exception {
        UUID userId = UUID.randomUUID();

        User currentUser = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .role(UserRole.ADMIN)
                .build();

        User viewedUser = User.builder()
                .id(userId)
                .username("user")
                .role(UserRole.USER)
                .addresses(new HashSet<>())
                .build();

        when(userService.findByUsername("admin")).thenReturn(currentUser);
        when(userService.getById(userId)).thenReturn(viewedUser);

        mockMvc.perform(get("/users/" + userId + "/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("userEditRequest"))
                .andExpect(model().attributeExists("addAddressRequest"))
                .andExpect(view().name("profile-menu"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getProfileMenu_shouldReturnProfileMenu_forSameUser() throws Exception {
        UUID userId = UUID.randomUUID();

        User mockUser = User.builder()
                .id(userId)
                .username("user")
                .role(UserRole.USER)
                .addresses(new HashSet<>())
                .build();

        when(userService.findByUsername("user")).thenReturn(mockUser);
        when(userService.getById(userId)).thenReturn(mockUser);

        mockMvc.perform(get("/users/" + userId + "/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("userEditRequest"))
                .andExpect(model().attributeExists("addAddressRequest"))
                .andExpect(view().name("profile-menu"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getProfileMenu_shouldRedirect_ifUserTriesToAccessOtherProfile() throws Exception {
        UUID myId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();

        User mockUser = User.builder()
                .id(myId)
                .username("user")
                .role(UserRole.USER)
                .addresses(new HashSet<>())
                .build();

        when(userService.findByUsername("user")).thenReturn(mockUser);

        mockMvc.perform(get("/users/" + otherId + "/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));
    }




}
