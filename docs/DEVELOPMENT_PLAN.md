# VetCalendar Android — Development Plan

**Phase 1 Duration:** 4 weeks (accelerated)  
**Team:** 1 senior Android dev (or 1 dev + 1 part-time)  
**Delivery:** Private testing APK on Galaxy S22 + emulator  
**Scope:** Local storage, offline-first, view-only

---

## Sprint Breakdown

### Sprint 1: Foundation & Setup (Days 1–5)

**Goal:** Project scaffolding, database schema, basic MVVM structure.

#### Tasks

**[Day 1] Project Setup & Android Studio Config**
- [ ] Create new Android project (Kotlin, Jetpack Compose template)
- [ ] Configure build.gradle.kts with all dependencies
- [ ] Set up Hilt DI container
- [ ] Create project folder structure (data/, ui/, viewmodel/, utils/)
- [ ] Add Proguard minification rules
- [ ] Commit: `chore: init android project with compose template`

**[Day 2–3] Database Schema & Room Setup**
- [ ] Create `Shift` entity + `Employee` entity
- [ ] Create `ShiftDao` + `EmployeeDao` with query methods
- [ ] Create `VetCalendarDatabase` (Room database class)
- [ ] Write Room migration strategy placeholder
- [ ] Create `ShiftRepository` + `EmployeeRepository` (business logic layer)
- [ ] Unit test: DAO queries return correct data
- [ ] Commit: `feat: add room database schema and daos`

**[Day 3–4] DataStore & UserPreferences**
- [ ] Create `UserPreferencesSerializer` for DataStore
- [ ] Create `PreferencesRepository` (theme, primary employee, etc.)
- [ ] Unit test: Read/write preferences
- [ ] Commit: `feat: add datastore for user preferences`

**[Day 5] Theme System & Color Tokens**
- [ ] Create `ui/theme/Color.kt` with brand colors (light + dark)
- [ ] Create `ui/theme/Typography.kt` with font styles
- [ ] Create `ui/theme/Shape.kt` with border radius
- [ ] Create `PetShiftTheme` composable (Material3 integration)
- [ ] Test light/dark mode switching
- [ ] Commit: `feat: implement material3 theme system with brand colors`

**Deliverable:** Empty app runs, database schema created, theme system working.

---

### Sprint 2: DOCX Parser & File Upload (Days 6–10)

**Goal:** Import functionality end-to-end. Parse DOCX → save to Room → display success.

#### Tasks

**[Day 6] Apache POI Setup & Parser Skeleton**
- [ ] Add POI dependencies to build.gradle.kts
- [ ] Create `utils/DocxParser.kt` with `parseShifts()` stub
- [ ] Understand DOCX table structure from sample files
- [ ] Create `DocxParserTest.kt` (JUnit) with mock DOCX
- [ ] Commit: `feat: add apache poi library and parser skeleton`

**[Day 7–8] Full DOCX Parser Implementation**
- [ ] Implement `DocxParser.parseShifts(fileUri, context): List<Shift>`
- [ ] Extract month/year from filename (fallback to date picker)
- [ ] Parse table structure: iterate rows, extract initials, times
- [ ] Handle edge cases: empty cells, malformed data, duplicate shifts
- [ ] Create test fixture: mock DOCX file or use real sample
- [ ] Test against all 5 sample DOCX files
- [ ] Commit: `feat: implement docx parser with poi library`

**[Day 9] File Upload UI & Flow**
- [ ] Create `UploadScheduleScreen` (Compose UI)
- [ ] Integrate system file picker (`ActivityResultContracts.GetContent()`)
- [ ] Handle file permissions (scoped storage, Android 11+)
- [ ] Show month/year confirmation dialog
- [ ] Trigger `Repository.insertShifts(shifts)` on confirm
- [ ] Show loading indicator → success toast → navigate back
- [ ] Error handling: invalid file, duplicates, storage full
- [ ] Commit: `feat: add upload screen and file picker integration`

