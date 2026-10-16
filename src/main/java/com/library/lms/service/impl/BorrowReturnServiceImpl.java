package com.library.lms.service.impl;

import com.library.lms.config.LibraryProperties;
import com.library.lms.dto.request.BorrowRequest;
import com.library.lms.dto.request.ReturnRequest;
import com.library.lms.dto.response.BorrowRecordResponse;
import com.library.lms.entity.Book;
import com.library.lms.entity.BorrowRecord;
import com.library.lms.entity.Student;
import com.library.lms.exception.BadRequestException;
import com.library.lms.exception.BusinessRuleException;
import com.library.lms.exception.ResourceNotFoundException;
import com.library.lms.mapper.BorrowRecordMapper;
import com.library.lms.repository.BookRepository;
import com.library.lms.repository.BorrowRecordRepository;
import com.library.lms.repository.StudentRepository;
import com.library.lms.service.BorrowReturnService;
import com.library.lms.util.FineCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowReturnServiceImpl implements BorrowReturnService {

    private final StudentRepository studentRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BorrowRecordMapper borrowRecordMapper;
    private final LibraryProperties properties;

    // ------------------------------------------------------------------ borrow

    @Override
    @Transactional
    public BorrowRecordResponse borrow(BorrowRequest request) {
        // Lock order is always student -> book to avoid deadlocks.
        Student student = lockStudent(request.studentId());
        Book book = lockBook(request.bookId());
        int quantity = request.numberOfBooks();

        LocalDate borrowDate = request.borrowDate() != null ? request.borrowDate() : LocalDate.now();
        LocalDate dueDate = request.dueDate() != null
                ? request.dueDate()
                : borrowDate.plusDays(properties.getDefaultLoanDays());
        if (dueDate.isBefore(borrowDate)) {
            throw new BadRequestException("Due date cannot be earlier than borrow date");
        }

        if (properties.isBlockBorrowWithOutstandingFine() && student.getFineAmount().signum() > 0) {
            throw new BusinessRuleException("Student has an unpaid fine of Rs. " + student.getFineAmount()
                    + ". Please settle it before borrowing more books.");
        }

        // Rule: enough copies must be on the shelf.
        if (book.getAvailableQuantity() < quantity) {
            throw new BusinessRuleException(String.format(
                    "Cannot borrow %d copy/copies of '%s': only %d available. Please choose a quantity of %d or less.",
                    quantity, book.getBookTitle(), book.getAvailableQuantity(), book.getAvailableQuantity()));
        }

        // Rule: a student may hold at most N books at the same time.
        long currentlyBorrowed = borrowRecordRepository.countCurrentlyBorrowedByStudent(student.getStudentId());
        long newTotal = currentlyBorrowed + quantity;
        int max = properties.getMaxBooksPerStudent();
        if (newTotal > max) {
            throw new BusinessRuleException(String.format(
                    "Cannot borrow %d book(s): this student currently has %d and the request would bring the total to %d. The maximum is %d books per student.",
                    quantity, currentlyBorrowed, newTotal, max));
        }

        book.setAvailableQuantity(book.getAvailableQuantity() - quantity);

        BorrowRecord record = new BorrowRecord();
        record.setStudent(student);
        record.setBook(book);
        record.setBorrowDate(borrowDate);
        record.setDueDate(dueDate);
        record.setNumberOfBooks(quantity);
        record.setOriginalQuantity(quantity);

        return borrowRecordMapper.toResponse(borrowRecordRepository.save(record));
    }

    // ------------------------------------------------------------------ return

    @Override
    @Transactional
    public BorrowRecordResponse returnBooks(ReturnRequest request) {
        Student student = lockStudent(request.studentId());
        Book book = lockBook(request.bookId());
        int quantity = request.numberOfBooks();
        LocalDate returnDate = request.returnDate() != null ? request.returnDate() : LocalDate.now();

        List<BorrowRecord> activeRecords =
                borrowRecordRepository.findActiveByStudentAndBook(student.getStudentId(), book.getBookId());
        if (activeRecords.isEmpty()) {
            throw new BusinessRuleException("Student #" + student.getStudentId()
                    + " has no active borrow record for book #" + book.getBookId());
        }

        int currentlyBorrowed = activeRecords.stream().mapToInt(BorrowRecord::getNumberOfBooks).sum();
        if (quantity > currentlyBorrowed) {
            throw new BusinessRuleException(String.format(
                    "Cannot return %d book(s): student currently has only %d of this book", quantity, currentlyBorrowed));
        }

        // Apply the return to the oldest records first.
        List<BorrowRecord> touched = new ArrayList<>();
        int remaining = quantity;
        for (BorrowRecord record : activeRecords) {
            if (remaining == 0) {
                break;
            }
            if (returnDate.isBefore(record.getBorrowDate())) {
                throw new BadRequestException("Return date cannot be earlier than the borrow date ("
                        + record.getBorrowDate() + ")");
            }

            int returning = Math.min(record.getNumberOfBooks(), remaining);
            record.setNumberOfBooks(record.getNumberOfBooks() - returning); // never negative
            remaining -= returning;

            if (record.getNumberOfBooks() == 0) {
                record.setReturnDate(returnDate); // record is closed once every book is back
            }

            applyFine(record, student, returnDate);
            touched.add(record);
        }

        book.setAvailableQuantity(book.getAvailableQuantity() + quantity);

        borrowRecordRepository.saveAll(touched);
        return borrowRecordMapper.toResponse(touched.get(0));
    }

    /**
     * Automatic fine: late days = returnDate - dueDate, fine looked up from the fine table.
     * The record keeps the fine for its WORST lateness so far; only the increase is added to the
     * student's total, so returning one record in several parts never charges the same days twice.
     */
    private void applyFine(BorrowRecord record, Student student, LocalDate returnDate) {
        int lateNow = (int) Math.max(0, ChronoUnit.DAYS.between(record.getDueDate(), returnDate));
        int lateDays = Math.max(lateNow, record.getOverdueDays() == null ? 0 : record.getOverdueDays());

        BigDecimal newFine = FineCalculator.calculate(lateDays);
        BigDecimal previousFine = record.getFineAmount() == null ? BigDecimal.ZERO : record.getFineAmount();
        BigDecimal increase = newFine.subtract(previousFine);

        if (increase.signum() > 0) {
            student.setFineAmount(student.getFineAmount().add(increase));
        }
        record.setOverdueDays(lateDays);
        record.setFineAmount(newFine);
    }

    // ------------------------------------------------------------------ queries

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getAllRecords() {
        return borrowRecordMapper.toResponseList(borrowRecordRepository.findAllWithDetails());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getRecordsByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student Not Found with id " + studentId);
        }
        return borrowRecordMapper.toResponseList(borrowRecordRepository.findByStudentWithDetails(studentId));
    }

    // ------------------------------------------------------------------ helpers

    private Student lockStudent(Long id) {
        return studentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student Not Found with id " + id));
    }

    private Book lockBook(Long id) {
        return bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book Not Found with id " + id));
    }
}
