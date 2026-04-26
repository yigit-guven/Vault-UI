## [1.0.0-beta3] - 2026-04-26

### Added
- **Integrated Sorting System**: A new button in the top right corner allows you to organize your items instantly.
- **Multiple Sort Modes**: Cycle through three distinct organization styles:
    - **Most Items**: Keep your bulk resources at the top.
    - **A-Z (Mod ID)**: Group items by mod (e.g., all Create items together).
    - **A-Z**: Standard alphabetical sorting by display name.
- **Independent Player Preferences**: Sorting and theme choices are now saved to a **Client Config** (`vault_ui-client.toml`).
- **Persistent Selection**: The mod now remembers your preferred sort mode across different vaults and game sessions.

### Fixed
- **UI Layout Optimization**: Adjusted the top-bar button positioning and size to prevent overlap with the vault title.
- **Instant Responsiveness**: Sorting is processed locally on the client, ensuring zero lag when switching modes.