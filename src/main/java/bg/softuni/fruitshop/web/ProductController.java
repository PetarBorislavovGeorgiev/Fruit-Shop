package bg.softuni.fruitshop.web;


import bg.softuni.fruitshop.product.service.ProductService;
import bg.softuni.fruitshop.web.dto.CreateProductRequest;
import bg.softuni.fruitshop.web.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView showAddProductForm() {
        ModelAndView modelAndView = new ModelAndView("add-product");
        modelAndView.addObject("createProductRequest", new CreateProductRequest());
        return modelAndView;
    }

    @PostMapping("/add")
    public String handleAddProduct(@Valid @ModelAttribute("createProductRequest") CreateProductRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "add-product";
        }

        productService.createProduct(request);
        return "redirect:/products";
    }

    @GetMapping
    public ModelAndView showAllProducts() {
        ModelAndView modelAndView = new ModelAndView("products");
        modelAndView.addObject("products", productService.listAllProducts());
        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView showProductDetails(@PathVariable UUID id) {
        ProductResponse product = productService.getProductById(id);
        ModelAndView modelAndView = new ModelAndView("product-details");
        modelAndView.addObject("product", product);
        return modelAndView;
    }


}
