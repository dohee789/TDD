package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {
    private static final long MAX_POINT = 1000L;
    private static final long MIN_POINT = 0L;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint charge(long amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("최소 충전 금액은 1포인트 입니다");
        }
        long currentPoint = this.point() + amount;
        if (currentPoint > MAX_POINT) {
            throw new IllegalArgumentException("최대 잔고 제한을 초과했습니다. 현재 포인트: " + currentPoint);
        }
        return new UserPoint(this.id(), currentPoint, System.currentTimeMillis());
    }

    public UserPoint use(long amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("최소 사용 금액은 1포인트 입니다");
        }
        long currentPoint = this.point() - amount;
        if (currentPoint < MIN_POINT) {
            throw new IllegalArgumentException("포인트 잔액이 부족합니다. 현재 포인트: " + currentPoint);
        }
        return new UserPoint(this.id(), currentPoint, System.currentTimeMillis());
    }
}
