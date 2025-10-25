package com.kindergarten.management_school.service.parent;

import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.ParentResponse;
import com.kindergarten.management_school.entity.Parents;
import com.kindergarten.management_school.repository.ParentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ParentsRepository parentRepository;

    @Override
    public ApiResponse<List<ParentResponse>> getAllParents() {
        List<ParentResponse> responses = parentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.<List<ParentResponse>>builder()
                .status(200)
                .message("Lấy danh sách phụ huynh thành công")
                .data(responses)
                .build();
    }

    @Override
    public ApiResponse<ParentResponse> getParentById(Long id) {
        Parents parent = parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phụ huynh với ID: " + id));

        return ApiResponse.<ParentResponse>builder()
                .status(200)
                .message("Lấy thông tin phụ huynh thành công")
                .data(mapToResponse(parent))
                .build();
    }

    private ParentResponse mapToResponse(Parents parent) {
        return ParentResponse.builder()
                .id(parent.getId())
                .fullName(parent.getFullName())
                .dateOfBirth(parent.getDateOfBirth())
                .username(parent.getUsername())
                .phone(parent.getPhone())
                .email(parent.getEmail())
                .studentNames(parent.getChildren() != null
                        ? parent.getChildren().stream()
                        .map(s -> s.getFullName() +
                                (s.getClazz() != null
                                        ? " - " + s.getClazz().getClassName()
                                        : ""))
                        .collect(Collectors.toList())
                        : null)
                .status(Boolean.TRUE.equals(parent.getIsBlocked())
                        ? "Đã khóa"
                        : "Hoạt động")
                .build();
    }
}
