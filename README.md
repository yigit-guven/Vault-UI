<div align="center">
  <a href="https://github.com/yigit-guven/Vault-UI">
    <img src="https://raw.githubusercontent.com/yigit-guven/Vault-UI/363106241f88599aadba3ed16e624a2dd54202da/src/main/resources/logo.png" width="128">
  </a>
  <h1>Create: Vault UI</h1>
</div>

A clean and simple interface for the **Create mod**'s **Item Vaults**. It aggregates all your items into one easy-to-read screen, making it much easier to manage large storage systems.

---

## ⭐ Features

### 📊 Fullness Indicator
A simple bar that shows how much space is left in your vault.
- **Color Gradient**: Changes from **Green** to **Orange** and **Red** as the vault fills up.
- **Smart Tracking**: The bar knows when you've run out of slots, even if you have a low item count (important for unstackable items).
- **Detailed Tooltip**: Hover over the bar to see exactly how many items and slots are being used.

### 📶 Item Sorting
Organize your items however you prefer.
- **Three Modes**: Sort by **Most Items**, **Alphabetical**, or **Alphabetical by Mod ID**.
- **Personalized**: Every player can have their own sorting method, and the mod will remember it for the next time you open a vault.
- **Instant**: Sorting happens instantly on your screen without any server lag.

### 📦 Consolidated View
Instead of seeing dozens of separate stacks of the same item, Vault UI merges them all together.
- **One Slot per Item**: All identical items are grouped into a single slot.
- **Readable Numbers**: Large counts are automatically shortened (e.g., `1.2k` or `1.5M`) so they don't overlap.

### 🎨 Themes
- **Vanilla Light**: A pixel-perfect look that matches the standard Minecraft chest UI.
- **Dark Mode**: A clean dark theme for those who prefer a modern look.
- **Independent**: You can switch your theme without affecting other players on the server.

### 🧩 Compatibility
- **Broad Support**: Designed to work with most Create-styled vault mods. If it uses the standard Item Vault logic and `ItemHandler` capability, Vault UI should be able to handle it!
- [**Create: Vibrant Vaults**](https://www.curseforge.com/minecraft/mc-mods/create-vibrant-vaults): Fully supported, including variants and shipping containers with dynamic titles.

---

## 🛠 Installation & Usage

**Requirements:**
- **Create**

**How to Use:**
1. **Open UI**: **Right-Click** any part of an Item Vault structure.
2. **Expand Vaults**: Hold an **Item Vault** block while clicking to expand your vault normally without opening the UI.
3. **Paging**: Use the arrow buttons on the right to flip through your items.

---

## 🔧 Configuration
Preferences are saved per-player in `.minecraft/config/vault_ui-client.toml`:

```toml
# Use the dark theme.
darkMode = false

# Default sorting method (COUNT, NAME_ID, or NAME).
sortMode = "COUNT"
```

## 🔗 Links
- **Modrinth**: [vault-ui](https://modrinth.com/mod/vault-ui)
- **CurseForge**: [vault-ui](https://www.curseforge.com/minecraft/mc-mods/vault-ui)
- **GitHub Repository**: [yigit-guven/Vault-UI](https://github.com/yigit-guven/Vault-UI)
- **Developer**: [Yigit Guven](https://github.com/yigit-guven)

---

*Special thanks to the Create Team for their mod.*

Distributed under the **LGPL v3** License.