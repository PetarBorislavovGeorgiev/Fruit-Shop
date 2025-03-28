package bg.softuni.fruitshop.web;

import bg.softuni.fruitshop.order.model.OrderItem;
import bg.softuni.fruitshop.order.service.OrderItemService;
import bg.softuni.fruitshop.security.AuthenticationMetadata;
import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.service.UserService;
import bg.softuni.fruitshop.web.dto.LoginRequest;
import bg.softuni.fruitshop.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class IndexController {
    private final UserService userService;
    private final OrderItemService orderItemService;

    @Autowired
    public IndexController(UserService userService, OrderItemService orderItemService) {
        this.userService = userService;
        this.orderItemService = orderItemService;
    }

    @GetMapping("/")
    public String getIndexPage() {

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

        List<OrderItem> completedOrders = orderItemService.getCompletedOrdersForUser(user.getId());
        modelAndView.addObject("orders", completedOrders);

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
