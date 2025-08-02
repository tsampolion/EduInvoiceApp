# Changelog

All notable changes to this project will be documented in this file.

## 0.23.8
### Added
- Legacy plaintext databases are automatically converted to SQLCipher on first launch with a fallback export dialog if migration fails.
### Fixed
- Startup shows an error dialog guiding the user to Settings → Restore Backup when the database fails to open.

## 0.23.7
### Added
- Plaintext database files are automatically migrated into the new encrypted format.
### Changed
- Register screen now specifies `MenuAnchorType.PrimaryNotEditable` for the
  years-of-experience dropdown.
### Fixed
- Replaced deprecated status bar color setter with property assignment.

## 0.23.6
### Fixed
- Settings and Profile viewmodels now derive the logged-in user from
  `CurrentUserProvider` directly.
- Removed the obsolete `SharedUserViewModel`.
- Database provider now opens the database immediately and recovers from corrupt files.

## 0.23.5
### Added
- SharedUserViewModel exposes logged-in user and login status.
- Settings and Profile screens now observe this shared state.

## 0.23.4
### Added
- ViewModel logs student and lesson counts as they load.
- HomeMenuScreen logs collected state and button color recomputation.

## 0.23.3
### Added
- Toolbar now hides on the Welcome screen and a hamburger button opens the drawer.

## 0.23.2
### Changed
- Added hamburger menu button on top-level screens to open the navigation drawer.

## 0.23.1
### Changed
- Invoice utilities now return operation success and log failures.
- Database initialization aborts if old DB cannot be removed.
- PassphraseCrypto throws on encryption/decryption errors.
- Invoice numbers sanitized before creating PDF filenames.

## 0.23
### Feature: Navigation Drawer
- Introduced a sliding drawer with header and menu for quick navigation.
- Updated MainActivity layout with toolbar and drawer integration.
- Drawer selections navigate using a stored `NavHostController`.
- Added open/close string resources and vector assets.
- Build now includes `appcompat` and `drawerlayout`.

## 0.22.16
### Build/CI
- Added `androidx.appcompat` and `androidx.drawerlayout` dependencies.

## 0.22.15
### Added
- MainActivity handles navigation drawer item clicks and stores the app's `NavHostController`.

## 0.22.14
### Changed
- `TutorBillingApp` now accepts an optional `NavHostController` parameter for easier navigation testing.

## 0.22.13
### Added
- Vector assets for bottom navigation icons.
- Drawer open/close messages in `strings.xml` and wired up `MainActivity` to use them.

## 0.22.12
### Added
- Main activity layout with drawer, toolbar, compose host and navigation view.

## 0.22.11
### Added
- Navigation drawer header layout resource.

## 0.22.10
### Added
- Navigation drawer menu resource for Home, Students, Lessons, Groups and Settings.

## 0.22.9
### Fixed
- Login error message now uses a string resource and displays with smaller typography.

## 0.22.8
### Added
- Prompt to enable Autofill when no service is active on login or registration screens.

## 0.22.7
### Added
- `FormCard` composable for consistent form styling.
### Changed
- Register screen now wraps inputs and the register button in `FormCard`.

## 0.22.6
### Fixed
- Silenced experimental coroutines warnings by opting in where required.
- Replaced deprecated status bar color API.

## 0.22.5
### Added
- Validation helpers extracted in `StudentViewModel` with new unit tests.
- Error handling for `LessonViewModel.saveLesson`.

## 0.22.4
### Added
- Extracted PDF generation into `PdfGenerator` utility with improved error reporting.
- Unit tests for the new PDF generator.

## 0.22.3
### Added
- Unit tests for login and registration flows verifying preferences updates.
- Backup restore now validates JSON before parsing.
### Fixed
- Home screen now scopes data to the logged-in user.

## 0.22.2
### Fixed
- Backup restore now validates JSON schema and reports errors to the caller.
- Backup restore errors now show a snackbar message.
### Build/CI
- Documented `FIREBASE_API_KEY` usage for injecting Firebase credentials.

## 0.22.1
### Added
- Instrumentation tests for Settings screen flows.

## 0.22.0
### Feature: Hardened Security
- Improved data protection by disabling Android's automatic backup feature (`android:allowBackup="false"`).
- Enhanced build security by loading the Firebase API key from a secure environment variable.
- A complete user account system was introduced with login, registration, and profile management.
- Implemented BCrypt for password hashing with an automatic upgrade mechanism for existing users.
- Added a secure password reset feature using full-name verification.
- The entire database is now encrypted using SQLCipher, with the passphrase stored securely in the Android Keystore.

