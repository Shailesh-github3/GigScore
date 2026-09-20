---
name: GigScore Precision
colors:
  surface: '#f9f9ff'
  surface-dim: '#d3daea'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f0f3ff'
  surface-container: '#e7eefe'
  surface-container-high: '#e2e8f8'
  surface-container-highest: '#dce2f3'
  on-surface: '#151c27'
  on-surface-variant: '#3d4a42'
  inverse-surface: '#2a313d'
  inverse-on-surface: '#ebf1ff'
  outline: '#6d7a72'
  outline-variant: '#bccac0'
  surface-tint: '#006c4a'
  primary: '#006948'
  on-primary: '#ffffff'
  primary-container: '#00855d'
  on-primary-container: '#f5fff7'
  inverse-primary: '#68dba9'
  secondary: '#5e5e61'
  on-secondary: '#ffffff'
  secondary-container: '#e3e2e5'
  on-secondary-container: '#646467'
  tertiary: '#0051d5'
  on-tertiary: '#ffffff'
  tertiary-container: '#316bf3'
  on-tertiary-container: '#fefcff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#85f8c4'
  primary-fixed-dim: '#68dba9'
  on-primary-fixed: '#002114'
  on-primary-fixed-variant: '#005137'
  secondary-fixed: '#e3e2e5'
  secondary-fixed-dim: '#c7c6c9'
  on-secondary-fixed: '#1b1c1e'
  on-secondary-fixed-variant: '#464749'
  tertiary-fixed: '#dbe1ff'
  tertiary-fixed-dim: '#b4c5ff'
  on-tertiary-fixed: '#00174b'
  on-tertiary-fixed-variant: '#003ea8'
  background: '#f9f9ff'
  on-background: '#151c27'
  surface-variant: '#dce2f3'
typography:
  headline-xl:
    fontFamily: Geist
    fontSize: 36px
    fontWeight: '600'
    lineHeight: 44px
    letterSpacing: -0.03em
  headline-xl-mobile:
    fontFamily: Geist
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Geist
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Geist
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Geist
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Geist
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: -0.005em
  body-md:
    fontFamily: Geist
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0em
  body-sm:
    fontFamily: Geist
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0em
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.04em
  metric-display:
    fontFamily: Geist
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 38px
    letterSpacing: -0.03em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-desktop: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1.25rem
  space-xl: 2rem
---

## Brand & Style

This design system delivers a disciplined, high-density financial utility built specifically for independent professionals and gig economy operators. The aesthetic rejects decorative clutter and pseudo-luxury in favor of absolute technical clarity, systematic efficiency, and understated utility—heavily informed by the precision engineering of modern developer tooling and institutional financial infrastructure.

The interface prioritizes dense legibility, deliberate informational hierarchy, and calm, confident restraint. It treats earnings, platform metrics, and tax analytics with mathematical poise, transforming erratic gig streams into clean, actionable performance metrics.

### Visual Principles
- **Monochrome Foundation:** The visual weight is carried by neutral grays, stark typographic contrast, and hairline structural dividers.
- **Single-Frequency Accent:** Color is reserved strictly for affirmative status, actionable primary targets, or metric trajectory—never for mere decoration.
- **Tectonic Alignment:** Strict tabular grids, zero decorative curvature, and precise micro-alignments reflect computational integrity.
- **Hairline Tactility:** Boundaries are established through razor-thin borders and subtle contrast shifts rather than heavy elevations or blurred planes.

## Colors

The palette is engineered around an achromatic hierarchy punctuated by a precise emerald anchor that communicates financial growth and stability.

### Color Matrix
- **Primary (`#059669`):** Precision Emerald. Applied surgically to critical conversion vectors, positive financial velocity, and verified states.
- **Secondary (`#090A0C`):** Deep Obsidian. Primary text anchor, structural contrast accents, and focused interactive surfaces.
- **Tertiary (`#2563EB`):** Precision Cobalt. Utility accent reserved for platform integration indicators, system telemetry, and contextual informational states.
- **Neutral (`#6B7280`):** Slate Neutral. Middle gray calibrated for auxiliary metadata, secondary labels, and neutral status indicators.

### Background & Border Tiers
- **Canvas (`#FFFFFF` / `#F9FAFB`):** The foundational substrate. Core work areas sit on pure `#FFFFFF`, transitioning to subtle `#F9FAFB` for overall shell canvas framing.
- **Surface Containers (`#F3F4F6`):** Dedicated to recessed data blocks, metric tables, and inactive interaction zones.
- **Hairline Dividers (`#E5E7EB`):** Standard single-pixel boundary for all card edges, row borders, and split views.

## Typography

Typography relies on the disciplined balance between **Geist** for natural structural reading and **JetBrains Mono** for numerical values, financial figures, timestamps, and metadata tags.

### Typographic Rules
- **Monospaced Numerics:** All currencies, platform rating counts, transaction hashes, and percentage deltas must render using the label font (`JetBrains Mono`) or enable tabular numbers (`font-feature-settings: "tnum" 1`) in Geist to guarantee column alignment across fast-scrolling financial sheets.
- **Negative Letter Spacing:** Headings from 20px upward mandate tight, deliberate negative tracking (`-0.015em` to `-0.03em`) to mimic the crisp editorial polish of modern software documentation.
- **Uppercase Hierarchy:** Micro-labels, categorical headers, and currency descriptors should be set using `label-sm` with all-caps casing and relaxed tracking (`+0.04em`).

## Layout & Spacing

Layouts adhere to an inflexible 4px baseline rhythm optimized for maximum information density without sacrificing spatial clarity.

