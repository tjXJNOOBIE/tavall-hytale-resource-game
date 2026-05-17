# About - Asset Rules

Rules for game assets, which asset type to use when, where each asset type belongs, and which assets are shareable between platforms.

The goal of this document is to keep the game asset pi([learn.microsoft.com](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/jsonuireference/examples/jsonuilist?view=minecraft-bedrock-stable&utm_source=chatgpt.com))m to receive the correct exported format.

The preferred rule is:

```txt
Design once.
Author once where possible.
Compile/export per platform.
Never manually fork the same asset unless the platform truly requires it.
```

A shared asset should represent the same gameplay/cosmetic identity everywhere, even when the exported implementation differs by platform.

Example:

```txt
cosmetic:royal_copper_saber
  -> Minecraft Java resource pack item model
  -> Minecraft Bedrock resource/behavior pack item
  -> Hytale asset pack item/model
  -> Roblox mesh/image asset
  -> Android/Mobile/PC runtime asset
```

## Core Rule

PNGs are the shared visual payload for most 2D/UI assets.

PNGs should be used for visual elements, not behavior, layout, dynamic text, pricing, inventory data, localization, or platform-specific logic.

Use PNGs as reusable pieces that platform renderers can arrange, scale, tint, animate, or combine.

Do not treat PNGs as full UI screens unless the screen is static, decorative, non-interactive, and contains no text that will ever change.

## Use PNGs for:

* Icons
* Buttons
* Panels
* Borders
* Inventory slots
* Tooltips
* Rarity frames
* Progress bars
* Quest badges
* Currency icons
* Item thumbnails
* Background ornaments
* Small animated spritesheets
* HUD elements
* Faction badges
* Kingdom banners
* Gem icons
* Status icons
* Buff/debuff icons
* Ability icons
* Companion icons
* Building icons
* Troop icons
* Shop card backgrounds
* Modal backgrounds
* Toast/notification frames
* Scrollbar skins
* Tab backgrounds
* Decorative separators
* Loading screen ornaments
* Cursor or pointer graphics where supported

## Do not use PNGs for:

* Dynamic text
* Full screens with text baked in
* Layout rules
* Input logic
* Scrolling lists
* Responsive sizing
* Hotbar/inventory data
* Shop item names/prices
* Localization
* Player names
* Item stats
* Quest descriptions
* Currency values
* Countdown timers
* Cooldown numbers
* Server names
* Region names
* Any copy that may change by platform, language, balance patch, promotion, or event

## Prefer Component-Based UI

All UI should be composed from reusable components.

A component is a logical UI element made from images, text, layout rules, state rules, and data bindings.

Examples:

* Button
* Inventory slot
* Item card
* Shop card
* Tooltip
* Modal
* Quest tracker
* Companion card
* Troop card
* Building upgrade card
* Currency counter
* Resource counter
* Navigation tab
* Reward popup
* Battle status bar

A component should define what it is, what visual assets it uses, what states it supports, and what data slots it expects.

Example component data slots:

```txt
item_icon
item_name
item_count
rarity_frame
price_value
currency_icon
owned_state
equipped_state
locked_state
```

Images should provide the frame, icon, and visual identity.

Text and data should be rendered by the platform-specific UI system.

## Never Bake Dynamic Text Into Images

Do not bake the following into UI PNGs:

* Buy
* Equip
* Sell
* Claim
* Upgrade
* Train
* Heal
* Locked
* Owned
* New
* Sale
* Limited
* Item names
* Item prices
* Stats
* Descriptions
* Localization strings

Instead, use an empty visual background and place dynamic text on top.

Correct:

```txt
button_primary.png + dynamic label text
```

Incorrect:

```txt
buy_now_button.png
```

## Use 9-Slice Assets For Scalable UI

Resizable UI should use 9-slice assets where possible.

Use 9-slice assets for:

