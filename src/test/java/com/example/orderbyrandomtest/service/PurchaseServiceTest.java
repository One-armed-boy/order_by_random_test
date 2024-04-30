package com.example.orderbyrandomtest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Limit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import com.example.orderbyrandomtest.domain.Member;
import com.example.orderbyrandomtest.domain.Ticket;
import com.example.orderbyrandomtest.domain.Ticketing;
import com.example.orderbyrandomtest.repository.MemberRepository;
import com.example.orderbyrandomtest.repository.PurchaseRepository;
import com.example.orderbyrandomtest.repository.TicketRepository;
import com.example.orderbyrandomtest.repository.TicketingRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class PurchaseServiceTest {
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private PurchaseRepository purchaseRepository;
	@Autowired
	private TicketingRepository ticketingRepository;
	@Autowired
	private TicketRepository ticketRepository;
	@Autowired
	private PurchaseService purchaseService;
	private List<String> userEmails;
	private UUID ticketingId;

	private final static int STOCK_CNT = 300;
	private final static int USER_CNT = 20;
	private final static int TICKET_PURCHASE_AMOUNT_PER_USER = 3;

	@BeforeEach
	@Transactional
	void initTable() {
		var ticketing = ticketingRepository.save(Ticketing.builder().title("title").build());
		var stocks = IntStream.rangeClosed(1, STOCK_CNT).boxed().map((i)-> Ticket.builder().ticketing(ticketing).build()).toList();
		ticketRepository.saveAll(stocks);
		ticketingId = ticketing.getId();
		userEmails = new ArrayList<>();
		for (var num = 1; num <= USER_CNT; num++) {
			userEmails.add("name" + num);
		}
		userEmails.forEach(this::createMember);
	}

	@AfterEach
	@Transactional
	void clearTable() {
		ticketRepository.deleteAllInBatch();
		purchaseRepository.deleteAllInBatch();
		ticketingRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@Test
	@DisplayName("N개 재고 > 3개씩 10개의 유저(스레드)가 구매 요청 > 30개의 재고가 팔림")
	void purchaseOrder() {
		execTest(purchaseService::purchaseTicketWithOLock);
	}

	@Test
	@DisplayName("N개 재고 > 3개씩 10개의 유저가 구매 요청, 단 재고 랜덤 추출 방식 사용 > 30개 재고가 팔림")
	void purchaseOrderRandom() {
		execTest(purchaseService::purchaseTicketWithOLockRandom);
	}

	@Test
	@DisplayName("N개 재고 > 3개씩 10개의 유저가 구매 요청, 단 최적화된 재고 랜덤 추출 방식 사용 > 30개 재고가 팔림")
	void purchaseOrderRandomOptimize() {
		execTest(purchaseService::purchaseTicketWithOLockRandomOptimize);
	}

	// @Test
	// void test() {
	// 	Assertions.assertThat(1).isNotNull();
	// }

	private void execTest(PurchaseOrder purchaseOrder) {
		// given
		var executorService = Executors.newFixedThreadPool(USER_CNT);
		var startLatch = new CountDownLatch(1);
		var endLatch = new CountDownLatch(USER_CNT);
		var atomicNumber = new AtomicInteger();

		for (var i = 0; i < USER_CNT; i++) {
			var userIdx = i;
			executorService.submit(()->{
				try {
					startLatch.await();
					var retryCnt = purchaseOrder.exec(userEmails.get(userIdx), ticketingId, TICKET_PURCHASE_AMOUNT_PER_USER);
					atomicNumber.addAndGet(retryCnt);
				} catch (Exception err) {
					throw new RuntimeException(err);
				} finally {
					endLatch.countDown();
				}
			});
		}

		wrapWithTimeConsole(()->{
			try {
				startLatch.countDown();
				endLatch.await();
			} catch (InterruptedException err) {
				throw new RuntimeException(err);
			}
		});

		log.info("최종 재시도 횟수: {}", atomicNumber.get());

		Assertions.assertThat(purchaseRepository.findAll().size()).isEqualTo(USER_CNT);
		var unsaledStocks = ticketRepository.findByTicketingIdAndNonPurchased(ticketingId, Limit.unlimited());
		Assertions.assertThat(unsaledStocks.size()).isEqualTo(STOCK_CNT - USER_CNT * TICKET_PURCHASE_AMOUNT_PER_USER);
	}

	private void createMember(String email) {
		memberRepository.save(Member.builder().email(email).build());
	}

	private void wrapWithTimeConsole(Runnable runnable) {
		var stopWatch = new StopWatch();
		stopWatch.start();
		runnable.run();
		stopWatch.stop();
		log.info("소요 시간: {} ms", stopWatch.getTotalTimeMillis());
	}

	@FunctionalInterface
	private interface PurchaseOrder {
		int exec(String email, UUID ticketingId, int amount);
	}
}
