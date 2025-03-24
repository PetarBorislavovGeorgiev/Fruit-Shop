package bg.softuni.fruitshop.cartItem.model;


import bg.softuni.fruitshop.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private long quantity;

    @ManyToOne(optional = false)
    private Product product;
}
