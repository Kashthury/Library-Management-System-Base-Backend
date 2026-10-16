package com.library.lms.service;

import com.library.lms.config.LibraryProperties;
import com.library.lms.dto.request.BorrowRequest;
import com.library.lms.dto.request.ReturnRequest;
import com.library.lms.dto.response.BorrowRecordResponse;
import com.library.lms.entity.Book;
import com.library.lms.entity.BorrowRecord;
import com.library.lms.entity.Student;
import com.library.lms.exception.BusinessRuleException;
import com.library.lms.mapper.BorrowRecordMapper;
import com.library.lms.repository.BookRepository;
import com.library.lms.repository.BorrowRecordRepository;
import com.library.lms.repository.StudentRepository;
import com.library.lms.service.impl.BorrowReturnServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pure unit tests (no Spring context, no database). */
class BorrowReturnServiceImplTest {

    private StudentRepository studentRepository;
    private BookRepository bookRepository;
    private BorrowRecordRepository borrowRecordRepository;
    private BorrowReturnServiceImpl service;

    private Student student;
    private Book book;

    @BeforeEach
    void setUp() {
        studentRepository = Mockito.mock(StudentRepository.class);
        bookRepository = Mockito.mock(BookRepository.class);
        borrowRecordRepository = Mockito.mock(BorrowRecordRepository.class);
        BorrowRecordMapper mapper = Mappers.getMapper(BorrowRecordMapper.class);

        service = new BorrowReturnServiceImpl(studentRepository, bookRepository,
                borrowRecordRepository, mapper, new LibraryProperties());

        student = new Student();
        student.setStudentId(1L);
        student.setStudentName("Nimal");
        student.setFineAmount(BigDecimal.ZERO);

        book = new Book();
        book.setBookId(10L);
        book.setBookTitle("Clean Code");
        book.setAvailableQuantity(5);

        when(studentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(student));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(book));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void borrowIsAllowedWhenTotalStaysWithinFive() {
        when(borrowRecordRepository.countCurrentlyBorrowedByStudent(1L)).thenReturn(2L); // 2 + 2 = 4

        BorrowRecordResponse response = service.borrow(new BorrowRequest(1L, 10L, 2, null, null));

        assertEquals(2, response.numberOfBooks());
        assertEquals(3, book.getAvailableQuantity());                       // stock reduced
        assertEquals(response.borrowDate().plusDays(14), response.dueDate()); // default loan period
    }

    @Test
    void borrowIsRejectedWhenTotalExceedsFive() {
        when(borrowRecordRepository.countCurrentlyBorrowedByStudent(1L)).thenReturn(4L); // 4 + 2 = 6

        BusinessRuleException error = assertThrows(BusinessRuleException.class,
                () -> service.borrow(new BorrowRequest(1L, 10L, 2, null, null)));

        assertEquals("Cannot borrow 2 book(s): this student currently has 4 and the request would bring the total to 6. The maximum is 5 books per student.",
                error.getMessage());
        assertEquals(5, book.getAvailableQuantity());                        // untouched
        verify(borrowRecordRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    void borrowIsRejectedWhenNotEnoughStock() {
        book.setAvailableQuantity(1);

        BusinessRuleException error = assertThrows(BusinessRuleException.class,
                () -> service.borrow(new BorrowRequest(1L, 10L, 2, null, null)));

        assertEquals("Cannot borrow 2 copy/copies of 'Clean Code': only 1 available. Please choose a quantity of 1 or less.",
                error.getMessage());
        assertEquals(1, book.getAvailableQuantity());
        verify(borrowRecordRepository, never()).countCurrentlyBorrowedByStudent(1L);
        verify(borrowRecordRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    void lateReturnClosesRecordRestocksBookAndAddsFine() {
        BorrowRecord record = activeRecord(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));
        when(borrowRecordRepository.findActiveByStudentAndBook(1L, 10L)).thenReturn(List.of(record));

        // due 2026-01-15, returned 2026-01-24 -> 9 days late -> Rs. 250
        BorrowRecordResponse response =
                service.returnBooks(new ReturnRequest(1L, 10L, 2, LocalDate.of(2026, 1, 24)));

        assertEquals(0, response.numberOfBooks());
        assertEquals(LocalDate.of(2026, 1, 24), response.returnDate());
        assertEquals(9, response.overdueDays());
        assertEquals(0, new BigDecimal("250").compareTo(response.fineAmount()));
        assertEquals(0, new BigDecimal("250").compareTo(student.getFineAmount())); // student updated
        assertEquals(7, book.getAvailableQuantity());                              // 5 + 2
    }

    @Test
    void onTimeReturnAddsNoFine() {
        BorrowRecord record = activeRecord(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));
        when(borrowRecordRepository.findActiveByStudentAndBook(1L, 10L)).thenReturn(List.of(record));

        service.returnBooks(new ReturnRequest(1L, 10L, 1, LocalDate.of(2026, 1, 15)));

        assertEquals(0, BigDecimal.ZERO.compareTo(student.getFineAmount()));
    }

    @Test
    void partialReturnKeepsRecordActive() {
        BorrowRecord record = activeRecord(3, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));
        when(borrowRecordRepository.findActiveByStudentAndBook(1L, 10L)).thenReturn(List.of(record));

        BorrowRecordResponse response =
                service.returnBooks(new ReturnRequest(1L, 10L, 1, LocalDate.of(2026, 1, 10)));

        assertEquals(2, response.numberOfBooks());
        assertNull(response.returnDate());
        assertEquals("ACTIVE", response.status());
    }

    @Test
    void returningMoreThanBorrowedIsRejected() {
        BorrowRecord record = activeRecord(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));
        when(borrowRecordRepository.findActiveByStudentAndBook(1L, 10L)).thenReturn(List.of(record));

        assertThrows(BusinessRuleException.class,
                () -> service.returnBooks(new ReturnRequest(1L, 10L, 3, LocalDate.of(2026, 1, 10))));

        assertEquals(5, book.getAvailableQuantity());
    }

    @Test
    void returningABookThatWasNeverBorrowedIsRejected() {
        when(borrowRecordRepository.findActiveByStudentAndBook(1L, 10L)).thenReturn(List.of());

        assertThrows(BusinessRuleException.class,
                () -> service.returnBooks(new ReturnRequest(1L, 10L, 1, null)));
    }

    private BorrowRecord activeRecord(int quantity, LocalDate borrowDate, LocalDate dueDate) {
        BorrowRecord record = new BorrowRecord();
        record.setBorrowRecordId(100L);
        record.setStudent(student);
        record.setBook(book);
        record.setBorrowDate(borrowDate);
        record.setDueDate(dueDate);
        record.setNumberOfBooks(quantity);
        record.setOriginalQuantity(quantity);
        return record;
    }
}
