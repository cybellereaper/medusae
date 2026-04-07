# Security hardening notes

This project is primarily JVM-based. Most memory is managed by the Java runtime, so native allocator replacement is a **defense-in-depth** concern for JNI/native dependencies rather than a first-line memory-safety control.

## Baseline hardening in this repository

- Multipart upload headers sanitize user-controlled fields to prevent CRLF/header-injection vectors.
- Path identifiers are validated to reject unsafe/control characters before route construction.
- Interaction tokens are URL-encoded before being interpolated into REST paths.
- Attachment byte arrays are defensively copied on input/output to prevent accidental external mutation.
- Build output is made reproducible (`reproducibleFileOrder`, `preserveFileTimestamps=false`).
- Compiler/test defaults are tightened with linting and JVM assertions enabled in tests.

## `hardened_malloc` assessment

Status: **Prepared but not enabled by default**.

Why:

- Jellycord itself is Java code and does not directly control the allocator used for most heap objects.
- Replacing the process allocator globally (for example through `LD_PRELOAD`) can affect JVM and native libraries in ways that are deployment-specific.
- For many deployments this introduces compatibility/performance risk without proportionate benefit.

When it can make sense:

- Linux-only runtime.
- You have native/JNI dependencies or sidecars where allocator hardening is valuable.
- You have a staging environment to run soak and startup compatibility checks.

### Optional enablement pattern (Linux)

Do **not** enable by default. Use explicit opt-in for controlled environments:

```bash
LD_PRELOAD=/usr/lib/libhardened_malloc.so \
MALLOC_CONF=abort:true \
java -jar jellycord-app.jar
```

Validation checklist before production rollout:

1. Run full integration and load tests under the preload.
2. Verify gateway connect/login flows and attachment uploads.
3. Verify startup, shutdown, and retry behavior.
4. Check CPU/memory overhead and tail-latency impact.
5. Keep a one-flag rollback path by removing `LD_PRELOAD`.

If you do not run JNI/native code that processes untrusted memory, prioritize JVM-level controls (input validation, dependency hygiene, least privilege, observability) over allocator replacement.
