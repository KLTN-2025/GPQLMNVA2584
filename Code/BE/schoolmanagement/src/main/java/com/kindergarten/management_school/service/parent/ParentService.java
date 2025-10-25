package com.kindergarten.management_school.service.parent;

import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.ParentResponse;

import java.util.List;

public interface ParentService {
    ApiResponse<List<ParentResponse>> getAllParents();
    ApiResponse<ParentResponse> getParentById(Long id);
}
