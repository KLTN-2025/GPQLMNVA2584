package com.kindergarten.management_school.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemRequest {
    private String dayOfWeek; // Thứ 2, Thứ 3...
    private String mealType;  // Sáng, Trưa, Chiều
    private List<String> dishes;// Tên món ăn
}
