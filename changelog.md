## [1.0.0-beta2] - 2026-04-26

### Added
- **Vault Fullness Indicator**: A new vertical bar that tracks how full your vault is in real-time.
- **Smart Color Gradient**: The indicator bar now transitions smoothly from **Green** (empty) to **Orange** (50%) and finally to **Red** (full).
- **Detailed Analytics Tooltip**: Hovering over the fullness bar now reveals a comprehensive breakdown:
    - **Items Count**: Raw item count vs. total theoretical capacity.
    - **Slot Occupancy**: Used slots vs. total available slots.
- **Dynamic Color Coding**: Tooltip values now share the same color gradient as the bar, highlighting exactly which metric is reaching its limit.
- **Mod Logo**: Integrated a custom mod logo for the in-game mod list.

### Fixed
- **Visual Symmetry**: Fixed corner leaks and refined the 2-pixel vanilla bezel for a perfect, professional look.

### Changed
- **Authentic Vanilla UI**: Reverted light theme corners to be 100% sharp to match the standard Minecraft chest UI.
- **Label Alignment**: Lowered "Item Vault" and "Inventory" labels by 2 pixels for a more balanced layout.
- **Consistent Backgrounds**: Unified the inventory background color in light mode to remove unnecessary grey dividers.