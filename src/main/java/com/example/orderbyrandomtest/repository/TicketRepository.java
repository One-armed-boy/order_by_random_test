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
	@Transactional
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT t FROM Ticket t WHERE t.ticketing.id = :ticketingId AND t.purchase IS NULL ORDER BY t.id")
	List<Ticket> findByTicketingIdAndNonPurchased(UUID ticketingId, Limit limit);

	@Transactional
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT t FROM Ticket t WHERE t.ticketing.id = :ticketingId AND t.purchase IS NULL ORDER BY FUNCTION('RAND')")
	List<Ticket> findByTicketingIdAndNonPurchasedRandom(UUID ticketingId, Limit limit);

	@Transactional
	@Query(value = """
        with target as (
        	select
        		t.ticket_id as ticket_id,
        		row_number() over(order by t.ticket_id) as rownum
        	from tickets t
        	where
        		t.ticketing_id = :ticketingId
                and t.purchase_id is null
            order by t.ticket_id
        ), tmp as (
        	select floor(rand() * (count(*) - :limit)) as base from target
        )
        select t.*
        from target t, tmp tmp
        where t.rownum > tmp.base
        limit :limit
        for update
      """, nativeQuery = true)
	List<Ticket> findByTicketingIdAndNonPurchasedRandomOptimize(UUID ticketingId, int limit);
}
