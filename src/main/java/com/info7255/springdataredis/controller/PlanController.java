package com.info7255.springdataredis.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.info7255.springdataredis.entity.Plan;
import com.info7255.springdataredis.repository.PlanRepository;
import com.info7255.springdataredis.util.JsonSchemaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

/**
 * @author jovinnicholas
 */
@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {
    @Autowired
    private PlanRepository planRepository;
    @Autowired
    private JsonSchemaValidator jsonSchemaValidator;
    Logger log = Logger.getLogger(PlanController.class.getName());

    @PostMapping("/")
    public ResponseEntity<?> createPlan(@RequestBody JsonNode jsonNode) {
        try {
            log.info(jsonNode.get("objectId").asText());
            jsonSchemaValidator.validate(jsonNode);

            ObjectMapper objectMapper = new ObjectMapper();
            Plan plan = objectMapper.treeToValue(jsonNode, Plan.class);
            planRepository.savePlan(plan);
            return new ResponseEntity<>("Plan is valid and saved!", HttpStatus.CREATED);

        }
        catch (Exception e) {
            return new ResponseEntity<>("Invalid JSON: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // response body is stored using the Plan POJO defined
    // all fields in the json request body will be converted to the correct mapping
//    @PostMapping("/validatedPost")
//    public ResponseEntity<?> createValidatedPlan(@RequestBody Plan plan) {
//        try {
//            log.info(plan.getObjectId());
//            jsonSchemaValidator.validatePlan(plan);
//
//            planRepository.savePlan(plan);
//            return new ResponseEntity<>("Plan is valid and saved!", HttpStatus.CREATED);
//
//        }
//        catch (Exception e) {
//            return new ResponseEntity<>("Invalid JSON: " + e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/{objectId}")
    public ResponseEntity<Plan> getPlan(@PathVariable String objectId, @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        Plan plan = planRepository.findById(objectId);
        if (plan == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

//        Implement conditional GET (If-None-Match with ETag support)
        String hash = Integer.toString(plan.hashCode());
        String eTag = "W/\"" + hash + "\"";
        if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }

        return ResponseEntity.ok().eTag(eTag).body(plan);
    }

    @DeleteMapping("/{objectId}")
    public ResponseEntity<?> deletePlan(@PathVariable String objectId){
        if(planRepository.findById(objectId)!=null){
            planRepository.deletePlan(objectId);
            return new ResponseEntity<>("Plan with id " + objectId + " deleted succesfully!", HttpStatus.NO_CONTENT);
        }
        else {
            return new ResponseEntity<>("Plan with id " + objectId + " not found!", HttpStatus.CONFLICT);
        }
    }
}
