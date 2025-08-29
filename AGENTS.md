# AGENTS.md – EduInvoiceApp Guidance for Autonomous Coding Agents
<!-- Keep this file <300 lines so agents can parse it on every run. -->

## 1. Project Overview
EduInvoiceApp is a production-ready Android application for tutoring business management with enterprise-grade features.
**Current Version:** 0.29.0 | **Status:** Production Ready with Enterprise Features
**Tech Stack:** Kotlin 2.1.10, Android Gradle Plugin 8.8.0, Jetpack Compose, Room with SQLCipher, Hilt, DataStore, Robolectric

## 2. Environment Setup
```bash
# 1 · Install JDK 17 (or higher) on PATH
# 2 · Bootstrap Android SDK *once*:
bash setup-android-sdk.sh
source ~/.bashrc          # or the profile printed by the script
# 3 · Verify:
java -version              # should be 17+
sdkmanager --version       # should respond
```

> The helper script installs cmdline-tools r12b, Build Tools 35.0.0 and sets `local.properties`.
> Agents running in CI **must** source the profile before any Gradle task so `ANDROID_HOME` is visible.

## 3. Building, Testing & Linting

Run these *exact* commands before proposing code changes:

```bash
./gradlew clean             # wipes build cache
./gradlew assemble          # compiles debug & release variants
./gradlew test              # JUnit + Robolectric unit tests
./gradlew lintDebug         # Android Lint (plain text output)
```

*Instrumentation tests* are available via `./gradlew connectedAndroidTest` when emulator is available.

## 4. Code Standards

* **Language level**: Kotlin JVM 17 (`kotlin.jvm.target=17` in *gradle.properties*)
* **Formatting**: Use `ktfmt` or IntelliJ default; no tabs; 120-char line cap
* **Compose**: Prefer `@Stable` data classes; pass `Modifier` as first optional param
* **Room**: DAO methods return `Flow<>`; migrations handled via `autoMigrations`
* **Dependency-Injection**: All ViewModels live under `gr.eduinvoice.ui.*` and are Hilt-annotated
* **Error Handling**: Use ErrorBoundary, ErrorHandler, and RetryManager for robust error handling
* **Concurrency**: Use ConcurrencyController for thread-safe database operations
* **Performance**: Implement pagination and background processing for large datasets

## 5. Directory & Naming Conventions

| ------------------------------ | ------------------------------------------------------ |
| `/app`                         | Android application module (UI, ViewModels, Components) |
| `/domain`                      | Pure Kotlin business logic (Use cases, entities) |
| `/data`                        | Database and repository layer (Room, DAOs, Repositories) |
| `/docs`                        | **NEW** - Comprehensive documentation |
| `/app/src/main`                | Production code (namespace `gr.eduinvoice`) |
| `/app/src/test`                | JVM unit tests (Robolectric) |
| `/data/schemas`                | Room JSON schemas (auto-generated; keep under VC) |
| `build/`, `.gradle/`, `.idea/` | **Ignored** – see `.gitignore` |
| `local.properties`             | **Ignored** – SDK path, never commit |
| `CHANGELOG.md`                 | Project changelog; start a new versioned section for each pull request |

## 6. Pull-Request Messaging Template

```
[feat|fix|refactor](scope): short summary

- WHAT changed
- WHY it matters
- HOW to test (`./gradlew test`)
```

Checklist:
* [ ] Version bumped and `CHANGELOG.md` updated
* [ ] Unit tests passing
* [ ] Lint shows **0** new warnings
* [ ] No TODOs or commented-out code left
* [ ] `./gradlew assemble` succeeds on CI
* [ ] Documentation updated if needed

## 7. Enterprise Features & Procedures

### Error Handling & Resilience
* **ErrorBoundary**: Wrap UI components with ErrorBoundary for graceful error handling
* **ErrorHandler**: Use centralized error classification and user-friendly messages
* **RetryManager**: Implement automatic retry with exponential backoff
* **ErrorReporter**: Report errors to Firebase Crashlytics and local logging

### Concurrency Safety
* **ConcurrencyController**: Use for all database operations to ensure thread safety
* **TransactionManager**: Implement ACID-compliant transactions with rollback
* **OperationQueueManager**: Queue operations with priorities (LOW, NORMAL, HIGH, CRITICAL)
* **Resource Locking**: Prevent deadlocks with efficient resource management

