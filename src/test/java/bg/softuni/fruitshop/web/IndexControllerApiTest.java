package bg.softuni.fruitshop.web;


import bg.softuni.fruitshop.cartItem.service.CartItemService;
import bg.softuni.fruitshop.exception.UsernameAlreadyExistException;
import bg.softuni.fruitshop.order.model.Order;
import bg.softuni.fruitshop.order.model.OrderItem;
import bg.softuni.fruitshop.order.service.OrderItemService;
import bg.softuni.fruitshop.product.model.Product;
import bg.softuni.fruitshop.security.AuthenticationMetadata;
import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.model.UserRole;
import bg.softuni.fruitshop.user.service.UserService;
import bg.softuni.fruitshop.weather.entity.Weather;
import bg.softuni.fruitshop.weather.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static bg.softuni.fruitshop.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private OrderItemService orderItemService;

    @MockitoBean
    private CartItemService cartItemService;

    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexViewWithWeatherData() throws Exception {
        Weather mockWeather = new Weather();
        mockWeather.setTimezone("Europe/Sofia");
        mockWeather.setTime(List.of("2025-04-02T15:00"));
        mockWeather.setTemperature(List.of(20.5));

        when(weatherService.getCurrentHourWeather()).thenReturn(Optional.of(mockWeather));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("weatherData"));
    }

    @Test
    void getRequestToRegisterEndpoint_shouldReturnRegisterView() throws Exception {

        MockHttpServletRequestBuilder request = get("/register");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {

        MockHttpServletRequestBuilder request = get("/login");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getRequestToLoginEndpointWithErrorParameter_shouldReturnLoginViewAndErrorMessageAttribute() throws Exception {

        MockHttpServletRequestBuilder request = get("/login").param("error", "");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest", "errorMessage"));
    }

    @Test
    void postRequestToRegisterEndpoint_happyPath() throws Exception {


        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Pesho")
                .formField("password", "123456")
                .with(csrf());


        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpointWhenUsernameAlreadyExist_thenRedirectToRegisterWithFlashParameter() throws Exception {


        when(userService.register(any())).thenThrow(new UsernameAlreadyExistException("Username already exist!"));
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Pesho")
                .formField("password", "123456")
                .with(csrf());


        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("usernameAlreadyExistMessage"));
        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpointWithInvalidData_returnRegisterView() throws Exception {


        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "")
                .formField("password", "")
                .with(csrf());


        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
        verify(userService, never()).register(any());
    }

    @Test
    void getAuthenticatedRequestToHome_returnsHomeView() throws Exception {


        when(userService.getById(any())).thenReturn(aRandomUser());

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));


        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(1)).getById(userId);
    }


    @Test
    void getUnauthenticatedRequestToHome_redirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/home");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
        verify(userService, never()).getById(any());
    }

    @Test
    void getAuthenticatedRequestToHome_returnsHomeViewWithGroupedOrdersAndTotals() throws Exception {
        UUID userId = UUID.randomUUID();

        User testUser = aRandomUser();
        testUser.setId(userId);

        when(userService.getById(userId)).thenReturn(testUser);


        Order testOrder = new Order();

        Product testProduct = new Product();
        testProduct.setName("Apple");

        OrderItem item1 = new OrderItem();
        item1.setPrice(new BigDecimal("5.00"));
        item1.setProduct(testProduct);

        OrderItem item2 = new OrderItem();
        item2.setPrice(new BigDecimal("10.00"));
        item2.setProduct(testProduct);

        Map<Order, List<OrderItem>> groupedOrders = Map.of(testOrder, List.of(item1, item2));

        when(orderItemService.getGroupedOrdersForUser(userId)).thenReturn(groupedOrders);


        AuthenticationMetadata principal = new AuthenticationMetadata(
                userId,
                "User123",
                "123123",
                UserRole.USER,
                true
        );

        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("groupedOrders"))
                .andExpect(model().attributeExists("orderTotals"));

        verify(userService, times(1)).getById(userId);
        verify(orderItemService, times(1)).getGroupedOrdersForUser(userId);
    }

    @Test
    void getRequestToAbout_returnsAboutView() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }

    @Test
    void getRequestToContact_returnsContactView() throws Exception {
        mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"));
    }

    @Test
    void getRequestToLocation_returnsLocationView() throws Exception {
        mockMvc.perform(get("/location"))
                .andExpect(status().isOk())
                .andExpect(view().name("location"));
    }




}
