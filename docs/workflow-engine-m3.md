# M3 Workflow Engine Scaffold (April 16-22, 2026)

Implemented baseline pieces:
- workflow domain graph (`WorkflowDefinition`, `WorkflowNode`, `WorkflowEdge`, `WorkflowTrigger`)
- declarative parser support (`JacksonWorkflowDefinitionParser`) for JSON and YAML
- deterministic execution ordering in `WorkflowEngine` (topological order with lexical tie-breaking)
- dry-run mode (`WorkflowExecutionMode.DRY_RUN`) that records audits without executing actions
- publish-time validation and audit trail (`WorkflowPublishService` + `WorkflowValidator`)
- trigger adapter scaffolding for Discord events, cron, and webhook

Next steps:
- wire adapters to real gateway/scheduler/http ingress
- add persisted workflow and audit repositories (Mongo)
- add permission evaluator per action and per guild role/scope