### Performance Optimization
* **BackgroundProcessor**: Use for heavy operations (PDF generation, data sync)
* **MemoryMonitor**: Monitor memory usage and trigger cleanup when needed
* **PaginatedList**: Implement pagination for large datasets
* **LazyLoadingList**: Use Compose components for efficient list rendering

### Network Resilience
* **OfflineDataManager**: Store data locally for offline functionality
* **SyncManager**: Synchronize data when network is available
* **NetworkMonitor**: Monitor connectivity and connection quality
* **ConflictResolver**: Handle data conflicts intelligently

### Security Features
* **SQLCipher Encryption**: All database operations use encrypted storage
* **BCrypt Password Hashing**: Secure password storage with automatic upgrades
* **Multi-User Isolation**: Complete data separation between users
* **Secure Backup**: Encrypted backup and restore functionality

## 8. Common Pitfalls

1. **ANDROID_HOME not set** – always source the profile written by `setup-android-sdk.sh`
2. **Out-of-date Gradle wrapper** – update with `./gradlew wrapper --gradle-version 8.10.2` when bumping AGP
3. **Room schema drift** – run `./gradlew test` after changing entities to auto-regenerate `/data/schemas`
4. **Accidentally committed build output** – confirm `.gitignore` still excludes `build/`, `.gradle/`, `.idea/`
5. **Robolectric memory leaks** – never keep global state in test classes; use `@Config` with `sdk = 34`
6. **Missing error handling** – always wrap operations with appropriate error handling components
7. **Concurrency issues** – use ConcurrencyController for all database operations
8. **Performance problems** – implement pagination and background processing for large datasets

## 9. Changelog Management

From version `0.27.0` onward, changelog entries use the full `[MAJOR.MINOR.PATCH]` format. Gather changes under the next release heading and bump `versionName` in `app/build.gradle` only when cutting a release (patch/minor/major). The current version is `0.29.0`.

**IMPORTANT**: When updating the changelog, also bump version numbers as explicitly described in the documentation, and keep `README.md`, `AGENTS.md`, and `CHANGELOG.md` in sync.

## 10. Documentation Standards

### New Documentation Structure
* **docs/DOCUMENTATION_INDEX.md** - Main documentation index
* **docs/PROJECT_OVERVIEW.md** - Comprehensive project overview
* **docs/INSTALLATION.md** - Detailed installation guide
* **docs/USER_MANUAL.md** - Complete user guide
* **docs/DEVELOPMENT.md** - Development guidelines
* **docs/TESTING_STRATEGY.md** - Testing approach and procedures

### Documentation Updates
* Update documentation for any new features or API changes
* Maintain consistency with current version (0.28.0)
* Include code examples and usage patterns
* Document all enterprise features and procedures

## 11. Testing Procedures

### Unit Testing
* **Test Coverage**: Aim for 80%+ test coverage
* **Test Infrastructure**: Use existing test infrastructure in `/app/src/test`
* **MockK Integration**: Use MockK for mocking final classes like ConcurrencyController
* **Robolectric**: Use for Android framework testing without emulator

### Integration Testing
* **Database Testing**: Test database operations with TestDatabaseContainer
* **Error Handling**: Test error scenarios and recovery mechanisms
* **Performance Testing**: Validate performance with large datasets
* **Security Testing**: Test encryption and authentication features

### UI Testing
* **Compose Testing**: Use ComposeTestEnvironment for UI component testing
* **Navigation Testing**: Test navigation flows and deep linking
* **Accessibility Testing**: Ensure accessibility compliance
* **Theme Testing**: Test dark/light theme switching

## 12. Production Readiness Checklist

Before any production release:
* [ ] All enterprise features implemented and tested
* [ ] Error handling covers all failure scenarios
* [ ] Performance optimized for large datasets
* [ ] Security features properly configured
* [ ] Offline functionality tested
* [ ] Concurrency safety validated
* [ ] Documentation updated and complete
* [ ] All tests passing with good coverage
* [ ] Lint shows no warnings
* [ ] Release signing configured

---

*End of AGENTS.md*
