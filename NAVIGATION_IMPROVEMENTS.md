# Navigation Improvements for PrettyJSON

## Current Issues
1. **Hidden FAB Menu**: Options are buried in a floating action button menu that requires discovery
2. **Vertical Stacking**: FAB menu items stack vertically, requiring scrolling and multiple taps
3. **Poor Discoverability**: Users don't know what options are available until they open the menu
4. **Inefficient**: Too many taps to access common actions like "Format", "Export", etc.
5. **Scattered Actions**: Actions are spread across toolbar icons, FAB menu, and tab content

## Proposed Solution: Horizontal Action Bar + Bottom Sheet

### 1. **Horizontal Scrolling Action Chips Bar**
Replace the FAB menu with a horizontal scrolling row of action chips at the top toolbar:

```
┌─────────────────────────────────────────────────┐
│ [< Undo] [Redo >] [🔍 Search] [✓ Valid] [⋯] │
└─────────────────────────────────────────────────┘
```

**Benefits:**
- All actions visible at a glance
- One-tap access to common actions
- Horizontal scrolling for more actions
- Better for one-handed use
- More ergonomic (thumb zone)

### 2. **Action Chips Design**
- Use `FilterChip` or `SuggestionChip` with icon + label
- Group by category visually (Load | Format | Export)
- Color code: Load (blue), Format (green), Export (orange)
- Show contextually (e.g., Export only when output exists)

### 3. **Bottom Sheet for Advanced Options**
- Swipe up bottom sheet for less common actions
- Group actions by category:
  - **Load**: Clipboard, File, URL, Recent
  - **Format**: Minify, Sort (with submenu), Tab Spaces
  - **Export**: PDF, JSON File, Share
  - **View**: Tree/Editor toggle, Full Screen

### 4. **Smart Contextual Actions**
- Show only relevant actions based on:
  - Input tab vs Output tab
  - JSON validity state
  - Content availability

### 5. **Quick Actions Bar (Most Used)**
Top row with most frequently used actions:
- Undo/Redo
- Search
- Paste (on Input tab)
- Copy (on Output tab)
- Format (quick format button)
- Export (when output exists)

### 6. **Better Organization**

**Toolbar Structure:**
```
┌─────────────────────────────────────────────────────────┐
│ Title                                [Settings] [Menu] │
├─────────────────────────────────────────────────────────┤
│ [←] [→] [🔍] [Paste] [Format] [Export] [⋯] [✓ Valid] │
└─────────────────────────────────────────────────────────┘
```

**Bottom Sheet (Swipe Up):**
```
┌─────────────────────────┐
│ Actions                 │
├─────────────────────────┤
│ 📋 Load                 │
│   • Paste from Clipboard│
│   • Open File           │
│   • Load from URL       │
│   • Recent Files        │
├─────────────────────────┤
│ ✨ Format               │
│   • Minify              │
│   • Sort Keys (ASC/DESC)│
│   • Sort by Type/Value  │
│   • Tab Spaces (1-4)    │
├─────────────────────────┤
│ 📤 Export               │
│   • Export as PDF       │
│   • Save as JSON File   │
│   • Share as Text       │
│   • Copy Output         │
├─────────────────────────┤
│ 👁️ View                 │
│   • Tree View           │
│   • Editor View         │
│   • Full Screen         │
└─────────────────────────┘
```

## Implementation Priority

1. **Phase 1**: Replace FAB menu with horizontal action chips
2. **Phase 2**: Add bottom sheet for advanced options
3. **Phase 3**: Add smart contextual visibility
4. **Phase 4**: Add animations and polish

## UX Benefits

✅ **Discoverability**: All actions visible, no hidden menus
✅ **Efficiency**: 1-2 taps to access any action
✅ **Ergonomics**: Thumb-friendly horizontal layout
✅ **Clarity**: Grouped by function with clear labels
✅ **Context**: Actions adapt to current state
✅ **Professional**: Matches modern app design patterns

