package com.kindergarten.management_school;

import com.kindergarten.management_school.dto.request.RoleDTO;
import com.kindergarten.management_school.service.role.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestRole {
    @Autowired
    private RoleService roleService;

    @Test
    void create(){
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("PARENT");
//        roleDTO.setName("TEACHER");
//        roleDTO.setName("ADMIN");
        roleService.createRole(roleDTO);
    }
}
