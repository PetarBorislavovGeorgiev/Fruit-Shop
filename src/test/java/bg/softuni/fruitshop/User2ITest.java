package bg.softuni.fruitshop;


import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.model.UserRole;
import bg.softuni.fruitshop.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class User2ITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testActivateUser_shouldUpdateActiveFlag() throws Exception {

        User admin = User.builder()
                .username("admin")
                .password("pass")
                .role(UserRole.ADMIN)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        userRepository.save(admin);


        User user = TestBuilder.anInactiveUser();
        user = userRepository.save(user);
        UUID userId = user.getId();

        mockMvc.perform(post("/users/activate/" + userId).with(csrf()))
                .andExpect(redirectedUrl("/users"));

        entityManager.flush();
        entityManager.clear();

        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.isActive()).isTrue();
    }



}
