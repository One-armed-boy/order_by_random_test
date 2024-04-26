package com.example.orderbyrandomtest.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.orderbyrandomtest.domain.Purchase;
import com.example.orderbyrandomtest.repository.MemberRepository;
import com.example.orderbyrandomtest.repository.PurchaseRepository;
import com.example.orderbyrandomtest.repository.TicketRepository;
import com.example.orderbyrandomtest.repository.TicketingRepository;

@Service
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

	@Transactional
	public void purchaseTicketWithPLock(String email, UUID ticketingId, int amount) {
		ticketingRepository.findById(ticketingId).orElseThrow();
		var member = memberRepository.findByEmail(email).orElseThrow();
		var purchase = Purchase.builder().member(member).build();
		purchaseRepository.save(purchase);
		var tickets = ticketRepository.findByTicketingIdAndNonPurchased(ticketingId, Limit.of(amount));
		tickets.forEach((ticket -> ticket.setPurchase(purchase)));
	}

	@Transactional
	public void purchaseTicketWithPLockRandom(String email, UUID ticketingId, int amount) {
		ticketingRepository.findById(ticketingId).orElseThrow();
		var member = memberRepository.findByEmail(email).orElseThrow();
		var purchase = Purchase.builder().member(member).build();
		purchaseRepository.save(purchase);
		var tickets = ticketRepository.findByTicketingIdAndNonPurchasedRandom(ticketingId, Limit.of(amount));
		tickets.forEach((ticket -> ticket.setPurchase(purchase)));
	}

	@Transactional
	public void purchaseTicketWithPLockRandomOptimize(String email, UUID ticketingId, int amount) {
		ticketingRepository.findById(ticketingId).orElseThrow();
		var member = memberRepository.findByEmail(email).orElseThrow();
		var purchase = Purchase.builder().member(member).build();
		purchaseRepository.save(purchase);
		var tickets = ticketRepository.findByTicketingIdAndNonPurchasedRandomOptimize(ticketingId, amount);
		tickets.forEach((ticket -> ticket.setPurchase(purchase)));
	}
}
