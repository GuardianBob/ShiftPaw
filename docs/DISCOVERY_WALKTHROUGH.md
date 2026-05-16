# VetCalendar Android — Discovery Walkthrough

**Project:** Simplified Android native port of VetCalendar (Vue.js web app)  
**Timeline:** *To be determined during discovery*  
**Scope:** Phase 1 features only (file upload, schedule views, employee filtering, local storage)

---

## 1. PROJECT VISION & CONSTRAINTS

### 1.1 Why Android? What's the business driver?

- [ ] **Field staff need offline access** to schedules while traveling/at clinics?
- [ ] **Faster load times** on slow clinic WiFi vs. web?
- [ ] **Native notification support** for shift reminders?
- [ ] **Tablet-friendly** layout for check-in kiosks?
- [ ] **Push notifications** from backend when shifts change?
- [ ] **Other:** ________________

**User Question:** What's the PRIMARY pain point the Android app solves that the web version doesn't?

---

### 1.2 Target Users & Devices

**Primary user roles:**
- [ ] Veterinary clinic staff (receptionists, techs, vets)?
- [ ] Pet groomers?
- [ ] Kennel managers?
- [ ] Clinic managers (view + admin)?
- [ ] All of the above?

**Device targeting:**
- [ ] **Phone-only** (4.5"–6.7")?
- [ ] **Phone + Tablet** (7"–12")?
- [ ] **Foldable** support needed?

**Minimum Android version:**
- [ ] Android 10 (API 29)?
- [ ] Android 11 (API 30)?
- [ ] Android 12+ (API 31+)?

**User Question:** Who's using this on which device, and do they need offline-first or online-first behavior?

---

### 1.3 Phase 1 Scope Lock

**From REQUIREMENTS.md, PHASE 1 includes:**
- ✅ Upload DOCX shift schedule
- ✅ View Schedule (Month, Week, Day)
- ✅ Filter by employee(s)
- ✅ Search shifts
- ✅ Primary employee highlight
- ✅ Shift count stats
- ✅ Date picker for navigation
- ✅ Light/Dark/System theme toggle
- ✅ **Local storage only** (no backend)

**Out of scope for Phase 1:**
- ❌ Remote database integration
- ❌ API/backend CRUD
- ❌ Multi-user sync
- ❌ Push notifications
- ❌ Shift editing/creation (view-only)

**User Question:** Does this scope feel right, or are there features Phase 1 **must** have?

---

## 2. PLATFORM & ARCHITECTURE DECISIONS

### 2.1 Native vs. Cross-Platform

**Option A: Native Kotlin (Android-only)**
- ✅ Best performance, native Material Design 3 built-in
- ✅ Full access to device features (camera for DOCX upload, biometrics, etc.)
- ✅ Smallest APK size
- ❌ No code sharing with web
- ⏱️ ~6–10 weeks for Phase 1

**Option B: Jetpack Compose (Modern Native UI)**
- ✅ Kotlin + modern reactive UI framework
- ✅ Closer to web component model (easier for web devs to contribute)
- ✅ Material Design 3 built-in
- ⏱️ ~6–10 weeks for Phase 1

**Option C: Flutter (Cross-platform)**
- ✅ Code sharing potential (iOS later?)
- ✅ Single codebase
- ❌ Larger APK, slightly slower startup
- ✅ Material Design 3 support
- ⏱️ ~8–12 weeks for Phase 1

**Option D: React Native**
- ⚠️ Not recommended; inferior Material Design 3 support in RN ecosystem

**User Question:** **Native Kotlin + Jetpack Compose** recommended. Are you locked into iOS later, or Android-only for now?

---

### 2.2 Data Persistence Strategy

**Local Storage Options:**

