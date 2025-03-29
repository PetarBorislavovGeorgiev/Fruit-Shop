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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public ModelAndView getProfileMenu(@PathVariable UUID id) {
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
            BindingResult bindingResult) {

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
}
