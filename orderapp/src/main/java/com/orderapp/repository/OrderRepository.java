package com.orderapp.repository;

import com.orderapp.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findBySupplierIdOrConsumerId(Long customerId, Long customerId2);

    @Query("""
            SELECT o FROM Order o 
            LEFT JOIN FETCH o.supplier s 
            LEFT JOIN FETCH o.consumer c 
            WHERE s.id IN (:supplierId, :consumerId) OR c.id IN (:supplierId, :consumerId)
            """)
    List<Order> findAllOrdersForClients(@Param("supplierId") Long supplierId, @Param("consumerId") Long consumerId);

    boolean existsByTitleAndSupplierIdAndConsumerId(String title, Long supplierId, Long consumerId);
}
