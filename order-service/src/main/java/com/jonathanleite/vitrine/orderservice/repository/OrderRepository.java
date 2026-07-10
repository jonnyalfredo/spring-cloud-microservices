package com.jonathanleite.vitrine.orderservice.repository;

import com.jonathanleite.vitrine.orderservice.entity.Order;
import com.jonathanleite.vitrine.orderservice.entity.OrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByClientId(Long clientId);

    List<Order> findByStatus(OrderStatus status);

    Optional<Order> findByIdAndClientId(Long id, Long clientId);

    Page<Order> findByClientId(Long clientId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.amount > :amount")
    List<Order> findOrdersWithAmountGreaterThan(@Param("amount") Double amount);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.amount >= :amount")
    List<Order> findByStatusAndMinAmount(@Param("status") OrderStatus status,
                                         @Param("amount") Double amount);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
    int updateOrderStatus(@Param("id") Long id,
                          @Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.clientId = :clientId")
    Double sumAmountByClient(@Param("clientId") Long clientId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);

    boolean existsByClientId(Long clientId);

    boolean existsByIdAndStatus(Long id, OrderStatus status);
}
