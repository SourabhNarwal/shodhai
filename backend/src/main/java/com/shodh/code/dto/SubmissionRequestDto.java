package com.shodh.code.dto;

import lombok.Data;

@Data
public class SubmissionRequestDto {
    private String userId;
    private String problemId;
    private String code;
    private String language;
}
