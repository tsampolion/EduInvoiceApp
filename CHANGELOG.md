# Changelog

All notable changes to this project will be documented in this file.

## [0.21.15] - 2025-08-15
### Fix
- Preserve group ID when editing group lessons and update navigation.

## [0.21.14] - 2025-08-14
### Docs
- Restructured CHANGELOG into themed sub-sections.

## [0.21.13] - 2025-08-13
### UI/Design
- Made Settings, Register, Login and Profile screens scrollable with keyboard padding.

## [0.21.12] - 2025-08-12
### UI/Design
- Unified top bars and paddings using `AppTopBar` and `Dimensions`.
- Replaced manual metric rows with `MetricCard` and applied `AppColors`.

## [0.21.11] - 2025-08-11
### Invoicing
- Improved invoice PDF generation with Material3 styling and share/print options.

## [0.21.10] - 2025-08-10
### Build/CI
- Compose instrumentation tests for Login, Student and Invoice screens.
- CI now runs connectedAndroidTest using an emulator.

## [0.21.9] - 2025-08-09
### Database/Room
- Backup and restore via Settings screen.
- Encrypted database with SQLCipher.

## [0.21.8] - 2025-08-08
### User Accounts
- Profile screen to edit account details.

## [0.21.7] - 2025-08-07
### User Accounts
- Categorised settings screen with account info.
- Strings extracted for login and registration.

## [0.21.6] - 2025-08-06
### User Accounts
- Number keyboard for entering years of experience in registration.

## [0.21.5] - 2025-08-05
### User Accounts
- User profile fields `subjectSpecialty` and `yearsExperience` with auto-migration.

## [0.21.4] - 2025-08-04
### Lessons
- Updated lesson tests to construct `Lesson` with named arguments.

## [0.21.3] - 2025-08-03
### Lessons
- Updated lesson-related integration tests to use group DAO when constructing `TutorBillingRepository`.

## [0.21.2] - 2025-08-02
### Build/CI
- Test fakes updated with userId parameters to match DAO signatures.

## [0.21.1] - 2025-08-01
### User Accounts
- Basic user accounts with login and registration screens.
- Secure password hashing and session persistence with logout option.

## [0.20.7] - 2025-07-31
### Build/CI
- Namespace updated to `gr.eduinvoice`.

## [0.20.5] - 2025-07-30
### Build/CI
- Package renamed to `gr.eduinvoice` and app name updated to EduInvoice.

## [0.20.4] - 2025-07-29
### Testing
- Domain tests for AddGroupLesson ensuring one lesson per group member.
- ViewModel tests covering GroupViewModel and LessonViewModel group logic.

## [0.20.3] - 2025-07-28
### Lessons
- Group lesson selection on Lesson screen with fee calculation.

## [0.20.2] - 2025-07-27
### Lessons
- Record lessons for groups of students with shared billing.

## [0.20.1] - 2025-07-26
### Lessons
- Basic group management screens and domain use-cases.

## [0.20.0] - 2025-07-25
### Database/Room
- Student group tables and DAO with repository and DI modules.
- Auto-migration to database version 11.

## [0.19] - 2025-07-24
### Lessons
- Prevent double navigation when saving new students or lessons.
- Display full student name in headers without extra spaces.

## [0.18] - 2025-07-23
### UI/Design
- Date and time fields in Lesson screen are now directly clickable to open their pickers.

## [0.21.0] - 2025-07-22
### Build/CI
- Reset version numbers to start the 0.21 series.

## [0.17] - 2025-07-22
### UI/Design
- Unified theme utilities and replaced per-screen colors.

## [0.16] - 2025-07-21
### Build/CI
- Introduce per-pull-request version bump policy; each PR increments the project version.
- Add rebuild walkthrough in `rebuild.md` describing detailed steps.
- Add CI workflow to run clean, assemble, test and lint on every push.
- Integrate Firebase Crashlytics for runtime crash reporting.
- Provide Hilt modules for DAOs and repositories.
- Expose domain use-cases and inject them into ViewModels.

