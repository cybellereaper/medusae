package com.github.cybellereaper.medusae.servicea.application.workflow;

import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;

public interface WorkflowDefinitionParser {

    WorkflowDefinition parseJson(String payload);

    WorkflowDefinition parseYaml(String payload);
}
