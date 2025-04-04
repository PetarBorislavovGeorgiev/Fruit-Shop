package bg.softuni.fruitshop.web;

import bg.softuni.fruitshop.order.model.Order;
import bg.softuni.fruitshop.order.model.OrderItem;
import bg.softuni.fruitshop.order.service.OrderItemService;
import bg.softuni.fruitshop.security.AuthenticationMetadata;
import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.service.UserService;
import bg.softuni.fruitshop.weather.service.WeatherService;
import bg.softuni.fruitshop.web.dto.LoginRequest;
import bg.softuni.fruitshop.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    private final UserService userService;
    private final OrderItemService orderItemService;
    private final WeatherService weatherService;

    @Autowired
    public IndexController(UserService userService, OrderItemService orderItemService, WeatherService weatherService) {
        this.userService = userService;
        this.orderItemService = orderItemService;
        this.weatherService = weatherService;
    }

    @GetMapping("/")
    public String getIndexPage(Model model) {

        weatherService.getCurrentHourWeather().ifPresent(currentWeather -> {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");


            List<String> mutableTimeList = new ArrayList<>(currentWeather.getTime());


            for (int i = 0; i < mutableTimeList.size(); i++) {
                String originalTime = mutableTimeList.get(i);
                LocalDateTime parsedTime = LocalDateTime.parse(originalTime);
                String formattedTime = parsedTime.format(formatter);
                mutableTimeList.set(i, formattedTime);
            }

            currentWeather.setTime(mutableTimeList);
            model.addAttribute("weatherData", currentWeather);
        });

        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "Incorrect username or password!");
        }

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.register(registerRequest);

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject("user", user);

        Map<Order, List<OrderItem>> groupedOrdersForUser = new LinkedHashMap<>();

        orderItemService.getGroupedOrdersForUser(user.getId()).entrySet().stream()
                .sorted((e1, e2) -> e2.getKey().getCreatedOn().compareTo(e1.getKey().getCreatedOn()))
                .forEachOrdered(e -> groupedOrdersForUser.put(e.getKey(), e.getValue()));

        Map<Order, BigDecimal> orderTotals = new LinkedHashMap<>();

        groupedOrdersForUser.forEach((order, items) -> {
            BigDecimal sum = items.stream()
                    .map(OrderItem::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            orderTotals.put(order, sum);
        });
        modelAndView.addObject("groupedOrders", groupedOrdersForUser);
        modelAndView.addObject("orderTotals", orderTotals);

        return modelAndView;
    }

    @GetMapping("/about")
    public String getAboutPage() {

        return "about";
    }

    @GetMapping("/contact")
    public String getContactPage() {

        return "contact";
    }

    @GetMapping("/location")
    public String getLocationPage() {

        return "location";
    }

}
