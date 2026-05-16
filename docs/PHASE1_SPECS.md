# VetCalendar Android — Phase 1 Specifications

**Release:** v1.0.0-alpha  
**Target Device:** Samsung Galaxy S22 (API 33+)  
**Scope:** Local storage, offline-first, view-only  
**User Testing:** Private (2 users)

---

## Functional Specifications

### Feature 1: File Upload & DOCX Parsing

**User Story:**
> As a clinic manager, I want to upload a monthly shift schedule (DOCX) so that my team can view all shifts in the app.

**Workflow:**
1. User opens Calendar screen
2. User taps purple FAB ("Upload Schedule")
3. System file picker opens (scoped to Downloads/Documents)
4. User selects `.docx` file (e.g., "April 2026.docx")
5. App shows confirmation dialog:
   - Detected month: "April 2026"
   - Option to confirm or pick different month
6. User taps "Confirm"
7. App shows loading indicator ("Parsing schedule...")
8. App parses DOCX file:
   - Extracts table structure
   - Reads employee initials (2 characters per cell)
   - Maps initials to shift times (07:00, 10:00, 14:00, 18:00)
   - Creates Shift objects
9. App clears existing shifts for April 2026 (if any)
10. App inserts new shifts into Room database
11. App shows success toast: "Imported 24 shifts"
12. Calendar refreshes (live via Flow)
13. User returns to Calendar view with shifts visible

**Input Data Format (DOCX Table):**
```
┌─────┬──────────┬──────────┬──────────┬──────────┐
│ Day │ 07:00    │ 10:00    │ 14:00    │ 18:00    │
├─────┼──────────┼──────────┼──────────┼──────────┤
│  1  │ JD       │ MS       │          │ AL       │
│  2  │ MS       │          │ JD       │          │
│  3  │ JD, MS   │ AL       │ JD       │ MS       │
└─────┴──────────┴──────────┴──────────┴──────────┘
```

**Parsing Logic:**
- Column 0: Day of month (1, 2, 3, ...)
- Columns 1–4: Employee initials (JD = Jane Doe, MS = Mike Smith, AL = Anna Lee)
- If cell empty: no shift at that time
- If cell contains "JD, MS": two employees (two Shift records created)
- Shift times hardcoded: 07:00, 10:00, 14:00, 18:00

**Location Mapping (Configurable in Phase 2):**
- For now: default location = "To be determined"
- Can be extracted from DOCX metadata (future)
- User can manually edit later (Phase 2)

**Error Cases:**

| Error | Message | Recovery |
|-------|---------|----------|
| File not found | "File not accessible. Pick another file." | Retry picker |
| Invalid DOCX | "File is not a valid schedule. Check format." | Retry picker |
| Parse error | "Could not read schedule. File may be corrupted." | Retry picker |
| Duplicate month | "April 2026 already imported. Overwrite?" | Confirm/cancel |
| Storage full | "Not enough space. Free up storage and retry." | Exit |

**Acceptance Criteria:**
- [ ] App successfully parses all 5 sample DOCX files
- [ ] Correctly extracts month/year from filename
- [ ] Correctly extracts employee initials (JD, MS, AL, etc.)
- [ ] Correctly maps times to shifts (07:00, 10:00, 14:00, 18:00)
- [ ] Handles multi-employee cells (e.g., "JD, MS" → 2 shifts)
- [ ] Handles empty cells (no shift created)
- [ ] Clears previous month's shifts on re-import
- [ ] Persists shifts to Room database
- [ ] Shows success toast
- [ ] Returns to Calendar with shifts visible

---

### Feature 2: Calendar Views (Month, Week, Day)

**User Story:**
> As a staff member, I want to see the shift schedule in month/week/day views so I can find my shifts easily.

#### Month View (Default)

**Layout:**
- 7-column grid (Mon–Sun)
- Date headers (M, T, W, T, F, S, S)
- Date cells (1–31)
- Grayed-out dates from previous/next month
- Colored dots below date if shifts exist
  - 1 dot = 1–2 shifts on that day
  - 2 dots = 3–4 shifts
  - 3+ dots = packed day
  - Color varies by employee (if filtered)
