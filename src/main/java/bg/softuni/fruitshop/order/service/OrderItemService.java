package bg.softuni.fruitshop.order.service;

import bg.softuni.fruitshop.order.model.OrderItem;
import bg.softuni.fruitshop.order.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }


    public List<OrderItem> getCompletedOrdersForUser(UUID userId) {
        List<OrderItem> allItems = orderItemRepository.findAllByOrderCustomerId(userId);

        return allItems.stream()
                .sorted((a, b) -> b.getOrder().getCreatedOn().compareTo(a.getOrder().getCreatedOn()))
                .limit(10)
                .toList();
    }

}
