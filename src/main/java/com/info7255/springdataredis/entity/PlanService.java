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
public class PlanService {
    private LinkedService linkedService;
    private PlanCostShare planserviceCostShares;
    private String _org;
    private String objectId;
    private String objectType;
}
