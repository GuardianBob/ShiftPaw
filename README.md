# ShiftPaw 🐾 v1.1

A native Android application for veterinary clinic staff to view, search, and filter work schedules. ShiftPaw simplifies schedule management by allowing vets to upload distributed DOCX-based schedules and access them through an intuitive, mobile-friendly interface.

**Status:** First Android app prototype (Phase 1 complete)  
**Target:** Android 13+ (API 33+)  
**Architecture:** MVVM + Clean Architecture with Jetpack Compose

---

## 🎯 Purpose

ShiftPaw addresses a common pain point in a local veterinary clinic: **schedule fragmentation**. Instead of managing schedules across multiple DOCX files, emails, or printed sheets, vets can:

1. **Upload** monthly DOCX schedule files
2. **View** shifts in an interactive calendar
3. **Filter** by employee
4. **Search** for specific shifts (*future feature*)
5. **Track** shift counts and types for a primary employee (*future feature*)

This is an **interim solution** designed to bridge the gap between distributed DOCX schedules and a future backend-driven system.

---

## ✨ Features

### Phase 1 (Current) ✅

#### Schedule Import
- **Upload DOCX files** with veterinary clinic schedules
- **Auto-detect month** from filename (e.g., `May_2024.docx`) (~80% accurate)
- **Parse shift data** from 14-column table layout
- **Extract employee initials** (e.g., "JD", "MS") as employee names
- **Support multiple shift types**: Day, Evening (Swing 1), On-Call (Swing 2), Night
- **Hardcoded shift times**:
  - Day: 07:00–19:00
  - Evening: 10:00–22:00
  - On-Call: 14:00–02:00
  - Night: 18:00–06:00

#### Calendar View
- **Month view** with interactive calendar grid
- **Shift display** showing employee initials and shift type
- **Month navigation** (Previous, Next, Today buttons)
- **Date picker** to jump to specific months
- **Responsive layout** adapting to phone screen sizes

#### Employee Management
- **View all employees** in the schedule
- **Set primary employee** for highlighting
- **Filter shifts** by single or multiple employees
- **Shift statistics** for primary employee: (*future feature*)
  - Total shift count
  - Breakdown by shift type

#### Theme Support
- **Light mode** (default)
- **Dark mode** (manual toggle)
- **System default** detection (respects device theme preference)
- **Persistent theme preference** via DataStore

#### Navigation
- **Bottom navigation** between screens:
  - Calendar (schedule view)
  - Employees (staff management)
  - Settings (theme, preferences)

---

## 🏗️ Architecture

### Tech Stack

| Layer | Technology |
|-------|-----------|
| **UI** | Jetpack Compose, Material Design 3 |
| **Navigation** | Jetpack Navigation Compose |
| **State Management** | ViewModel + StateFlow |
| **Local Storage** | Room Database, DataStore Preferences |
| **Dependency Injection** | Hilt |
| **File Parsing** | Apache POI (DOCX parsing) |
| **Language** | Kotlin |
| **Build System** | Gradle (Kotlin DSL) |

### Project Structure

```
android/app/src/main/java/com/example/shiftpaw/
├── data/
│   ├── local/
│   │   ├── dao/                    # Room DAOs
│   │   │   ├── EmployeeDao.kt
│   │   │   ├── ShiftDao.kt
│   │   │   └── ImportedScheduleDao.kt
│   │   ├── database/
│   │   │   └── ShiftPawDatabase.kt # Room database definition
│   │   └── entity/                 # Database entities
│   │       ├── EmployeeEntity.kt
│   │       ├── ShiftEntity.kt
│   │       └── ImportedScheduleEntity.kt
│   ├── parser/
│   │   └── DocxScheduleParser.kt   # DOCX file parsing logic
│   └── repository/
│       ├── ShiftRepository.kt      # Shift data access
│       ├── EmployeeResolver.kt     # Employee resolution
│       └── UserPreferencesRepository.kt # Theme & settings
├── domain/
│   └── model/
│       ├── Shift.kt                # Shift domain model
│       ├── ShiftType.kt            # Shift type enum
│       ├── Employee.kt             # Employee domain model
│       └── ShiftDay.kt             # Daily shift aggregation
├── ui/
│   ├── screens/
│   │   ├── calendar/
│   │   │   ├── CalendarScreen.kt   # Month view UI
│   │   │   └── CalendarViewModel.kt
│   │   ├── employees/
│   │   │   ├── EmployeesScreen.kt  # Staff list & filtering
│   │   │   └── EmployeesViewModel.kt
│   │   ├── importschedule/
│   │   │   ├── ImportScheduleScreen.kt # File upload UI
│   │   │   └── ImportViewModel.kt
│   │   └── settings/
│   │       ├── SettingsScreen.kt   # Theme & preferences
│   │       └── ThemeViewModel.kt
│   ├── navigation/
│   │   └── NavHost.kt              # Navigation graph
│   └── theme/
│       ├── Color.kt                # Color palette
│       ├── Theme.kt                # Compose theme
│       └── Type.kt                 # Typography
├── di/
│   └── DatabaseModule.kt           # Hilt dependency injection
├── MainActivity.kt                 # App entry point
└── ShiftPawApplication.kt          # Application class
```

