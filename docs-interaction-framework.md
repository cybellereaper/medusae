# Interaction Command Framework Migration Notes

The command framework now supports annotation-driven UI interaction handlers in addition to slash/context commands.

## What remains backward compatible

- Existing `@Command` modules and handler signatures continue to work unchanged.
- Returning existing `ImmediateResponse`, `DeferredResponse`, and `FollowupResponse` still works.
- Imperative response helpers on `InteractionContext` still function.

## New capabilities

- Add `@ButtonHandler`, `@StringSelectHandler`, `@UserSelectHandler`, `@RoleSelectHandler`, `@MentionableSelectHandler`, `@ChannelSelectHandler`, and `@ModalHandler` in the same module as `@Command` handlers.
- Use `@PathParam` to bind route-template params from component custom IDs.
- Use `@Field` to bind modal inputs by field ID.
- Return `InteractionReply` for rich message replies, message updates, and defer flows.
- Return `ModalReply` to open Discord modals from commands/components.

## Route templates

`ticket:close:{ticketId}` templates are now compiled and matched deterministically with conflict detection.

## Example flow

1. `/ticket` returns an ephemeral panel with embed + buttons.
2. `ticket:create` button opens a modal (`ModalReply`).
3. `ticket:create` modal submit binds fields by ID via `@Field`.
4. `ticket:close:{ticketId}` button binds typed route params via `@PathParam` and updates the original message.
