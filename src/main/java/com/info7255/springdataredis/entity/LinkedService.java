package com.info7255.springdataredis.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jovinnicholas
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkedService {
    private String _org;
    private String objectId;
    private String objectType;
    private String name;
}
