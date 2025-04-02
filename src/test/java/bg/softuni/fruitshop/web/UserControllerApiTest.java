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

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void updateUserProfile_shouldRedirect_onValidInput() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("user").role(UserRole.USER).build();

        when(userService.findByUsername("user")).thenReturn(user);

        mockMvc.perform(post("/users/" + userId + "/profile")
                        .with(csrf())
                        .param("username", "user")
                        .param("firstName", "Pesho")
                        .param("lastName", "Pesho")
                        .param("email", "Pesho@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/" + userId + "/profile"));

        verify(userService).editUserDetails(eq(userId), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void addAddress_shouldAddAndRedirect_onValidInput() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("admin").addresses(new HashSet<>()).build();

        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(post("/users/" + userId + "/add-address")
                        .with(csrf())
                        .param("city", "Sofia")
                        .param("street", "Test Street")
                        .param("zipCode", "1000"))
                .andExpect(redirectedUrl("/users/" + userId + "/profile"));

        verify(addressRepository).save(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void addAddress_shouldRedirectBack_onInvalidInput() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/users/" + userId + "/add-address")
                        .with(csrf())
                        .param("city", "") // invalid field
                )
                .andExpect(redirectedUrl("/users/" + userId + "/profile"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void allUsers_shouldReturnUsersPage() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users"));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void viewUserDetails_shouldReturnDetailsPage() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("admin").build();

        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(view().name("user-details"))
                .andExpect(model().attribute("user", user));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void editUserFormAdmin_shouldReturnEditForm() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("admin").build();

        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/edit/" + userId))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-user"))
                .andExpect(model().attribute("user", user));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateUserAdmin_shouldRedirectAfterUpdate() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/users/edit/" + userId)
                        .with(csrf())
                        .param("username", "admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/" + userId));

        verify(userService).updateUserFromAdmin(eq(userId), any());
    }



}