* Panels
* Buttons
* Tooltips
* Modals
* Shop cards
* Item cards
* Quest boxes
* Confirmation dialogs
* Notification boxes
* Tabs
* Input fields

Do not stretch normal PNGs directly if the corners or borders become distorted.

Recommended source structure:

```json
{
  "id": "panel_dark",
  "texture": "ui_core_atlas.png",
  "region": [0, 0, 64, 64],
  "slice": {
    "left": 8,
    "right": 8,
    "top": 8,
    "bottom": 8
  }
}
```

## Use Texture Atlases

Prefer texture atlases over many tiny loose PNGs.

A texture atlas combines related UI sprites into one image with metadata describing each sprite region.

Use atlases for:

* Core UI panels
* Buttons
* Icons
* Rarity frames
* Inventory slots
* HUD elements
* Gem icons
* Status effects

Recommended structure:

```txt
ui_core_atlas.png
ui_core_atlas.json
```

Example atlas metadata:

```json
{
  "atlas": "ui_core_atlas.png",
  "sprites": {
    "button_primary_left": { "x": 0, "y": 0, "w": 8, "h": 24 },
    "button_primary_center": { "x": 8, "y": 0, "w": 8, "h": 24 },
    "button_primary_right": { "x": 16, "y": 0, "w": 8, "h": 24 },
    "slot_empty": { "x": 0, "y": 32, "w": 18, "h": 18 },
    "rarity_rare_frame": { "x": 32, "y": 32, "w": 24, "h": 24 },
    "currency_crowns": { "x": 64, "y": 32, "w": 16, "h": 16 }
  }
}
```

Platforms may split atlases into loose PNGs if required, but source assets should stay organized as atlases.

## UI States Must Be Designed Explicitly

Every interactive UI asset must define supported states.

Button states:

* Normal
* Hover
* Pressed
* Disabled
* Selected
* Alert
* Cooldown

Item/card states:

* Normal
* Hovered
* Selected
* Owned
* Not owned
* Equipped
* Locked
* Affordable
* Too expensive
* New
* Sale
* Limited
* Expired

Inventory slot states:

* Empty
* Filled
* Hovered
* Selected
* Equipped
* Locked
* Invalid
* Drag source
* Drag target

Quest states:

* Available
* In progress
* Complete
* Claimable
* Locked
* Failed

Companion states:

* Idle
* Training
* Injured
* Healing
* Ready
* Level up
* Evolving

Any state that can appear in gameplay should have an intentional visual design.

## Naming Rules

Asset names must be boring, explicit, searchable, and stable.

Use lowercase snake_case.

Use clear prefixes:

```txt
ui_
icon_
panel_
button_
slot_
frame_
rarity_
gem_
item_
weapon_
cosmetic_
companion_
building_
troop_
particle_
sound_
model_
texture_
```

Good:

```txt
ui_button_primary_normal.png
ui_button_primary_hover.png
ui_panel_dark_9slice.png
ui_slot_inventory_selected.png
icon_currency_crowns.png
icon_gem_ruby.png
rarity_frame_legendary.png
companion_wolf_ruby_adult.png
weapon_saber_royal_copper.png
```

Bad:

```txt
button.png
button_final.png
new_button.png
shopthing.png
cool_icon.png
final_final_real.png
```

Never use temporary names in committed assets.

## Source Files vs Exported Files

Source files are editable originals.

Exported files are generated platform-specific outputs.

Do not edit exported files manually unless debugging a platform issue.

If a manual export fix becomes permanent, move the fix back into the source asset, manifest, exporter, or platform adapter.

Recommended structure:

```txt
/assets
  /source
  /ui
  /models
  /textures
  /audio
  /manifests
  /exports
    /minecraft_java
    /minecraft_bedrock
    /hytale
    /roblox
    /android
    /mobile
    /pc
    /web
```

## Asset Manifest Rule

Every shared asset should have a manifest.

The manifest defines identity, category, source files, platform exports, visual rules, gameplay tags, and validation budgets.

Example:

