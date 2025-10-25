package com.kindergarten.management_school.controller.classes;

import com.kindergarten.management_school.dto.request.ClassRequest;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.ClassResponse;
import com.kindergarten.management_school.service.classes.ClassesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
public class ClassesController {

    private final ClassesService classesService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ClassResponse>> create(@RequestBody ClassRequest request) {
        return ResponseEntity.status(201).body(classesService.createClass(request));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ClassResponse>> update(@PathVariable Long id, @RequestBody ClassRequest request) {
        return ResponseEntity.ok(classesService.updateClass(id, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(classesService.deleteClass(id));
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<ApiResponse<ClassResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(classesService.getClassById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getAll() {
        return ResponseEntity.ok(classesService.getAllClasses());
    }
}