| Option | Pros | Cons | Recommendation |
|--------|------|------|---|
| **SharedPreferences** | Simple, built-in, fast | Limited to small JSON; no querying | ❌ Not suitable for schedule data |
| **SQLite (Room ORM)** | Full ACID, queryable, reliable | ~2–3 MB overhead | ✅ **RECOMMENDED** |
| **DataStore (Jetpack)** | Modern, async-first, encrypted | Smaller data sets | ⚠️ Consider as supplement |
| **Realm** | Cross-platform ORM, fast | External dependency | ✅ Alternative if team familiar |

**Recommended:** **Room (SQLite) + DataStore**
- Room for shift schedules, employees, metadata
- DataStore for user preferences (theme, selected employee, filters)

**User Question:** Any existing database preference on Android, or should we standardize on Room?

---

### 2.3 File Upload & DOCX Parsing

**In Web version:**
- Vue.js form uploads `.docx` → Flask backend → `python-docx` library parses

**In Android, 3 approaches:**

1. **Parse on-device (Recommended for Phase 1)**
   - Use Apache POI (Java library) or `docx4j` library
   - No backend needed, offline-capable
   - User picks file → Android parses → Room inserts
   - ✅ Aligns with "local storage only"
   - ⏱️ Adds ~3–5 days to dev

2. **Upload to backend, download parsed data**
   - Requires backend availability
   - Backend returns JSON shift data
   - ❌ Violates "local storage only" phase constraint
   - Better for Phase 2

3. **Hybrid: Try on-device, fallback to server**
   - More complex; skip for Phase 1

**User Question:** Should we parse DOCX on-device in Phase 1, or do you have a backend ready?

---

## 3. UI/UX & DESIGN SYSTEM

### 3.1 Design System Implementation

**Web uses:**
- Tailwind CSS + Material Design 3 color tokens
- Plus Jakarta Sans (headlines), Be Vietnam Pro (body)
- 8px spacing scale
- Material Symbols Outlined icons

**Android equivalents:**
- **Material Design 3 for Jetpack Compose**
  - `androidx.compose.material3` library
  - Material Colors (auto dark mode support)
  - Material Typography
  - Material Icons (Material Symbols)
  
- **Custom color tokens** (from DESIGN.md)
  ```kotlin
  val Primary = Color(0xFF4F378A)          // Deep Purple
  val Secondary = Color(0xFF63597C)
  val Tertiary = Color(0xFF765B00)         // Gold
  val Error = Color(0xFFBA1A1A)
  // ... dark mode variants
  ```

**Typography mapping:**
- `displayLarge` → headline text
- `headlineSmall` → section headers
- `titleLarge` → card titles
- `bodyLarge` → shift details
- `labelSmall` → tags, chips

**User Question:** Do you want to replicate the exact Tailwind/web design in Compose, or adapt Material Design defaults?

---

### 3.2 Screen Hierarchy & Navigation

**Primary screens (Bottom Nav Bar):**

1. **Dashboard** (Home)
   - Today's shifts summary
   - Quick stats (selected employee, shift count)
   - Trending/upcoming

2. **Calendar** (Schedule)
   - Month view (default)
   - Week view
   - Day view
   - Date picker
   - Employee filter chips

3. **Employees**
   - List of all staff
   - Select as "primary"
   - Quick filter toggle

4. **Settings**
   - Theme (Light/Dark/System)
   - Clear data / Reset
   - About / Version

**Secondary screens:**
- **Shift Details** (tap card on Calendar → modal/detail screen)
- **File Upload** (FAB or Settings → pick DOCX → parse)

**User Question:** Should "Upload Schedule" be a FAB on Calendar screen, or a dedicated screen in nav?

---

### 3.3 Responsive Design

**Breakpoints:**
- **Phone portrait** (360–480 dp width): 1-column layout, compact spacing
- **Phone landscape** (480–640 dp): 2-column layout where applicable
- **Tablet** (720+ dp): 2–3 column layout, larger cards
- **Foldables** (outer/inner): Adapt to dynamic window size

