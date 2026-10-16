package com.library.lms.controller;

import com.library.lms.dto.response.LibrarySummaryResponse;
import com.library.lms.service.LibrarySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibrarySummaryController {

    private final LibrarySummaryService librarySummaryService;

    @GetMapping("/summary")
    public LibrarySummaryResponse getSummary() {
        return librarySummaryService.getSummary();
    }
}
