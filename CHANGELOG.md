# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]
- Ensure date and time fields in Lesson screen open the appropriate picker when tapped.
- Flatten Room migrations; keep only latest schema in data/schemas.
- Replace bar chart drawable with `Icons.Default.BarChart` for Revenue FAB.
- Add student domain tests for insert/update and archive/restore flows.
- Add rebuild walkthrough in `rebuild.md` describing detailed steps.
- Simplify LessonsUiState.lessons to a list and group in the screen.
- Group lessons by student in Lessons screen with headers.
- Add context menu actions to delete or archive past invoices.
- Add domain and data modules for clean architecture foundation.
- Add AddLesson/GetStudentLessons use-cases and Room DAO tests.
- Move Room and repository code into `data` module and business utilities into `domain`.
- Reintroduce LessonsViewModel.updatePaid tests for invoicing prompts.
- Track invoicing state on lessons and prompt when toggling paid status.
- Fix launcher icon references in the AndroidManifest.
- Add CI workflow to run clean, assemble, test and lint on every push.
- Upgrade to Android Gradle Plugin 8.8.0 and Gradle 8.10.2.
- Show student surname in all UI lists using `getFullName()` extension.
- Make release signingConfig conditional on keystore properties.
- Integrate Firebase Crashlytics for runtime crash reporting.
- Allow saving students without a phone number and warn if no contact details are provided.
- Removed Google Sign-In feature and account persistence.
- Show Classes button highlighted when a valid class exists.
- In-app privacy policy screen linked from Settings.
- Provide Hilt modules for DAOs and repositories.
- Expose domain use-cases and inject them into ViewModels.
- Provide offline Robolectric artifacts and a coroutine MainDispatcherRule for tests.
- Split large Compose screens and add design system shapes.
- Reinstate Room auto-migrations up to version 9.
- Document modules overview in README.
- Refresh README with build commands and module layout after rebuild.
- Align Settings screen styling with Revenue screen for consistent Material 3 design.
- Redesign Settings screen with dropdown menus and colourful cards matching Revenue metrics.
- Apply `primaryContainer` colors to remaining screens' top app bars.

## [0.14] - 2025-06-15
### Added
- Display app logo and name on the Home screen.

### Changed
- Invoice navigation now uses a default `-1` ID for optional student selection.

## [0.13] - 2025-06-14
### Added
- Student archive and restore flow with Archived Students screen.
- Add unit test for `RevenueViewModel` debts calculation.

## [0.12] - 2025-06-14
### Changed
- Invoice route now supports optional student selection

## [0.11] - 2025-06-14
### Added
- Display outstanding debts per student in the Revenue screen.
- `StudentDebt` data class annotated with `@Stable`.
- Ability to mark lessons paid and navigate to Invoice with pre-selected student.
- Reminder share option for unpaid debts.

## 0.10 - 2025-06-14
### Added
- Student management screens with class grouping and detail views.
- Lesson tracking with billing type support.
- Invoice creation with PDF generation and past invoices list.
- Revenue dashboard and settings screen.
- Setup script for Android SDK.

### Changed
- Navigation graph refactored for type-safe routes.
- Removed manual Room migration scripts in favour of auto-migrations.
