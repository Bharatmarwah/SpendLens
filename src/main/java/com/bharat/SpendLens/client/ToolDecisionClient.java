package com.bharat.SpendLens.client;

import com.bharat.SpendLens.requestdto.AiRequest;
import com.bharat.SpendLens.requestdto.AiReportRequest;
import com.bharat.SpendLens.responsedto.AiResponse;
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


    public AiResponse getExpenseSummary(AiReportRequest request) {
        try {
            AiResponse response = webClient.post()
                    .uri("/generate-report")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiResponse.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Tool decision service returned null response");
            }
            return response;

        } catch (WebClientResponseException e) {
            throw new RuntimeException("FastAPI service error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call report generate service: " + e.getMessage(), e);
        }
    }

    public ToolMessageResponse getToolExtraction(AiRequest request){
        try{
            com.bharat.SpendLens.responsedto.ToolMessageResponse response
                    = webClient
                    .post()
                    .uri("/decide-tool")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ToolMessageResponse.class)
                    .block();

            if(response==null){
                throw new RuntimeException("Tool decision service returning null");
            }

            return response;
        }catch (Exception e){
            throw new RuntimeException("FastAPI failed to response "+e.getMessage());
        }
    }
}
