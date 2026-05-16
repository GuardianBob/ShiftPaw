# VetCalendar Android — Development Plan

**Phase 1 Duration:** 4 weeks (accelerated)  
**Team:** 1 senior Android dev (or 1 dev + 1 part-time)  
**Delivery:** Private testing APK on Galaxy S22 + emulator  
**Scope:** Local storage, offline-first, view-only

**Plan Status:** IN PROGRESS  
**Last Updated:** 2026-05-15  
**Owner:** Rocket + Mad Science Division

---

## Overall Progress

| Sprint | Status | Notes |
|--------|--------|-------|
| Sprint 1 — Foundation & Setup | ✅ **COMPLETE** | All tasks done |
| Sprint 2 — UI Scaffold & Calendar | ✅ **COMPLETE** | Reordered from original plan — DOCX parser skeleton delivered in Sprint 1 |
| Sprint 3 — DOCX Import Flow | ⬜ Not started | |
| Sprint 4 — Filtering, Stats & Polish | ⬜ Not started | |
| Sprint 5 — Integration, Testing & Release | ⬜ Not started | |

---

## Sprint Breakdown

### Sprint 1: Foundation & Setup (Days 1–5)
**Status: ✅ COMPLETE**

**Goal:** Project scaffolding, database schema, basic MVVM structure.

#### Tasks

**[Day 1] Project Setup & Android Studio Config**
- [x] Create new Android project (Kotlin, Jetpack Compose template) — app name: **ShiftPaw**
- [x] Configure `build.gradle.kts` with all dependencies
  - *Note: AGP downgraded 9.2.1 → 8.7.3 (Hilt incompatibility)*
  - *Note: KSP pinned to `2.1.20-1.0.32` (exact Kotlin prefix match required)*
  - *Note: `android.useAndroidX=true` added to `gradle.properties`*
  - *Note: `kotlin-android` plugin added (was missing; caused `kotlinOptions` error)*
- [x] Set up Hilt DI container (`@HiltAndroidApp` on `ShiftPawApplication`)
- [x] Create project folder structure (`data/`, `domain/`, `ui/`, `di/`, `util/`)
- [x] Apache POI packaging exclusions added (`META-INF/NOTICE.md`, `LICENSE.md`, `DEPENDENCIES`)
- [x] `build_run.bat` created for CLI builds (requires Android Studio JBR at `C:\Program Files\Android\Android Studio\jbr`)

**[Day 2–3] Database Schema & Room Setup**
- [x] Create `EmployeeEntity`, `ShiftEntity`, `ImportedScheduleEntity`
- [x] Create `EmployeeDao`, `ShiftDao`, `ImportedScheduleDao` (all with Flow returns)
- [x] Create `ShiftPawDatabase` (Room, version 1, `exportSchema = true`)
- [x] Create `ShiftRepository` (maps entities ↔ domain models, `getShiftsForMonth`, `getShiftsForDate`, `getActiveEmployees`, `importShifts`)
- [x] `DatabaseModule.kt` — Hilt `@Singleton` bindings for DB + all DAOs
- [ ] Unit test: DAO queries return correct data *(deferred to Sprint 5)*

**[Day 3–4] DataStore & UserPreferences**
- [x] Create `UserPreferencesRepository` (selected employee ID, last viewed month)
- [x] `preferences: Flow<UserPreferences>` with `setSelectedEmployee()` + `setLastViewedMonth()`
- [ ] Unit test: Read/write preferences *(deferred to Sprint 5)*

**[Day 5] Theme System & Color Tokens**
- [x] `ui/theme/Color.kt` — brand palette: Deep Purple `#6750A4` (primary), Gold `#C9A74D` (secondary)
- [x] `ui/theme/Theme.kt` — `ShiftPawTheme` composable, full light/dark `ColorScheme`, status bar tinting
- [ ] `ui/theme/Shape.kt` *(not yet created — using Material3 defaults)*
- [ ] `ui/theme/Typography.kt` *(using default — custom fonts deferred)*

