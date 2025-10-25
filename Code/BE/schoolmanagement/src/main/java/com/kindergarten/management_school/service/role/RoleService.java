package com.kindergarten.management_school.service.role;


import com.kindergarten.management_school.dto.request.RoleDTO;
import com.kindergarten.management_school.entity.Role;
import com.kindergarten.management_school.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoleService {
    private RoleRepository roleRepo;

    public void createRole(RoleDTO roleDTO) {
        Role role = Role.builder()
                .name(roleDTO.getName())
                .build();
        roleRepo.save(role);
    }
}
