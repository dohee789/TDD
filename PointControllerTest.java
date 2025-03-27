package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    static final long USER_ID = 1L;
    static final long CURRENT_POINT= 100L;
    static final long CHARGE_POINT= 50L;
    static final long USE_POINT = 20L;
    @Test
    @DisplayName("GET /point/{id} - 유저 포인트 조회 성공")
    void getUserPoint() throws Exception {
        // given
        UserPoint testUserPoint = new UserPoint(USER_ID, CURRENT_POINT, System.currentTimeMillis());
        when(pointService.getPoint(USER_ID)).thenReturn(testUserPoint);

        // when
        mockMvc.perform(get("/point/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.point").value(CURRENT_POINT));

        // then
        verify(pointService, times(1)).getPoint(USER_ID);
    }

    @Test
    @DisplayName("GET /point/{id}/histories - 유저 포인트 내역 조회 성공")
    void getPointHistories() throws Exception {
        // given
        List<PointHistory> testPointHistories = Arrays.asList(
                new PointHistory(1L, USER_ID, CHARGE_POINT, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(1L, USER_ID, USE_POINT, TransactionType.USE, System.currentTimeMillis())
        );
        when(pointService.getPointHistory(USER_ID)).thenReturn(testPointHistories);

        // when
        mockMvc.perform(get("/point/1/histories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].amount").value(CHARGE_POINT))
                .andExpect(jsonPath("$[1].amount").value(USE_POINT));

        // then
        verify(pointService, times(1)).getPointHistory(USER_ID);
    }

    @Test
    @DisplayName("PATCH /point/{id}/charge - 포인트 충전 성공")
    void chargePoint() throws Exception {
        // given
        UserPoint chargedUserPoint = new UserPoint(USER_ID, CURRENT_POINT + CHARGE_POINT, System.currentTimeMillis());
        when(pointService.chargePoint(USER_ID, CHARGE_POINT)).thenReturn(chargedUserPoint);

        // when
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(CHARGE_POINT)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(USER_ID))
                        .andExpect(jsonPath("$.point").value(CURRENT_POINT + CHARGE_POINT));

        // then
        verify(pointService, times(1)).chargePoint(USER_ID, CHARGE_POINT);
    }

    @Test
    @DisplayName("PATCH /point/{id}/use - 포인트 사용 성공")
    void usePoint() throws Exception {
        // given
        UserPoint usedUserPoint = new UserPoint(USER_ID, CURRENT_POINT - USE_POINT, System.currentTimeMillis());
        when(pointService.usePoint(USER_ID, USE_POINT)).thenReturn(usedUserPoint);

        // when
        mockMvc.perform(patch("/point/1/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(USE_POINT)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(USER_ID))
                        .andExpect(jsonPath("$.point").value(CURRENT_POINT - USE_POINT));

        // then
        verify(pointService, times(1)).usePoint(USER_ID, USE_POINT);
    }

}