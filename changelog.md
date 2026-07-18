## [1.0.0-beta10] - 2026-07-18

### Added
- **Server Configuration**: Added server-side configuration options (`preventItemRetrieval`, `preventItemInsertion`) that only operators can modify, which prevent players from retrieving items from or inserting items into the vault interface directly.

### Fixed
- **Log Spam**: Removed debug log messages that were spamming the server console on every block interaction.