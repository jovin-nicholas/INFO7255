package com.info7255.springdataredis.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.info7255.springdataredis.entity.Plan;
import com.info7255.springdataredis.entity.PlanService;
import com.info7255.springdataredis.repository.PlanRepository;
import com.info7255.springdataredis.util.JsonSchemaValidator;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author jovinnicholas
 */
@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {
    @Autowired
    private PlanRepository planRepository;
    private final JsonSchemaValidator jsonSchemaValidator;
    Logger log = Logger.getLogger(PlanController.class.getName());

    public PlanController(JsonSchemaValidator jsonSchemaValidator) {
        this.jsonSchemaValidator = jsonSchemaValidator;
    }

    @PostMapping("/")
    public ResponseEntity<?> createPlan(@RequestBody JsonNode jsonNode) {
        try {
            log.info(jsonNode.get("objectId").asText());
            jsonSchemaValidator.validate(jsonNode);

            String planId = jsonNode.get("objectId").asText();
            if(planRepository.findById(planId)==null) {
                ObjectMapper objectMapper = new ObjectMapper();
                Plan plan = objectMapper.treeToValue(jsonNode, Plan.class);
                planRepository.savePlan(plan);
                String eTag = getEtag(plan);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .header("ETag", eTag)
                        .body("Plan is valid and saved!");
            }
            else{
                return new ResponseEntity<>("Plan already exists!", HttpStatus.CONFLICT);
            }
        }
        catch (Exception e) {
            return new ResponseEntity<>("Invalid JSON: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping ("/{objectId}")
    public ResponseEntity<?> updatePlan(
            @PathVariable String objectId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestBody JsonNode jsonNode) {
        try {
            Plan plan = planRepository.findById(objectId);

            // Validate incoming JSON
            jsonSchemaValidator.validate(jsonNode);

            // Check if the plan exists
            if (plan == null) {
                return new ResponseEntity<>("Plan not found with id: " + objectId, HttpStatus.NOT_FOUND);
            }

            // Ensure the objectId cannot be changed
            String updatePlanId = jsonNode.get("objectId").asText();
            if (!Objects.equals(plan.getObjectId(), updatePlanId)) {
                return new ResponseEntity<>("Plan id cannot change: " + objectId, HttpStatus.CONFLICT);
            }

            // ETag-based conditional update check
            ResponseEntity<String> PRECONDITION_FAILED = checkIfMatch(ifMatch, plan);
            if (PRECONDITION_FAILED != null) return PRECONDITION_FAILED;

            ObjectMapper objectMapper = new ObjectMapper();
            Plan newPlan = objectMapper.treeToValue(jsonNode, Plan.class);
            planRepository.savePlan(newPlan);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("ETag", getEtag(newPlan))
                    .body("Plan updated!");
        }
        catch (Exception e) {
            return new ResponseEntity<>("Invalid JSON: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{objectId}")
    public ResponseEntity<?> patchPlan(
            @PathVariable String objectId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestBody JsonNode jsonNode) {
        try {
            // Find the existing plan by ID
            Plan plan = planRepository.findById(objectId);

            // Validate incoming JSON
            jsonSchemaValidator.validate(jsonNode);

            // Check if the plan exists
            if (plan == null) {
                return new ResponseEntity<>("Plan not found with id: " + objectId, HttpStatus.NOT_FOUND);
            }

            // Ensure the objectId cannot be changed
            String updatePlanId = jsonNode.get("objectId").asText();
            if (!Objects.equals(plan.getObjectId(), updatePlanId)) {
                return new ResponseEntity<>("Plan id cannot change: " + objectId, HttpStatus.CONFLICT);
            }

            // ETag-based conditional update check
            ResponseEntity<String> PRECONDITION_FAILED = checkIfMatch(ifMatch, plan);
            if (PRECONDITION_FAILED != null) return PRECONDITION_FAILED;

            ObjectMapper objectMapper = new ObjectMapper();

            // Handle array appending specifically for "linkedPlanServices"
            if (jsonNode.has("linkedPlanServices") && jsonNode.get("linkedPlanServices").isArray()) {
                List<PlanService> existingArray = plan.getLinkedPlanServices();
                if (existingArray == null) {
                    existingArray = new ArrayList<>();
                    plan.setLinkedPlanServices(existingArray);
                }
                // Append new elements
                for (JsonNode newElement : jsonNode.get("linkedPlanServices")) {
                    PlanService element = objectMapper.treeToValue(newElement, PlanService.class);
                    existingArray.add(element);
                }
            }

            // Create a temporary JSON without the array field(s) to prevent overwriting them
            ObjectNode jsonNodeWithoutArray = jsonNode.deepCopy();
            jsonNodeWithoutArray.remove("linkedPlanServices"); // Remove array fields to avoid overwriting

            Plan updatedPlan = objectMapper.readerForUpdating(plan).readValue(jsonNodeWithoutArray.toString(), Plan.class);
            planRepository.savePlan(updatedPlan);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("ETag", getEtag(updatedPlan))
                    .body("Plan updated!");
        } catch (Exception e) {
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
        String eTag = getEtag(plan);
        if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }

        return ResponseEntity.ok().eTag(eTag).body(plan);
    }

    @DeleteMapping("/{objectId}")
    public ResponseEntity<?> deletePlan(@PathVariable String objectId, @RequestHeader(value = "If-Match", required = false) String ifMatch){
        if(planRepository.findById(objectId)!=null){
            Plan plan = planRepository.findById(objectId);
            ResponseEntity<String> PRECONDITION_FAILED = checkIfMatch(ifMatch, plan);
            if (PRECONDITION_FAILED != null) return PRECONDITION_FAILED;
            planRepository.deletePlan(objectId);
            return new ResponseEntity<>("Plan with id " + objectId + " deleted succesfully!", HttpStatus.NO_CONTENT);
        }
        else {
            return new ResponseEntity<>("Plan with id " + objectId + " not found!", HttpStatus.CONFLICT);
        }
    }

    @Nullable
    private static ResponseEntity<String> checkIfMatch(String ifMatch, Plan plan) {
        // ETag-based conditional update check
        String eTag = getEtag(plan);
        if (ifMatch == null || !ifMatch.equals(eTag)) {
            return new ResponseEntity<>("Precondition failed", HttpStatus.PRECONDITION_FAILED);
        }
        return null;
    }

    @NotNull
    private static String getEtag(Plan plan) {
        String hash = Integer.toString(plan.hashCode());
        return "W/\"" + hash + "\"";
    }
}
