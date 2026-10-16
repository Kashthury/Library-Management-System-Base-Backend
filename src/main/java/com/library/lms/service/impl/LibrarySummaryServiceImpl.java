package com.library.lms.service.impl;

import com.library.lms.dto.response.LibrarySummaryResponse;
import com.library.lms.enums.Gender;
import com.library.lms.repository.BookRepository;
import com.library.lms.repository.BorrowRecordRepository;
import com.library.lms.repository.StudentRepository;
import com.library.lms.service.LibrarySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Everything is calculated live from the Student, Book and BorrowRecord tables.
 * <p>
 * Definitions:
 *   totalRegisteredBooks = every copy the library owns (on the shelf + currently borrowed)
 *   totalAvailableBooks  = copies on the shelf
 *   totalBorrowedBooks   = copies currently borrowed (same value as totalBooksCurrentlyBorrowed)
 */
@Service
@RequiredArgsConstructor
public class LibrarySummaryServiceImpl implements LibrarySummaryService {

    private final StudentRepository studentRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public LibrarySummaryResponse getSummary() {
        long totalStudents = studentRepository.count();
        long male = studentRepository.countByGender(Gender.M);
        long female = studentRepository.countByGender(Gender.F);

        long borrowedNow = borrowRecordRepository.totalCurrentlyBorrowed();
        long available = bookRepository.totalAvailableQuantity();

        // How many students hold exactly 1..5 books right now.
        long[] byCount = new long[6];
        long studentsWithBooks = 0;
        List<Object[]> rows = borrowRecordRepository.findCurrentlyBorrowedPerStudent();
        for (Object[] row : rows) {
            long held = ((Number) row[1]).longValue();
            studentsWithBooks++;
            if (held >= 1 && held <= 5) {
                byCount[(int) held]++;
            }
        }
        long studentsWithNoBooks = totalStudents - studentsWithBooks;

        return LibrarySummaryResponse.builder()
                .totalStudents(totalStudents)
                .totalMaleStudents(male)
                .totalFemaleStudents(female)
                .totalBooksCurrentlyBorrowed(borrowedNow)
                .studentsBorrowing0Books(studentsWithNoBooks)
                .studentsBorrowing1Book(byCount[1])
                .studentsBorrowing2Books(byCount[2])
                .studentsBorrowing3Books(byCount[3])
                .studentsBorrowing4Books(byCount[4])
                .studentsBorrowing5Books(byCount[5])
                .totalRegisteredBooks(available + borrowedNow)
                .totalAvailableBooks(available)
                .totalBorrowedBooks(borrowedNow)
                .build();
    }
}
