# VetCalendar Android — Architecture Decision Record

**Project:** Native Android (Kotlin + Jetpack Compose)  
**Target:** Android 13+ (optimize for Samsung Galaxy S22)  
**Timeline:** **4 weeks** (accelerated Phase 1)  
**Scope:** Local storage, offline-first, view-only

---

## FINAL DECISIONS (LOCKED)

### Technology Stack

| Component | Choice | Rationale |
|-----------|--------|-----------|
| **Language** | Kotlin | Industry standard, null-safe, web dev-friendly |
| **UI Framework** | Jetpack Compose | Declarative like Vue, Material3 built-in, fastest to iterate |
| **Database** | Room (SQLite) | Queryable, reliable, zero setup friction |
| **Preferences** | DataStore | Async-first, encrypted, modern replacement for SharedPreferences |
| **DI** | Hilt | Minimal boilerplate, auto-wired, standard in modern Android |
| **Navigation** | Jetpack Navigation Compose | Built for Compose, handles back stack automatically |
| **DOCX Parsing** | Apache POI (lightweight) | Proven, handles table structures, ~5 MB after minification |
| **Date/Time** | `java.time` API | Built-in (Android 13+), no dependencies |
| **Testing** | JUnit 5 + Mockk | Standard, minimal setup |

---

## Architecture Pattern: MVVM + Repository

```
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer (Compose)                      │
│  (DashboardScreen, CalendarScreen, EmployeesScreen, etc.)   │
└──────────────────────┬──────────────────────────────────────┘
                       │ observes
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              ViewModel (State Management)                     │
│  (DashboardVM, CalendarVM, EmployeesVM, SettingsVM)         │
│  Holds: StateFlow<ScreenState>, StateFlow<UiEvent>          │
└──────────────────────┬──────────────────────────────────────┘
                       │ calls
                       ▼
┌─────────────────────────────────────────────────────────────┐
│               Repository (Business Logic)                    │
│  (ShiftRepository, EmployeeRepository, PreferencesRepository)|
│  Single source of truth for data                            │
└──────────────────────┬──────────────────────────────────────┘
                       │ accesses
                       ▼
┌─────────────────────────────────────────────────────────────┐
│               Data Layer (Room + DataStore)                  │
│  (ShiftDao, EmployeeDao, DataStorePreferences)             │
│  Persistent local storage                                   │
└─────────────────────────────────────────────────────────────┘
```

**Why MVVM?**
- Clear separation of concerns
- ViewModels survive configuration changes (rotation)
- Testable: ViewModel logic independent of UI
- StateFlow is reactive like Vue's reactive refs

---

## Data Model

### Entities (Room)

```kotlin
// shifts table
@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,              // LocalDate.toEpochDay() * 86400000
    val startTime: String,       // "07:00"
    val endTime: String,         // "10:00"
    val userInitials: String,    // "JD"
    val location: String,        // "Main Yard"
    val month: Int,              // 4 (for filtering)
    val year: Int,               // 2026
    val type: String? = null,    // "kennel", "grooming", etc. (optional)
    val notes: String? = null,   // for future use
)

// employees table
@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val initials: String,  // "JD"
    val fullName: String? = null,      // "Jane Doe"
    val isPrimary: Boolean = false,    // selected as primary
    val color: String? = null,         // hex color for UI (future)
)
```

### User Preferences (DataStore)

```kotlin
data class UserPreferences(
    val isDarkMode: Boolean? = null,        // null = system default
    val primaryEmployeeInitials: String? = null,
    val lastUploadedMonth: String? = null,  // "Apr-2026" for deduplication
)
```

### View Models: StateFlow (Observable State)

```kotlin
// Example: CalendarViewModel
data class CalendarUiState(
    val shifts: List<Shift> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedEmployees: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class CalendarViewModel(
    private val shiftRepository: ShiftRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    fun filterByEmployees(employees: Set<String>) {
        // Update selectedEmployees
        // Repository queries shifts filtered by set
        // Flow emits new shifts
    }

    fun selectDate(date: LocalDate) {
        // Update selectedDate
        // Repository loads shifts for that date
        // Flow emits new state
    }
}
```

---

## Database Schema

### Shifts Table

```sql
CREATE TABLE shifts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date INTEGER NOT NULL,
    startTime TEXT NOT NULL,
    endTime TEXT NOT NULL,
    userInitials TEXT NOT NULL,
    location TEXT NOT NULL,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    type TEXT,
    notes TEXT,
    UNIQUE(date, startTime, userInitials)  -- Prevent duplicates
);

CREATE INDEX idx_shifts_month_year ON shifts(month, year);
CREATE INDEX idx_shifts_userInitials ON shifts(userInitials);
CREATE INDEX idx_shifts_date ON shifts(date);
```

