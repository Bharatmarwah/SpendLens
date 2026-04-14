package com.bharat.SpendLens.controller;

import com.bharat.SpendLens.requestdto.AiRequest;
import com.bharat.SpendLens.responsedto.AiResponse;
import com.bharat.SpendLens.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/ask")
    public ResponseEntity<AiResponse> askAi(@RequestBody AiRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = authentication.getName();
        request.setUserId(userIdStr);
        return ResponseEntity.ok(aiService.askAi(request));
    }
}
