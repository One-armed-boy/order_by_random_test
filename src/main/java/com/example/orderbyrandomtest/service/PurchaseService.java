package com.example.orderbyrandomtest.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Limit;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.orderbyrandomtest.domain.Purchase;
import com.example.orderbyrandomtest.repository.MemberRepository;
import com.example.orderbyrandomtest.repository.PurchaseRepository;
import com.example.orderbyrandomtest.repository.TicketRepository;
import com.example.orderbyrandomtest.repository.TicketingRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PurchaseService {
	private MemberRepository memberRepository;
	private TicketingRepository ticketingRepository;
	private TicketRepository ticketRepository;
	private PurchaseRepository purchaseRepository;

	@Autowired
	public PurchaseService(MemberRepository memberRepository, TicketingRepository ticketingRepository, TicketRepository ticketRepository, PurchaseRepository purchaseRepository) {
		this.memberRepository = memberRepository;
		this.ticketingRepository = ticketingRepository;
		this.ticketRepository = ticketRepository;
		this.purchaseRepository = purchaseRepository;
	}

	@Retryable(
		retryFor = OptimisticLockingFailureException.class,
		backoff = @Backoff(delay = 50),
		maxAttempts = 100
	)
	@Transactional
	public int purchaseTicketWithOLock(String email, UUID ticketingId, int amount) {
		ticketingRepository.findById(ticketingId).orElseThrow();
		var member = memberRepository.findByEmail(email).orElseThrow();
		var tickets = ticketRepository.findByTicketingIdAndNonPurchased(ticketingId, Limit.of(amount));
		var purchase = Purchase.builder().member(member).build();
		purchaseRepository.save(purchase);
		tickets.forEach((ticket -> ticket.setPurchase(purchase)));
		return RetrySynchronizationManager.getContext().getRetryCount();
	}

	@Retryable(
		retryFor = OptimisticLockingFailureException.class,
		backoff = @Backoff(delay = 50),
		maxAttempts = 100
	)
	@Transactional
	public int purchaseTicketWithOLockRandom(String email, UUID ticketingId, int amount) {
		ticketingRepository.findById(ticketingId).orElseThrow();
		var member = memberRepository.findByEmail(email).orElseThrow();
		var tickets = ticketRepository.findByTicketingIdAndNonPurchasedRandom(ticketingId, Limit.of(amount));
		var purchase = Purchase.builder().member(member).build();
		purchaseRepository.save(purchase);
		tickets.forEach((ticket -> ticket.setPurchase(purchase)));
		return RetrySynchronizationManager.getContext().getRetryCount();
	}

	@Retryable(
		retryFor = OptimisticLockingFailureException.class,
		backoff = @Backoff(delay = 50),
		maxAttempts = 100
	)
	@Transactional
	public int purchaseTicketWithOLockRandomOptimize(String email, UUID ticketingId, int amount) {
		ticketingRepository.findById(ticketingId).orElseThrow();
		var member = memberRepository.findByEmail(email).orElseThrow();
		var tickets = ticketRepository.findByTicketingIdAndNonPurchasedRandomOptimize(ticketingId, amount);
		var purchase = Purchase.builder().member(member).build();
		purchaseRepository.save(purchase);
		tickets.forEach((ticket -> ticket.setPurchase(purchase)));
		return RetrySynchronizationManager.getContext().getRetryCount();
	}
}
