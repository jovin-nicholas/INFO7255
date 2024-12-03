package com.info7255.springdataredis.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author jovinnicholas
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanService {
    @Field(type = FieldType.Nested)
    private LinkedService linkedService;

    @Field(type = FieldType.Nested)
    private PlanCostShare planserviceCostShares;

    @Field(type = FieldType.Text)
    private String _org;

    @Field(type = FieldType.Text)
    private String objectId;

    @Field(type = FieldType.Text)
    private String objectType;
}
