package bg.softuni.fruitshop.web.dto;

import bg.softuni.fruitshop.product.model.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private UUID id;

    private String name;

    private BigDecimal price;

    private CategoryType category;

}
