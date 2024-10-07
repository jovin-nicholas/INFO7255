package com.info7255.springdataredis.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jovinnicholas
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanCostShare {
    private int deductible;
    private String _org;
    private int copay;
    private String objectId;
    private String objectType;
}
