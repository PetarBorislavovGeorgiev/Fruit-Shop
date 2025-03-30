package bg.softuni.fruitshop.order.service;

import bg.softuni.fruitshop.order.model.Order;
import bg.softuni.fruitshop.order.model.OrderItem;
import bg.softuni.fruitshop.order.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }


    public Map<Order, List<OrderItem>> getGroupedOrdersForUser(UUID userId) {
        return orderItemRepository.findAllByOrderCustomerId(userId).stream()
                .collect(Collectors.groupingBy(OrderItem::getOrder,
                        Collectors.toList()));
    }

    public List<OrderItem> getAllCompletedOrdersSorted() {
        return orderItemRepository.findAllWithOrderSortedByOrderCreationDateDesc();
    }


    public Map<Order, List<OrderItem>> getGroupedCompletedOrders() {
        List<OrderItem> items = orderItemRepository.findAllByOrderIsNotNullOrderByOrder_CreatedOnDesc();

        return items.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrder, LinkedHashMap::new, Collectors.toList()));
    }
}
