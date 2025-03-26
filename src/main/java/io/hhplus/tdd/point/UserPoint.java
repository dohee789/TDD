package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint charge(long point) {
        if (point <= 0) {
            throw new IllegalArgumentException("최소 충전 금액은 1포인트 입니다");
        }
        long maxPoint = 1000L;
        long currentPoint = this.point() + point;
        if (currentPoint > maxPoint) {
            throw new IllegalArgumentException("최대 잔고 제한을 초과했습니다. 현재 포인트: " + currentPoint);
        }
        return new UserPoint(this.id(), currentPoint, System.currentTimeMillis());
    }

    public UserPoint use(long point) {
        if (point <= 0) {
            throw new IllegalArgumentException("최소 사용 금액은 1포인트 입니다");
        }
        long minPoint = 0L;
        long currentPoint = this.point() - point;
        if (currentPoint < minPoint) {
            throw new IllegalArgumentException("포인트 잔액이 부족합니다. 현재 포인트: " + currentPoint);
        }
        return new UserPoint(this.id(), minPoint, System.currentTimeMillis());
    }
}
