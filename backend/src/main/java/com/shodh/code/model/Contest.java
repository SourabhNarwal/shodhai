package com.shodh.code.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "contests")
@Data
public class Contest {
    @Id
    private String id;
    private String name;
    private Instant startTime;
    private Instant endTime;
}
