package io.hhplus.tdd.point;

import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class ConcurrencyTest {
    private static final Logger log = LoggerFactory.getLogger(ConcurrencyTest.class);

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointRepository userPointRepository;

    private static final int THREAD_COUNT = 100;

    private final ReentrantLock lock = new ReentrantLock();

    @BeforeEach
    void setUp() {
        // setup initial point for all the users
        for (long userId = 0; userId < THREAD_COUNT; userId++) {
            UserPoint initialPoint = UserPoint.empty(userId);
            userPointRepository.insertOrUpdate(initialPoint);
        }
    }

    @DisplayName("여러 사용자가 충전 시도시 포인트 이력의 총 개수가 성공한 충전 횟수와 일치한다")
    @Test
    void pointTransactionHistoryTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        // 성공/실패 추적을 위한 Map
        Map<Long, AtomicInteger> successfulChargesPerUser = new HashMap<>();
        Map<Long, AtomicInteger> failedChargesPerUser = new HashMap<>();

        // when
        for (long userId = 0; userId < THREAD_COUNT; userId++) {
            successfulChargesPerUser.put(userId, new AtomicInteger(0));
            failedChargesPerUser.put(userId, new AtomicInteger(0));
            userPointRepository.insertOrUpdate(UserPoint.empty(userId));

            final long finalUserId = userId;

            executorService.submit(() -> {
                try {
                    lock.lock();
                    try {
                        pointService.chargePoint(finalUserId, 10);
                        successfulChargesPerUser.get(finalUserId).incrementAndGet();
                    } catch (IllegalArgumentException e) {
                        // 최대 포인트 초과 등으로 인한 실패는 별도 추적
                        failedChargesPerUser.get(finalUserId).incrementAndGet();
                    } catch (Exception e) {
                        log.error("Unexpected error during charge", e);
                        failedChargesPerUser.get(finalUserId).incrementAndGet();
                    } finally {
                        lock.unlock();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 작업 완료 대기
        executorService.shutdown();

        // 포인트 이력 조회 및 검증
        for (long userId = 0; userId < THREAD_COUNT; userId++) {
            List<PointHistory> histories = pointService.getPointHistory(userId);
            log.info("User id: {}, Successful charges: {}, Total history entries: {}",
                    userId, successfulChargesPerUser.get(userId).get(), histories.size());
            // then
            assertThat(histories.size()).isEqualTo(successfulChargesPerUser.get(userId).get());
        }
    }
}