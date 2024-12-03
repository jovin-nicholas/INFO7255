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
public class PlanCostShare {
    @Field(type = FieldType.Integer)
    private int deductible;

    @Field(type = FieldType.Text)
    private String _org;

    @Field(type = FieldType.Integer)
    private int copay;

    @Field(type = FieldType.Text)
    private String objectId;

    @Field(type = FieldType.Text)
    private String objectType;
}
