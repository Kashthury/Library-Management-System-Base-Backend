package com.library.lms.mapper;

import com.library.lms.dto.request.BookRequest;
import com.library.lms.dto.response.BookResponse;
import com.library.lms.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(target = "bookId", ignore = true)
    Book toEntity(BookRequest request);

    @Mapping(target = "bookId", ignore = true)
    void updateEntity(BookRequest request, @MappingTarget Book book);

    BookResponse toResponse(Book book);

    List<BookResponse> toResponseList(List<Book> books);
}