**[Day 10] FAB & Navigation to Upload**
- [ ] Create FAB button on Calendar screen
- [ ] Add `UploadSchedule` route to NavHost
- [ ] Navigation from Calendar FAB → UploadScreen → back to Calendar (with refresh)
- [ ] Integration test: File upload end-to-end
- [ ] Commit: `feat: add fab and upload navigation on calendar screen`

**Deliverable:** Users can select DOCX file, app parses and saves shifts to Room.

---

### Sprint 3: Calendar Views & Data Display (Days 11–18)

**Goal:** Display shifts in month/week/day views. Make calendar interactive.

#### Tasks

**[Day 11–12] Month View (Jetpack Compose)**
- [ ] Create `CalendarViewModel` with StateFlow<CalendarUiState>
- [ ] Create `MonthViewCalendarGrid` composable (7-column grid)
- [ ] Query Room for shifts by month/year
- [ ] Draw date cells, highlight current day
- [ ] Show colored dots below dates (indicator of shifts)
- [ ] Handle month navigation (prev/next buttons)
- [ ] Commit: `feat: implement month view calendar grid`

**[Day 12–13] Day View Drill-Down**
- [ ] Tap date cell → show shifts for that day
- [ ] Create `ShiftCard` composable (time, employee, location, more menu)
- [ ] Display shifts in vertical list ordered by start time
- [ ] Show shift count ("2 shifts on Oct 11")
- [ ] "More" menu: show detail modal (future)
- [ ] Commit: `feat: add day view and shift cards`

**[Day 14–15] Week View (Optional for Accelerated Timeline)**
- [ ] If time: horizontal scroll of 7 day columns
- [ ] Each column shows shifts for that day
- [ ] Swipe to navigate weeks
- [ ] If not time: defer to Phase 1.5 polish
- [ ] Commit: `feat: add week view (optional)`

**[Day 16–17] Date Picker Navigation**
- [ ] Create `DatePickerDialog` (Material3)
- [ ] Allow user to jump to specific month/year
- [ ] Persist selected month in ViewModel
- [ ] Calendar re-queries shifts for new month
- [ ] Commit: `feat: add month/year date picker`

**[Day 18] Calendar Screen Assembly**
- [ ] Compose CalendarScreen with:
  - Top app bar (title "Schedule", theme toggle, search icon)
  - Employee filter chips (below title)
  - Month view + month nav buttons
  - FAB (Upload)
  - Bottom navigation (Calendar active)
- [ ] Connect all pieces: filtering, navigation, queries
- [ ] Commit: `feat: assemble full calendar screen with all controls`

**Deliverable:** Calendar screen displays shifts correctly. Users can navigate months and view shifts by day.

---

### Sprint 4: Filtering, Employee Management & Polish (Days 19–25)

**Goal:** Filter by employee, set primary employee, stats, theme toggle, final polish.

#### Tasks

**[Day 19–20] Employee Filtering**
- [ ] Create `EmployeeFilterChips` composable (horizontal scrollable)
- [ ] Show "All Staff" + per-employee chip (JD, MS, AL, etc.)
- [ ] Tap chip to toggle selection (multi-select)
- [ ] ViewModel updates `selectedEmployees: StateFlow<Set<String>>`
- [ ] Calendar re-queries shifts filtered by set
- [ ] Persist filter selection to DataStore
- [ ] Commit: `feat: add employee filter chips with multi-select`

**[Day 20–21] Primary Employee & Stats**
- [ ] Create `EmployeesScreen` (nav tab 3)
- [ ] List all employees extracted from shifts
- [ ] Radio button to select "primary" employee
- [ ] Primary employee highlighted on Calendar screen
- [ ] EmployeesScreen shows stats:
  - Total shifts (all time)
  - Shifts this month
  - Upcoming shifts (next 7 days)
  - Shifts by location (if data allows)
- [ ] Persist primary employee selection to DataStore
- [ ] Commit: `feat: add employees screen with primary selection and stats`

