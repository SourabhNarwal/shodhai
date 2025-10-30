package com.shodh.code.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "problems")
@Data
public class Problem {
    @Id
    private String id;
    private String title;
    private String description;
    private List<String> inputTestCases;
    private List<String> outputTestCases;
    private String contestId;
    private Integer maxScore = 100;
}