**Safe areas:**
- System top/bottom insets (status bar, nav bar) handled by Compose automatically
- Floating Action Button avoids bottom nav (offset by ~80 dp)

**User Question:** Do you need tablet (7"+) support in Phase 1, or phone-first only?

---

## 4. TECHNICAL ARCHITECTURE

### 4.1 Project Structure

```
VetCalendar-Android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/vetcalendar/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── database/ (Room entities, DAOs)
│   │   │   │   │   ├── models/ (data classes)
│   │   │   │   │   └── repository/ (single source of truth)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/ (Jetpack Compose screens)
│   │   │   │   │   ├── components/ (reusable Compose components)
│   │   │   │   │   ├── theme/ (colors, typography)
│   │   │   │   │   └── navigation/ (NavHost, routes)
│   │   │   │   ├── viewmodel/ (StateHolder for each screen)
│   │   │   │   ├── utils/ (DOCX parser, helpers)
│   │   │   │   └── di/ (Dependency injection with Hilt)
│   │   │   └── res/
│   │   │       └── values/
│   │   │           └── strings.xml
│   │   └── test/
│   │       ├── unit/ (ViewModel, Parser tests)
│   │       └── instrumented/ (Room, UI tests)
│   └── build.gradle.kts
├── docs/
│   ├── DISCOVERY_WALKTHROUGH.md (this file)
│   ├── ARCHITECTURE.md
│   ├── DEVELOPMENT_PLAN.md
│   └── API_HANDOFF.md (for Phase 2 backend integration)
└── README.md
```

**Tech Stack:**
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Database:** Room (SQLite)
- **Architecture:** MVVM + Repository pattern
- **Dependency Injection:** Hilt
- **Navigation:** Jetpack Navigation Compose
- **Date/Time:** kotlinx-datetime or Java Time API
- **DOCX Parsing:** Apache POI or docx4j
- **Testing:** JUnit 5, MockK, Compose testing library

**User Question:** Are you comfortable with Kotlin/Jetpack ecosystem, or prefer a different tech stack?

---

### 4.2 Data Model (from web version)

**Calendar (Shift):**
```kotlin
@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,           // milliseconds since epoch
    val startTime: String,    // "07:00", "10:00", etc.
    val endTime: String,
    val userInitials: String, // "JD", "MS", etc.
    val location: String,     // "Main Yard", "Spa Wing", etc.
    val month: Int,           // for filtering/clearing
    val year: Int,
    val type: String?,        // shift type (optional)
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val initials: String, // "JD"
    val fullName: String?,
    val isPrimary: Boolean = false,
)
```

**User Preferences (DataStore):**
```kotlin
data class UserPrefs(
    val isDarkMode: Boolean?,      // null = system default
    val primaryEmployeeInitials: String?,
    val lastUploadedMonth: String?, // "Oct 2023"
)
```

**User Question:** Does this data model match your backend schema, or do we need to adjust for Phase 2 integration?

---

## 5. FEATURE BREAKDOWN

### 5.1 File Upload & DOCX Parsing

**Workflow:**
1. User taps FAB or navigates to Settings → Upload
2. System file picker opens
3. User selects `.docx` file
4. App extracts filename → detect month/year
5. Show date picker to confirm month (fallback if auto-detect fails)
6. Parse DOCX:
   - Read table structure (6 columns: dates, then employee initials)
   - Extract shift times (hardcoded: 07:00, 10:00, 14:00, 18:00)
   - Build Shift objects
7. Clear existing shifts for that month/year
8. Insert into Room
9. Show success toast + return to Calendar

**Tech:**
- Apache POI `org.apache.poi:poi:5.2.3` for DOCX parsing
- File picker: `androidx.activity.result.contract.ActivityResultContracts`

**Error handling:**
- Invalid DOCX structure → user-friendly error message
- Duplicate file upload → confirm overwrite
- Storage permissions → request at runtime (Android 6+)

**User Question:** Do you have sample DOCX files we can test parsing against?

---

### 5.2 Calendar Views (Month, Week, Day)

**Month View:**
- 7-column grid (M–S)
- Dates in cells
- Colored dots below date if shifts exist
- Tap date → drill down to day view

**Week View:**
- Horizontal scroll of 7 days
- Each day shows timeline of shifts
- Time slots on left (07:00, 10:00, 14:00, 18:00)
- Shift cards with employee initials + location

**Day View:**
- Vertical list of shifts for selected date
- Time, employee, location, actions (more menu)
- Same card design as web

**Tech:**
- No external calendar library needed (custom Compose layout)
- Date navigation: `kotlinx.datetime.LocalDate` or `java.time`
- Swipe to prev/next month/week (or buttons)

**User Question:** Should week view show time grid, or card-based?

---

### 5.3 Filtering & Search

**Filtering:**
- Chips for "All Staff" + individual employees
- Multi-select (tap to add/remove)
- Live update of calendar/list

**Search:**
- Text field in top app bar or Settings
- Filter by employee name or location
- Results shown as filtered list or calendar overlay

**Tech:**
- ViewModel holds `selectedEmployees: StateFlow<Set<String>>`
- Calendar query filters Room results

**User Question:** Should search be full-text, or just employee/location names?

---

### 5.4 Primary Employee Highlight & Stats

**Primary employee:**
- Chips or button in Calendar header
- User selects one employee
- Calendar highlights their shifts (different color or border)
- Settings screen shows stats:
  - Total shifts (all time)
  - Shifts by type/title (if captured in DOCX)
  - Upcoming shifts (next 7 days)

**Tech:**
- DataStore persists `primaryEmployeeInitials`
- ViewModel calculates stats from Room query

**User Question:** Should stats auto-calculate from file upload, or require explicit selection?

---

### 5.5 Theme & Dark Mode

**Options:**
1. **Light** (system light theme)
2. **Dark** (system dark theme)
3. **System default** (follows device setting)

**Tech:**
- Jetpack Compose Material3 `dynamicColorScheme()` (auto-generated from device wallpaper on Android 12+)
- Custom color set (from DESIGN.md) as fallback for older Android versions
- DataStore persists user choice
- `CompositionLocal` or ViewModel exposes theme state

**User Question:** Do you want Material You color extraction (Android 12+), or stick to custom brand colors?

---

## 6. DEPENDENCIES & LIBRARIES

**Core:**
- `androidx.compose.ui:ui:1.6+`
- `androidx.compose.material3:material3:1.1+`
- `androidx.lifecycle:lifecycle-viewmodel-compose:2.6+`
- `androidx.room:room-runtime:2.5+`
- `androidx.datastore:datastore-preferences:1.0+`
- `com.google.dagger:hilt-android:2.48+`

**DOCX Parsing:**
- `org.apache.poi:poi:5.2.3`
- `org.apache.xmlbeans:xmlbeans:5.1+`

**Date/Time:**
- `org.jetbrains.kotlinx:kotlinx-datetime:0.4+` (recommended)
- Or built-in `java.time` API (API 26+)

**Testing:**
- `junit:junit:4.13+`
- `io.mockk:mockk:1.13+`
- `androidx.room:room-testing:2.5+`
- `androidx.compose.ui:ui-test-junit4:1.6+`

**User Question:** Any corporate dependency restrictions (licensing, security scanning)?

---

## 7. TIMELINE & EFFORT ESTIMATES

### Phase 1 Development (Local Storage Only)

| Epic | Effort | Duration | Owner |
|------|--------|----------|-------|
| **Project Setup & Architecture** | 5 pts | 3–4 days | Lead |
| **Database Schema & Room** | 5 pts | 3–4 days | Backend |
| **UI Framework & Theme System** | 8 pts | 4–5 days | Frontend |
| **Calendar Views (Month/Week/Day)** | 13 pts | 5–6 days | Frontend |
| **File Upload & DOCX Parser** | 13 pts | 5–7 days | Full-stack |
| **Filtering & Search** | 8 pts | 3–4 days | Frontend |
| **Primary Employee & Stats** | 5 pts | 2–3 days | Backend |
| **Testing & Bug Fixes** | 13 pts | 5–7 days | QA/Full-stack |
| **Docs & Handoff** | 3 pts | 2 days | Tech Writer |
| **Total** | **73 pts** | **~6–7 weeks** | — |

**Assumptions:**
- 1 senior Android dev (or 2 part-time)
- No platform-specific gotchas
- Standard feature complexity
- 1–2 week buffer for unknown unknowns

**User Question:** What's your timeline constraint? Do 6–7 weeks work, or do you need faster/slower?

---

## 8. ACCEPTANCE CRITERIA & SUCCESS METRICS

### Must-Have (Phase 1):
- ✅ App loads, installs on Android 10+
- ✅ Upload `.docx` → parses without crashes
- ✅ Calendar displays shifts correctly
- ✅ Filter by employee works
- ✅ Dark mode toggle works
- ✅ Data persists after app close
- ✅ No UI crashes on rotation or app backgrounding
- ✅ APK size < 20 MB

### Nice-to-Have:
- 🎨 Animated transitions between views
- 🔍 Full-text search
- 📊 Advanced stats (shifts per type)
- ♿ Full WCAG 2.1 compliance
- 🧪 90%+ unit test coverage

### Post-Launch (Phase 2):
- 🌐 Remote sync via backend API
- 🔔 Push notifications for schedule changes
- ✏️ Shift editing/creation UI
- 👥 Multi-user support

**User Question:** Which nice-to-have features are critical for launch?

---

## 9. RISKS & MITIGATIONS

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|-----------|
| DOCX parsing library bloat (POI adds ~5 MB) | APK size grows | Medium | Evaluate minification, R8/Proguard rules, consider lightweight alternative (docx4j) |
| Room migration complexity (later phases) | Tech debt | Medium | Design schema carefully now; plan versioning strategy early |
| Complex filtering performance (large dataset) | UI lag | Low | Use Flow queries, implement pagination/virtualization if needed |
| Compose learning curve (if new team) | Onboarding time | Medium | Plan 2–3 day ramp-up; pair with experienced dev |
| File picker permission chaos (Android 11+ scoped storage) | Upload failures on some devices | Low | Test on multiple API levels; use modern APIs |

**User Question:** Are there other known risks (org, tech, timeline) I should flag?

---

## 10. QUESTIONS FOR YOU (DECISION POINTS)

**Must answer before starting development:**

1. **Technology:** Native Kotlin + Jetpack Compose (recommended), or other preference?
2. **Data Parsing:** Parse DOCX on-device (Phase 1), or assume backend ready?
3. **Device Support:** Phone-only or phone + tablet?
4. **Min Android Version:** API 29 (Android 10), 30, or 31+?
5. **Primary Features:** Any Phase 1 features must be cut, or add new ones?
6. **Timeline:** 6–7 weeks acceptable, or need faster/slower?
7. **Design:** Replicate web design exactly, or adapt to Material Design 3 norms?
8. **Theme:** Brand colors + custom theme, or Material You dynamic colors?
9. **Notifications:** Phase 1 or Phase 2? (affects architecture)
10. **Upload method:** FAB on Calendar, Settings menu, or dedicated Upload screen?

---

## Next Steps

1. **You fill in answers** to Section 1–10 above
2. **I create ARCHITECTURE.md** with detailed tech decisions
3. **I create DEVELOPMENT_PLAN.md** with sprint breakdown & stories
4. **We kick off Phase 1** with project setup + first sprint

---

**Document Status:** Draft — Awaiting stakeholder input  
**Last Updated:** [Now]  
**Owner:** [Your name / team lead]