**[Day 22] Dashboard Screen**
- [ ] Create `DashboardScreen` (nav tab 1)
- [ ] Show today's shifts summary
- [ ] Quick stats card: selected employee, shift count
- [ ] Upcoming shifts list (next 3 days)
- [ ] Tap shift → navigate to Calendar (filtered to that date)
- [ ] Commit: `feat: add dashboard with today's summary and stats`

**[Day 23] Settings Screen & Theme Toggle**
- [ ] Create `SettingsScreen` (nav tab 4)
- [ ] Radio button group: Light / Dark / System
- [ ] On change: update DataStore + apply theme immediately
- [ ] "Clear All Data" button (confirm dialog)
- [ ] About section (version, links)
- [ ] Commit: `feat: add settings screen with theme toggle and data clear`

**[Day 24] Search & Polish**
- [ ] Add search icon to top app bar (Calendar screen)
- [ ] Tap → TextField appears, filter by employee name or location
- [ ] Results show as filtered calendar/list
- [ ] Close search → restore full calendar
- [ ] Polish: animations (Fade, Slide transitions)
- [ ] Commit: `feat: add search by employee/location`

**[Day 25] Testing & Bug Fixes**
- [ ] Unit tests: ViewModels, Repository queries, Parser
- [ ] Integration tests: Room queries, Preferences read/write
- [ ] UI tests: Calendar renders, filters work, navigation works
- [ ] Test on Samsung Galaxy S22 emulator + real device (if available)
- [ ] Verify light/dark mode, rotation (portrait/landscape)
- [ ] APK size check (target < 20 MB)
- [ ] Commit: `test: add comprehensive unit, integration, and ui tests`

**Deliverable:** Feature-complete Phase 1 app. All 5 sample DOCX files parse successfully. Calendar filters by employee, stats calculated, theme toggles work.

---

### Sprint 5: Integration, Testing & Release (Days 26–28)

**Goal:** Integration testing, performance optimization, release-ready APK.

#### Tasks

**[Day 26] End-to-End Testing**
- [ ] Test complete workflow:
  1. Upload April 2026.docx
  2. Verify shifts appear on calendar
  3. Filter by employee (verify filters work)
  4. Select primary employee (verify highlight)
  5. Toggle theme (verify light/dark)
  6. Rotate device (verify no crashes)
  7. Close/reopen app (verify data persists)
- [ ] Test all 5 sample DOCX files
- [ ] Test edge cases:
  - Empty month (no shifts)
  - Upload duplicate month (confirm overwrite)
  - Storage permission denied
  - Device in low-memory state
- [ ] Commit: `test: e2e testing on all sample files`

**[Day 27] Performance & APK Optimization**
- [ ] Profile APK size:
  - Run minification (R8/Proguard)
  - Strip unused resources
  - Compress images/fonts
- [ ] Performance profiling:
  - Measure calendar rendering time (target < 200 ms)
  - Measure DOCX parse time (target < 2 sec for 24 shifts)
  - Memory profiling (target < 100 MB heap)
- [ ] Generate release APK
- [ ] Commit: `chore: optimize apk size and performance`

**[Day 27–28] Documentation & Release Prep**
- [ ] Create `README.md`:
  - Build instructions
  - How to run emulator
  - How to install APK on device
  - Known limitations
- [ ] Update `DEVELOPMENT_PLAN.md` with actual timings
- [ ] Create `RELEASE_NOTES.md` for Phase 1
- [ ] Create `TESTING_CHECKLIST.md` for QA
- [ ] Commit: `docs: add readme and release notes`

**[Day 28] Deploy to Private Testing**
- [ ] Build release APK
- [ ] Test on Galaxy S22 emulator (verify works)
- [ ] Share APK with two users for private testing
- [ ] Collect feedback, log issues for Phase 1.5
- [ ] Commit: `release: v1.0.0-alpha (phase 1 private release)`

**Deliverable:** Release-ready APK tested on Android 13+ devices. Ready for user feedback.

