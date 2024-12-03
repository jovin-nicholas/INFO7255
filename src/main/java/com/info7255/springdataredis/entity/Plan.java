package com.info7255.springdataredis.entity;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * @author jovinnicholas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "plan_index")
public class Plan {
    @Id
    private String objectId;

    @Field(type = FieldType.Nested)
    private PlanCostShare planCostShares;

    @Field(type = FieldType.Nested)
    private List<PlanService> linkedPlanServices;

    @Field(type = FieldType.Text)
    private String _org;

    @Field(type = FieldType.Text)
    private String objectType;

//    @Nullable
//    private String planType;
    @Nullable
    @Field(type = FieldType.Text)
    private String planStatus;

    @Field(type = FieldType.Date, format = {}, pattern = "MM-dd-yyyy")
    private String creationDate;
}