**[Day 1] DOCX Parser Skeleton** *(pulled forward from original Sprint 2)*
- [x] `data/parser/DocxScheduleParser.kt` — Apache POI `XWPFDocument`, table parsing, shift code mapping (`D/E/N/OC`), default start/end times per shift type, `ParseResult` data class
- [x] `AndroidManifest.xml` — `READ_MEDIA_DOCUMENTS` permission

**Deliverable:** ✅ App builds (`BUILD SUCCESSFUL`, exit 0). Database schema created, theme system working, DI wired.

---

### Sprint 2: UI Scaffold & Calendar Views (Days 6–10)
**Status: ✅ COMPLETE**
*Note: Sprint reordered — full UI scaffold delivered here to unblock testing. Full DOCX import flow (file picker, upload screen, FAB) moved to Sprint 3.*

**Goal:** Working navigation skeleton + all 3 primary screens with real ViewModels.

#### Tasks

**[Day 6] Navigation Scaffold** — Branch: `sprint2/navigation`
- [x] `MainActivity.kt` — `@AndroidEntryPoint`, delegates to `ShiftPawNavHost()`
- [x] `ui/navigation/NavHost.kt` — Material3 `NavigationBar` with 3 tabs (Calendar / Employees / Settings), `saveState`/`restoreState` on tab switch, routes: `"calendar"`, `"employees"`, `"settings"`

**[Day 7–8] CalendarScreen + ViewModel** — Branch: `sprint2/calendar-screen`
- [x] `CalendarViewModel.kt` — `@HiltViewModel`, `flatMapLatest` on `_currentMonth: StateFlow<YearMonth>`, `shiftDays`, `selectedEmployeeId`, `employees`, `selectedDate`; `prevMonth()`/`nextMonth()` persist to DataStore
- [x] `CalendarScreen.kt` — month header + arrows, Mon–Sun row, 6×7 grid, employee color dots (max 3/cell), `FilterChip` employee bar, `ModalBottomSheet` day detail with shift list

**[Day 8] EmployeesScreen** — Branch: `sprint2/employees-screen`
- [x] `EmployeesViewModel.kt` — `stateIn` on `getActiveEmployees()`
- [x] `EmployeesScreen.kt` — `LazyColumn` with color avatar + name cards, FAB gated behind Snackbar ("Coming in Phase 2"), illustrated empty state

**[Day 9] SettingsScreen** — Branch: `sprint2/settings-screen`
- [x] `SettingsScreen.kt` — Import / Display / About sections, `HorizontalDivider` between, Import → Snackbar, Dark Mode `Switch`, version row ("ShiftPaw 1.0.0-beta")

**[Day 10] Integration** — Branch: `sprint2/integration`
- [x] All screens wired into `NavHost` (placeholders replaced)
- [x] Final build on `main`: **EXIT 0 — 2 seconds**

**Deliverable:** ✅ Full 3-screen navigation. Calendar grid renders with employee dot indicators. Month navigation works. Day detail sheet shows shift list.

---

### Sprint 3: DOCX Import Flow (Days 11–15)
**Status: ⬜ NOT STARTED**

*Original "Sprint 2" scope. File picker + import UI deferred here.*

**Goal:** Users can select a DOCX file → shifts parsed → saved to Room → appear on calendar.

#### Tasks

**[Day 11] File Picker Integration**
- [ ] `ActivityResultContracts.OpenDocument()` for DOCX (`application/vnd.openxmlformats-officedocument.wordprocessingml.document`)
- [ ] Scoped storage handling (Android 13+ — `READ_MEDIA_DOCUMENTS` already in manifest)
- [ ] Pass URI → `DocxScheduleParser.parse(inputStream, fileName)`

**[Day 12–13] Full Parser Implementation**
- [ ] Refine `DocxScheduleParser` against all 5 sample DOCX files in `samples/`
- [ ] Resolve employee names → DB IDs (create if not exists)
- [ ] Extract schedule month from filename with Hungarian month name fallback
- [ ] Handle duplicate import (confirm overwrite dialog)
- [ ] Test against all 5 samples: `January 2026.docx` through `May 2026.docx`

