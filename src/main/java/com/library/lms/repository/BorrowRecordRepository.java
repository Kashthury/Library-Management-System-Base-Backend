package com.library.lms.repository;

import com.library.lms.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    /** Books the student currently holds (sum of the still-borrowed quantity of all records). */
    @Query("select coalesce(sum(r.numberOfBooks), 0L) from BorrowRecord r where r.student.studentId = :studentId")
    long countCurrentlyBorrowedByStudent(@Param("studentId") Long studentId);

    /** Active records of one student+book, oldest first (returns are applied FIFO). */
    @Query("select r from BorrowRecord r "
            + "where r.student.studentId = :studentId and r.book.bookId = :bookId and r.numberOfBooks > 0 "
            + "order by r.borrowDate asc, r.borrowRecordId asc")
    List<BorrowRecord> findActiveByStudentAndBook(@Param("studentId") Long studentId,
                                                  @Param("bookId") Long bookId);

    @Query("select r from BorrowRecord r join fetch r.student join fetch r.book "
            + "order by case when r.returnDate is null and r.numberOfBooks > 0 then 0 else 1 end asc, "
            + "r.borrowRecordId desc")
    List<BorrowRecord> findAllWithDetails();

    @Query("select r from BorrowRecord r join fetch r.student join fetch r.book "
            + "where r.student.studentId = :studentId "
            + "order by case when r.returnDate is null and r.numberOfBooks > 0 then 0 else 1 end asc, "
            + "r.borrowRecordId desc")
    List<BorrowRecord> findByStudentWithDetails(@Param("studentId") Long studentId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from BorrowRecord r where r.student.studentId = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);

    // ---- summary queries ----

    @Query("select coalesce(sum(r.numberOfBooks), 0L) from BorrowRecord r")
    long totalCurrentlyBorrowed();

    /** One row per student that currently holds books: [studentId, booksHeld]. */
    @Query("select r.student.studentId, sum(r.numberOfBooks) from BorrowRecord r "
            + "group by r.student.studentId having sum(r.numberOfBooks) > 0")
    List<Object[]> findCurrentlyBorrowedPerStudent();
}
