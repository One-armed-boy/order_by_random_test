package com.example.orderbyrandomtest.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.orderbyrandomtest.domain.Ticketing;

@Repository
public interface TicketingRepository extends JpaRepository<Ticketing, UUID> {
}
