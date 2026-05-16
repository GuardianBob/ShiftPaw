---
name: Paws & Shifts
colors:
  surface: '#fdf7ff'
  surface-dim: '#ded8e0'
  surface-bright: '#fdf7ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f8f2fa'
  surface-container: '#f2ecf4'
  surface-container-high: '#ece6ee'
  surface-container-highest: '#e6e0e9'
  on-surface: '#1d1b20'
  on-surface-variant: '#494551'
  inverse-surface: '#322f35'
  inverse-on-surface: '#f5eff7'
  outline: '#7a7582'
  outline-variant: '#cbc4d2'
  surface-tint: '#6750a4'
  primary: '#4f378a'
  on-primary: '#ffffff'
  primary-container: '#6750a4'
  on-primary-container: '#e0d2ff'
  inverse-primary: '#cfbcff'
  secondary: '#63597c'
  on-secondary: '#ffffff'
  secondary-container: '#e1d4fd'
  on-secondary-container: '#645a7d'
  tertiary: '#765b00'
  on-tertiary: '#ffffff'
  tertiary-container: '#c9a74d'
  on-tertiary-container: '#503d00'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e9ddff'
  primary-fixed-dim: '#cfbcff'
  on-primary-fixed: '#22005d'
  on-primary-fixed-variant: '#4f378a'
  secondary-fixed: '#e9ddff'
  secondary-fixed-dim: '#cdc0e9'
  on-secondary-fixed: '#1f1635'
  on-secondary-fixed-variant: '#4b4263'
  tertiary-fixed: '#ffdf93'
  tertiary-fixed-dim: '#e7c365'
  on-tertiary-fixed: '#241a00'
  on-tertiary-fixed-variant: '#594400'
  background: '#fdf7ff'
  on-background: '#1d1b20'
  surface-variant: '#e6e0e9'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 57px
    fontWeight: '700'
    lineHeight: 64px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  title-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
  body-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  label-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-md:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  margin-mobile: 16px
  margin-tablet: 24px
  gutter: 16px
  section-padding: 24px
  card-gap: 12px
---

## Brand & Style

The design system is built for a pet-centric professional environment, balancing the reliability needed for workforce management with the warmth of the veterinary and pet-care industry. It follows a **Modern Corporate** style infused with **Playful Tactility**, adhering strictly to Material Design 3 (MD3) logic while introducing custom brand flourishes.

The interface should evoke a sense of organized joy. We utilize generous white space, soft edges, and "creature comforts" in the UI—such as subtle abstract patterns reminiscent of fur or paw prints—to ensure the app feels specialized rather than generic. The target audience includes pet hospital staff, groomers, and kennel managers who require high efficiency under pressure but appreciate a friendly, non-clinical aesthetic.

## Colors

The color strategy utilizes two distinct palettes to cater to different lighting environments and user preferences.

**Light Theme (Professional Warmth):**
Centered around **Deep Purple** for authority and **Gold** for energy. The purple provides the structural "scheduling" feel, while Gold highlights active shifts, alerts, and high-priority pet tasks.

**Dark Theme (High-Contrast Energy):**
Switches to a **Vibrant Orange** and **Magenta** palette. This ensures maximum legibility in low-light environments (like night shifts at a clinic). The high-contrast nature of these neon-adjacent tones keeps the "playful" brand spirit alive even against dark surfaces.

Both modes follow MD3 color slotting (Containers, On-Colors, and Surface Tones) to ensure accessibility and hierarchy.

## Typography

This design system uses a dual-font approach to balance personality with data-heavy readability.

**Plus Jakarta Sans** is used for headlines and display titles. Its soft, rounded geometric forms echo the "friendly" brand pillar. It maintains a modern, clean look that prevents the pet theme from feeling juvenile.

**Be Vietnam Pro** is utilized for body text, shift details, and labels. It offers exceptional legibility at small sizes, which is critical for viewing complex weekly schedules on mobile devices. The slightly wider apertures help reduce eye strain during long periods of shift management.

## Layout & Spacing

Following Android's adaptive layout principles, this design system uses a **Fluid Grid** model with generous padding to create a "breathable" and modern interface. 

- **Mobile:** 4-column grid with 16px side margins. 
- **Tablet:** 12-column grid with 24px side margins.
- **Rhythm:** An 8px linear scale is used for all spatial relationships. 

To emphasize the "generous feel," vertical spacing between schedule cards is set to 12px rather than the standard 8px, ensuring that each shift entry feels distinct and easily tappable. Safe areas are strictly observed for the bottom navigation and the prominent Floating Action Button.

## Elevation & Depth

We utilize **Tonal Layers** as the primary method of showing depth, supplemented by **Ambient Shadows** to signify interactable components.

1.  **Level 0 (Surface):** The background layer using the `surface` color.
2.  **Level 1 (Cards/Lists):** A slight tonal shift or a very soft shadow (4px blur, 2% opacity) to lift shift cards off the background.
3.  **Level 2 (Navigation):** Bottom navigation bars use a subtle backdrop blur or tonal elevation to stay persistent above content.
4.  **Level 3 (FAB):** The 'Upload Word Doc' FAB uses the highest elevation with a more pronounced shadow to indicate it is the primary action on the screen.

In the Dark Theme, elevation is communicated through color luminance (lighter greys/oranges) rather than shadows, maintaining a "sleek" glow effect.

## Shapes

The shape language is **Rounded (Level 2)** to align with the friendly brand personality. 

- **Buttons:** 0.5rem (8px) radius for standard buttons; fully pill-shaped (2rem+) for the Floating Action Button.
- **Cards:** 1rem (16px) radius for shift cards and containers to create a soft, inviting container for data.
- **Inputs:** 0.5rem (8px) radius to maintain consistency with buttons.

Small accents, like "paw-clip" corners (where one corner is more rounded than others), can be used on decorative elements or profile avatars to reinforce the pet theme without compromising the professional structure.

## Components

**Floating Action Button (FAB):**
The "Upload Word Doc" FAB is a large, pill-shaped button. In the light theme, it uses the Primary Purple with a white icon; in the dark theme, it uses Vibrant Orange with a dark icon. It should include an extended label ("Upload Schedule") that collapses into just an icon on scroll.

**Shift Cards:**
Cards feature a high-contrast "Secondary Color" vertical bar on the left edge to denote shift status (e.g., Pending, Confirmed). Information is grouped logically: Time (Bold), Role (Label), and Pet/Area assigned (Body).

**Bottom Navigation:**
Strictly follows MD3 specs. Icons use a "tonal pill" active state. The icons themselves should have a soft, rounded line style.

**Input Fields:**
Filled style with a bottom-line stroke. In the Light theme, the fill is a very pale purple; in the Dark theme, it is a deep charcoal. The focus state uses the Secondary Color (Gold/Magenta) for the cursor and stroke to provide high visibility.

**Chips:**
Used for filtering pets or shift types (e.g., "Grooming," "Emergency," "Night"). These utilize the `secondary_container` colors to provide a "pop" of color without overwhelming the primary actions.