### Data Flow

```
DOCX File
    ↓
[ImportScheduleScreen] → User selects file
    ↓
[ImportViewModel] → Triggers parsing
    ↓
[DocxScheduleParser] → Parses DOCX table
    ↓
[ShiftRepository] → Saves to Room database
    ↓
[Room Database] → Persists shifts & employees
    ↓
[CalendarViewModel] → Queries shifts for date range
    ↓
[CalendarScreen] → Displays month view
```

### Key Components

#### DocxScheduleParser
Parses veterinary clinic DOCX schedules with a specific 14-column layout:
- **Columns 0, 2, 4, ...** = Date numbers
- **Columns 1, 3, 5, ...** = Employee initials
- **Header row** = Weekday names (skipped)
- **Date row** = Month/year reference
- **Shift rows** = Shift type + employee assignments

Supports shift types: Day, Swing 1, Swing 2, Night  
Supports multiple languages (English, Hungarian month names)

#### Room Database
Three main entities:
- **ShiftEntity** — Individual shift records (date, time, employee, type)
- **EmployeeEntity** — Staff members (name, initials)
- **ImportedScheduleEntity** — Metadata about uploaded schedules

#### ViewModels
- **CalendarViewModel** — Manages calendar state, shift queries, filtering
- **EmployeesViewModel** — Manages employee list, primary employee selection
- **ImportViewModel** — Handles file selection and parsing
- **ThemeViewModel** — Manages light/dark mode preference

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (latest stable)
- **Android SDK** 35 (compileSdk)
- **Kotlin** 1.9+
- **Java 11+**

### Build & Run

1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd ShiftPaw
   ```

2. **Open in Android Studio:**
   ```bash
   cd android
   # Open in Android Studio
   ```

3. **Build the app:**
   ```bash
   ./gradlew build
   ```

4. **Run on emulator or device:**
   ```bash
   ./gradlew installDebug
   ```

### Gradle Build Configuration

- **Min SDK:** 33 (Android 13)
- **Target SDK:** 35 (Android 15)
- **Compile SDK:** 35
- **Java Target:** 11
- **Kotlin JVM Target:** 11

---

## 📱 Usage

### Uploading a Schedule

1. Navigate to the **Import** tab
2. Tap **Select File** and choose a DOCX schedule
3. Confirm the detected month (or adjust via date picker)
4. Tap **Upload** — shifts are parsed and saved

### Viewing the Calendar

1. Navigate to the **Calendar** tab
2. Use **Previous/Next** buttons to navigate months
3. Tap a date to see shifts for that day
4. Tap a shift to view details

### Filtering by Employee

1. Navigate to the **Employees** tab
2. Tap an employee to select them as primary
3. Shifts are highlighted/filtered in the calendar
4. View shift statistics (total count, breakdown by type)

### Changing Theme

1. Navigate to the **Settings** tab
2. Toggle between **Light**, **Dark**, or **System Default**
3. Preference is saved automatically

---

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Key Test Files
- `DocxScheduleParserTest.kt` — DOCX parsing logic
- `ExampleInstrumentedTest.kt` — UI integration tests

---

## 📋 DOCX Schedule Format

The parser expects a specific DOCX table layout:

```
| Mon | Tue | Wed | Thu | Fri | Sat | Sun | Mon | Tue | Wed | Thu | Fri | Sat | Sun |
|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|
|  1  | JD  |  2  | MS  |  3  | JD  |  4  | MS  |  5  | JD  |  6  | MS  |  7  | JD  |
| Day | Day | Day | Day | Day | Day | Day | Day | Day | Day | Day | Day | Day | Day |
| JD  | MS  | JD  | MS  | JD  | MS  | JD  | MS  | JD  | MS  | JD  | MS  | JD  | MS  |
```

**Supported shift types:**
- `Day` → 07:00–19:00
- `Swing 1` / `Swing1` → 10:00–22:00
- `Swing 2` / `Swing2` → 14:00–02:00
- `Night` → 18:00–06:00

---

## 🔧 Development Notes

### Key Dependencies

```kotlin
// Compose & UI
androidx.compose.ui:ui
androidx.compose.material3:material3
androidx.navigation:navigation-compose

// Data
androidx.room:room-runtime
androidx.datastore:datastore-preferences

// DI
com.google.dagger:hilt-android

// File Parsing
org.apache.poi:poi-ooxml
```

---

## 🐛 Known Limitations

- **Hardcoded shift times** — Times are fixed per shift type; no custom times yet
- **Single month per upload** — Each DOCX file represents one month
- **No edit capability** — Shifts are read-only after import
- **Local storage only** — No cloud sync (Phase 2 feature)
- **No user authentication** — All data is local
- **Limited error handling** — Parser errors may not be user-friendly

---

## 📄 License

[PolyForm Noncommercial License 1.0.0](LICENSE)

---

---

**🐾 Built with ❤️ for veterinary clinics 🐾**
