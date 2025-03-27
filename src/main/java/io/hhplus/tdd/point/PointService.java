package io.hhplus.tdd.point;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    /**
     * 유저의 포인트 조회
     */
    public UserPoint getPoint(long userId) {
        return userPointRepository.selectById(userId);
    }

    /**
     * 유저의 포인트 사용, 사용 내역 추가
     */
    public UserPoint usePoint(long userId, long point) {
        UserPoint userPoint = userPointRepository.selectById(userId);
        UserPoint usePoint = userPoint.use(point);
        UserPoint saveUser = userPointRepository.insertOrUpdate(usePoint);

        pointHistoryRepository.insert(saveUser.id(), saveUser.point(), TransactionType.USE, System.currentTimeMillis());

        return saveUser;
    }

    /**
     * 유저의 포인트 충전, 충전 내역 추가
     */
    public UserPoint chargePoint(long userId, long point) {
        UserPoint userPoint = userPointRepository.selectById(userId);
        UserPoint chargePoint = userPoint.charge(point);
        UserPoint saveUser = userPointRepository.insertOrUpdate(chargePoint);

        pointHistoryRepository.insert(saveUser.id(), saveUser.point(), TransactionType.CHARGE, System.currentTimeMillis());

        return saveUser;
    }

    /**
     * 유저의 포인트 충전/사용 내역 조회
     */
    public List<PointHistory> getPointHistory(long userId) {
        return pointHistoryRepository.selectAllByUserId(userId);
    }

}

