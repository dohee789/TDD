package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserPointRepositoryImpl {
    private final UserPointTable userPointTable;

    public UserPoint selectById(long id) {
        return userPointTable.selectById(id);
    }

    public UserPoint insertOrUpdate(UserPoint userPoint) {
        return userPointTable.insertOrUpdate(userPoint.id(), userPoint.point());
    }
}

