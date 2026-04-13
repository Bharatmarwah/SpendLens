package com.bharat.SpendLens.client;

import com.bharat.SpendLens.requestdto.AiRequest;
import com.bharat.SpendLens.responsedto.ToolMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToolDecisionClient {

    private final WebClient webClient;

    public ToolMessageResponse getToolMessageResponse(AiRequest request) {
        try {
            ToolMessageResponse response = webClient
                    .post()
                    .uri("/decide-tool")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ToolMessageResponse.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Tool decision service returned null response");
            }
            return response;

        } catch (WebClientResponseException e) {
            throw new RuntimeException("FastAPI service error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call tool decision service: " + e.getMessage(), e);
        }
    }





}
