# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [0.21.12] - 2025-08-12
### Changed
- Unified top bars and paddings using `AppTopBar` and `Dimensions`.
- Replaced manual metric rows with `MetricCard` and applied `AppColors`.

## [0.21.11] - 2025-08-11
### Changed
- Improved invoice PDF generation with Material3 styling and share/print options.

## [0.21.10] - 2025-08-10
### Added
- Compose instrumentation tests for Login, Student and Invoice screens
- CI now runs connectedAndroidTest using an emulator

## [0.21.9] - 2025-08-09
### Added
- Backup and restore via Settings screen
- Encrypted database with SQLCipher

## [0.21.8] - 2025-08-08
### Added
- Profile screen to edit account details

## [0.21.7] - 2025-08-07
### Added
- Categorised settings screen with account info
- Strings extracted for login and registration

## [0.21.6] - 2025-08-06
### Changed
- Number keyboard for entering years of experience in registration

## [0.21.5] - 2025-08-05
### Added
- User profile fields `subjectSpecialty` and `yearsExperience` with auto-migration.

## [0.21.4] - 2025-08-04
### Changed
- Updated lesson tests to construct `Lesson` with named arguments.

## [0.21.3] - 2025-08-03
### Changed
- Updated lesson-related integration tests to use group DAO when constructing `TutorBillingRepository`.

## [0.21.2] - 2025-08-02
### Changed
- Test fakes updated with userId parameters to match DAO signatures.

## [0.21.1] - 2025-08-01
### Added
- Basic user accounts with login and registration screens.
- Secure password hashing and session persistence with logout option.

## [0.20.7] - 2025-07-31
### Changed
- Namespace updated to `gr.eduinvoice`.

## [0.21.0] - 2025-07-22
### Changed
- Reset version numbers to start the 0.21 series.

## [0.20.5] - 2025-07-30
### Changed
- Package renamed to `gr.eduinvoice` and app name updated to EduInvoice.
## [0.20.4] - 2025-07-29
### Added
- Domain tests for AddGroupLesson ensuring one lesson per group member.
- ViewModel tests covering GroupViewModel and LessonViewModel group logic.

## [0.20.3] - 2025-07-28
### Added
- Group lesson selection on Lesson screen with fee calculation.


## [0.20.2] - 2025-07-27
### Added
- Record lessons for groups of students with shared billing.

## [0.20.1] - 2025-07-26
### Added
- Basic group management screens and domain use-cases.

## [0.20.0] - 2025-07-25
### Added
- Student group tables and DAO with repository and DI modules.
- Auto-migration to database version 11.

## [0.19] - 2025-07-24
### Fixed
- Prevent double navigation when saving new students or lessons.
- Display full student name in headers without extra spaces.

## [0.18] - 2025-07-23
### Changed
- Date and time fields in Lesson screen are now directly clickable to open their pickers.

## [0.17] - 2025-07-22
### Changed
- Unified theme utilities and replaced per-screen colors.
## [0.16] - 2025-07-21
### Added
- Introduce per-pull-request version bump policy; each PR increments the project version.
- Add student domain tests for insert/update and archive/restore flows.
- Add rebuild walkthrough in `rebuild.md` describing detailed steps.
- Group lessons by student in Lessons screen with headers.
- Add context menu actions to delete or archive past invoices.
- Add domain and data modules for clean architecture foundation.
- Add AddLesson/GetStudentLessons use-cases and Room DAO tests.
- Reintroduce LessonsViewModel.updatePaid tests for invoicing prompts.
- Add CI workflow to run clean, assemble, test and lint on every push.
- Integrate Firebase Crashlytics for runtime crash reporting.
- In-app privacy policy screen linked from Settings.
- Provide Hilt modules for DAOs and repositories.
- Expose domain use-cases and inject them into ViewModels.
- Provide offline Robolectric artifacts and a coroutine MainDispatcherRule for tests.
- Document modules overview in README.

### Changed
- Ensure date and time fields in Lesson screen open the appropriate picker when tapped.
- Flatten Room migrations; keep only latest schema in data/schemas.
- Replace bar chart drawable with `Icons.Default.BarChart` for Revenue FAB.
- Simplify LessonsUiState.lessons to a list and group in the screen.
- Move Room and repository code into `data` module and business utilities into `domain`.
- Track invoicing state on lessons and prompt when toggling paid status.
- Fix launcher icon references in the AndroidManifest.
- Upgrade to Android Gradle Plugin 8.8.0 and Gradle 8.10.2.
- Show student surname in all UI lists using `getFullName()` extension.
- Make release signingConfig conditional on keystore properties.
- Allow saving students without a phone number and warn if no contact details are provided.
- Removed Google Sign-In feature and account persistence.
- Show Classes button highlighted when a valid class exists.
- Split large Compose screens and add design system shapes.
- Reinstate Room auto-migrations up to version 9.
- Refresh README with build commands and module layout after rebuild.
- Relax phone validation when saving students; accept any non-blank value.
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
