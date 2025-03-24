package bg.softuni.fruitshop.order.model;


import bg.softuni.fruitshop.address.model.Address;
import bg.softuni.fruitshop.cartItem.model.CartItem;
import bg.softuni.fruitshop.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;


    @ManyToOne(optional = false)
    private User customer;

    @ManyToMany
    private List<CartItem> items;

    @ManyToOne(optional = false)
    private Address address;
}