### Lessons
- Add student domain tests for insert/update and archive/restore flows.
- Group lessons by student in Lessons screen with headers.
- Add AddLesson/GetStudentLessons use-cases and Room DAO tests.
- Reintroduce LessonsViewModel.updatePaid tests for invoicing prompts.

### Invoicing
- Add context menu actions to delete or archive past invoices.
- Track invoicing state on lessons and prompt when toggling paid status.

### Testing
- Provide offline Robolectric artifacts and a coroutine MainDispatcherRule for tests.

### Docs
- Document modules overview in README.

### UI/Design
- Ensure date and time fields in Lesson screen open the appropriate picker when tapped.
- Replace bar chart drawable with `Icons.Default.BarChart` for Revenue FAB.
- Simplify LessonsUiState.lessons to a list and group in the screen.
- Show student surname in all UI lists using `getFullName()` extension.
- Allow saving students without a phone number and warn if no contact details are provided.
- Show Classes button highlighted when a valid class exists.
- Split large Compose screens and add design system shapes.
- Align Settings screen styling with Revenue screen for consistent Material 3 design.
- Redesign Settings screen with dropdown menus and colourful cards matching Revenue metrics.
- Apply `primaryContainer` colors to remaining screens' top app bars.

### Database/Room
- Flatten Room migrations; keep only latest schema in data/schemas.
- Move Room and repository code into `data` module and business utilities into `domain`.
- Reinstate Room auto-migrations up to version 9.

### Build/CI
- Fix launcher icon references in the AndroidManifest.
- Upgrade to Android Gradle Plugin 8.8.0 and Gradle 8.10.2.
- Make release signingConfig conditional on keystore properties.
- Removed Google Sign-In feature and account persistence.
- Refresh README with build commands and module layout after rebuild.

### User Accounts
- Relax phone validation when saving students; accept any non-blank value.

## [0.14] - 2025-06-15
### UI/Design
- Display app logo and name on the Home screen.

### Invoicing
- Invoice navigation now uses a default `-1` ID for optional student selection.

## [0.13] - 2025-06-14
### Lessons
- Student archive and restore flow with Archived Students screen.

### Testing
- Add unit test for `RevenueViewModel` debts calculation.

## [0.12] - 2025-06-14
### Invoicing
- Invoice route now supports optional student selection.

## [0.11] - 2025-06-14
### Invoicing
- Display outstanding debts per student in the Revenue screen.
- `StudentDebt` data class annotated with `@Stable`.
- Ability to mark lessons paid and navigate to Invoice with pre-selected student.
- Reminder share option for unpaid debts.

## 0.10 - 2025-06-14
### Lessons
- Student management screens with class grouping and detail views.
- Lesson tracking with billing type support.

### Invoicing
- Invoice creation with PDF generation and past invoices list.

### UI/Design
- Revenue dashboard and settings screen.

### Build/CI
- Setup script for Android SDK.
- Navigation graph refactored for type-safe routes.
- Removed manual Room migration scripts in favour of auto-migrations.

## [0.9] - 2025-06-09
### Invoicing
- Invoice generation with payment tracking.

### UI/Design
- Home menu alignment and deprecated API fixes.

## [0.8] - 2025-06-07
### UI/Design
- Search query handling improved across screens.
- Home and lesson screens redesigned.

## [0.7] - 2025-06-07
### Lessons
- Compose pickers for scheduling lessons.

### Build/CI
- Time picker import issues.

## [0.6] - 2025-06-06
### Lessons
- Date and time pickers with field validation.

### UI/Design
- Switched to Android platform pickers.

## [0.5] - 2025-06-05
### Lessons
- Student form validation with search functionality.

### Build/CI
- Cleaned up Compose imports.

## [0.4] - 2025-06-05
### Lessons
- Lesson validation rules and CI checks.
- Initial Classes feature for grouping students.

## [0.3] - 2025-06-05
### Lessons
- `RateTypes` constants and lesson duration validation.

## [0.2] - 2025-06-05
### Build/CI
- Repository and ViewModel structure cleanup.

## [0.1] - 2025-05-25
### Build/CI
- Initial project setup with `.gitignore`.
