package com.info7255.springdataredis.repository;

import com.info7255.springdataredis.entity.Plan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author jovinnicholas
 */
@Service
public class PlanRepository {
    @Autowired
    private RedisTemplate<String, Plan> redisPlanTemplate;

    public void savePlan(Plan plan) {
        redisPlanTemplate.opsForValue().set(plan.getObjectId(), plan);
    }

    public Plan findById(String objectId){
        return redisPlanTemplate.opsForValue().get(objectId);
    }

    public void deletePlan(String id) {
        redisPlanTemplate.delete(id);
    }
}
