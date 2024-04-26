package com.example.orderbyrandomtest.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.orderbyrandomtest.domain.Purchase;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {
}
