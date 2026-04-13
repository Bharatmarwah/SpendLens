package com.bharat.SpendLens.responsedto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ToolMessageResponse {

    private String type; // "tool_call" or "final_answer"

    @JsonProperty("tool_name")
    private String toolName;

    private Map<String, Object> arguments;

    private String message;
}