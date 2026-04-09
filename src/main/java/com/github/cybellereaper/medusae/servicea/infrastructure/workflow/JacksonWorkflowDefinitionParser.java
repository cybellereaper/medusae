package com.github.cybellereaper.medusae.servicea.infrastructure.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.cybellereaper.medusae.servicea.application.workflow.WorkflowDefinitionParser;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public final class JacksonWorkflowDefinitionParser implements WorkflowDefinitionParser {

    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    public JacksonWorkflowDefinitionParser() {
        this(new ObjectMapper(), new YAMLMapper());
    }

    public JacksonWorkflowDefinitionParser(ObjectMapper jsonMapper, ObjectMapper yamlMapper) {
        this.jsonMapper = Objects.requireNonNull(jsonMapper, "jsonMapper");
        this.yamlMapper = Objects.requireNonNull(yamlMapper, "yamlMapper");
    }

    @Override
    public WorkflowDefinition parseJson(String payload) {
        return parse(jsonMapper, payload, "JSON");
    }

    @Override
    public WorkflowDefinition parseYaml(String payload) {
        return parse(yamlMapper, payload, "YAML");
    }

    private WorkflowDefinition parse(ObjectMapper mapper, String payload, String format) {
        try {
            return mapper.readValue(payload, WorkflowDefinition.class);
        } catch (IOException exception) {
            throw new UncheckedIOException("Invalid " + format + " workflow payload", exception);
        }
    }
}
