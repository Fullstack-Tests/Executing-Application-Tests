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
        // 필수 요구값 누락 시 컨트롤러 초입의 @Valid 유효성 검증 장치가 정상 작동하여 입구를 차단하는지 검증하는 메서드
        mockMvc.perform(post("/api/library/lend")
                        // MockMvc를 통해 동일한 대출 API 엔드포인트로 가상의 POST 요청을 진행
                        .contentType(MediaType.APPLICATION_JSON)
                        // 전송 데이터 규격을 JSON 포맷으로 세팅
                        .content("{\"bookId\":null,\"member\":\"\"}"))
                // 책 ID는 null로 비우고, 회원명은 빈 공백 문자열("")로 조작하여
                // 유효성 규칙을 무시한 데이터를 보냄
                .andExpect(status().isBadRequest());
        //  컨트롤러의 유효성 검증 가드에 걸려 서비스 계층까지 진입 못하고
        //  즉시 400 Bad Request 에러로 튕겨 나갔는지 검증
    }

    @Test
    @DisplayName("대출 비즈니스 예외: 재고 없음이면 400 + error 메시지")
    void lend_재고없음_400() throws Exception {
        // 도서 재고 부족 예외 발생 시 전역 컨트롤러 어드바이스 등이 예외를 안전하게 포맷팅 응답을 주는지 검증하는 메서드
        when(libraryService.lend(eq(2L), anyString()))
        // 가짜 서비스와 lend 메서드에 책 번호 2번과 아무 회원명이 주입되어 작동을 시작하면,
                .thenThrow(new BizException("대출 가능한 재고가 없습니다."));
                // 정상 객체를 리턴하지 말고 "재고가 없다"는 사유와 BizException 에러를 강제로 발생

        mockMvc.perform(post("/api/library/lend")
                        // 책 번호 2번을 빌리겠다는 HTTP POST 요청 패키지를 컨트롤러에 전달
                        .contentType(MediaType.APPLICATION_JSON)
                        // 컨텍트 타입을 JSON 형식으로 체크
                        .content("{\"bookId\":2,\"member\":\"kim\"}"))
                // 책 ID 2번과 회원명 kim 구조로 JSON 바디를 세팅하여 발송
                .andExpect(status().isBadRequest())
                // 가짜 서비스가 던진 비지니스 예외가 컨트롤러에서 캐치되어
                // 최종 400 Bad Request 응답 코드로 매핑 환원되었는지 확인
                .andExpect(jsonPath("$.error").exists());
        // 사용자 브라우저에게 실어다 주는 최종 에러 JSON 결과 구조 내에 "error"라는
        // 구체적인 사유 키(key) 필드가 실존하여 생성되어 있는지 점검
    }
}
