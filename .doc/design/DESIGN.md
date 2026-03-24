# Design System Strategy: Precision Ecology

## 1. Overview & Creative North Star
**The Creative North Star: "The Digital Agronomist"**

This design system rejects the cluttered, "machinery-heavy" aesthetic of traditional industrial software. Instead, it adopts the persona of a high-end editorial journal—clean, authoritative, and deeply intentional. We are moving away from the "standard dashboard" look by utilizing **Organic Structuralism**. 

While the data is technical (IoT sensors, soil pH, industrial throughput), the interface must feel like a premium, breathable workspace. We achieve this through **Intentional Asymmetry**: sidebars are not just "boxes" on the left; they are anchored surfaces that allow the main stage to breathe. We utilize **High-Contrast Typography Scales** to ensure that critical telemetry data feels like a headline, not just a number in a cell.

## 2. Colors & Surface Philosophy
The palette centers on the deep, authoritative `primary` (#154212) and the stabilizing `secondary` (#545f72).

### The "No-Line" Rule
To achieve a high-end feel, **1px solid borders are strictly prohibited for sectioning.** Boundaries must be defined solely through background color shifts or subtle tonal transitions. 
- Use `surface-container-low` for large background areas.
- Use `surface-container-lowest` (Pure White) for active data cards.
- The contrast between these two tokens creates a crisp edge without the "cheap" look of a stroke.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers. 
1.  **Base Layer:** `surface` (#f7f9fb) – The canvas.
2.  **Section Layer:** `surface-container-low` (#f2f4f6) – Grouping related sensor clusters.
3.  **Action Layer:** `surface-container-lowest` (#ffffff) – The interactive cards.
4.  **Floating Layer:** `surface-bright` with 80% opacity and 12px Backdrop Blur – For tooltips and popovers.

### The "Glass & Gradient" Rule
Standard flat buttons are for utility; signature actions deserve "soul." For primary CTA's and real-time status headers, apply a subtle linear gradient from `primary` (#154212) to `primary-container` (#2D5A27) at a 135-degree angle. This adds a "weighted" feel that communicates industrial reliability.

## 3. Typography: The Editorial Scale
We pair the utilitarian **Inter** (Body) with the architectural **Manrope** (Display) to bridge the gap between technical data and premium brand identity.

*   **Data Hero Metrics (`display-lg`):** Manrope, 3.5rem. Used for the single most important KPI (e.g., Current Yield).
*   **Section Headers (`headline-sm`):** Manrope, 1.5rem. Bold, tight tracking (-0.02em).
*   **Technical Readouts (`body-md`):** Inter, 0.875rem. Increased line height (1.6) to ensure sensor logs are readable during high-stress alerts.
*   **The "Metric Label" (`label-sm`):** Inter, 0.6875rem, All Caps, Letter Spacing 0.05em. Used above data points to provide a "Blueprint" aesthetic.

## 4. Elevation & Depth
We convey hierarchy through **Tonal Layering** rather than traditional structural lines.

*   **The Layering Principle:** Place a `surface-container-lowest` card on top of a `surface-container-low` section. This "lifts" the content naturally.
*   **Ambient Shadows:** For floating elements like modals, use a shadow with a 32px blur, 4% opacity, using the `on-surface` (#191c1e) color. It should feel like a soft glow, not a drop shadow.
*   **The "Ghost Border" Fallback:** If a border is required for accessibility in data tables, use `outline-variant` (#c2c9bb) at **15% opacity**. It should be felt, not seen.
*   **Glassmorphism:** Use semi-transparent `surface-container-highest` for the Sidebar background with a `backdrop-filter: blur(20px)`. This allows the "Forest Green" primary elements to subtly bleed through as the user scrolls, creating a sense of environmental depth.

## 5. Components

### Cards & Data Displays
*   **Style:** No borders. `surface-container-lowest` background. 
*   **Spacing:** Use `8` (1.75rem) internal padding. 
*   **Layout:** Forbid the use of divider lines. Separate content blocks using `4` (0.9rem) or `6` (1.3rem) vertical white space from the Spacing Scale.

### Real-Time Line Charts
*   **Stroke:** 2.5px width. Use `primary` for standard telemetry.
*   **Success State:** `on-tertiary-container` (#49da9f) for "In-Range" metrics.
*   **Failure State:** `error` (#ba1a1a) for "Out-of-Bounds" alerts.
*   **Area Fill:** A gradient from the line color (20% opacity) to 0% opacity. No grid lines except for the 0-baseline.

### Status Indicators (High-Contrast)
*   **The "Signal" Chip:** Pills with `full` roundedness. 
*   **Success:** `on-tertiary-container` text on `tertiary-container` background.
*   **Critical:** `on-error-container` text on `error_container` background.
*   **Interaction:** Add a subtle "pulse" animation (10% scale swell) for live "Failure" alerts to draw immediate eye movement.

### Input Fields
*   **Style:** `surface-container-high` background, `none` border. 
*   **Focus State:** A 2px "Ghost Border" of `primary` at 40% opacity.

## 6. Do’s and Don’ts

### Do:
*   **DO** use whitespace as a functional tool. If data feels crowded, increase spacing to `10` (2.25rem) before considering a divider line.
*   **DO** use `secondary` (#545f72) for all non-interactive UI metadata to keep the focus on the `primary` green actions.
*   **DO** align all telemetry data to a hard vertical axis to create a "columnar" editorial look.

### Don’t:
*   **DON’T** use pure black (#000000) for text. Always use `on-surface` (#191c1e) to maintain a premium, soft-contrast feel.
*   **DON’T** use standard 4px border radii for everything. Use `xl` (0.75rem) for large cards and `none` for sidebars to create a "custom-built" architectural feel.
*   **DON’T** use high-saturation reds for alerts. Use the specified `error` (#ba1a1a) which has a sophisticated, deep-crimson "industrial" tone.