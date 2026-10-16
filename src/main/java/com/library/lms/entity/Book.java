package com.library.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "book_title", nullable = false, length = 200)
    private String bookTitle;

    @Column(nullable = false, length = 150)
    private String author;

    @Column(nullable = false, length = 100)
    private String category;

    /** Copies currently on the shelf (not borrowed). */
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
}
