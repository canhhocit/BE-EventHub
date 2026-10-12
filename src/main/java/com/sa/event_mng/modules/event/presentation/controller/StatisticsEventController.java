package com.sa.event_mng.modules.event.presentation.controller;

import com.sa.event_mng.modules.event.application.dto.response.*;
import com.sa.event_mng.shared.dto.ApiResponse;
import com.sa.event_mng.modules.event.application.service.StatisticsEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistics-event")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Thống kê sự kiện", description = "Quản lý dữ liệu tổng hợp sự kiện")
@SecurityRequirement(name = "bearerAuth")
public class StatisticsEventController {

    StatisticsEventService statisticsService;

    @GetMapping("/by-status/{quarter}/{year}")
    @Operation(summary = "Tỉ lệ phân bổ trạng thái từng quý (Admin)")
    public ApiResponse<EventStatusStatsResponse> getStatisticsByStatus(@PathVariable Long quarter, @PathVariable Long year) {
        return ApiResponse.<EventStatusStatsResponse>builder().result(statisticsService.getEventStatusStats(quarter, year)).build();
    }

    @GetMapping("/by-temporal/{dayOfWeek}")
    @Operation(summary = "Thống kê số lượng sự kiện bắt đầu (start_time) theo từng ngày trong tuần hoặc giờ trong ngày để tìm giờ vàng (Admin)")
    public ApiResponse<EventTemporalStatsResponse> getStatisticsByTemporal(@PathVariable int dayOfWeek) {
        return ApiResponse.<EventTemporalStatsResponse>builder().result(statisticsService.getEventTemporalStats(dayOfWeek)).build();
    }

    @GetMapping("/top5-events")
    @Operation(summary = "Top 5 sự kiện nổi bật theo quý hiện tại (Admin)")
    public ApiResponse<TopEventQuarterResponse> getTop5EventsByCurrentQuarter() {
        return ApiResponse.<TopEventQuarterResponse>builder().result(statisticsService.getTop5EventsByQuarter()).build();
    }

    @GetMapping("/{id_organizer}/{year}/overview")
    @Operation(summary = "Tổng quan các sự kiện trong năm (Organizer)")
    public ApiResponse<EventYearlyOverviewResponse> getEventYearlyOverview(
            @PathVariable("id_organizer") Long idOrganizer,
            @PathVariable int year) {
        return ApiResponse.<EventYearlyOverviewResponse>builder()
                .result(statisticsService.getEventYearlyOverview(idOrganizer, year))
                .build();
    }

}
