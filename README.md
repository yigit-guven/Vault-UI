# Create: Vault UI

A powerful and visually polished NeoForge addon for the **Create mod** that revolutionizes how you interact with **Item Vaults**. No more digging through dozens of chests or scrolling endlessly; Vault UI brings a professional, consolidated, and highly readable interface to your high-capacity storage.

---

## ⭐ Key Features

### 📊 Vault Fullness Indicator
Stay on top of your storage with the new **Dynamic Fullness Bar**. 
- **Smart Color Gradient**: The bar transitions from **Green** to **Orange** to **Red**, giving you an immediate visual cue when your storage is reaching its limit.
- **Intelligent Bottleneck Detection**: Our "Smart Simulation" logic accounts for both raw item counts and slot occupancy. The bar correctly hits 100% when no more items can be added, even if the total count is low due to unstackable items.

### 🔍 Deep Storage Analytics
Hover over the fullness bar to see exactly what's happening inside your vault. The detailed breakdown shows:
- **Total Fullness %**: An honest percentage that only hits 100% when the vault is truly full.
- **Items Breakdown**: Total items vs. theoretical capacity.
- **Slot Usage**: Occupied slots vs. total available slots.
- **Color-Coded Stats**: Every number in the breakdown shares the bar's color gradient, so you can instantly spot the bottleneck.

### 📦 Consolidated Item Management
Tired of seeing 100 different stacks of Cobblestone? Vault UI automatically **aggregates** all identical items into a single slot. You see exactly what you have, and exactly how much, instantly.

### 🔢 High-Density Readability
Vaults are meant for massive amounts of items, and our UI is built to handle that scale:
- **Dynamic Count Formatting**: Totals are automatically shortened (e.g., `1,250` → **1.2k**, `1,000,000` → **1.0M**) to prevent text overlap.
- **Scaled Typography**: Item counts are rendered at 80% size, ensuring that even multi-million totals stay neatly within their slots.

### 🎨 Authentic Vanilla Experience
We believe mods should feel like a part of the game:
- **Vanilla Light (Default)**: A pixel-perfect recreation of the classic Minecraft chest UI, featuring sharp corners and the iconic 2-pixel bezel.
- **Modern Dark**: A sleek dark mode for those using dark-themed resource packs or who simply prefer a modern aesthetic.
- *Toggle between them effortlessly in the `vault_ui-common.toml` config.*

### 📑 Infinite Scaling with Paging
Integrated navigation buttons and a clear page indicator allow you to manage thousands of unique item types without cluttering your screen.

### 🛠️ Seamless Create Integration
Vault UI is designed specifically for **Create**:
- **Smart Interaction**: Right-clicking opens the UI, but if you're holding a Vault block, the mod stays out of your way so you can still expand your 9x9 or continuous vault structures normally.

---

## 🛠 Requirements & Compatibility

To use this mod, you **must** have the following installed:
- **NeoForge**: [Download](https://neoforged.net/)
- **Minecraft**: 1.21.1
- **Create Mod**: [CurseForge](https://www.curseforge.com/minecraft/mc-mods/create) / [Modrinth](https://modrinth.com/mod/create)

---

## 🔧 Configuration

Tailor the mod to your preference in `.minecraft/config/vault_ui-common.toml`:

```toml
# Whether to use the dark theme for the Vault UI. If false, uses a vanilla-like light theme.
darkMode = false
```

---

## 🔗 Links & Credits

- **GitHub Repository**: [yigit-guven/Vault-UI](https://github.com/yigit-guven/Vault-UI)
- **Issue Tracker**: [Report Bugs Here](https://github.com/yigit-guven/Vault-UI/issues)
- **Developer**: [Yigit Guven](https://github.com/yigit-guven)

*Special thanks to the Industrialist mod for inspiring the consolidated viewing logic.*

---

## 📜 License

Distributed under the **LGPL v3** License. See `LICENSE` for more information.