# VPS Deployment Strategy (GitHub Actions)

## Build artifact
- CI builds `build/libs/*.jar` on every push/PR.
- Release workflow publishes tagged versions (`v*.*.*`).

## Deployment model
1. Pull release artifact to VPS.
2. Validate config with startup fail-fast checks.
3. Run blue/green style switch:
   - Start new systemd unit instance on alternate port.
   - Execute health check.
   - Shift reverse proxy upstream.
   - Stop previous instance after connection drain.

## Rollback
- Keep previous release jar and environment file.
- If health or smoke checks fail, switch proxy back and restart previous instance.
- Preserve audit logs and deployment metadata for incident review.

## Runtime config checklist
- `DISCORD_BOT_TOKEN`
- `DISCORD_APP_ID`
- `MONGO_URI`
- `KEYDB_URI`
- `OAUTH_CLIENT_ID`
- `OAUTH_CLIENT_SECRET`
- `OAUTH_REDIRECT_URI`
- `LOG_LEVEL`