- Current day highlighted (blue ring)

**Interactions:**
- Tap date cell → drill down to Day view
- Prev/Next buttons → navigate months
- Date picker button → jump to month/year

**Display Logic:**
```
// Pseudocode
for date in 1..31 {
    shifts = db.query("SELECT * FROM shifts WHERE date = ? AND month = ? AND year = ?", 
                       date, month, year)
    filteredShifts = shifts.filter { it.userInitials in selectedEmployees }
    
    if (filteredShifts.empty) {
        drawCell(date, noDots = true)
    } else {
        dotCount = ceil(filteredShifts.count / 2)
        drawCell(date, dots: dotCount)
    }
}
```

**Acceptance Criteria:**
- [ ] Displays dates 1–31 in correct day-of-week columns
- [ ] Shows dots below dates with shifts
- [ ] Highlights current day
- [ ] Prev/Next navigation updates calendar
- [ ] Date picker works
- [ ] Tapping date shows Day view
- [ ] Respects employee filter (dots reflect filtered shifts)
- [ ] Responsive on portrait + landscape

#### Day View

**Layout:**
- Header: "Oct 11, 2023" + "Busy Day" badge (if > 2 shifts)
- List of shifts (ordered by start time)
- Each shift card shows:
  - Time (bold): "07:00 – 10:00"
  - Employee: "JD" or "Jane Doe"
  - Location: "Main Yard" (icon + text)
  - "More" menu (3 dots)

**Shift Card Example:**
```
┌─────────────────────────────────────┐
│ Morning Kennel Walk : JD   08:00    │
│ 📍 Main Yard           10:00        │
│                                  ⋮ │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap card → show Shift Details modal (text-only for Phase 1)
- Tap "More" → show menu: "View Details", "Call Employee" (future), "Delete" (Phase 2)
- Back button → return to Calendar

**Acceptance Criteria:**
- [ ] Lists all shifts for selected date
- [ ] Orders by start time
- [ ] Shows time, employee, location correctly
- [ ] Tap card shows modal or detail screen
- [ ] Responsive layout on phone + landscape

#### Week View (Defer to Phase 1.5 if Time Allows)

**Layout:**
- Horizontal scroll of 7 day cards
- Each day shows stacked shift cards
- Time slot indicators (07:00, 10:00, 14:00, 18:00)
- Swipe left/right to navigate weeks

---

### Feature 3: Employee Filtering

**User Story:**
> As a manager, I want to filter the calendar by employee so I can see only their shifts.

**Layout:**
- Below "Schedule" title in Calendar screen
- Horizontal scrollable chip row
- Chips: "All Staff" (always first), then per-employee (JD, MS, AL, etc.)

**Interactions:**
- Tap chip to toggle (selected = filled bg, deselected = outlined)
- "All Staff" chip: resets all employees (shows all shifts)
- Individual chips: multi-select (can select JD + MS together)
- Tapping individual chip while "All Staff" is selected → deselects "All Staff"
- Calendar and Day view re-filter live

**State Management:**
```kotlin
selectedEmployees: StateFlow<Set<String>> = MutableStateFlow(emptySet()) // empty = all

// When user taps chip
if (chip == "All Staff") {
    selectedEmployees.value = emptySet()
} else {
    selectedEmployees.value = selectedEmployees.value.toggle(chip)
}