### Grid Architecture
- **Desktop (>= 1024px):** 12-column fluid grid, locked to a max width of `1440px`. Gutters are fixed to `gutter-desktop` (24px) with canvas margins at `margin-desktop` (32px).
- **Tablet (768px - 1023px):** 8-column layout with 16px gutters and 24px margins. Metrics panels shift from 4-across to 2x2 matrix groupings.
- **Mobile (< 768px):** 4-column responsive grid with 12px gutters and 16px side margins. Multi-column metric summaries stack vertically.

### Density Tiers
- **Tight (Financial Tables, Filter Strips):** Gap tokens default to `space-xs` (4px) and `space-sm` (8px).
- **Standard (Card Content, Forms):** Padding within components uses `space-md` (12px) or `space-lg` (20px).
- **Structural (Section Boundaries, Dashboard Panels):** Visual divisions between discrete feature groups leverage `space-xl` (32px).

## Elevation & Depth

Visual hierarchy is achieved through razor-sharp surface contrast and hairline structural borders rather than blur-heavy drop shadows. This preserves render performance and maintains a technical, high-precision aesthetic.

### Layering Philosophy
- **Base Substrate:** `#F9FAFB` serves as the canvas frame.
- **Active Plane (Level 0):** Primary cards, list rows, and metric widgets live on `#FFFFFF`, enclosed by a crisp, 1-pixel border (`#E5E7EB`).
- **Recessed Insets:** Chart canvases, form inputs, and nested sub-panels adopt `#F3F4F6` with no border or an inner hairline border (`#E5E7EB`).
- **Interactive Floating (Level 1):** Dropdowns, popovers, and contextual action menus sit elevated using a flat `#FFFFFF` background, a 1px border (`#E5E7EB`), and a calibrated micro-tinted shadow: `0 4px 12px -2px rgba(9, 10, 12, 0.05), 0 1px 3px -1px rgba(9, 10, 12, 0.08)`.
- **Modal Backdrops:** Pure obsidian overlay with high diffusion: `rgba(9, 10, 12, 0.4)` coupled with a low-intensity backdrop blur (`4px`).

## Shapes

The design system employs a restrained **Soft (`1`)** shape language to evoke industrial tool precision. Broad, playful curves are strictly avoided; interfaces feel milled and deliberate.

### Radius Assignments
- **Micro Radii (2px):** Tooltips, micro-indicators, inline status tags, and progress bar tracks.
- **Default Radii (4px):** Form inputs, buttons, chips, tabular rows, and inner metric blocks.
- **Container Radii (8px / `rounded-lg`):** Primary dashboard cards, modular panels, dialogs, and slide-overs.
- **Pill Radius (Full / 9999px):** Restricted solely to status pills (e.g., active platform connection tags).

## Components

### Buttons
- **Primary:** Solid `#090A0C` fill, `#FFFFFF` text, 4px corner radius, font `Geist` medium (14px). In hover state: `#1F2937`. Active: subtle scale down (`0.99`). 
- **Accent Primary (Actionable/Financial):** `#059669` fill, `#FFFFFF` text. Hover: `#047857`.
- **Secondary / Outline:** Pure `#FFFFFF` background, 1px border `#E5E7EB`, `#090A0C` text. Hover: `#F9FAFB` with border `#D1D5DB`.
- **Ghost:** No border, `#6B7280` text. Hover: `#090A0C` text on `#F3F4F6` background.

### Cards & Metric Panels
- **Structure:** `#FFFFFF` background, 1px `#E5E7EB` border, 8px radius, `space-lg` (20px) internal padding.
- **Metric Header:** 11px uppercase label font (`JetBrains Mono`) in `#6B7280`.
- **Metric Value:** 32px display font (`Geist`) in `#090A0C` with tight letter spacing.
- **Delta Indicator:** Monospaced 12px pill containing numeric change: positive trends feature `#ECFDF5` background with `#059669` text; negative trends feature `#FEF2F2` background with `#DC2626` text.

### Data Tables & List Rows
- **Headers:** `#F9FAFB` or transparent surface, 11px uppercase `JetBrains Mono` tracking `+0.04em`, `#6B7280`. Bottom border 1px `#E5E7EB`.
- **Rows:** 40px compact height, 1px bottom border `#E5E7EB`. Hover state activates `#F9FAFB` background fill across the entire row span.
- **Cell Alignment:** Text left-aligned; financial sums, hourly rates, and status timestamps strictly right-aligned using `JetBrains Mono`.

### Form Inputs & Controls
- **Inputs:** Height 36px, `#FFFFFF` background, 1px border `#E5E7EB`, 4px radius, horizontal padding 12px. Focus state transitions border to `#090A0C` with a 1px outline offset.
- **Checkboxes & Radios:** Sharp 16px square (checkbox: 3px radius; radio: 50% circle). Border 1px `#D1D5DB`. Active fill `#090A0C` with `#FFFFFF` glyph.

### Badges & Platform Chips
- **Platform Sync Badges:** Height 22px, padding 2px 8px, 4px radius. 1px `#E5E7EB` border, `#F9FAFB` background, `#090A0C` label with an 6px status dot (connected: `#10B981`, syncing: `#2563EB`, disconnected: `#9CA3AF`).

### Sparklines & Minimal Charts
- **Stroke Width:** 1.5px continuous path without area drop shadow or fill gradients.
- **Coloration:** Primary accent `#059669` for growth intervals; neutral `#E5E7EB` for comparison baselines.
- **Crosshair Reticle:** 1px dashed `#9CA3AF` cursor tracking line, pairing with a minimal 4px dot marker.