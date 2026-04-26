## [1.0.0-beta1] - 2026-04-26

### Added
- **Custom Vault UI**: A dedicated, consolidated viewing interface for Create Item Vaults.
- **Item Aggregation**: Items of the same type are now merged into a single view slot, allowing you to see your total stock at a glance.
- **Paging System**: Support for large vaults with a "PAGE X/Y" navigation system and custom buttons.
- **Vanilla Light Theme**: A default light theme that perfectly matches the vanilla Minecraft chest UI (sharp corners and 2-pixel bezel).
- **Dark Mode Option**: A sleek, modern dark theme accessible via the configuration file.
- **Dynamic Count Formatting**: Item counts greater than 999 are formatted as `1k`, `1.2k`, `100k`, `1M`, etc., to prevent text overlap.
- **Scaled Count Text**: Item counts are rendered at 80% size for improved readability in high-density storage.
- **Mod Logo**: Integrated a custom logo for the in-game mod list.
- **Configuration**: Added `vault_ui-common.toml` for theme toggling.

### Changed
- **Interaction Logic**: Right-clicking a Vault will open the new UI, but right-clicking with another Vault in hand will still allow you to expand the structure (9x9 or continuous placement preserved).
- **Consolidated Text**: Removed redundant "STOCK TERMINAL" text for a cleaner look.

### Technical
- Ported and optimized the viewing system from the Industrialist mod.
- Fixed NeoForge 1.21.1 compatibility for screen registration and networking.
- Updated dependencies to properly resolve Create 1.21.1 via CurseMaven.
- Licensed under **LGPL v3**.