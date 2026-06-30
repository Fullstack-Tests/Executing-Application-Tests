package com.example.demo;

import com.example.demo.Domain.Common.Entity.Book;
import com.example.demo.Domain.Common.Repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("ISBN 으로 도서를 조회하고 존재 여부를 확인한다")
    void findByIsbn_existsByIsbn() {
        bookRepository.save(Book.builder()
                .title("리팩터링").isbn("REPO-1").totalCopies(2).availableCopies(2).build());

        assertTrue(bookRepository.findByIsbn("REPO-1").isPresent());
        assertEquals("리팩터링", bookRepository.findByIsbn("REPO-1").get().getTitle());
        assertTrue(bookRepository.existsByIsbn("REPO-1"));
        assertFalse(bookRepository.existsByIsbn("NONE-0"));
    }
}
