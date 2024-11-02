package com.info7255.springdataredis.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.logging.Logger;

@Component
public class JsonSchemaValidator {

    Logger log = Logger.getLogger(JsonSchemaValidator.class.getName());

    private final Schema schema;
    private final ObjectMapper objectMapper = new ObjectMapper();

    enum SchemaType {
            DEMO1,
            DEMO2
    }

    public JsonSchemaValidator() throws Exception {
        // Load JSON Schema from file (located in src/main/resources)
        InputStream schemaStream = new ClassPathResource("plan-schema-demo2.json").getInputStream();
        JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaStream));
        this.schema = SchemaLoader.load(jsonSchema);
    }

    public JsonSchemaValidator(@Value("${schema.type}") String enumType) throws Exception {
        SchemaType schemaType = SchemaType.valueOf(enumType.toUpperCase());

        InputStream schemaStream = switch (schemaType) {
            case DEMO1 -> new ClassPathResource("plan-schema-demo1.json").getInputStream();
            case DEMO2 -> new ClassPathResource("plan-schema-demo2.json").getInputStream();
            default -> throw new IllegalArgumentException("Invalid schema type: " + enumType);
        };

        // Load the JSON schema from the selected file
        JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaStream));
        this.schema = SchemaLoader.load(jsonSchema);
    }

    public void validate(JsonNode jsonNode) throws Exception {
        JSONObject jsonObject = new JSONObject(objectMapper.writeValueAsString(jsonNode));
        System.out.println("Validating..");
        System.out.println("Serialized JSON: " + jsonObject);
        schema.validate(jsonObject);
    }
}
