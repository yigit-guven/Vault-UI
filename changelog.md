## [1.0.0-beta7.1] - 2026-04-28

### Changed
- Count Formatting: Items exceeding 9.9k (10,000+) now stop showing decimals (e.g., '10k' instead of '10.1k') for cleaner slot rendering.
- Dynamic Count Scaling: Items with counts of 4 or more characters (excluding dots, e.g., '100k', '10.5k') now use a smaller font scale to ensure the text fits perfectly within the item slot.

### Fixed
- Accidental Item Drops: Prevented items from being dropped on the ground when the player's inventory is full. Items will now be safely returned to the vault instead.
- Closing Interaction: Holding an item on the cursor while closing the vault now attempts to return it to the vault or inventory before dropping.