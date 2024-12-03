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
@NoArgsConstructor
@AllArgsConstructor
public class LinkedService {
    @Field(type = FieldType.Text)
    private String _org;

    @Field(type = FieldType.Text)
    private String objectId;

    @Field(type = FieldType.Text)
    private String objectType;

    @Field(type = FieldType.Text)
    private String name;
}