// Room query
val shifts = if (selectedEmployees.isEmpty()) {
    db.getAllShifts(month, year)
} else {
    db.getShiftsByEmployees(month, year, selectedEmployees)
}
```

**Persistence:**
- Filter selection persisted to DataStore
- On app restart, filter restored

**Acceptance Criteria:**
- [ ] Chip display shows all employees from current month's shifts
- [ ] Tap chip toggles selection
- [ ] Calendar filters in real-time
- [ ] "All Staff" chip clears all filters
- [ ] Multi-select works (JD + MS)
- [ ] Filter persists across app restarts
- [ ] Responsive scrolling on narrow screens

---

### Feature 4: Primary Employee Highlight & Stats

**User Story:**
> As a staff member, I want to mark myself as "primary" so my shifts are highlighted and I can see my stats.

**Primary Employee Selection:**
- Location: Employees screen (nav tab 3)
- List of all employees extracted from shifts
- Radio button next to each name ("Set as Primary")
- Only one employee can be primary at a time

**Calendar Highlight:**
- Primary employee's shifts have:
  - Bold border (2 dp, primary color)
  - Or highlight background (subtle tint)
- Non-primary shifts: normal styling

**Stats Display:**
- Location 1: Top of Employees screen
  - Card showing: "Jane Doe (Primary)"
  - Total shifts (all time)
  - Shifts this month
  - Upcoming shifts (next 7 days)
  - Shifts by location

- Location 2: Dashboard screen (quick stats)
  - Show primary employee + shift count

**Example Stats Card:**
```
┌────────────────────────────────────┐
│ Primary Employee: Jane Doe         │
├────────────────────────────────────┤
│ Total shifts (all time): 156       │
│ Shifts this month: 22              │
│ Upcoming (7 days): 5               │
│ Breakdown:                         │
│   Main Yard: 10                    │
│   Spa Wing: 8                      │
│   Clinic: 4                        │
└────────────────────────────────────┘
```

**Acceptance Criteria:**
- [ ] User can select primary employee
- [ ] Selection persisted to DataStore
- [ ] Calendar highlights primary employee's shifts
- [ ] Stats calculated correctly
- [ ] Stats update on file upload
- [ ] Primary employee persists across app restarts

---

### Feature 5: Search

**User Story:**
> As a user, I want to search for shifts by employee or location so I can find specific shifts quickly.

**Search UI:**
- Search icon in top app bar (Calendar screen)
- Tap → TextField appears (slides down or replaces title)
- Placeholder: "Search by employee or location..."
- Real-time results (debounced 300 ms)
- Result list: filtered calendar view or detail list

**Search Logic:**
```kotlin
searchQuery: StateFlow<String> = MutableStateFlow("")

val searchResults = searchQuery.flatMapLatest { query ->
    if (query.isEmpty()) {
        repository.getShifts(month, year, selectedEmployees)
    } else {
        repository.searchShifts(query, month, year, selectedEmployees)
    }
}

// Room query
SELECT * FROM shifts 
WHERE (userInitials LIKE ? OR location LIKE ?)
AND month = ? AND year = ?
ORDER BY date, startTime
```

**Acceptance Criteria:**
- [ ] Search icon visible in top app bar
- [ ] Tap opens search TextField
- [ ] Real-time filtering as user types
- [ ] Results respect current employee filter
- [ ] Results respect current month
- [ ] Close search → restore full calendar
- [ ] Search history (nice-to-have, defer)

---

### Feature 6: Theme Toggle (Light / Dark / System)

**User Story:**
> As a user, I want to toggle dark mode so the app is easy to read in low-light environments.

**Settings Screen:**
- Radio button group: "Light", "Dark", "System"
- Default: "System" (follows device setting)
- On change: apply theme immediately

**Implementation:**
```kotlin
data class UserPreferences(
    val isDarkMode: Boolean? = null  // null = system, true = dark, false = light
)

// Apply theme
val isDarkMode = when (userPrefs.isDarkMode) {
    true -> true
    false -> false
    null -> isSystemInDarkTheme()  // Compose built-in
}