### Employees Table

```sql
CREATE TABLE employees (
    initials TEXT PRIMARY KEY,
    fullName TEXT,
    isPrimary BOOLEAN DEFAULT 0,
    color TEXT
);
```

### Queries

**Get all shifts for a month:**
```sql
SELECT * FROM shifts WHERE month = ? AND year = ?
ORDER BY date, startTime;
```

**Get shifts filtered by employees:**
```sql
SELECT * FROM shifts 
WHERE month = ? AND year = ? AND userInitials IN (?, ?, ...)
ORDER BY date, startTime;
```

**Get shifts for a single date:**
```sql
SELECT * FROM shifts 
WHERE date = ? 
ORDER BY startTime;
```

**Get all employees:**
```sql
SELECT DISTINCT userInitials FROM shifts
UNION
SELECT initials FROM employees;
```

---

## DOCX Parser: Data Flow

**Input:** User selects April 2026.docx  
**Output:** 24 Shift objects inserted into Room

```
┌──────────────────────────┐
│ File Picker              │
│ (System file browser)    │
└────────────┬─────────────┘
             │ File URI
             ▼
┌──────────────────────────┐
│ DocxParser.parse()       │
│ Apache POI reads table   │
└────────────┬─────────────┘
             │ List<Shift>
             ▼
┌──────────────────────────┐
│ Room DAO Insert          │
│ insertShifts(shifts)     │
└────────────┬─────────────┘
             │ Success
             ▼
┌──────────────────────────┐
│ Repository Flow updates  │
│ Calendar UI re-renders   │
└──────────────────────────┘
```

### Parser Implementation (Kotlin pseudocode)

```kotlin
object DocxParser {
    fun parseShifts(fileUri: Uri, context: Context): List<Shift> {
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val workbook = XSSFWorkbook(inputStream)  // POI
        val sheet = workbook.getSheetAt(0)
        
        val shifts = mutableListOf<Shift>()
        
        // Row 0: month/year (skip)
        // Row 1: day headers (skip)
        // Rows 2+: shift data
        
        for (rowIdx in 2 until sheet.physicalNumberOfRows) {
            val row = sheet.getRow(rowIdx)
            
            // Extract date from first cell
            val dayString = row.getCell(0).stringCellValue // "1", "2", etc.
            val date = LocalDate.of(year, month, dayString.toInt())
            
            // Extract employee initials from cells 1–4 (one per shift slot)
            val shiftSlots = listOf("07:00–10:00", "10:00–14:00", "14:00–18:00", "18:00–22:00")
            for (cellIdx in 1..4) {
                val cellValue = row.getCell(cellIdx).stringCellValue.trim()
                if (cellValue.isNotEmpty() && cellValue.length == 2) {
                    shifts.add(
                        Shift(
                            date = date.toEpochDay(),
                            startTime = shiftSlots[cellIdx - 1].split("–")[0],
                            endTime = shiftSlots[cellIdx - 1].split("–")[1],
                            userInitials = cellValue,
                            location = "To be determined",  // or parse from metadata
                            month = month,
                            year = year,
                        )
                    )
                }
            }
        }
        
        return shifts
    }
}
```

---

## UI Component Hierarchy

```
MainScreen
├── BottomNavigation
│   ├── Dashboard
│   ├── Calendar (default)
│   ├── Employees
│   └── Settings
│
└── Content Area (NavHost)
    ├── DashboardScreen
    │   ├── TodayShiftsCard
    │   ├── StatCard (selected employee)
    │   └── UpcomingShiftsList
    │
    ├── CalendarScreen
    │   ├── TopAppBar (title, search, theme toggle)
    │   ├── EmployeeFilterChips
    │   ├── MonthViewCalendarGrid
    │   │   └── DateCell (with shift indicators)
    │   ├── ShiftsList (for selected date)
    │   │   └── ShiftCard (with tap handler)
    │   └── FAB (Upload Schedule)
    │
    ├── EmployeesScreen
    │   ├── EmployeeListItem (with primary toggle)
    │   └── EmployeeStatsCard (shifts count)
    │
    └── SettingsScreen
        ├── ThemeSelector
        ├── ClearDataButton
        └── AboutSection
```

### Reusable Composables

