package io.hhplus.tdd.point;

import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointService pointService;

    static final long USER_ID = 1L;
    static final long TIME_STAMP = System.currentTimeMillis();

    @DisplayName("1포인트 미만 사용시 사용에 실패합니다")
    @Test
    void usePointFailIfUsePointUnderOnePoint() {
        // given
        long usePoint = 0L;

        // selectById() 목 설정
        when(userPointRepository.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, 0L, TIME_STAMP));

        // when
        Throwable exception = catchThrowable(() -> pointService.usePoint(USER_ID, usePoint));

        // then
        assertThat(exception).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최소 사용 금액은 1포인트 입니다");
    }

    @DisplayName("포인트 잔액이 1미만일때 포인트 사용시 사용이 실패합니다")
    @Test
    void usePointFailIfBalanceUnderOnePoint() {
        // given
        long currentPoint = 0L;
        long usePoint = 1L;

        // selectById() 목 설정
        when(userPointRepository.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, currentPoint, TIME_STAMP));

        // when
        Throwable exception = catchThrowable(() -> pointService.usePoint(USER_ID, usePoint));
        // then
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트 잔액이 부족합니다. 현재 포인트: " + (currentPoint - usePoint));
    }

    @DisplayName("포인트 사용 성공시 내역에 저장됩니다")
    @Test
    void savePointHistorySucceedIfUsePointSucceed() {
        // given
        long usePoint = 1L;
        long initialTimestamp = System.currentTimeMillis();
        long currentPoint = 2L;

        UserPoint initialUserPoint = new UserPoint(USER_ID, currentPoint, initialTimestamp);
        UserPoint afterUseUserPoint = new UserPoint(USER_ID, currentPoint - usePoint, initialTimestamp);

        // selectById() 목 설정
        when(userPointRepository.selectById(USER_ID))
                .thenReturn(initialUserPoint);

        // insertOrUpdate() 목 설정
        when(userPointRepository.insertOrUpdate(any(UserPoint.class)))
                .thenReturn(afterUseUserPoint);

        // pointHistoryRepository insert() 목 설정
        PointHistory mockPointHistory = new PointHistory(1L, USER_ID, currentPoint - usePoint, TransactionType.USE, initialTimestamp);
        when(pointHistoryRepository.insert(
                eq(USER_ID),
                eq(currentPoint - usePoint),
                eq(TransactionType.USE),
                anyLong())
        ).thenReturn(mockPointHistory);

        // when
        UserPoint saveUser = pointService.usePoint(USER_ID, usePoint);

        // then
        // 포인트 이력 저장 메서드 호출 검증
        verify(pointHistoryRepository).insert(
                eq(saveUser.id()),
                eq(saveUser.point()),
                eq(TransactionType.USE),
                longThat(timestamp -> timestamp >= initialTimestamp)
        );
    }

    @DisplayName("1포인트 미만 충전시 충전에 실패합니다")
    @Test
    void chargePointFailIfChargeUnderOnePoint() {
        // given
        long chargePoint = 0L;

        // NullPointerException 방지를 위해 selectById()의 반환값을 명시적으로 설정
        when(userPointRepository.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, 0L, System.currentTimeMillis()));

        // when
        Throwable exception = catchThrowable(() -> pointService.chargePoint(USER_ID, chargePoint));

        // then
        assertThat(exception).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최소 충전 금액은 1포인트 입니다");
    }

    @DisplayName("포인트 잔액이 1000을 초과할때 충전하면 충전에 실패합니다")
    @Test
    void chargePointFailIfChargeThousandPoint() {
        // given
        long currentPoint = 1000L;
        long chargePoint = 1L;

        // selectById() 목 설정
        when(userPointRepository.selectById(USER_ID))
                .thenReturn(new UserPoint(USER_ID, currentPoint, System.currentTimeMillis()));

        // when
        Throwable exception = catchThrowable(() -> pointService.chargePoint(USER_ID, chargePoint));
        // then
        assertThat(exception).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대 잔고 제한을 초과했습니다. 현재 포인트: " + (currentPoint + chargePoint));
    }

    @DisplayName("포인트 충전 성공시 내역에 저장됩니다")
    @Test
    void savePointHistoryIfChargePointSucceed() {
        // given
        long chargePoint = 3L;
        long initialTimestamp = System.currentTimeMillis();
        long currentPoint = 2L;

        UserPoint initialUserPoint = new UserPoint(USER_ID, currentPoint, initialTimestamp);
        UserPoint afterChargeUserPoint = new UserPoint(USER_ID, currentPoint + chargePoint, initialTimestamp);

        // selectById() 목 설정
        when(userPointRepository.selectById(USER_ID))
                .thenReturn(initialUserPoint);

        // insertOrUpdate() 목 설정
        when(userPointRepository.insertOrUpdate(any(UserPoint.class)))
                .thenReturn(afterChargeUserPoint);

        // pointHistoryRepository insert() 목 설정
        PointHistory mockPointHistory = new PointHistory(1L, USER_ID, currentPoint + chargePoint, TransactionType.CHARGE, initialTimestamp);
        when(pointHistoryRepository.insert(
                eq(USER_ID),
                eq(currentPoint + chargePoint),
                eq(TransactionType.CHARGE),
                anyLong())
        ).thenReturn(mockPointHistory);

        // when
        UserPoint saveUser = pointService.chargePoint(USER_ID, chargePoint);

        // then
        // 포인트 이력 저장 메서드 호출 검증
        verify(pointHistoryRepository).insert(
                eq(saveUser.id()),
                eq(saveUser.point()),
                eq(TransactionType.CHARGE),
                any(Long.class)
        );
    }
}