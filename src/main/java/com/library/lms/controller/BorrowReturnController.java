package com.library.lms.controller;

import com.library.lms.dto.request.BorrowRequest;
import com.library.lms.dto.request.ReturnRequest;
import com.library.lms.dto.response.BorrowRecordResponse;
import com.library.lms.service.BorrowReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BorrowReturnController {

    private final BorrowReturnService borrowReturnService;

    @PostMapping("/borrow")
    public ResponseEntity<BorrowRecordResponse> borrow(@Valid @RequestBody BorrowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(borrowReturnService.borrow(request));
    }

    @PostMapping("/return")
    public BorrowRecordResponse returnBooks(@Valid @RequestBody ReturnRequest request) {
        return borrowReturnService.returnBooks(request);
    }

    /** Used by the Borrow / Return page to list every record (newest first). */
    @GetMapping("/borrow")
    public List<BorrowRecordResponse> getAllRecords() {
        return borrowReturnService.getAllRecords();
    }

    @GetMapping("/borrow/student/{studentId}")
    public List<BorrowRecordResponse> getRecordsByStudent(@PathVariable Long studentId) {
        return borrowReturnService.getRecordsByStudent(studentId);
    }
}
