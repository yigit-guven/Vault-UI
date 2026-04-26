## [1.0.0-beta3.4] - 2026-04-26

### Fixed
- **Sorting Desync**: Fixed "wrong item pickup" bug by synchronizing sorting preferences between client and server.
- **UI Performance**: Throttled vault data refreshes to 1-second intervals to prevent slots from "jumping" while auto-farms are filling the vault.

## [1.0.0-beta3.3] - 2026-04-26

### Fixed
- **Server Crash**: Fixed `IllegalStateException` when opening the vault on a dedicated server caused by client-only config access.
- **Config Handling**: Migrated settings to a common configuration to ensure server-side compatibility.
- **Mod Compatibility**: Improved vault interaction logic to prevent other mods from intercepting right-clicks.
- **Client Synchronization**: Added client-side interaction handling to prevent desyncs during vault access.