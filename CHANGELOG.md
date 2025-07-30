# Changelog

All notable changes to this project will be documented in this file.

## [0.22.2] - 2025-07-28
### Fixed
- Backup restore errors now show a snackbar message.

## [0.22.1] - 2025-07-27
### Added
- Instrumentation tests for Settings screen flows.

## [0.22.0] - 2025-07-26
### Feature: Hardened Security
- Improved data protection by disabling Android's automatic backup feature (`android:allowBackup="false"`).
- Enhanced build security by loading the Firebase API key from a secure environment variable.
- A complete user account system was introduced with login, registration, and profile management.
- Implemented BCrypt for password hashing with an automatic upgrade mechanism for existing users.
- Added a secure password reset feature using full-name verification.
- The entire database is now encrypted using SQLCipher, with the passphrase stored securely in the Android Keystore.

## [0.21] - 2025-07-25
### Feature: Multi-User Architecture
- Completed the foundational refactoring to enforce strict multi-user data isolation.
- All data access and manipulation (including `softDeleteStudent` and `deleteById`) now filter by the `ownerId`.
- The `userId` is sourced from a `CurrentUserProvider` in ViewModels and propagated through all application layers (Domain, Data, DAOs) to ensure users can only access their own data.
- The database schema was updated to store the `owning user ID` in group-student cross-reference tables.

## [0.20] - 2025-07-24
### Feature: Database & Groups
- Implemented database backup and restore functionality, accessible via the Settings screen.
- Introduced core functionality for creating and managing student groups, including group-specific lesson recording, billing, and domain tests.
- Modularized the project by separating code into `data` and `domain` layers.

## [0.19] - 2025-07-23
### Feature: UI/UX Refinements
- Improved the invoicing workflow with clearer PDF rendering logic and validation for date ranges. Users are now shown errors if PDF operations fail.
- Redesigned multiple core screens (Welcome, Settings, Login) to align with a consistent Material 3 design, using standardized components like `AppTopBar` and `MetricCard`.
- Enhanced user experience by making key screens scrollable, adding keyboard padding, and making date/time fields directly interactive.

## [0.18] - 2025-07-22
### Infrastructure & Quality
- Improved build performance by increasing Gradle's memory allocation and enabling caching for the Android SDK in the CI pipeline.
- Enhanced code quality by replacing broad `Exception` catches with specific types for better error handling.
- Optimized release builds by enabling code and resource shrinking.
- The project was officially renamed to "EduInvoice" with corresponding package and namespace updates.
- Added a suite of instrumented tests to the CI pipeline, running on an emulator.

## [0.17] - 2025-07-22
### UI/Design
- Unified theme utilities and replaced per-screen colors.

## [0.16] - 2025-07-21
### Lessons & Invoicing
- Added domain tests for student insert/update and archive/restore flows.
- Grouped lessons by student in the Lessons screen and added `AddLesson`/`GetStudentLessons` use-cases.
- Implemented context menu actions to delete or archive past invoices and track their paid status.
### UI/Design
- Split large Compose screens, added design system shapes, and redesigned the Settings screen.
- Applied consistent Material 3 styling and colors across multiple screens.
- Improved various UI elements, including lists, buttons, and input fields.

## [0.15] - 2025-07-20
### Build & Architecture
- Established a CI workflow to run clean, assemble, test, and lint on every push.
- Integrated Firebase Crashlytics for runtime crash reporting and fixed dependency issues.
- Upgraded to Android Gradle Plugin 8.8.0 and Gradle 8.10.2.
- Provided Hilt modules for DAOs/repositories and injected use-cases into ViewModels.
- Flattened Room migrations and reinstated auto-migrations up to version 9.
### Testing
- Provided offline Robolectric artifacts and a `MainDispatcherRule` for coroutine testing.

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
