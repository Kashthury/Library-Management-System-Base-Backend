package com.library.lms.repository;

import com.library.lms.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    /** Row lock so concurrent borrowers cannot both take the last copy. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.bookId = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);

    @Query("select coalesce(sum(b.availableQuantity), 0L) from Book b")
    long totalAvailableQuantity();
}
