# Changelog

All notable changes to this project will be documented in this file.

## [0.24.3] - 2025-01-27
### Added
- **Comprehensive Error Boundary Implementation**: Centralized error handling, user-friendly error messages, and automatic retry mechanisms.
  - **ErrorBoundary**: Composable that catches and handles errors in the UI tree with user-friendly fallback UI.
  - **ErrorHandler**: Centralized error classification, retry logic, and user-friendly message generation.
  - **ErrorDialog**: User-friendly error dialogs with recovery options and detailed error information.
  - **RetryManager**: Automatic retry mechanisms with exponential backoff, parallel execution, and smart retry logic.
  - **ErrorReporter**: Comprehensive error reporting to Firebase Crashlytics, local logging, and analytics.

### Infrastructure
- **Error Handling Components**:
  - `ErrorBoundary.kt` - UI error boundary with fallback UI
  - `ErrorHandler.kt` - Centralized error handling and classification
  - `ErrorDialog.kt` - User-friendly error dialogs
  - `RetryManager.kt` - Automatic retry mechanisms
  - `ErrorReporter.kt` - Error reporting and analytics
- **Error Handling Features**:
  - Error classification by type (network, IO, permission, validation, etc.)
  - Automatic retry logic with exponential backoff and jitter
  - User-friendly error messages with recovery suggestions
  - Error history tracking and statistics
  - Firebase Crashlytics integration for error reporting
  - Local error logging for debugging
  - Error pattern analysis for analytics

## [0.24.2] - 2025-01-27
### Added
- **Memory Management Optimization**: Comprehensive memory monitoring, pressure handling, and optimization systems.
  - **MemoryMonitor**: Real-time memory usage tracking, pressure detection, and cleanup operations with detailed metrics and recommendations.
  - **MemoryPressureHandler**: Automatic memory pressure response with low/critical memory handling, aggressive cleanup, and failure recovery.
  - **PaginatedList**: Efficient data structure for handling large datasets with pagination, caching, and memory management.
  - **PaginationManager**: Advanced pagination management with caching, state management, and flow-based data loading.
  - **LazyLoadingList**: Compose components for efficient list rendering with automatic pagination, loading states, and error handling.

### Infrastructure
- **Memory Management Components**: 
  - `MemoryMonitor.kt` - Comprehensive memory monitoring and cleanup
  - `MemoryPressureHandler.kt` - Memory pressure detection and response
  - `PaginatedList.kt` - Efficient pagination data structure
  - `LazyLoadingList.kt` - Compose components for lazy loading
- **Memory Optimization Features**:
  - Real-time memory usage monitoring with configurable thresholds
  - Automatic garbage collection and cache clearing
  - Aggressive cleanup for critical memory situations
  - Pagination with configurable page sizes and caching
  - Lazy loading with automatic threshold-based loading
  - Memory pressure event handling and recovery

## [0.24.1] - 2025-08-05
### Added
- **Database Resilience Overhaul**: Comprehensive database health monitoring, integrity validation, and recovery mechanisms.
  - **DatabaseHealthMonitor**: Real-time health monitoring with performance metrics, maintenance operations, and integrity checks.
  - **DatabaseIntegrityValidator**: Comprehensive data validation and repair mechanisms for orphaned records, invalid data, and constraint violations.
  - **DatabaseFallbackManager**: Graceful degradation with read-only mode, offline mode, and automatic recovery procedures.
  - **Enhanced BackupRepository**: Automatic backup creation before risky operations with retention policies and cleanup mechanisms.

### Infrastructure
- **Database Resilience Components**: 
  - `DatabaseHealthMonitor.kt` - Comprehensive health monitoring and maintenance
  - `DatabaseIntegrityValidator.kt` - Data validation and repair mechanisms
  - `DatabaseFallbackManager.kt` - Graceful degradation and recovery management
  - Enhanced `BackupRepository.kt` - Automatic backup with retention policies
  - `DatabaseResilienceTest.kt` - Comprehensive test suite for resilience components
- **Dependency Injection**: Updated `DatabaseModule.kt` with new resilience component providers

## [0.24.0] - 2025-08-05
### Added
- **Strategic Plan Documentation**: Comprehensive 12-week strategic plan for transforming EduInvoiceApp into a production-ready, enterprise-grade application.
  - **Phase 1: Foundation & Stability** (Weeks 1-4): Database resilience, memory optimization, error handling, network resilience, performance optimization, concurrent operation safety, and testing infrastructure.
  - **Phase 2: Enhancement & User Experience** (Weeks 5-8): Loading states, accessibility, advanced search/filtering, data export/import, analytics, performance monitoring, and comprehensive testing.
  - **Phase 3: Production Readiness** (Weeks 9-12): Automated release pipeline, production monitoring, security hardening, documentation, and final validation.
