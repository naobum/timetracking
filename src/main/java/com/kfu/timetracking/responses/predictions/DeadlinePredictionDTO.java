package com.kfu.timetracking.responses.predictions;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeadlinePredictionDTO {
    private String subject; 
    private LocalDateTime deadline; 
    private Double hoursLeft;
    private RiskLevel risk;
}
