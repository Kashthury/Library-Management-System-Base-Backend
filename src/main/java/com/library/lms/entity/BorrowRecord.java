package com.library.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One borrowing transaction: a student borrows N copies of one book.
 * <p>
 * numberOfBooks is the quantity STILL borrowed (it is reduced on every return and reaches 0
 * when everything has been returned). originalQuantity keeps the quantity that was borrowed.
 */
@Entity
@Table(name = "borrow_records", indexes = {
        @Index(name = "idx_borrow_student", columnList = "student_id"),
        @Index(name = "idx_borrow_book", columnList = "book_id")
})
@Getter
@Setter
@NoArgsConstructor
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "borrow_record_id")
    private Long borrowRecordId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_borrow_record_student"))
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_borrow_record_book"))
    private Book book;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /** Set only when the LAST book of this record has been returned. */
    @Column(name = "return_date")
    private LocalDate returnDate;

    /** Books of this record that are still borrowed. */
    @Column(name = "number_of_books", nullable = false)
    private Integer numberOfBooks;

    @Column(name = "original_quantity", nullable = false)
    private Integer originalQuantity;

    /** Late days measured at the latest return (null until something is returned). */
    @Column(name = "overdue_days")
    private Integer overdueDays;

    /** Fine charged for this record (null until something is returned). */
    @Column(name = "fine_amount", precision = 10, scale = 2)
    private BigDecimal fineAmount;

    public boolean isActive() {
        return returnDate == null && numberOfBooks != null && numberOfBooks > 0;
    }
}
