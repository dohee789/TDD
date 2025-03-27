package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class UserPointTest {
    static final long USER_ID = 1L;

    @DisplayName("충전할 포인트가 0미만 경우 IllegalArgumentException 이 발생합니다.")
    @Test
    void chargePointException() {
        // given
        UserPoint userPoint = UserPoint.empty(USER_ID);
        long chargePoint = -1L;
        // when
        Throwable exception = catchThrowable(() -> userPoint.charge(chargePoint));
        // then
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최소 충전 금액은 1포인트 입니다");
    }

    @DisplayName("충전 최대 잔고를 초과할 경우 IllegalArgumentException 이 발생합니다.")
    @Test
    void maxChargedPointException() {
        // given
        long currentPoint = 1000L;
        long chargePoint = 1L;
        UserPoint userPoint = new UserPoint(USER_ID, currentPoint, System.currentTimeMillis()); // 최대 충전 금액으로 세팅
        // when
        Throwable exception = catchThrowable(() -> userPoint.charge(chargePoint));
        // then
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대 잔고 제한을 초과했습니다. 현재 포인트: " + (userPoint.point() + chargePoint));
    }

    @DisplayName("포인트 충전 성공")
    @Test
    void chargePointSucceed() {
        // given
        long currentPoint = 1L;
        long chargePoint = 1L;
        UserPoint userPoint = new UserPoint(USER_ID, currentPoint, System.currentTimeMillis());
        // when
        UserPoint chargedPoint = userPoint.charge(chargePoint);
        // then
        assertThat(chargedPoint.point()).isEqualByComparingTo(currentPoint + chargePoint);
    }

    @DisplayName("사용할 포인트가 0미만 경우 IllegalArgumentException 이 발생합니다.")
    @Test
    void usePointException() {
        // given
        UserPoint userPoint = UserPoint.empty(USER_ID);
        long chargePoint = -1L;
        // when
        Throwable exception = catchThrowable(() -> userPoint.use(chargePoint));
        // then
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최소 사용 금액은 1포인트 입니다");
    }

    @DisplayName("포인트 잔액이 부족할 경우 IllegalArgumentException 이 발생합니다.")
    @Test
    void minChargedPointException() {
        // given
        long currentPoint = 0L;
        long usePoint = 1L;
        UserPoint userPoint = new UserPoint(USER_ID, currentPoint, System.currentTimeMillis()); // 최소 잔액으로 세팅
        // when
        Throwable exception = catchThrowable(() -> userPoint.use(usePoint));
        // then
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트 잔액이 부족합니다. 현재 포인트: " + (userPoint.point() - usePoint));
    }

    @DisplayName("포인트 사용 성공")
    @Test
    void usePointSucceed() {
        // given
        long currentPoint = 2L;
        long usePoint = 1L;
        UserPoint userPoint = new UserPoint(USER_ID, currentPoint, System.currentTimeMillis());
        // when
        UserPoint usedPoint = userPoint.use(usePoint);
        // then
        assertThat(usedPoint.point()).isEqualByComparingTo(currentPoint - usePoint);
    }

}