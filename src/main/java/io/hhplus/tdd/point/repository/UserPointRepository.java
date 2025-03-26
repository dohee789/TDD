package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.UserPoint;

public interface UserPointRepository {
    UserPoint selectById(Long id);
    UserPoint insertOrUpdate(UserPoint userPoint);
}