- **Master Index**: Strategic plan overview with progress tracking, success criteria, risk management, and implementation guidelines.
- **Detailed Implementation Plans**: Each phase includes specific tasks, code examples, file modifications, success criteria, and testing requirements.
- **Metrics Dashboard**: Technical and business metrics tracking across all phases with specific targets.
- **Risk Mitigation Strategies**: Comprehensive contingency plans and high-risk area identification.

### Documentation
- Added `STRATEGIC_PLAN_MASTER_INDEX.md` - Master index with overview and progress tracking
- Added `PHASE_1_FOUNDATION_STABILITY.md` - Detailed Phase 1 implementation plan
- Added `PHASE_2_ENHANCEMENT_USER_EXPERIENCE.md` - Detailed Phase 2 implementation plan  
- Added `PHASE_3_PRODUCTION_READINESS.md` - Detailed Phase 3 implementation plan

## [0.23.20] - 2025-08-04
### Fixed
- **ViewModel Dispatcher Issues**: Removed explicit `Dispatchers.IO` usage from ViewModel methods to work properly with test dispatchers.
- **Test Infrastructure**: Fixed `ComposeTestEnvironment` and `ComposeTestBase` to not be treated as test classes.
- **PDF Document Lifecycle**: Fixed PDF document management in tests to prevent "document is closed" errors.
- **Test Assertions**: Fixed missing `assertTrue` import in `LoginViewModelTest`.
- **ViewModel State Synchronization**: Improved test assertions to check ViewModel state instead of direct flow access.
- **Error Message Handling**: Made test assertions more flexible for error messages that may vary in test environments.

## [0.23.19] - 2025-08-04
### Changed
- Overhauled test infrastructure for clearer PDF and Compose setups.
- Improved group data handling for more reliable relationships.
### Fixed
- Use `android.graphics.pdf.PdfDocument` in test utilities to resolve compilation errors.

## [0.23.18] - 2025-08-03
### Fixed
- Validate SQLCipher passphrase and rebuild database after removing corrupt file in debug builds.
- Initialize `navController` before usage to avoid startup crash.

## [0.23.17] - 2025-08-02
### Fixed
- Groups now store owner IDs and link selected students on save.
- Lessons list sorts by date and defers paid status until confirmed.
- Backup restore ignores unknown JSON fields and surfaces parse errors.

## [0.23.16] - 2025-08-02
### Fixed
- Align Compose UI tests with current layouts and formatting; add SDK 34 annotations.

## [0.23.15] - 2025-08-02
### Fixed
- Register BouncyCastle provider during Robolectric tests to resolve cryptography errors.
### Build
- Added BouncyCastle provider dependency for test scope.

## [0.23.14] - 2025-08-02
### Changed
- Gracefully handle DataStore I/O errors when fetching DB passphrase.
- Backup restoration now logs and returns failure on unexpected exceptions.
- Updated `androidx.security:security-crypto` to 1.1.0.
- CI runs OWASP DependencyCheck for vulnerability scanning.

## [0.23.13] - 2025-08-02
### Added
- Test verifying transaction rollback when a group lesson insert fails.
- Unit tests covering print job cancellation.
### Changed
- Separated financial calculations into `FinancialService` and slimmed down `TutorBillingRepository`.
- Group lesson creation now uses a Room transaction to ensure all-or-nothing inserts.
- Replaced PDF layout magic numbers with named constants.
### Fixed
- Invoice number validated to allow only alphanumerics.
- Print adapter respects cancellation signals during PDF copy.

## [0.23.12] - 2025-08-02
### Added
- Tests covering passphrase generation, encryption, and decryption utilities.
### Fixed
- Restoring students and updating lesson statuses now require matching owner IDs.
- Password reset flow includes verification code check.
- Passphrase generation now uses `SecureRandom` and the database no longer logs passphrase length.

## [0.23.11] - 2025-08-02
### Fixed
- Recover from corrupt database files in debug builds by deleting and rebuilding once on initialization failure.
- Google services config updated and BuildConfig now exposes the Firebase API key.

## [0.23.10] - 2025-08-02
### Fixed
- Logged database passphrase length and ensured SQLCipher libraries load before Room initialization.
- Added defensive passphrase validation during database access.
- Initialize `navController` before usage to avoid startup crash and handle toolbar visibility within `LaunchedEffect`.

## [0.23.9] - 2025-08-02
### Fixed
- Updated StudentScreenTest fake `LessonDao` to include `userId` in `deleteById`.
- Login screen test now includes password reset use case and context dependency.

## [0.23.8] - 2025-08-02
### Added
- Legacy plaintext databases are automatically converted to SQLCipher on first launch with a fallback export dialog if migration fails.
### Fixed
- Startup shows an error dialog guiding the user to Settings → Restore Backup when the database fails to open.

## [0.23.7] - 2025-08-02
### Added
- Plaintext database files are automatically migrated into the new encrypted format.
### Changed
- Register screen now specifies `MenuAnchorType.PrimaryNotEditable` for the
  years-of-experience dropdown.
