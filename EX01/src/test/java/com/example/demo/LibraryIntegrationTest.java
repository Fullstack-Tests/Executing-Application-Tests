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

@SpringBootTest // 스프링 컨테이너를 실행하여 통합 테스트 환경을 구성하는 어노테이션
@AutoConfigureMockMvc // MockMvc를 자동 등록하여 HTTP 요청 테스트를 가능하게 하는 어노테이션
class LibraryIntegrationTest {

    // === MockMvc 주입 ===
    @Autowired // 스프링 컨테이너에 등록된 Bean을 자동으로 주입하는 어노테이션
    private MockMvc mockMvc; // HTTP 요청을 테스트하기 위한 MockMvc 객체

    // === Repository 주입 ===
    @Autowired // 스프링 컨테이너에 등록된 Bean을 자동으로 주입하는 어노테이션
    private BookRepository bookRepository; // BookRepository를 스프링 컨테이너에서 주입받는다.

    // === 도서 대출 후 재고 반영 테스트 ===
    // 도서를 대출한 뒤 재고가 0으로 변경되고 조회 API에 반영되는지 확인한다.
    @Test // JUnit이 해당 메서드를 테스트 케이스로 인식하고 실행하도록 지정하는 어노테이션
    @DisplayName("대출하면 재고가 0 이 되고 조회 API 에 반영된다") // 테스트의 목적을 문자열로 정의하는 어노테이션

    void 대출_재고반영_흐름() throws Exception {

        // == 테스트용 도서 저장 ==
        // .builder(): 복잡한 객체를 단계별로 생성할 수 있도록 하는 생성 패턴
        Book saved = bookRepository.save(Book.builder()     // Book 생성 & DB에 저장
                .title("통합테스트 도서") // 제목 설정
                .isbn("INT-1")         // ISBN 설정
                .totalCopies(1)        // 총 권수 설정
                .availableCopies(1)    // 대출 가능 권수 설정
                .build());             // Book 객체 생성

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
