package com.example.orderbyrandomtest.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.orderbyrandomtest.domain.Ticket;

import jakarta.persistence.LockModeType;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
	@Transactional(readOnly = true)
	@Lock(LockModeType.OPTIMISTIC)
	@Query("SELECT t FROM Ticket t WHERE t.ticketing.id = :ticketingId AND t.purchase IS NULL ORDER BY t.id")
	List<Ticket> findByTicketingIdAndNonPurchased(UUID ticketingId, Limit limit);

	@Transactional(readOnly = true)
	@Lock(LockModeType.OPTIMISTIC)
	@Query("SELECT t FROM Ticket t WHERE t.ticketing.id = :ticketingId AND t.purchase IS NULL ORDER BY FUNCTION('RAND')")
	List<Ticket> findByTicketingIdAndNonPurchasedRandom(UUID ticketingId, Limit limit);

	@Transactional(readOnly = true)
	@Query(value = """
        with target as (
        	select *, row_number() over(order by t.ticket_id) as num
        	from tickets t
        	where t.ticketing_id = :ticketingId and purchase_id is null
        )
        select *
        from target t, (
        	select floor(rand() * (count(*) - :limit)) as base from target
        ) c
        where t.num > c.base
        limit :limit
      """, nativeQuery = true)
	List<Ticket> findByTicketingIdAndNonPurchasedRandomOptimize(UUID ticketingId, int limit);
}
