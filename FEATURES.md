## Schedule Import (DOCX Parser)

| Feature | Status | Notes |
|---------|--------|-------|
| Upload `.docx` schedule file | ✅ Working | `ScheduleImport.vue` → `POST /upload_file` → `load_schedule()` in `scripts.py` |
| Auto-detect month from filename | ✅ Working | Reads month abbreviation from filename; falls back to UI date picker |
| Date picker (month/year) | ✅ Working | Quasar `q-date` picker with `MMM YYYY` mask |
| Parse DOCX table into shifts | ✅ Working | Parses 6-column row layout; hardcoded time slots: 07:00 / 10:00 / 14:00 / 18:00 |
| Extract user initials from DOCX | ✅ Working | `get_users()` in `scripts.py` — reads 2-character alpha strings from table cells |
| Load parsed shifts into database | ✅ Working | `load_database()` writes to `Calendar` model; clears existing data for month/year first |
| Filter calendar by user initials | ✅ Working | `filterShifts()` — filters `calendarOptions.events` client-side |
| Clear user filter | ✅ Working | Clears user selection and restores all events |

## Calendar View

| Feature | Status | Notes |
|---------|--------|-------|
| Month view calendar (FullCalendar.js v6) | ✅ Working | `ScheduleImport.vue` embeds `FullCalendar` with `dayGridPlugin` |
| Fetch and display shifts from database | ✅ Working | `return_shifts` endpoint filters `Calendar` objects by date range; returns user + start per event |
| Navigate months (Prev / Next / Today) | ✅ Working | Custom FullCalendar buttons trigger `handleCalendarChange()` → `getShifts()` |
| Calendar syncs with date picker | ✅ Working | Changing the date picker navigates the calendar and re-fetches shifts |
| Display shift owner (user initials) as event title | ✅ Working | Event `title` is set to `user_initials` from `Calendar` model |

## Page Structure
| Page | Purpose | Key Components |
|------|---------|---|
| **DashboardPage** | Overview of today's shifts, KPIs, quick actions | StatCard, ShiftListItem, CalendarGrid |
| **MonthlySchedulePage** | Month view with daily drill-down | CalendarGrid, StatusChip |
| **ScheduleImportPage** | File upload for bulk shift data | Form inputs, upload UI |
| **ShiftDetailsPage** | Single shift view/edit | Detail card, action buttons |