PetShiftTheme(darkTheme = isDarkMode) {
    MainScreen()
}
```

**Color Mappings:**

**Light Theme:**
- Primary: #4F378A (Deep Purple)
- Secondary: #63597C
- Tertiary: #765B00 (Gold)
- Surface: #FDF7FF (Near-white)
- On-Surface: #1D1B20 (Dark gray)

**Dark Theme:**
- Primary: #CFBCFF (Light purple)
- Secondary: #CDC0E9
- Tertiary: #FFD466 (Light gold)
- Surface: #1D1B20 (Dark)
- On-Surface: #F5EFF7 (Light gray)

**Acceptance Criteria:**
- [ ] Settings screen shows 3 options (Light/Dark/System)
- [ ] Default is "System"
- [ ] Theme changes immediately on selection
- [ ] Theme persists across app restarts
- [ ] All UI components respect theme (no hardcoded colors)
- [ ] Text readable in both themes (WCAG contrast)

---

### Feature 7: Date Picker Navigation

**User Story:**
> As a user, I want to jump to a specific month/year so I can quickly navigate to future schedules.

**UI:**
- Calendar icon + "October 2023" text (Calendar header)
- Tap → Material3 DatePicker dialog
- Select month/year (year spinner, month dropdown)
- "OK" → calendar navigates to selected month

**Implementation:**
```kotlin
// Material3 DatePickerDialog
val state = rememberDatePickerState(
    initialSelectedDateMillis = currentMonth.toEpochDay() * 86400000
)
DatePickerDialog(
    onDismissRequest = { },
    confirmButton = {
        Button(onClick = {
            val selectedDate = state.selectedDateMillis
            viewModel.selectMonth(selectedDate)
        }) { Text("OK") }
    },
    content = { DatePicker(state) }
)
```

**Acceptance Criteria:**
- [ ] Date picker icon visible in Calendar header
- [ ] Tap opens Material3 DatePickerDialog
- [ ] Can select month/year
- [ ] OK button navigates calendar
- [ ] Calendar re-queries shifts for new month
- [ ] Responsive on phone + landscape

---

### Feature 8: Dashboard Screen

**User Story:**
> As a staff member, I want to see a quick summary of today's shifts so I know what I'm working.

**Layout:**
- Today's date + day of week
- "Today's Shifts" section: list of all shifts today
  - Or empty message if no shifts
- Quick stats card (if primary employee selected):
  - "Jane Doe: 5 shifts today"
  - "Upcoming: 3 shifts"
- Upcoming shifts (next 3 days)
  - Group by date
  - Show time, employee, location

**Example:**
```
┌─────────────────────────────────────┐
│ Monday, October 11, 2023            │
├─────────────────────────────────────┤
│ Today's Shifts (4)                  │
│ ┌─────────────────────────────────┐ │
│ │ 07:00 – 10:00: JD (Main Yard)  │ │
│ │ 10:00 – 14:00: MS (Spa Wing)   │ │
│ │ 14:00 – 18:00: AL (Clinic)     │ │
│ │ 18:00 – 22:00: JD (Main Yard)  │ │
│ └─────────────────────────────────┘ │
├─────────────────────────────────────┤
│ Upcoming (Oct 12–14)                │
│ ┌─────────────────────────────────┐ │
│ │ Tue 12: 3 shifts                │ │
│ │ Wed 13: 2 shifts                │ │
│ │ Thu 14: 4 shifts                │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap shift card → navigate to Calendar (filtered to that date)
- Tap day in Upcoming → navigate to Calendar for that day

