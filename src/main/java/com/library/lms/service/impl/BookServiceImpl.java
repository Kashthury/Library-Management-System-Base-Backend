package com.library.lms.service.impl;

import com.library.lms.dto.request.BookRequest;
import com.library.lms.dto.response.BookResponse;
import com.library.lms.entity.Book;
import com.library.lms.exception.ResourceNotFoundException;
import com.library.lms.mapper.BookMapper;
import com.library.lms.repository.BookRepository;
import com.library.lms.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public BookResponse create(BookRequest request) {
        return bookMapper.toResponse(bookRepository.save(bookMapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getAll() {
        return bookMapper.toResponseList(bookRepository.findAll(Sort.by("bookId")));
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getById(Long id) {
        return bookMapper.toResponse(findBook(id));
    }

    @Override
    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book Not Found with id " + id));
        bookMapper.updateEntity(request, book);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book Not Found with id " + id));
    }
}
