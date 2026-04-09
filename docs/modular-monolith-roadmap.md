# Service A Modular Monolith Roadmap (Discord Gateway + Interactions)

Start date: **April 9, 2026**.

## Scope and assumptions
- Repository is currently a framework-oriented codebase; this roadmap defines a production wiring path without breaking existing public APIs.
- Tenant = Discord guild (`guildId`), enforced in every application boundary.
- Interaction ACK p95 target is `< 1s` with deferred fallback.

## Module boundaries (target)
- `commands` — slash command definitions, schema export, command execution.
- `interactions` — buttons/selects/modals routing and execution.
- `automations` — workflow model, validation, deterministic execution runtime.
- `authz` — OAuth identity mapping, RBAC, fine-grained scopes.
- `tenant` — guild configuration, tenant guards, isolation policies.
- `observability` — structured logging, trace/correlation, metrics.

## Milestones

### M1 (April 9-12, 2026): Architecture baseline + scaffolding
- Define clean architecture package boundaries (domain/application/infrastructure/interface).
- Add tenant boundary guards and command/interaction isolation checks.
- Add CI workflow gates (compile + test + static checks).
- Deliverable: compile-safe scaffold and regression tests.

### M2 (April 12-16, 2026): Commands/interactions/modals stable
- Add shard manager abstraction and reconnect/resume strategy interfaces.
- Add idempotency contract for interaction retries.
- Add timeout-safe ACK orchestration policy with metrics hooks.
- Deliverable: end-to-end interaction vertical slice with deterministic tests.

### M3 (April 16-22, 2026): Visual workflow automation engine
- Add workflow domain graph + declarative JSON/YAML parser.
- Support trigger adapters: Discord event, cron, webhook.
- Implement dry-run simulation mode and publish validation.
- Deliverable: per-guild workflow execution with audit trails.

### M4 (April 22-26, 2026): Hardening/compliance/observability/release
- GDPR DSAR export/deletion hooks and data mapping docs per module.
- Health/readiness probes and graceful shutdown for in-flight tasks.
- VPS deployment runbook + rollback verification.
- Deliverable: release candidate and operations checklist.
