package com.info7255.springdataredis.repository;

import com.info7255.springdataredis.entity.Plan;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author jovinnicholas
 */
@Repository
public interface IPlanRepository extends ElasticsearchRepository<Plan, String> {
}
