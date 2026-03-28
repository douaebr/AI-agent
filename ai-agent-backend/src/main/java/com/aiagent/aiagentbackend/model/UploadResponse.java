package com.aiagent.aiagentbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    private String message;
    private String filename;
    private Integer characterCount;
    private boolean success;
}