```kotlin
// Components in ui/components/

ShiftCard(
    shift: Shift,
    isHighlighted: Boolean,
    onTap: () -> Unit,
)

EmployeeFilterChips(
    employees: List<Employee>,
    selected: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
)

MonthViewCalendarGrid(
    shifts: List<Shift>,
    month: Int,
    year: Int,
    onDateSelected: (LocalDate) -> Unit,
)

ShiftDetailModal(
    shift: Shift,
    onDismiss: () -> Unit,
)

ThemeToggle(
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
)
```

---

## Navigation Routes

```kotlin
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Calendar : Screen("calendar")
    object CalendarDetail : Screen("calendar/{date}") // drill-down detail
    object Employees : Screen("employees")
    object Settings : Screen("settings")
    object UploadSchedule : Screen("upload_schedule")
}

// NavHost setup in MainActivity
NavHost(navController = navController, startDestination = Screen.Calendar.route) {
    composable(Screen.Dashboard.route) { DashboardScreen() }
    composable(Screen.Calendar.route) { CalendarScreen() }
    composable(Screen.Employees.route) { EmployeesScreen() }
    composable(Screen.Settings.route) { SettingsScreen() }
    composable(Screen.UploadSchedule.route) { UploadScheduleScreen() }
}
```

---

## Error Handling Strategy

### File Upload Errors

| Error | Cause | User Message | Recovery |
|-------|-------|--------------|----------|
| File not found | Invalid file URI | "File not accessible" | Pick file again |
| Invalid structure | Wrong DOCX format | "File is not a valid schedule" | Check file format |
| Parse failure | Corrupted file | "Could not read schedule data" | Pick different file |
| Duplicate month | Already imported | "April 2026 already exists. Overwrite?" | Confirm or cancel |
| Storage full | Device storage | "Not enough space" | Clear data / free space |

### UI Error Display

- **Toast:** Quick feedback for successful operations ("Imported 24 shifts")
- **Snackbar:** Dismissible alerts with action ("Error: Retry" button)
- **Alert Dialog:** Blocking error ("Invalid file format. Pick another?")
- **Validation errors:** Inline field errors

---

## Performance Considerations

### APK Size Target: < 20 MB

| Component | Estimated Size | Mitigation |
|-----------|---|---|
| Base app | 2–3 MB | Minimal |
| Compose runtime | 3–4 MB | Standard overhead |
| Material3 | 1–2 MB | Standard overhead |
| Room + SQLite | 2–3 MB | Standard overhead |
| Apache POI (minified) | 4–5 MB | Use R8 Proguard rules to strip unused deps |
| Resources (fonts, icons) | 1–2 MB | Compress, use vector drawables |
| **Total** | **~13–15 MB** | ✅ Well under 20 MB |

### Query Optimization

- **Indexed columns:** `month`, `year`, `date`, `userInitials`
- **Queries use Flow:** Automatic requery on data change
- **Pagination:** If > 1000 shifts, load by month (not all-at-once)
- **Room caching:** Memory cache of recent queries

### Memory Optimization

- Use `@Stable` annotations on data classes for Compose recomposition
- Lazy load shift details (not all shifts in memory)
- Clear old months from database (settings → "Clean Old Data")

---

## Security Considerations

### Data Storage

- Room database is **unencrypted by default** (local-only testing)
- **Phase 2:** Encrypt with SQLCipher if backend integration planned
- DataStore uses **automatic encryption** (no action needed)

### File Handling

- Use **Scoped Storage** (Android 11+) for file access
- No raw filesystem access needed
- System file picker restricts to user's Downloads/Documents

### Permissions

**Required:**
- `READ_EXTERNAL_STORAGE` (or scoped storage equivalent)

**Not required:**
- Network (offline-only Phase 1)
- Camera (no biometrics yet)
- Location (no geo features)

---

## Testing Strategy

### Unit Tests (ViewModel + Parser)

```kotlin
// CalendarViewModelTest
@Test
fun filterByEmployees_updatesShifts() {
    val vm = CalendarViewModel(mockRepository)
    vm.filterByEmployees(setOf("JD"))
    
    val state = vm.uiState.value
    assert(state.shifts.all { it.userInitials == "JD" })
}

// DocxParserTest
@Test
fun parseShifts_extractsCorrectData() {
    val testFile = File("sample_april_2026.docx")
    val shifts = DocxParser.parseShifts(testFile.toUri(), context)
    
    assert(shifts.size == 24)
    assert(shifts.any { it.userInitials == "JD" })
}
```

