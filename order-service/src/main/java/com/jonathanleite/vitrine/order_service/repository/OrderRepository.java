package com.jonathanleite.vitrine.order_service.repository;

import com.jonathanleite.vitrine.order_service.entity.Order;
import com.jonathanleite.vitrine.order_service.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClientId(Long clientId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByClientIdAndStatus(Long clientId, OrderStatus status);
}