```json
{
  "id": "royal_copper_saber",
  "displayName": "Royal Copper Saber",
  "category": "weapon_skin",
  "rarity": "rare",
  "styleFamily": "royal_copper",
  "sourceModel": "source/royal_copper_saber.bbmodel",
  "sourceInterchange": "source/royal_copper_saber.glb",
  "textures": {
    "base": "textures/royal_copper_saber_base.png",
    "icon": "textures/royal_copper_saber_icon.png",
    "emissive": "textures/royal_copper_saber_emissive.png"
  },
  "platforms": {
    "minecraft_java": { "enabled": true },
    "minecraft_bedrock": { "enabled": true },
    "hytale": { "enabled": true },
    "roblox": { "enabled": true },
    "android": { "enabled": true },
    "mobile": { "enabled": true },
    "pc": { "enabled": true }
  }
}
```

The manifest is the source of truth for asset identity.

Platform-specific files should never redefine the asset identity differently.

## Shared Asset Identity Rule

The backend should track shared ownership by canonical asset ID.

Correct:

```txt
player owns cosmetic:royal_copper_saber
```

Incorrect:

```txt
player owns roblox_mesh_123456
player owns minecraft_model_royal_saber_v3
```

Platform adapters resolve canonical IDs to platform-specific files or asset IDs.

Example:

```txt
cosmetic:royal_copper_saber
  -> minecraft_java: custom_model_data/resource_pack_model
  -> minecraft_bedrock: behavior/resource pack item ID
  -> hytale: asset pack ID
  -> roblox: uploaded asset/package ID
  -> pc/mobile: local or CDN runtime asset ID
```

## Asset Categories

### UI Assets

2D interface visuals used by menus, HUDs, shops, inventories, tooltips, and overlays.

Preferred formats:

* Source: Aseprite, PSD, Krita, Affinity, Figma export, raw PNG
* Runtime: PNG
* Metadata: JSON

Use for:

* Buttons
* Panels
* Slots
* Icons
* Frames
* HUD elements
* Tooltips

### Item Icons

2D representations of inventory items, shop items, abilities, resources, and currencies.

Preferred format:

* PNG

Rules:

* Must be readable at small sizes.
* Must not rely on tiny details.
* Must not include item count text.
* Must not include price text.
* Should have transparent background unless the icon intentionally includes a frame.

### Item Models

3D models or platform-native item model definitions for held, placed, displayed, or equipped items.

Preferred source formats:

* Blockbench `.bbmodel`
* glTF/GLB interchange where useful
* Platform-specific exports generated from source

Use for:

* Weapons
* Tools
* Placeable objects
* Cosmetics
* Back items
* Hats
* Companions
* Mount equipment

### Textures

2D images applied to models, particles, UI, or world objects.

Preferred format:

* PNG

Rules:

* Keep texture sizes reasonable.
* Prefer pixel-painted or stylized textures.
* Avoid noisy photorealistic textures unless a platform-specific art direction requires it.
* Keep material palettes reusable.

### Audio Assets

Sounds, music, ambience, and UI feedback.

Preferred source formats:

* WAV for editable/source audio
* OGG where supported for runtime/export
* Platform-specific conversion if required

Use for:

* UI clicks
* Error sounds
* Item pickup
* Currency gain
* Companion actions
* Building placement
* Combat feedback
* Ambient loops

### Animation Assets

Animation metadata, spritesheets, model animations, UI motion rules, and platform-specific animation definitions.

Preferred source formats:

* Spritesheet PNG + JSON metadata for 2D animation
* Blockbench animation data for model animation
* Platform-specific animation exports for runtime

Use for:

* Animated icons
* Loading indicators
* Ability cooldowns
* Companion idle/move states
* Weapon effects
* Shop highlights
* Reward reveals

### Particle/VFX Assets

Small visual effect assets used for combat, UI feedback, magic, resources, gems, and progression.

Preferred source formats:

* PNG spritesheets
* Particle metadata JSON
* Platform-specific particle definitions

Rules:

* Do not assume all platforms support the same particle behavior.
* Keep VFX readable and performance-safe.
* Provide fallback static icons or simplified effects where needed.

## Rarity Rules

Rarity visuals must be consistent across platforms.

Recommended rarity IDs:

```txt
common
uncommon
rare
epic
legendary
mythic
event_limited
founder
```

Rarity should affect:

* Frame color
* Glow intensity
* Shop card treatment
* Tooltip header style
* Reward reveal style
* Optional particle/VFX treatment

Rarity must not be communicated by color alone.

Use at least two visual indicators, such as:

* Color
* Frame shape
* Icon badge
* Label text
* Glow pattern

## Gem Visual Identity Rules

Gem visuals are shared across gameplay, UI, item art, companion art, and shop presentation.

Recommended gem identities:

```txt
pearl      -> protection, healing, general wounds
amethyst   -> magic, poison, corruption
peridot    -> economy, resources, growth
ruby       -> combat, aggression, damage
sapphire   -> intelligence, scouting, precision
```

Gem identity should affect:

* Icon design
* UI frame accents
* Item variants
* Companion variants
* Ability icons
* Particle effects
* Crafting visuals

## Platform Sharing Levels

Assets should be classified by how shareable they are.

### Share Level 1 - Fully Shared

Same source and same runtime file can be used across many targets.

Examples:

* PNG icons
* PNG UI pieces
* PNG item thumbnails
* Common metadata JSON
* Design tokens
* Localization keys

### Share Level 2 - Shared Source, Different Export

Same source asset, platform-specific exported files.

Examples:

* Blockbench model exported to different formats
* UI component schema compiled into different platform UI definitions
* Texture atlas split differently per platform
* Spritesheet metadata converted per platform

### Share Level 3 - Shared Identity, Platform-Specific Implementation

Same canonical asset ID and visual direction, but the actual implementation differs.

Examples:

* Minecraft Java custom item model vs Roblox MeshPart
* Minecraft inventory GUI vs Roblox ScreenGui shop
* Hytale `.ui` page vs PC native UI layout
* Companion pet entity with different rig/runtime logic

### Share Level 4 - Platform-Only Asset

Asset exists only for one platform due to platform constraints.

Examples:

* A Minecraft legacy fallback model
* A Roblox-only accessory rig fix
* A Hytale-only `.ui` workaround
* A mobile-only touch control texture

Platform-only assets must still reference a canonical asset ID when connected to shared gameplay or ownership.

# Platforms

## Minecraft Java

Minecraft Java assets are delivered primarily through resource packs and server/plugin-driven behavior.

Use Minecraft Java resource packs for:

* Item textures
* Block textures
* Entity textures where supported
* GUI textures
* Font glyphs
* Language files
* Sounds
* Item/block model JSON
* Custom item visuals

Use server/plugin logic for:

* Inventory interactions
* Menu routing
* Button clicks
* Shop logic
* Item ownership
* Dynamic text
* Player-specific data
* Permissions
* Progression
* Currency values

### Minecraft Java UI Rules

PNGs may be used for:

* Inventory GUI backgrounds
* Slot overlays
* Icons
* Custom font glyph images
* HUD-style decorative elements
* Resource pack GUI textures

Do not use PNGs for:

* Player-specific text
* Item lore values
* Prices
* Live counters
* Menu logic

Minecraft Java UI should be treated as a constrained target.

Use platform adapters for:

* Inventory GUI layouts
* Custom font overlays
* Titles/subtitles/actionbar
* Bossbar overlays
* Resource pack item icons
* Clickable inventory slots

### Minecraft Java Compatibility Rule

Treat legacy Minecraft Java and modern Minecraft Java as separate export targets when needed.

Recommended targets:

```txt
minecraft_java_legacy
minecraft_java_modern
```

Legacy versions may need fallback assets or simplified UI behavior.