### Integration Tests (Room + ViewModel)

```kotlin
// CalendarRepositoryTest
@Test
fun insertShifts_queriesReturnsData() {
    val repo = ShiftRepository(testDatabase.shiftDao())
    repo.insertShifts(testShifts)
    
    val results = repo.getShiftsByMonth(4, 2026).first()
    assert(results.size == testShifts.size)
}
```

### UI Tests (Compose)

```kotlin
// CalendarScreenTest
@Test
fun calendarScreen_displaysDates() {
    composeTestRule.setContent {
        CalendarScreen(viewModel = mockViewModel)
    }
    
    composeTestRule.onNodeWithText("April 2026").assertExists()
}
```

---

## Deployment Checklist

- [ ] Proguard minification configured, APK < 20 MB
- [ ] App tested on Samsung Galaxy S22 + emulator
- [ ] All strings externalized (strings.xml for i18n)
- [ ] Light + dark theme verified
- [ ] File upload tested with all 5 sample DOCX files
- [ ] Rotation (portrait/landscape) tested
- [ ] Low-memory device stress tested
- [ ] Play Store listing prepared (Phase 3)
- [ ] Privacy policy drafted

---

## Design System Implementation

### Colors (Compose)

```kotlin
// ui/theme/Color.kt
val Primary = Color(0xFF4F378A)              // Deep Purple
val OnPrimary = Color(0xFFFFFFFF)            // White
val PrimaryContainer = Color(0xFF6750A4)    
val OnPrimaryContainer = Color(0xFFE0D2FF)

val Secondary = Color(0xFF63597C)            // Gray Purple
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFE1D4FD)
val OnSecondaryContainer = Color(0xFF645A7D)

val Tertiary = Color(0xFF765B00)             // Gold
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFC9A74D)
val OnTertiaryContainer = Color(0xFF503D00)

val Surface = Color(0xFFFDF7FF)
val OnSurface = Color(0xFF1D1B20)
val SurfaceVariant = Color(0xFFE6E0E9)

val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)

// Dark theme variants
val DarkPrimary = Color(0xFFCFBCFF)
val DarkSurface = Color(0xFF1D1B20)
// ... etc
```

### Typography (Compose)

```kotlin
// ui/theme/Typography.kt
val PetShiftTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,  // Plus Jakarta Sans (imported)
        fontSize = 57.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 64.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 32.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 40.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 28.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Serif,  // Be Vietnam Pro (imported)
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
    ),
    // ... etc
)
```

### Shapes (Compose)

```kotlin
// ui/theme/Shape.kt
val PetShiftShapes = Shapes(
    small = RoundedCornerShape(4.dp),      // 0.25rem
    medium = RoundedCornerShape(8.dp),     // 0.5rem
    large = RoundedCornerShape(12.dp),     // 0.75rem → cards
    extraLarge = RoundedCornerShape(16.dp), // 1rem
)
```

---

## Build Configuration (build.gradle.kts)

```kotlin
android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vetcalendar.android"
        minSdk = 33  // Android 13 (target Galaxy S22)
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true  // Strip unused resources
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // Compose
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    // Room
    implementation("androidx.room:room-runtime:2.5.2")
    kapt("androidx.room:room-compiler:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.1")

    // DOCX Parsing
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.xmlbeans:xmlbeans:5.1.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
}
```

---

## Proguard Rules (proguard-rules.pro)

```proguard
# Keep Room entities
-keepclasseswithmembers class com.vetcalendar.data.database.** {
    <init>(...);
    public <fields>;
}

# Keep Hilt generated code
-keepclasseswithmembers class **_HiltModules {
    *;
}

# Keep Composables
-keepclasseswithmembers class **${'$'}ComposableSingletons {
    *;
}

# Minimize POI
-dontwarn org.apache.poi.**
-keep class org.apache.poi.** { *; }
```

---

## Summary

| Aspect | Decision |
|--------|----------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material3 |
| **Database** | Room (SQLite) |
| **Preferences** | DataStore |
| **Architecture** | MVVM + Repository |
| **Parsing** | Apache POI (on-device) |
| **Min Android** | 13 (API 33) |
| **Target** | Galaxy S22 (API 33–34) |
| **Timeline** | 4 weeks |
| **APK Size** | < 20 MB (target: 13–15 MB) |
| **Testing** | JUnit 5 + Mockk + Compose tests |

---

**Document Status:** LOCKED  
**Last Updated:** [Now]  
**Owner:** [Rocket]