**[Day 13–14] Import Screen UI**
- [ ] `ui/screens/import/ImportScheduleScreen.kt` — file picker trigger, month/year confirmation dialog, loading indicator, success/error states
- [ ] Wire into `SettingsScreen` "Import Schedule" row (replace Snackbar with navigation)
- [ ] Add route `"import"` to `NavHost`

**[Day 15] FAB on Calendar + End-to-End Test**
- [ ] FAB on `CalendarScreen` → navigate to import screen
- [ ] Post-import: calendar auto-refreshes (Flow-driven, should be automatic)
- [ ] Integration test: upload Jan 2026 → shifts appear on calendar

**Deliverable:** Users can import DOCX files. All 5 sample files parse and display correctly on calendar.

---

### Sprint 4: Filtering, Stats & Polish (Days 16–22)
**Status: ⬜ NOT STARTED**

**Goal:** Multi-select employee filter, stats, date picker, search, theme persistence, polish.

#### Tasks

**[Day 16–17] Employee Multi-Select Filter**
- [ ] Upgrade `FilterChip` bar to multi-select (`Set<Long>` in ViewModel)
- [ ] Calendar grid filters shifts to selected employees only
- [ ] Persist selection to DataStore
- [ ] "All Staff" chip deselects individuals

**[Day 17–18] Employee Stats**
- [ ] `EmployeesScreen` cards expand to show: total shifts, shifts this month, next shift date
- [ ] Stats sourced from `ShiftRepository` aggregate queries
- [ ] Primary employee designation stored in DataStore

**[Day 19] Dashboard Screen** *(new nav tab or home route)*
- [ ] Today's shifts summary
- [ ] Upcoming shifts (next 3 days)
- [ ] Quick stats card for primary employee

**[Day 20] Date Picker Navigation**
- [ ] Material3 `DatePickerDialog` scoped to month/year selection
- [ ] Tap month header → jump to any month
- [ ] Persist to DataStore via ViewModel

**[Day 21] Search**
- [ ] Search icon in top app bar → `TextField` overlay
- [ ] Filter by employee name
- [ ] Results filter calendar grid in real time

**[Day 22] Theme Persistence & Polish**
- [ ] Dark Mode `Switch` in Settings actually toggles `ShiftPawTheme(darkTheme=...)`
- [ ] Persist theme choice to DataStore
- [ ] Animated transitions (Fade, Slide) between screens
- [ ] "Clear All Data" in Settings (confirm dialog → wipe Room + DataStore)

**Deliverable:** Full filtering, stats, date navigation, search, and theme toggle working end-to-end.

---

### Sprint 5: Testing, Optimization & Release (Days 23–28)
**Status: ⬜ NOT STARTED**

**Goal:** Tests, APK optimization, documentation, private release.

#### Tasks

**[Day 23–24] Testing**
- [ ] Unit tests: `CalendarViewModel`, `ShiftRepository`, `DocxScheduleParser`
- [ ] Integration tests: Room DAO queries, DataStore read/write
- [ ] UI tests: calendar renders, tab navigation, filter chips
- [ ] Test on Samsung Galaxy S22 emulator + real device

**[Day 25] End-to-End Testing**
- [ ] Full workflow: upload → calendar → filter → select employee → toggle theme → rotate → close/reopen
- [ ] All 5 sample DOCX files parse successfully
- [ ] Edge cases: empty month, duplicate import, low storage

**[Day 26–27] APK Optimization**
- [ ] Minification (R8/Proguard) — target APK < 20 MB
- [ ] Profile calendar render time (target < 200 ms)
- [ ] Profile DOCX parse time (target < 2 sec)
- [ ] Memory profiling (target < 100 MB heap)

**[Day 27–28] Documentation & Release**
- [ ] `README.md` — build instructions, emulator setup, APK install
- [ ] `RELEASE_NOTES.md` — Phase 1 feature list
- [ ] `TESTING_CHECKLIST.md` — QA guide for testers
- [ ] Release APK → share with 2 testers on Galaxy S22

