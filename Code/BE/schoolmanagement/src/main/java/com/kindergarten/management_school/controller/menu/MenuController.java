package com.kindergarten.management_school.controller.menu;

import com.kindergarten.management_school.dto.request.MenuItemRequest;
import com.kindergarten.management_school.dto.request.MenuRequest;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.MenuItemResponse;
import com.kindergarten.management_school.dto.response.MenuResponse;
import com.kindergarten.management_school.service.menu.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @PostMapping("/weekly/create")
    public ResponseEntity<ApiResponse<MenuResponse>> createWeeklyMenu(@RequestBody MenuRequest request) {
        var response = menuService.createWeeklyMenu(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(
            @RequestBody MenuItemRequest request) {
        ApiResponse<MenuItemResponse> response = menuService.createMenuItem(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<MenuResponse>> getWeeklyMenu(
            @RequestParam Long classId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        ApiResponse<MenuResponse> response = menuService.getWeeklyMenu(classId, startDate, endDate);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/update/{menuId}")
    public ResponseEntity<ApiResponse<MenuResponse>> updateWeeklyMenu(
            @PathVariable Long menuId,
            @RequestBody MenuRequest request
    ) {
        ApiResponse<MenuResponse> response = menuService.updateWeeklyMenu(menuId, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
