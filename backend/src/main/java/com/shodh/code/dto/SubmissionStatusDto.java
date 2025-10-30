package com.shodh.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionStatusDto {
    private String id;
    private String status;
    private String result;
    private Integer score;
}