**Deliverable:** Release-ready APK. Private testing with 2 users.

---

## Accelerated Timeline Assumptions

| Task | Duration | Notes |
|------|----------|-------|
| **Project setup** | 1 day | ✅ Done |
| **Room schema** | 2 days | ✅ Done |
| **DOCX parser** | 4 days | Skeleton done; full implementation Sprint 3 |
| **UI scaffold** | 3 days | ✅ Done (Sprint 2) |
| **Calendar/filter UI** | 4 days | Calendar done; filter enhancements Sprint 4 |
| **Import flow** | 3 days | Sprint 3 |
| **Stats/search/polish** | 4 days | Sprint 4 |
| **Testing** | 3 days | Sprint 5 |
| **Polish/docs** | 2 days | Sprint 5 |
| **Total** | **28 days** | ~4 weeks (5-day work weeks) |

---

## Feature Checklist (Phase 1)

- [ ] Upload DOCX file *(Sprint 3)*
- [ ] Parse shifts (extract employee names, times, dates) *(Sprint 3)*
- [x] Save to Room database *(schema + repo complete)*
- [x] View calendar (month view) *(Sprint 2)*
- [ ] View calendar (week, day) *(Sprint 4 — optional)*
- [ ] Filter by employee (single + multi-select) *(Sprint 4)*
- [ ] Search by employee name *(Sprint 4)*
- [ ] Select primary employee *(Sprint 4)*
- [ ] Display stats (total shifts, by month) *(Sprint 4)*
- [ ] Theme toggle (light/dark/system) *(Sprint 4)*
- [ ] Date picker (jump to month) *(Sprint 4)*
- [x] Bottom navigation (Calendar, Employees, Settings) *(Sprint 2)*
- [ ] Dashboard screen *(Sprint 4)*
- [x] Employees screen (list) *(Sprint 2 — read-only)*
- [x] Settings screen (scaffold) *(Sprint 2 — non-functional toggles)*
- [x] Data persistence (Room + DataStore) *(Sprint 1)*
- [x] Offline-first (no network required) *(Sprint 1)*
- [ ] Error handling (file errors, storage errors) *(Sprint 3)*
- [ ] Testing (unit, integration, UI) *(Sprint 5)*
- [ ] Documentation (README, TESTING_CHECKLIST) *(Sprint 5)*

---

## Known Unknowns & Risks

| Risk | Impact | Mitigation | Timeline |
|------|--------|-----------|----------|
| DOCX structure varies by file | Parser breaks | Test against all 5 samples early (Sprint 3) | Day 12–13 |
| Apache POI APK bloat | Exceeds 20 MB limit | Minification + resource stripping (Sprint 5) | Day 26 |
| Room query performance (large dataset) | Calendar lags | Indexing already on `date` + `employeeId` columns | Mitigated |
| Samsung Galaxy S22 compatibility | Device crashes | Test early on emulator | Day 23 |
| Scoped storage complexity (Android 13+) | File picker fails | `READ_MEDIA_DOCUMENTS` permission already added | Day 11 |
| Gradle/JDK fragility | CLI builds fail | `build_run.bat` sets `JAVA_HOME` to AS JBR | ✅ Solved |

---

## Success Criteria

**By Day 28:**
- ✅ APK builds without errors *(achieved Sprint 1)*
- [ ] Installs on Samsung Galaxy S22 (emulator + real device)
- [ ] Users can upload DOCX → see shifts on calendar
- [ ] Filter by employee works
- [ ] Theme toggle works (light/dark)
- ✅ Data persists after app close *(Room + DataStore wired)*
- [ ] No crashes on rotation or backgrounding
- [ ] APK size < 20 MB
- [ ] All 5 sample DOCX files parse successfully
- [ ] Private testing ready (shared with 2 users)

---

## Post-Launch (Phase 1.5 — Polish)

If time allows before private testing:
- [ ] Week view (timeline-based layout)
- [ ] Animated transitions (Fade, Slide, ScaleIn)
- [ ] Shift detail modal (show notes, edit placeholder)
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
