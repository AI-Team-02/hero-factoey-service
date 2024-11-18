package com.herofactory.inspecteditem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AutoInspectionResult {
    private String status; // "GOOD" or "BAD"
    private String[] tags;

}
