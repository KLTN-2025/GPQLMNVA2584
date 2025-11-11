package com.kindergarten.management_school.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemResponse {
    private String dayOfWeek;
    private String mealType;
    private List<String> dishes;
}