## 0.21
### Feature: Multi-User Architecture
- Completed the foundational refactoring to enforce strict multi-user data isolation.
- All data access and manipulation (including `softDeleteStudent` and `deleteById`) now filter by the `ownerId`.
- The `userId` is sourced from a `CurrentUserProvider` in ViewModels and propagated 
through all application layers (Domain, Data, DAOs) to ensure users can only access their own data.
- The database schema was updated to store the `owning user ID` in group-student cross-reference tables.

## 0.20
### Feature: Database & Groups
- Implemented database backup and restore functionality, accessible via the Settings screen.
- Introduced core functionality for creating and managing student groups, including group-specific lesson recording, billing, and domain tests.
- Modularized the project by separating code into `data` and `domain` layers.

## 0.19
### Feature: UI/UX Refinements
- Improved the invoicing workflow with clearer PDF rendering logic and validation for date ranges. Users are now shown errors if PDF operations fail.
- Redesigned multiple core screens (Welcome, Settings, Login) to align with a consistent Material 3 design,
using standardized components like `AppTopBar` and `MetricCard`.
- Enhanced user experience by making key screens scrollable, adding keyboard padding, and making date/time fields directly interactive.

## 0.18
### Infrastructure & Quality
- Improved build performance by increasing Gradle's memory allocation and enabling caching for the Android SDK in the CI pipeline.
- Enhanced code quality by replacing broad `Exception` catches with specific types for better error handling.
- Optimized release builds by enabling code and resource shrinking.
- The project was officially renamed to "EduInvoice" with corresponding package and namespace updates.
- Added a suite of instrumented tests to the CI pipeline, running on an emulator.

## 0.17
### UI/Design
- Unified theme utilities and replaced per-screen colors.

## 0.16
### Lessons & Invoicing
- Added domain tests for student insert/update and archive/restore flows.
- Grouped lessons by student in the Lessons screen and added `AddLesson`/`GetStudentLessons` use-cases.
- Implemented context menu actions to delete or archive past invoices and track their paid status.
### UI/Design
- Split large Compose screens, added design system shapes, and redesigned the Settings screen.
- Applied consistent Material 3 styling and colors across multiple screens.
- Improved various UI elements, including lists, buttons, and input fields.

## 0.15
### Build & Architecture
- Established a CI workflow to run clean, assemble, test, and lint on every push.
- Integrated Firebase Crashlytics for runtime crash reporting and fixed dependency issues.
- Upgraded to Android Gradle Plugin 8.8.0 and Gradle 8.10.2.
- Provided Hilt modules for DAOs/repositories and injected use-cases into ViewModels.
- Flattened Room migrations and reinstated auto-migrations up to version 9.
### Testing
- Provided offline Robolectric artifacts and a `MainDispatcherRule` for coroutine testing.

## 0.14
### UI/Design
- Display app logo and name on the Home screen.
### Invoicing
- Invoice navigation now uses a default `-1` ID for optional student selection.

## 0.13
### Lessons
- Student archive and restore flow with Archived Students screen.
### Testing
- Add unit test for `RevenueViewModel` debts calculation.

## 0.12
### Invoicing
- Invoice route now supports optional student selection.

## 0.11
### Invoicing
- Display outstanding debts per student in the Revenue screen.
- `StudentDebt` data class annotated with `@Stable`.
- Ability to mark lessons paid and navigate to Invoice with pre-selected student.
- Reminder share option for unpaid debts.

## 0.10
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

## 0.9
### Invoicing
- Invoice generation with payment tracking.
### UI/Design
- Home menu alignment and deprecated API fixes.

## 0.8
### UI/Design
- Search query handling improved across screens.
- Home and lesson screens redesigned.

## 0.7
### Lessons
- Compose pickers for scheduling lessons.
### Build/CI
- Time picker import issues.

## 0.6
### Lessons
- Date and time pickers with field validation.
### UI/Design
- Switched to Android platform pickers.

## 0.5
### Lessons
- Student form validation with search functionality.
### Build/CI
- Cleaned up Compose imports.

## 0.4
### Lessons
- Lesson validation rules and CI checks.
- Initial Classes feature for grouping students.

## 0.3
### Lessons
- `RateTypes` constants and lesson duration validation.

## 0.2
### Build/CI
- Repository and ViewModel structure cleanup.

## 0.1
### Build/CI
- Initial project setup with `.gitignore`.
