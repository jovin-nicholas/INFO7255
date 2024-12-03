package com.info7255.springdataredis.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.info7255.springdataredis.entity.Plan;
import com.info7255.springdataredis.entity.PlanService;
import com.info7255.springdataredis.repository.PlanRepService;
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
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author jovinnicholas
 */
@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {
    @Autowired
    private PlanRepService planService;
    private final JsonSchemaValidator jsonSchemaValidator;
    Logger log = Logger.getLogger(PlanController.class.getName());

    public PlanController(JsonSchemaValidator jsonSchemaValidator) {
        this.jsonSchemaValidator = jsonSchemaValidator;
    }

    // Test endpoint
    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    // Create a new plan
    @PostMapping("/")
    public ResponseEntity<?> createPlan(@RequestBody JsonNode jsonNode) {
        try {
            log.info(jsonNode.get("objectId").asText());
            jsonSchemaValidator.validate(jsonNode);

            String planId = jsonNode.get("objectId").asText();
            if(planService.findById(planId)==null) {
                // Convert JSON to Plan object
                ObjectMapper objectMapper = new ObjectMapper();
                Plan plan = objectMapper.treeToValue(jsonNode, Plan.class);
                planService.savePlan(plan);
                // Return ETag in response header
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

    // Update a plan
    @PutMapping ("/{objectId}")
    public ResponseEntity<?> updatePlan(
            @PathVariable String objectId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestBody JsonNode jsonNode) {
        try {
            Plan plan = planService.findById(objectId);

            // Validate incoming JSON
            jsonSchemaValidator.validate(jsonNode);
            String updatePlanId = jsonNode.get("objectId").asText();

            // Check if the plan exists
            if (plan == null) {
                return new ResponseEntity<>("Plan not found with id: " + objectId, HttpStatus.NOT_FOUND);
            }

            // Ensure the objectId cannot be changed
            if (!Objects.equals(plan.getObjectId(), updatePlanId)) {
                return new ResponseEntity<>("Plan id cannot change: " + objectId, HttpStatus.CONFLICT);
            }

            // ETag-based conditional update check
            ResponseEntity<String> PRECONDITION_FAILED = checkIfMatch(ifMatch, plan);
            if (PRECONDITION_FAILED != null) return PRECONDITION_FAILED;

            ObjectMapper objectMapper = new ObjectMapper();
            Plan newPlan = objectMapper.treeToValue(jsonNode, Plan.class);
            planService.savePlan(newPlan);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("ETag", getEtag(newPlan))
                    .body("Plan updated!");
        }
        catch (Exception e) {
            return new ResponseEntity<>("Invalid JSON: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Patch a plan
    @PatchMapping("/{objectId}")
    public ResponseEntity<?> patchPlan(
            @PathVariable String objectId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestBody JsonNode jsonNode) {
        try {
            // Find the existing plan by ID
            Plan plan = planService.findById(objectId);

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
            if (!Objects.equals(plan.getPlanCostShares().getObjectId(), jsonNode.get("planCostShares").get("objectId").asText())) {
                return new ResponseEntity<>("objectId cannot change: " + objectId, HttpStatus.CONFLICT);
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
            planService.savePlan(updatedPlan);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("ETag", getEtag(updatedPlan))
                    .body("Plan updated!");
        } catch (Exception e) {
            return new ResponseEntity<>("Invalid JSON: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Get plan by id from Elasticsearch
    @GetMapping("/search/{objectId}")
    public ResponseEntity<Plan> searchPlan(@PathVariable String objectId, @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        Optional<Plan> planOptional = planService.searchById(objectId);
        if (planOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Plan plan = planOptional.get();

//        Implement conditional GET (If-None-Match with ETag support)
        String eTag = getEtag(plan);
        if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }

        return ResponseEntity.ok().eTag(eTag).body(plan);
    }

    // Get plan by id from Redis
    @GetMapping("/{objectId}")
    public ResponseEntity<Plan> getPlan(@PathVariable String objectId, @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        Plan plan = planService.findById(objectId);
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

    // Delete a plan from Redis and Elasticsearch
    @DeleteMapping("/{objectId}")
    public ResponseEntity<?> deletePlan(@PathVariable String objectId, @RequestHeader(value = "If-Match", required = false) String ifMatch){
        if(planService.findById(objectId)!=null){
            Plan plan = planService.findById(objectId);
            ResponseEntity<String> PRECONDITION_FAILED = checkIfMatch(ifMatch, plan);
            if (PRECONDITION_FAILED != null) return PRECONDITION_FAILED;
            planService.deletePlan(objectId);
            return new ResponseEntity<>("Plan with id " + objectId + " deleted successfully!", HttpStatus.NO_CONTENT);
        }
        else {
            return new ResponseEntity<>("Plan with id " + objectId + " not found!", HttpStatus.NOT_FOUND);
        }
    }

    // Search all plans from Elasticsearch
    @GetMapping("/search")
    public Iterable<Plan> searchAll() {
        return planService.searchAll();
    }

    // Check if the ETag matches the plan
    @Nullable
    private static ResponseEntity<String> checkIfMatch(String ifMatch, Plan plan) {
        // ETag-based conditional update check
        String eTag = getEtag(plan);
        if (ifMatch == null || !ifMatch.equals(eTag)) {
            return new ResponseEntity<>("Precondition failed", HttpStatus.PRECONDITION_FAILED);
        }
        return null;
    }

    // Generate ETag for a plan
    @NotNull
    private static String getEtag(Plan plan) {
        String hash = Integer.toString(plan.hashCode());
        return "W/\"" + hash + "\"";
    }
}
