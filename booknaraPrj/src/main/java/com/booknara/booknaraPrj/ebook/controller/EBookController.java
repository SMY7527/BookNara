package com.booknara.booknaraPrj.ebook.controller;

import com.booknara.booknaraPrj.ebook.dto.DictRespDTO;
import com.booknara.booknaraPrj.ebook.dto.MyEBookItemDTO;
import com.booknara.booknaraPrj.ebook.dto.SaveCfiRequest;
import com.booknara.booknaraPrj.ebook.service.EBookService;
import com.booknara.booknaraPrj.ebook.service.GoogleDictionaryService;
import com.booknara.booknaraPrj.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class EBookController {
    private final GoogleDictionaryService dictService;
    private final EBookService service;

    @GetMapping("/ebook")
    public String ebookView(/*Authentication auth,*/
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) throws IOException {
        // DB에서 회원의 현재 대여중인 전자책 정보 가져오기
        System.out.println("ebook");
        //String userId = auth.getName();
        String userId = userDetails.getUserId();
        List<MyEBookItemDTO> e_list = service.findEBookList(userId);
        e_list.forEach(System.out::println);

        model.addAttribute("myEbookList", e_list);

        return "/ebook/ebook";
    }

    @GetMapping("/ebook/{bookid}")
    public String ebookView(@PathVariable("bookid") long bookid,
//                            Authentication auth,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) throws IOException {
//        String userId = auth.getName();
//        String userId = userDetails.getUserId();
//        // 검증
//        boolean hasAccess = service.canReadBook(userId, bookid);
//        if (!hasAccess) {
//            //throw new ResponseStatusException(HttpStatus.FORBIDDEN);
//            System.out.println("실패");
//        }
//        else {
//            // DB에서 도서 번호로 도서 isbn 가져와서 model에 저장
//            String isbn = service.findIsbn(bookid);
//            model.addAttribute("isbn", isbn);
//
//            SaveCfiRequest loc_info = service.findCfi(userId, isbn);
//            if(loc_info != null) {
//                float pct = loc_info.getPct() != null ? Float.parseFloat(loc_info.getPct()) : 0;
//                System.out.println(loc_info.getCfi());
//                System.out.println(loc_info.getPct());
//                System.out.println(loc_info.getHref());
//                model.addAttribute("lastCfi", loc_info.getCfi());
//                model.addAttribute("lastPct", pct);
//                model.addAttribute("lastHref", loc_info.getHref());
//            }
//        }
//
//        // DB에서 회원의 현재 대여중인 전자책 정보 가져오기
//        List<MyEBookItemDTO> e_list = service.findEBookList(userId);
//        e_list.forEach(System.out::println);
//
//        model.addAttribute("myEbookList", e_list);

        return "ebook/ebook";
    }

    @ResponseBody
    @GetMapping("/ebook/epub/{isbn}")
    public ResponseEntity<Resource> streamEpub(@PathVariable("isbn") String isbn,
//                                               Authentication auth,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        // 검증
//        String userId = auth.getName();
        String userId = userDetails.getUserId();
        boolean hasAccess = service.canReadBook(userId, isbn);
        if (!hasAccess) {
            //throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            System.out.println("실패");
            return null;
        }

        // DB에서 isbn기준으로 epub파일 가져오기
        System.out.println("stream");
        String epub = service.findEpub(isbn);
        return service.getEpubResource(epub);
    }

    @ResponseBody
    @PutMapping("/ebook/history/{isbn}")
    public ResponseEntity<Void> saveCfi(@PathVariable("isbn") String isbn,
                                        @RequestBody @Valid SaveCfiRequest req,
//                                        Authentication auth,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 세션에서 유저 정보 받아오기
//        String userId = auth.getName();
        String userId = userDetails.getUserId();

        boolean hasAccess = service.canReadBook(userId, isbn);
        if(hasAccess) {
            service.setCfi(userId, isbn, req.getCfi(), req.getPct(), req.getHref());
        }

        return ResponseEntity.noContent().build(); // 204
    }

    @ResponseBody
    @GetMapping("/ebook/dict/{word}")
    public DictRespDTO dict(@PathVariable("word") String word) {
        return dictService.getWordMean(word);
    }
}
