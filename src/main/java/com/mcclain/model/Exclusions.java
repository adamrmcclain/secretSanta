package com.mcclain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Exclusions {
    private String value;
    private CompareEnum evaluator;
    private String object;

    public Exclusions(){}
}
