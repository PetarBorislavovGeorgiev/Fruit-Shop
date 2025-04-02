package bg.softuni.fruitshop;


import bg.softuni.fruitshop.address.model.Address;
import bg.softuni.fruitshop.address.repository.AddressRepository;
import bg.softuni.fruitshop.order.model.Order;
import bg.softuni.fruitshop.order.model.OrderItem;
import bg.softuni.fruitshop.order.repository.OrderItemRepository;
import bg.softuni.fruitshop.order.repository.OrderRepository;
import bg.softuni.fruitshop.product.model.Product;
import bg.softuni.fruitshop.product.repository.ProductRepository;
import bg.softuni.fruitshop.security.AuthenticationMetadata;
import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
@ActiveProfiles("test")
public class Order2ITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testOrderConfirmationPage_shouldReturnCorrectViewAndModel() throws Exception {
        User user = userRepository.save(TestBuilder.aRandomUser());


        AuthenticationMetadata metadata = new AuthenticationMetadata(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), user.isActive());


        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(metadata, null, metadata.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        Address address = TestBuilder.aRandomAddress(user, addressRepository);

        Order order = Order.builder()
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .customer(user)
                .address(address)
                .items(List.of())
                .build();

        order = orderRepository.save(order);

        mockMvc.perform(get("/confirmation"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirmation"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("total"));
    }


    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testOrderConfirmationPage_withOrderItems_shouldReturnCorrectTotal() throws Exception {
        User user = userRepository.save(TestBuilder.aRandomUser());

        AuthenticationMetadata metadata = new AuthenticationMetadata(
                user.getId(), user.getEmail(), user.getPassword(), user.getRole(), user.isActive()
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(metadata, null, metadata.getAuthorities())
        );

        Address address = TestBuilder.aRandomAddress(user, addressRepository);
        Order order = orderRepository.save(
                Order.builder()
                        .createdOn(LocalDateTime.now())
                        .updatedOn(LocalDateTime.now())
                        .customer(user)
                        .address(address)
                        .build()
        );

        Product product = productRepository.save(TestBuilder.aRandomProduct());

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .price(BigDecimal.valueOf(4.50))
                .quantity(3)
                .build();

        orderItemRepository.save(item);

        order.setItems(new java.util.ArrayList<>(List.of(item)));
        orderRepository.save(order);

        mockMvc.perform(get("/confirmation"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirmation"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", BigDecimal.valueOf(13.50)));
    }


}
