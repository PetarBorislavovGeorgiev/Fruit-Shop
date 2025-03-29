package bg.softuni.fruitshop.address.model;


import bg.softuni.fruitshop.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String city;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(nullable = false)
    private String street;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
