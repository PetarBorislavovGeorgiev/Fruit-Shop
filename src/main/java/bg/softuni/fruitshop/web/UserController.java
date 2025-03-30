package bg.softuni.fruitshop.web;

import bg.softuni.fruitshop.address.model.Address;
import bg.softuni.fruitshop.address.repository.AddressRepository;
import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.service.UserService;
import bg.softuni.fruitshop.web.dto.AddAddressRequest;
import bg.softuni.fruitshop.web.dto.UserEditRequest;
import bg.softuni.fruitshop.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AddressRepository addressRepository;

    @Autowired
    public UserController(UserService userService, AddressRepository addressRepository) {
        this.userService = userService;
        this.addressRepository = addressRepository;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getProfileMenu(@PathVariable UUID id, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());

        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getId().equals(id)) {
            return new ModelAndView("redirect:/access-denied");
        }

        ModelAndView modelAndView = new ModelAndView("profile-menu");

        User user = userService.getById(id);
        UserEditRequest userEditRequest = DtoMapper.mapUserToUserEditRequest(user);

        modelAndView.addObject("userEditRequest", userEditRequest);

        AddAddressRequest addressForm;
        if (!user.getAddresses().isEmpty()) {
            Address address = user.getAddresses().iterator().next();

            addressForm = new AddAddressRequest();
            addressForm.setCity(address.getCity());
            addressForm.setZipCode(address.getZipCode());
            addressForm.setStreet(address.getStreet());
        } else {
            addressForm = new AddAddressRequest();
        }

        modelAndView.addObject("addAddressRequest", addressForm);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping("/{id}/profile")
    public ModelAndView updateUserProfile(
            @PathVariable UUID id,
            @Valid @ModelAttribute("userEditRequest") UserEditRequest userEditRequest,
            BindingResult bindingResult,
            Principal principal) {

        User currentUser = userService.findByUsername(principal.getName());

        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getId().equals(id)) {
            return new ModelAndView("redirect:/access-denied");
        }

        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("profile-menu");
            modelAndView.addObject("user", userService.getUserById(id));
            modelAndView.addObject("addressRequest", new AddAddressRequest());
            return modelAndView;
        }

        userService.editUserDetails(id, userEditRequest);
        modelAndView.setViewName("redirect:/users/" + id + "/profile");
        return modelAndView;
    }

    @PostMapping("/{id}/add-address")
    public String addAddress(@PathVariable UUID id,
                             @Valid @ModelAttribute("addAddressRequest") AddAddressRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please fill all address fields.");
            return "redirect:/users/" + id + "/profile";
        }

        User user = userService.getUserById(id);

        Address address = Address.builder()
                .city(request.getCity())
                .zipCode(request.getZipCode())
                .street(request.getStreet())
                .build();

        user.getAddresses().add(address);
        address.setUser(user);

        addressRepository.save(address);

        redirectAttributes.addFlashAttribute("successMessage", "Address added successfully.");
        return "redirect:/users/" + id + "/profile";
    }

    @GetMapping
    public String allUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users";
    }

    @GetMapping("/{id}")
    public String viewUserDetails(@PathVariable UUID id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "user-details";
    }

    @GetMapping("/edit/{id}")
    public String editUserFormAdmin(@PathVariable UUID id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "edit-user";
    }

    @PostMapping("/edit/{id}")
    public String updateUserAdmin(@PathVariable UUID id, @ModelAttribute("user") User updatedUser) {
        userService.updateUserFromAdmin(id, updatedUser);
        return "redirect:/users/" + id;
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateUser(@PathVariable UUID id, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());

        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getId().equals(id)) {
            return "redirect:/access-denied";
        }

        userService.deactivateUser(id);
        return "redirect:/users";
    }

    @PostMapping("/activate/{id}")
    public String activateUser(@PathVariable UUID id, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());

        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getId().equals(id)) {
            return "redirect:/access-denied";
        }

        userService.activateUser(id);
        return "redirect:/users";
    }
}
