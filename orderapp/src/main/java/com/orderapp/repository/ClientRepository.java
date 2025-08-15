package com.orderapp.repository;

import com.orderapp.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Page<Client> findAll(Pageable pageable);

    @Query("SELECT c FROM Client c WHERE c.id = :id")
    Optional<Client> findJustClientById(@Param("id") Long id);

    @Query("""
            SELECT c FROM Client c 
            LEFT JOIN FETCH c.suppliedOrders 
            LEFT JOIN FETCH c.consumedOrders 
            WHERE c.id = :id
            """)
    Optional<Client> findByIdWithOrders(@Param("id") Long id);

    Page<Client> findAll(Specification<Client> clientSpecification, Pageable pageable);
}
