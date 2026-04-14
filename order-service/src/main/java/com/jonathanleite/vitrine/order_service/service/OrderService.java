package com.jonathanleite.vitrine.order_service.service;

import com.jonathanleite.vitrine.order_service.client.ClientServiceClient;
import com.jonathanleite.vitrine.order_service.dto.ClientResponseDTO;
import com.jonathanleite.vitrine.order_service.entity.Order;
import com.jonathanleite.vitrine.order_service.entity.OrderStatus;
import com.jonathanleite.vitrine.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientServiceClient clientServiceClient;

    @Transactional
    public Order createOrder(Order order) {

        ClientResponseDTO client = clientServiceClient.getClientById(order.getClientId());

        if (client == null) {
            throw new RuntimeException("Cliente não encontrado para o ID: " + order.getClientId());
        }

        if (Boolean.FALSE.equals(client.getActive())) {
            throw new RuntimeException("Cliente está inativo");
        }


        order.setStatus(OrderStatus.CREATED);


        return orderRepository.save(order);
    }
}