**Acceptance Criteria:**
- [ ] Shows today's date and day of week
- [ ] Lists all shifts for today
- [ ] Shows quick stats if primary employee selected
- [ ] Lists upcoming shifts (next 3 days)
- [ ] Tap shift navigates to Calendar
- [ ] Responsive layout on phone + landscape
- [ ] Updates on app resume (user checks today's shifts)

---

### Feature 9: Employees Screen

**User Story:**
> As a user, I want to see all employees and select a primary so I can track my shifts and view my stats.

**Layout:**
- List of all employees (extracted from current month's shifts)
- Each row:
  - Employee avatar (initials in circle)
  - Employee name (if known)
  - Radio button (select as primary)
  - Shift count for this month

**Example:**
```
┌─────────────────────────────────────┐
│ Employees                           │
├─────────────────────────────────────┤
│ (JD) Jane Doe              22 shifts│ ◯ Set Primary
│ (MS) Mike Smith            18 shifts│ ◉ Primary
│ (AL) Anna Lee              20 shifts│ ◯ Set Primary
│ (RB) Robert Brown           8 shifts│ ◯ Set Primary
└─────────────────────────────────────┘

(Primary employee card below)
├─────────────────────────────────────┤
│ Primary: Mike Smith                 │
│ Total shifts (all time): 156        │
│ Shifts this month: 18               │
│ Upcoming (7 days): 4                │
│ Breakdown:                          │
│   Main Yard: 10                     │
│   Spa Wing: 6                       │
│   Clinic: 2                         │
└─────────────────────────────────────┘
```

**Acceptance Criteria:**
- [ ] Lists all employees from current month's shifts
- [ ] Shows employee name + initials
- [ ] Shows shift count for current month
- [ ] Radio button to select primary
- [ ] Primary stats card visible
- [ ] Stats update in real-time
- [ ] Responsive layout on phone + landscape

---

### Feature 10: Settings Screen

**User Story:**
> As a user, I want to manage theme, permissions, and data so I have control over my app experience.

**Layout:**
- **Theme Section**
  - Radio buttons: "Light", "Dark", "System" (default)
  - Preview: show light/dark color swatch

- **Data Section**
  - "Clear All Data" button (red, with confirmation dialog)
  - Confirmation: "Delete all shifts and settings? This cannot be undone."

- **About Section**
  - App version: "v1.0.0-alpha"
  - Build date
  - Privacy policy link (placeholder)
  - Support email (placeholder)

**Example:**
```
┌─────────────────────────────────────┐
│ Settings                            │
├─────────────────────────────────────┤
│ THEME                               │
│ ◯ Light                             │
│ ◉ Dark                              │
│ ◯ System                            │
├─────────────────────────────────────┤
│ DATA & PRIVACY                      │
│ [Clear All Data] (red)              │
├─────────────────────────────────────┤
│ ABOUT                               │
│ Version: v1.0.0-alpha               │
│ Build: 24 May 2026                  │
│ Privacy Policy                      │
│ Support: support@vetcal.app         │
└─────────────────────────────────────┘
```

**Acceptance Criteria:**
- [ ] Theme selector works (Light/Dark/System)
- [ ] Theme applies immediately
- [ ] Clear All Data shows confirmation dialog
- [ ] Confirmation clears Room + DataStore
- [ ] App returns to login or empty state
- [ ] Version + date displayed correctly
- [ ] Links are placeholders (functional in Phase 2)

---

### Feature 11: Bottom Navigation

**User Story:**
> As a user, I want a bottom navigation bar so I can quickly switch between main screens.

**Layout:**
- Fixed at bottom of screen
- 4 nav items:
  1. Dashboard (home icon)
  2. Calendar (calendar icon)
  3. Employees (group icon)
  4. Settings (gear icon)
- Active tab: filled background (secondary container)
- Inactive tabs: icons only

**Example:**
```
┌─────────────────────────────────────┐
│ Dashboard | Calendar | Employees | Settings
│ [Home]    |[Calendar]|  [Group]  |[Settings]
└─────────────────────────────────────┘
```

**Acceptance Criteria:**
- [ ] All 4 tabs visible and tappable
- [ ] Active tab highlighted
- [ ] Navigation to correct screen
- [ ] Persists state when switching tabs (ViewModel alive)
- [ ] Accessible on narrow screens (icons + text fit)

---

### Feature 12: FAB (Upload Schedule)

**User Story:**
> As a user, I want a quick button to upload schedules so I don't have to dig through menus.

**Location:** Calendar screen, bottom-right (above bottom nav)

**Appearance:**
- Floating Action Button (purple, Material3 style)
- Icon: upload (file_upload)
- Label: "Upload Schedule" (or collapse to just icon on scroll)
- Tap → open File Upload screen

**Acceptance Criteria:**
- [ ] FAB visible on Calendar screen only
- [ ] Positioned above bottom navigation
- [ ] Tap opens File Upload screen
- [ ] Icon + label visible
- [ ] Responds to scroll (optional: collapse on scroll)

---

## Non-Functional Specifications

### Performance

| Metric | Target | Notes |
|--------|--------|-------|
| App startup | < 2 sec | Cold start, device not throttled |
| DOCX parse (24 shifts) | < 3 sec | On Galaxy S22 |
| Calendar render | < 200 ms | 30 shifts displayed |
| Memory usage | < 100 MB | Under normal use |
| Scroll performance | 60 FPS | Month/week/day views smooth |

### APK Size

| Component | Size | Notes |
|-----------|------|-------|
| App code | 2–3 MB | Minified |
| Compose runtime | 3–4 MB | Standard |
| Room + SQLite | 2–3 MB | Standard |
| Apache POI | 4–5 MB | Minified, proguard rules |
| Resources | 1–2 MB | Compressed |
| **Total** | **13–15 MB** | ✅ < 20 MB target |

### Compatibility

| Requirement | Details |
|-------------|---------|
| Minimum Android | 13 (API 33) |
| Target Android | 15 (API 35) |
| Samsung Galaxy S22 | Primary test device (API 33–35) |
| Orientation | Portrait + landscape |
| Screen sizes | 5"–6.7" phones (Phase 2: tablets) |
| Accessibility | WCAG 2.1 level A (core), level AA (nice-to-have) |

### Security

| Aspect | Requirement |
|--------|-------------|
| Data storage | Room database (unencrypted local, Phase 2: SQLCipher) |
| User preferences | DataStore (auto-encrypted) |
| File access | Scoped storage (Android 11+) |
| Network | None required for Phase 1 |
| Permissions | READ_EXTERNAL_STORAGE only |

### Reliability

| Aspect | Requirement |
|--------|-------------|
| Crash-free rate | > 99.5% (target: 100% in testing) |
| Data loss | 0% (Room is ACID-compliant) |
| Offline capability | 100% (no network required) |
| Data persistence | Across app close/reopen, rotation, backgrounding |

---

## User Interface Guidelines

### Color System

**Light Theme (Default):**
- Primary buttons: Deep Purple (#4F378A)
- Accent: Gold (#765B00)
- Errors: Red (#BA1A1A)
- Text on light: Dark gray (#1D1B20)

**Dark Theme:**
- Primary buttons: Light Purple (#CFBCFF)
- Accent: Light gold (#FFD466)
- Errors: Light red (lighter shade)
- Text on dark: Light gray (#F5EFF7)

### Typography

- **Headlines:** Plus Jakarta Sans (Bold, 28–32 sp)
- **Body:** Be Vietnam Pro (Regular, 16 sp)
- **Labels:** Be Vietnam Pro (Medium, 12–14 sp)

### Spacing

- 8 dp base unit
- Card padding: 16 dp
- Section margins: 24 dp
- Gutter (side margins): 16 dp mobile, 24 dp tablet

### Components

- **Buttons:** Filled (primary action), Outlined (secondary)
- **Cards:** 16 dp corner radius, soft shadow
- **Chips:** Pill-shaped, selectable, multi-choice
- **FAB:** 56 dp diameter, positioned at bottom-right

---

## Acceptance Criteria Summary

### Must Have (Phase 1 MVP)
- [ ] All 12 features implemented
- [ ] APK < 20 MB
- [ ] Crash-free on Galaxy S22
- [ ] Light + dark theme working
- [ ] All 5 sample DOCX files parse successfully
- [ ] Database persists across app close/reopen
- [ ] No crashes on rotation or backgrounding

### Nice to Have (Phase 1.5 if Time)
- [ ] Week view calendar
- [ ] Shift detail modal with full information
- [ ] Animated transitions (Fade, Slide)
- [ ] 90%+ unit test coverage
- [ ] Advanced stats (shifts by type)
- [ ] Keyboard navigation (accessibility)

### Out of Scope (Phase 2+)
- [ ] Backend API integration
- [ ] Multi-user support / sync
- [ ] Push notifications
- [ ] Shift creation/editing
- [ ] Photo upload
- [ ] iOS app

---

**Specs Status:** LOCKED  
**Last Updated:** [Now]  
**Owner:** [Rocket]
