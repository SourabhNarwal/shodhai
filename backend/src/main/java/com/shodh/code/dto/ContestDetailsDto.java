package com.shodh.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestDetailsDto {
    private String id;
    private String name;
    private Instant startTime;
    private Instant endTime;
    private List<ProblemDto> problems;
}
