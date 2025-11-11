package com.kindergarten.management_school.service.menu;

import com.kindergarten.management_school.dto.request.MenuItemRequest;
import com.kindergarten.management_school.dto.request.MenuRequest;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.MenuItemResponse;
import com.kindergarten.management_school.dto.response.MenuResponse;

import java.time.LocalDate;

public interface MenuService {
    ApiResponse<MenuResponse> createWeeklyMenu(MenuRequest request);
    ApiResponse<MenuItemResponse> createMenuItem(MenuItemRequest request);
    ApiResponse<MenuResponse> getWeeklyMenu(Long classId, LocalDate startDate, LocalDate endDate);
    ApiResponse<MenuResponse> updateWeeklyMenu(Long menuId, MenuRequest request);
}