Modern versions may support cleaner resource pack/model workflows.

Do not assume one Java export works perfectly across all versions.

## Minecraft Bedrock

Minecraft Bedrock assets are delivered through resource packs and behavior packs.

Use Bedrock resource packs for:

* Textures
* UI images
* Sounds
* Models
* Client-facing visual definitions

Use Bedrock behavior packs for:

* Custom item behavior
* Entity behavior
* Block behavior
* Gameplay-facing definitions

Use shared backend/control-plane logic for:

* Ownership
* Economy
* Cross-platform account linkage
* Inventory authority
* Unlock state
* Entitlement resolution

### Minecraft Bedrock UI Rules

PNGs may be used for:

* UI controls
* Icons
* Frames
* HUD graphics
* Panel backgrounds
* Texture references inside UI definitions

Use JSON UI definitions or the current supported Bedrock UI system for layout and screen composition.

Do not bake dynamic text into PNGs.

Do not make Bedrock UI the source of truth.

Bedrock should be an export target generated from shared UI components and manifests.

### Minecraft Bedrock Compatibility Rule

Bedrock UI and add-on systems can change over time.

Platform adapters should be versioned.

Recommended targets:

```txt
minecraft_bedrock_stable
minecraft_bedrock_preview
```

Only stable targets should be used for production content unless explicitly testing preview behavior.

## Hytale

Hytale assets should be treated as a high-fit target for the game's blocky/stylized art direction.

Use Hytale asset packs for:

* UI images
* Custom UI templates
* Models
* Textures
* Sounds
* Animations
* Icons
* HUD overlays

Use Hytale `.ui` files or equivalent UI templates for:

* Shop screens
* Quest dialogs
* Server menus
* Admin panels
* Companion screens
* Kingdom screens
* HUD overlays
* Custom interaction pages

Use Java/server-side plugin logic for:

* Opening screens
* Handling interactions
* Sending player-specific data
* Shop purchases
* Inventory authority
* Companion state
* Kingdom state

### Hytale UI Rules

PNGs may be used for:

* Panels
* Buttons
* Icons
* Backgrounds
* HUD elements
* Decorative frames
* Spritesheets

`.ui` files should define:

* Layout
* Components
* Reusable templates
* Bindable fields
* Interaction hooks

Do not bake dynamic text into PNGs.

Do not make Hytale `.ui` files the only source of truth if the UI needs to exist on other platforms.

Generate Hytale UI files from shared component schemas where possible.

## Roblox

Roblox assets should be treated as a platform-specific runtime implementation of shared game/cosmetic identity.

Use Roblox assets for:

* ImageLabels
* ImageButtons
* ScreenGui UI
* MeshParts
* Accessories
* Decals/textures
* Sounds
* Animations
* Particle effects

Use PNGs for:

* UI icons
* Button images
* Panel backgrounds
* Item thumbnails
* Rarity frames
* Currency icons
* Shop card art
* HUD art
* Decals/textures where appropriate

Use Luau/client UI code for:

* Screen layout
* Input handling
* Hover/press states
* Touch/controller/mouse behavior
* Dynamic text
* Scrolling lists
* UI animation

Use backend/server logic for:

* Ownership
* Purchases
* Cross-platform entitlement resolution
* Inventory authority
* Player progression
* Anti-abuse checks

### Roblox UI Rules

Do not bake text into UI images.

Use Roblox UI objects for dynamic text and layout.

Use shared IDs in generated Luau constants.

Example:

```lua
return {
    CURRENCY_CROWNS = "icon_currency_crowns",
    BUTTON_PRIMARY = "ui_button_primary",
    RARITY_LEGENDARY = "rarity_frame_legendary"
}
```

Roblox may require uploaded asset IDs for runtime use.

The canonical asset ID must remain separate from the Roblox uploaded asset ID.

Correct:

```txt
canonical: icon_currency_crowns
roblox: rbxassetid://...
```

Incorrect:

```txt
canonical: rbxassetid://...
```

## Android/Mobile/PC

Android, mobile, and PC clients are controlled targets and should use the cleanest implementation of shared assets.

Use these targets as the reference implementation for:

* Proper responsive layouts
* Touch controls
* Mouse/keyboard controls
* Controller support
* High-quality scaling
* Texture atlases
* 9-slice panels
* Dynamic localization
* Accessibility options
* Native UI animation

Use PNGs for:

* Icons
* UI sprites
* Atlases
* Backgrounds
* Item thumbnails
* HUD elements
* Particles
* Spritesheets

Use layout/schema files for:

* Screen layouts
* Components
* Input variants
* Responsive behavior
* State rules
* Animation rules

Recommended runtime sources:

```txt
ui_core_atlas.png
ui_core_atlas.json
ui_tokens.json
ui_components.json
ui_screens.json
localization/*.json
```

### Mobile Rules

Mobile UI must account for:

* Touch targets
* Screen notches/safe areas
* Small screens
* Large tablets
* Orientation rules
* Fat-finger tolerance
* Gesture conflicts
* Low-memory devices
* Texture budget limits

Do not directly reuse desktop layouts on mobile.

Reuse components and art, not exact screen composition.

### PC Rules

PC UI must account for:

* Mouse hover states
* Keyboard shortcuts
* Controller support where applicable
* Resizable windows
* Ultrawide monitors
* High refresh UI feel
* Text readability
* Scalable HUD density

Do not assume PC users want mobile-sized buttons everywhere.

## Web/Storefront

Web storefront assets should reuse the same canonical art identity.

Use web exports for:

* Item previews
* Shop cards
* Account inventory
* Cosmetic galleries
* Patch notes
* Event pages
* Creator/admin dashboards

Preferred formats:

* PNG for icons and pixel/stylized UI
* WebP/AVIF where appropriate for web-only optimized delivery
* SVG only for web-only vector UI or generated simple icons
* JSON metadata for dynamic rendering

Do not make web-only art the canonical asset unless the asset exists only on the web.

# UI Source Structure

Recommended source structure:

```txt
/ui
  /tokens
    color.tokens.json
    spacing.tokens.json
    font.tokens.json
    rarity.tokens.json
    platform.tokens.json

  /sprites
    /source
      ui_core.aseprite
      icons.aseprite
      rarity_frames.aseprite
    /export
      ui_core_atlas.png
      ui_core_atlas.json

  /components
    button.primary.ui.json
    panel.dark.ui.json
    item-card.ui.json
    shop-card.ui.json
    inventory-slot.ui.json
    tooltip.item.ui.json

  /screens
    inventory.screen.json
    shop.screen.json
    companion.screen.json
    kingdom.screen.json
    battle-hud.screen.json

  /platforms
    /minecraft_java
    /minecraft_bedrock
    /hytale
    /roblox
    /android
    /mobile
    /pc
    /web
```

# Design Tokens

Design tokens are shared constants for visual style.

Use tokens for:

* Colors
* Rarity colors
* Gem colors
* Font sizes
* Spacing
* Border sizes
* Corner sizes
* Animation durations
* UI scale rules
* Platform-specific density rules

Example:

```json
{
  "colors": {
    "panel_dark": "#18151f",
    "gold_trim": "#d7a84f",
    "danger": "#c0392b",
    "success": "#3fa66b"
  },
  "rarity": {
    "common": "#9ca3af",
    "uncommon": "#4ade80",
    "rare": "#60a5fa",
    "epic": "#c084fc",
    "legendary": "#fbbf24",
    "mythic": "#fb7185"
  }
}
```

# Validation Rules

Assets must pass validation before they are exported or shipped.

Validate:

* File naming
* Folder location
* Manifest presence
* Canonical ID uniqueness
* Missing source files
* Missing exported files
* Texture dimensions
* Power-of-two requirements where needed
* Atlas metadata correctness
* 9-slice metadata correctness
* Unsupported file formats
* Oversized textures
* Missing UI states
* Missing localization keys
* Missing platform mappings
* Invalid rarity IDs
* Invalid gem IDs
* Broken references
* Duplicate asset IDs

Validation should fail loudly.

Do not silently ignore broken asset references.

# Export Rules

All exports should be generated from source assets and manifests.

Exporters may:

* Split atlases
* Resize textures
* Convert metadata
* Generate platform constants
* Generate platform import scripts
* Generate pack structures
* Generate UI layouts
* Generate fallback assets

Exporters should not:

* Rename canonical IDs randomly
* Bake dynamic text into images
* Modify source files
* Hide validation errors
* Create platform-only forks without manifest entries

# Fallback Rules

Every shared asset should define what happens if a target platform cannot support the full version.

Fallback options:

* Static image instead of animation
* Simpler model instead of complex model
* Basic icon instead of particle effect
* Standard button instead of custom animated button
* Simplified UI screen instead of full interactive layout
* Lower resolution texture
* Platform-only placeholder

Fallbacks must preserve:

* Canonical asset ID
* Display name
* Rarity
* Gameplay meaning
* Basic silhouette or icon identity

Fallbacks may change:

* Geometry complexity
* Animation quality
* Particle intensity
* Texture resolution
* Layout density
* Platform-specific behavior

# Performance Rules

Assets should be designed for multiplayer scale.

Avoid:

* Oversized textures
* Excessive particle counts
* Too many unique materials
* Too many loose tiny files
* Complex animated UI everywhere
* Massive uncompressed audio
* High-poly models for small cosmetics
* Huge spritesheets for tiny effects

Prefer:

* Texture atlases
* Reused materials
* Shared icon sets
* Small readable textures
* Simple silhouettes
* Platform-specific LOD/fallbacks
* Compressed runtime formats where appropriate

# Accessibility Rules

Do not rely on color alone.

Every important UI state should have at least two indicators.

Examples:

* Color + icon
* Color + text label
* Frame shape + glow
* Badge + text
* Pattern + color

UI must remain readable at small sizes.

Text must remain dynamic so platforms can scale it, localize it, or replace it for accessibility settings.

# Localization Rules

All user-facing text must use localization keys.

Do not bake localized text into images.

Recommended key style:

```txt
ui.shop.buy
ui.shop.equip
ui.inventory.locked
item.royal_copper_saber.name
item.royal_copper_saber.description
currency.crowns.name
rarity.legendary.name
```

Images may contain symbols, but not language-specific text unless the asset is explicitly platform-only or event-only and approved.

# Ownership and Entitlement Rules

Visual asset IDs are not ownership IDs.

Ownership should be tracked by canonical gameplay/cosmetic IDs.

Platform asset IDs are implementation details.

Correct:

```txt
cosmetic:royal_copper_saber
currency:crowns
companion:ruby_wolf
```

Incorrect:

```txt
roblox_asset_123456
minecraft_texture_saber_v2
hytale_ui_icon_88
```

# Review Checklist

Before shipping an asset, verify:

* The asset has a canonical ID.
* The asset has a manifest.
* The asset follows naming rules.
* The asset has a clear category.
* The asset has a rarity if applicable.
* The asset has gem/faction tags if applicable.
* The source file exists.
* Required platform exports exist.
* PNGs do not contain dynamic text.
* UI states are complete.
* Texture sizes are acceptable.
* Atlas metadata is valid.
* 9-slice metadata is valid.
* Localization keys exist.
* Fallbacks exist for constrained platforms.
* The asset is readable at gameplay size.
* The asset is not visually noisy.
* The asset does not break platform-specific restrictions.

# Final Rule

Assets should be shared by identity and source, not blindly shared by runtime file.

The pipeline should aim for:

```txt
One canonical asset identity.
One editable source where possible.
One shared visual language.
Multiple platform-specific exports.
No manual asset drift.
```