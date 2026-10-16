package com.library.lms.service;

import com.library.lms.dto.request.BookRequest;
import com.library.lms.dto.response.BookResponse;

import java.util.List;

public interface BookService {

    BookResponse create(BookRequest request);

    List<BookResponse> getAll();

    BookResponse getById(Long id);

    BookResponse update(Long id, BookRequest request);
}
