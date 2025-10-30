package com.shodh.code.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "submissions")
@Data
public class Submission {
    @Id
    private String id;
    private String userId;
    private String problemId;
    private String code;
    private String language;
    private String status;
    private String result;
    private Integer score;
    private Instant createdAt;
}
