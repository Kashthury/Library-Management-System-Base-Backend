package com.library.lms.controller;

import com.library.lms.dto.request.FinePaymentRequest;
import com.library.lms.dto.request.FineRequest;
import com.library.lms.dto.request.StudentRequest;
import com.library.lms.dto.response.StudentResponse;
import com.library.lms.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    @GetMapping
    public List<StudentResponse> getAll() {
        return studentService.getAll();
    }

    @GetMapping("/{id}")
    public StudentResponse getById(@PathVariable Long id) {
        return studentService.getById(id);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return studentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/gender/{gender}")
    public List<StudentResponse> getByGender(@PathVariable String gender) {
        return studentService.getByGender(gender);
    }

    @PostMapping("/{id}/fine")
    public StudentResponse calculateFine(@PathVariable Long id, @Valid @RequestBody FineRequest request) {
        return studentService.calculateFine(id, request);
    }

    @PostMapping("/{id}/fine/pay")
    public StudentResponse payFine(@PathVariable Long id, @Valid @RequestBody FinePaymentRequest request) {
        return studentService.payFine(id, request);
    }
}
