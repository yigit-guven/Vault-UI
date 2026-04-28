<div align="center">
  <a href="https://github.com/yigit-guven/Vault-UI">
    <img src="https://raw.githubusercontent.com/yigit-guven/Vault-UI/363106241f88599aadba3ed16e624a2dd54202da/src/main/resources/logo.png" width="128">
  </a>
  <h1>Create: Vault UI</h1>
</div>

Vault UI provides a specialized interface for the [**Create mod**](https://www.curseforge.com/minecraft/mc-mods/create)'s Item Vaults. It aggregates all stored items into a single, organized view, simplifying inventory management for large-scale storage systems.

---

## Features

### Unified Storage View
Vault UI merges separate stacks of identical items into a single slot. This provides a clear overview of total quantities, with large counts automatically formatted (e.g., `1.2k`, `1.5M`) for readability.

### Capacity & Fullness Tracking
A real-time fullness indicator tracks vault storage capacity.
- **Smart Tracking**: Accounts for both item counts and slot availability, ensuring accurate status even with unstackable items.
- **Detailed Stats**: Hover over the fullness bar to view precise item counts and occupied slot data.

### Item Sorting
Customizable sorting modes allow you to organize items efficiently:
- **Most Items**: Priority based on quantity.
- **Alphabetical**: Standard A-Z sorting by display name.
- **Mod ID**: Group items by their parent mod.

### Modern Interaction Shortcuts
- **Shift + Double-Click**: Rapidly transfer all matching items from the vault into your inventory. This shortcut requires holding an item in the cursor to trigger, preventing accidental bulk transfers.

### Customization & Themes
- **Adaptive UI Colors**: Fully compatible with [**Create: Vibrant Vaults**](https://www.curseforge.com/minecraft/mc-mods/create-vibrant-vaults). The UI background dynamically tints to match the color of the vault block being accessed.
- **Dark Mode**: A clean, high-contrast dark theme for better visibility.
- **Persistent Preferences**: Sorting modes and theme selections are saved per-player.

## Compatibility
Vault UI is designed to work with the standard Create Item Vault system and its internal `ItemHandler` logic.
- [**Create: Vibrant Vaults**](https://www.curseforge.com/minecraft/mc-mods/create-vibrant-vaults): Fully supported with dynamic UI color matching and variant detection.
- **General Support**: Works with most mods that extend Create's vault system or utilize standard vault capabilities.

---

## Installation & Usage

**Requirements:**
- [**Create**](https://www.curseforge.com/minecraft/mc-mods/create)

**How to Use:**
1. **Open Interface**: Right-click any part of an Item Vault structure.
2. **Expand Vault**: Hold an Item Vault block while clicking to expand the structure normally without opening the UI.
3. **Navigation**: Use the paging controls on the right to navigate through large inventories.

---

## Configuration
Client-side preferences are stored in `config/vault_ui-client.toml`:

```toml
# Use the dark theme.
darkMode = false

# Default sorting method (COUNT, NAME_ID, or NAME).
sortMode = "COUNT"

# Enable colored UI backgrounds for Vibrant Vaults.
vibrantColors = true
```

---

## Links
- **Modrinth**: [vault-ui](https://modrinth.com/mod/vault-ui)
- **CurseForge**: [vault-ui](https://www.curseforge.com/minecraft/mc-mods/vault-ui)
- **GitHub**: [yigit-guven/Vault-UI](https://github.com/yigit-guven/Vault-UI)

Distributed under the **LGPL v3** License.
*Special thanks to the Create Team.*