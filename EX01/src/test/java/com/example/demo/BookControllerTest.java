package com.example.demo;

import com.example.demo.Controller.BookController;
import com.example.demo.Domain.Common.Entity.Lend;
import com.example.demo.Domain.Common.Service.LibraryService;
import com.example.demo.Exception.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibraryService libraryService;

    @Test
    @DisplayName("대출 정상: 200 과 lendId 를 반환한다")
    void lend_정상_200() throws Exception {
        // 도서 대출 요청이 유효할 때 컨트롤러가 정상 처리 결과물을 리턴하는지 검증하는 메서드
        Lend lend = Lend.builder().id(10L).dueAt(LocalDateTime.now().plusDays(14)).build();
        // 가짜 서비스가 리턴해 줄 대출 id 10번과 반납기한 14일이라는 정보가 담긴 Lend 결과물을 객체를 조립
        when(libraryService.lend(eq(1L), anyString())).thenReturn(lend);
        // libraryService의 lend 메서드에 책 id 1번과 무작위 회원명이 전달되면,
        // 위에서 만든 lend 객체를 리턴하는 규칙 선언

        mockMvc.perform(post("/api/library/lend")
                        // MockMvc를 사용해 "/api/library/lend" 주소로 가상의 HTTP POST 요청을 전송
                        .contentType(MediaType.APPLICATION_JSON)
                        // 서버로 전송하는 포맷이 JSON 문서 구조 임을 헤더에 기입
                        .content("{\"bookId\":1,\"member\":\"kim\"}"))
                // 실제 입력 폼의 데이터 규격(책 번호 1, 회원명 kim)을
                // JSON 텍스트 문자열로 만들어 HTTP Body에 보냄
                .andExpect(status().isOk())
                // 컨트롤러의 응답 결과 상태 코드가 정상 처리를 뜻하는 200 OK 인지 판단
                .andExpect(jsonPath("$.lendId").value(10));
        // 응답으로 전달받은 JSON 데이터 중 'lendId'라는 이름의 필드 값 구조가
        // 가짜 서비스에서 넘겨준 id 10과 일치하는지 최종 검증
    }

    @Test
    @DisplayName("대출 검증 실패: bookId 누락/member 공백이면 400")
    void lend_검증실패_400() throws Exception {
        mockMvc.perform(post("/api/library/lend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":null,\"member\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("대출 비즈니스 예외: 재고 없음이면 400 + error 메시지")
    void lend_재고없음_400() throws Exception {
        when(libraryService.lend(eq(2L), anyString()))
                .thenThrow(new BizException("대출 가능한 재고가 없습니다."));

        mockMvc.perform(post("/api/library/lend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":2,\"member\":\"kim\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
