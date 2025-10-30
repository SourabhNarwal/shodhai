package com.shodh.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDto {
    private String userId;
    private String username;
    private Integer totalScore;
}
