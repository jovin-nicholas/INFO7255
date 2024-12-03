package com.info7255.springdataredis.repository;

import com.info7255.springdataredis.entity.Plan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * @author jovinnicholas
 */
@Service
public class PlanRepService {

    //Elasticsearch
    @Autowired
    private IPlanRepository planRepository;

    //search plan by id from Elasticsearch
    public Optional<Plan> searchById(String id) {
        return planRepository.findById(id);
    }

    //search all plans from Elasticsearch
    public Iterable<Plan> searchAll() {
        return planRepository.findAll();
    }


    //Redis
    @Autowired
    private RedisTemplate<String, Plan> redisPlanTemplate;

    //save plan to both Elasticsearch and Redis
    public void savePlan(Plan plan) {
        planRepository.save(plan);
        redisPlanTemplate.opsForValue().set(plan.getObjectId(), plan);
    }

    //find plan by id from Redis
    public Plan findById(String objectId){
        return redisPlanTemplate.opsForValue().get(objectId);
    }

    //delete plan from Redis and Elasticsearch
    public void deletePlan(String id) {
        if(planRepository.findById(id).isEmpty()){
            throw new NoSuchElementException("Plan not found");
        }
        planRepository.delete(planRepository.findById(id).get());
        redisPlanTemplate.delete(id);
    }
}
