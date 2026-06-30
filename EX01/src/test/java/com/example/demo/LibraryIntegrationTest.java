package com.example.demo;

import com.example.demo.Domain.Common.Entity.Book;
import com.example.demo.Domain.Common.Repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("대출하면 재고가 0 이 되고 조회 API 에 반영된다")
    void 대출_재고반영_흐름() throws Exception {
        Book saved = bookRepository.save(Book.builder()
                .title("통합테스트 도서").isbn("INT-1").totalCopies(1).availableCopies(1).build());

        mockMvc.perform(post("/api/library/lend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + saved.getId() + ",\"member\":\"hong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lendId").exists());

        mockMvc.perform(get("/api/library/books/" + saved.getId() + "/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(0));
    }
}