### Fixed
- Replaced deprecated status bar color setter with property assignment.

## [0.23.6] - 2025-08-02
### Fixed
- Settings and Profile viewmodels now derive the logged-in user from
  `CurrentUserProvider` directly.
- Removed the obsolete `SharedUserViewModel`.
- Database provider now opens the database immediately and recovers from corrupt files.

## [0.23.5] - 2025-08-02
### Added
- SharedUserViewModel exposes logged-in user and login status.
- Settings and Profile screens now observe this shared state.

## [0.23.4] - 2025-08-02
### Added
- ViewModel logs student and lesson counts as they load.
- HomeMenuScreen logs collected state and button color recomputation.

## [0.23.3] - 2025-08-01
### Added
- Toolbar now hides on the Welcome screen and a hamburger button opens the drawer.

## [0.23.2] - 2025-08-01
### Changed
- Added hamburger menu button on top-level screens to open the navigation drawer.

## [0.23.1] - 2025-08-01
### Changed
- Invoice utilities now return operation success and log failures.
- Database initialization aborts if old DB cannot be removed.
- PassphraseCrypto throws on encryption/decryption errors.
- Invoice numbers sanitized before creating PDF filenames.

## [0.23] - 2025-08-01
### Feature: Navigation Drawer
- Introduced a sliding drawer with header and menu for quick navigation.
- Updated MainActivity layout with toolbar and drawer integration.
- Drawer selections navigate using a stored `NavHostController`.
- Added open/close string resources and vector assets.
- Build now includes `appcompat` and `drawerlayout`.

## [0.22.16] - 2025-08-01
### Build/CI
- Added `androidx.appcompat` and `androidx.drawerlayout` dependencies.

## [0.22.15] - 2025-08-01
### Added
- MainActivity handles navigation drawer item clicks and stores the app's `NavHostController`.

## [0.22.14] - 2025-07-31
### Changed
- `TutorBillingApp` now accepts an optional `NavHostController` parameter for easier navigation testing.

## [0.22.13] - 2025-07-31
### Added
- Vector assets for bottom navigation icons.
- Drawer open/close messages in `strings.xml` and wired up `MainActivity` to use them.

## [0.22.12] - 2025-07-31
### Added
- Main activity layout with drawer, toolbar, compose host and navigation view.

## [0.22.11] - 2025-07-31
### Added
- Navigation drawer header layout resource.

## [0.22.10] - 2025-07-31
### Added
- Navigation drawer menu resource for Home, Students, Lessons, Groups and Settings.

## [0.22.9] - 2025-07-31
### Fixed
- Login error message now uses a string resource and displays with smaller typography.

## [0.22.8] - 2025-07-31
### Added
- Prompt to enable Autofill when no service is active on login or registration screens.

## [0.22.7] - 2025-07-31
### Added
- `FormCard` composable for consistent form styling.
### Changed
- Register screen now wraps inputs and the register button in `FormCard`.

## [0.22.6] - 2025-07-31
### Fixed
- Silenced experimental coroutines warnings by opting in where required.
- Replaced deprecated status bar color API.

## [0.22.5] - 2025-07-30
### Added
- Validation helpers extracted in `StudentViewModel` with new unit tests.
- Error handling for `LessonViewModel.saveLesson`.

## [0.22.4] - 2025-07-30
### Added
- Extracted PDF generation into `PdfGenerator` utility with improved error reporting.
- Unit tests for the new PDF generator.

## [0.22.3] - 2025-07-29
### Added
- Unit tests for login and registration flows verifying preferences updates.
- Backup restore now validates JSON before parsing.
### Fixed
- Home screen now scopes data to the logged-in user.

## [0.22.2] - 2025-07-28
### Fixed
- Backup restore now validates JSON schema and reports errors to the caller.
- Backup restore errors now show a snackbar message.
### Build/CI
- Documented `FIREBASE_API_KEY` usage for injecting Firebase credentials.

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
- The `userId` is sourced from a `CurrentUserProvider` in ViewModels and propagated 
through all application layers (Domain, Data, DAOs) to ensure users can only access their own data.
- The database schema was updated to store the `owning user ID` in group-student cross-reference tables.

## [0.20] - 2025-07-24
### Feature: Database & Groups
- Implemented database backup and restore functionality, accessible via the Settings screen.
- Introduced core functionality for creating and managing student groups, including group-specific lesson recording, billing, and domain tests.
- Modularized the project by separating code into `data` and `domain` layers.

## [0.19] - 2025-07-23
### Feature: UI/UX Refinements
- Improved the invoicing workflow with clearer PDF rendering logic and validation for date ranges. Users are now shown errors if PDF operations fail.
- Redesigned multiple core screens (Welcome, Settings, Login) to align with a consistent Material 3 design,
using standardized components like `AppTopBar` and `MetricCard`.
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