---

## Accelerated Timeline Assumptions

| Task | Duration | Notes |
|------|----------|-------|
| **Project setup** | 1 day | Template provided |
| **Room schema** | 2 days | Simple schema, 2 entities |
| **DOCX parser** | 4 days | Apache POI proven, ~200 lines of code |
| **UI components** | 7 days | No complex animations, Material3 built-in |
| **Filtering/search** | 3 days | ViewModel filtering, simple queries |
| **Testing** | 3 days | Basic unit + integration + UI tests |
| **Polish/docs** | 2 days | README, TESTING_CHECKLIST |
| **Total** | **28 days** | ~4 weeks (5-day work weeks) |

---

## Feature Checklist (Phase 1)

- [ ] Upload DOCX file
- [ ] Parse shifts (extract employee initials, times, dates)
- [ ] Save to Room database
- [ ] View calendar (month, week, day)
- [ ] Filter by employee (single + multi-select)
- [ ] Search by employee/location
- [ ] Select primary employee
- [ ] Display stats (total shifts, by month, by location)
- [ ] Light/dark/system theme toggle
- [ ] Date picker (jump to month)
- [ ] Bottom navigation (Dashboard, Calendar, Employees, Settings)
- [ ] Dashboard screen (today's summary)
- [ ] Employees screen (list + primary toggle + stats)
- [ ] Settings screen (theme, clear data)
- [ ] Data persistence (Room + DataStore)
- [ ] Offline-first (no network required)
- [ ] Error handling (file errors, storage errors)
- [ ] Testing (unit, integration, UI tests)
- [ ] Documentation (README, TESTING_CHECKLIST)

---

## Known Unknowns & Risks

| Risk | Impact | Mitigation | Timeline |
|------|--------|-----------|----------|
| DOCX structure varies by file | Parser breaks | Test against all 5 samples early (Sprint 2) | Day 7–8 |
| Apache POI APK bloat | Exceeds 20 MB limit | Minification + resource stripping (Sprint 5) | Day 27 |
| Compose learning curve | Dev slowdown | Pair programming, refer to Google samples | Day 1–5 |
| Room query performance (large dataset) | Calendar lags | Indexing + pagination strategy ready | Day 11–12 |
| Samsung Galaxy S22 compatibility | Device crashes | Test early on emulator + device (Day 26) | Day 26 |
| Scoped storage complexity (Android 11+) | File picker fails | Use modern APIs, test on Android 11+ (Day 9) | Day 9 |

---

## Success Criteria

**By Day 28:**
- ✅ APK builds without errors
- ✅ Installs on Samsung Galaxy S22 (emulator + real device)
- ✅ Users can upload April 2026.docx → see 24 shifts on calendar
- ✅ Filter by employee works
- ✅ Theme toggle works (light/dark)
- ✅ Data persists after app close
- ✅ No crashes on rotation or backgrounding
- ✅ APK size < 20 MB
- ✅ All 5 sample DOCX files parse successfully
- ✅ Private testing ready (shared with 2 users)

---

## Post-Launch (Phase 1.5 — Polish)

If time allows before private testing:
- [ ] Week view (timeline-based layout)
- [ ] Animated transitions (Fade, Slide, ScaleIn)
- [ ] Shift detail modal (show location, notes, edit placeholder)
- [ ] Advanced stats (shifts by type, weekly breakdown)
- [ ] Gestures (swipe to navigate months)
- [ ] 90%+ unit test coverage

---

## Phase 2 & Beyond

**Phase 2 (8 weeks):**
- Backend API integration (replace local Room with REST)
- Push notifications
- Multi-user support
- Shift creation/editing UI
- Photo upload (location proof)

**Phase 3 (4 weeks):**
- Play Store public release
- iOS port (Flutter)

**Phase 4:**
- Advanced features (recurring shifts, multi-location, reporting)

---

**Plan Status:** LOCKED  
**Last Updated:** [Now]  
**Owner:** [Rocket]
