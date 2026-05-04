package com.ordering.system.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ordering.system.entity.Orders;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
}

