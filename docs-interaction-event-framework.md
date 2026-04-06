# Interaction + Event Framework Design Notes

## Route model
- Custom IDs are compiled once into `RouteTemplate` segment plans.
- Supports exact routes (e.g. `ticket:open`) and template routes (`ticket:close:{ticketId}`).
- Registration fails fast on conflicting templates and malformed placeholders.

## Field/parameter binding
- Registration parses handler method signatures into deterministic `InteractionParameter` metadata.
- Dispatch binds typed path params, modal fields (`@Field`), select values, and context types.
- Missing or invalid bindings throw explicit `InteractionBindingException` errors.

## Session/collector strategy
- `SessionRegistry` manages scoped sessions (`user/message/channel/guild/route`) with TTL.
- Lookups are performed during dispatch and expired sessions are proactively cleaned.
- Sessions support completion and cancellation markers for collector workflows.

## Event dispatch model
- Listener methods are compiled into `EventListenerDefinition` metadata.
- Dispatch policy is **exact event-class match** with deterministic order (`@Order`, descending).
- Optional `@Async` execution and `@Once` one-shot listener removal are supported.

## Intent-awareness strategy
- `@IntentRequired` can be placed on listener classes/methods.
- `EventFramework.intentDiagnostics()` reports listeners that cannot fire under current gateway intents.
- Dispatch skips listeners with missing required intents to prevent false assumptions.
