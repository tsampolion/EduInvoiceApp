- [ ] [Setup] Confirm JDK 17 is configured for Gradle and Android Studio
- [ ] [Setup] Verify Gradle Wrapper is pinned (8.10.2) in gradle/wrapper/gradle-wrapper.properties
- [ ] [Setup] Ensure repositories include google() and mavenCentral() in all modules
- [ ] [Setup] Validate environment variables (JAVA_HOME -> JDK17) and Gradle JVM set in IDE
- [ ] [Setup] Enable Gradle build cache and parallel builds in gradle.properties
- [ ] [Setup] Disable deprecated flags in gradle.properties and settings.gradle

- [ ] [Build] Fix non-existent dependencies (e.g., LeakCanary 2.14 applied) and verify resolution
- [ ] [Build] Generate a fresh build profile (./gradlew build --profile) and archive HTML report
- [ ] [Build] Identify top 5 slow tasks from the profile and record actions
- [ ] [Build] Enable KSP incremental processing (ksp.incremental=true) and verify applied
- [ ] [Build] Add test logging for passed/skipped/failed with stack traces for all Test tasks
- [ ] [Build] Resolve Spotless serialization error on :app:spotlessKotlinGradle
- [ ] [Build] Upgrade Spotless plugin (pin to latest stable) and align formatting rules
- [ ] [Build] Unify line-endings policy in Spotless to avoid Git attributes conflicts
- [ ] [Build] Validate kover plugin works for all modules without variant ambiguity
- [ ] [Build] Add a top-level build health task alias (e.g., :quality) to run lint+detekt+spotless+tests

- [ ] [Static Analysis] Expand detekt.yml using recommended + formatting + complexity rules
- [ ] [Static Analysis] Create detekt-baseline for legacy issues, target new issues to fail CI
- [ ] [Static Analysis] Ensure detekt runs for all modules and publishes HTML/SARIF reports
- [ ] [Static Analysis] Ensure Spotless runs on Kotlin and Gradle files (kts) with consistent targets
- [ ] [Static Analysis] Add ktlint engine via Spotless (if not already) for Kotlin style

- [ ] [Dependencies] Run dependency audit (dependencyUpdates) and save report.json
- [ ] [Dependencies] Triage updates into: safe-stable, milestone/alpha, breaking-risk
- [ ] [Dependencies] Prepare PR 1: Safe platform bumps (Firebase BoM, Material, minor bugfix libs)
- [ ] [Dependencies] Prepare PR 2: Hilt (2.57.1), align hilt-android + compiler + plugin
- [ ] [Dependencies] Prepare PR 3: Kotlin Coroutines/Test (1.10.2) and KotlinX Serialization JSON (1.9.0)
- [ ] [Dependencies] Prepare PR 4: Compose BoM upgrade plan (validate Compose Compiler/Kotlin compatibility)
- [ ] [Dependencies] Prepare PR 5: Navigation, Lifecycle, Room minor upgrades (post-Compose verification)
- [ ] [Dependencies] Verify app builds, runs instrumentation tests after each PR before merging

- [ ] [Domain Tests] Confirm domain/build.gradle applies plugins: kotlin.jvm, kotlin.serialization, ksp
- [ ] [Domain Tests] Add JUnit 5 (junit-jupiter-api + engine) testImplementation/testRuntimeOnly
- [ ] [Domain Tests] Configure useJUnitPlatform() for all Test tasks in domain
- [ ] [Domain Tests] Ensure mockk and mockk-agent-jvm are present for mocking and inline support
- [ ] [Domain Tests] Migrate all domain test files to JUnit 5 annotations and Assertions
- [ ] [Domain Tests] Replace JUnit4 @Before with @BeforeEach and assertion imports to JUnit 5
- [ ] [Domain Tests] Replace MockK stubbing to coEvery for suspend and Flow-returning functions
- [ ] [Domain Tests] Ensure Flow-returning mocks use flowOf(...) consistently
- [ ] [Domain Tests] Run :domain:test and capture failing specs list
- [ ] [Domain Tests] Fix first failing spec (AddGroupLessonTest) until green
- [ ] [Domain Tests] Iterate through remaining failing specs until :domain:test is green

- [ ] [App Module] Run ./gradlew :app:assemblePlainDebug and :app:assembleEncryptedDebug
- [ ] [App Module] Fix Spotless errors in :app and :data modules (spotlessApply and rules tune)
- [ ] [App Module] Verify Crashlytics/Perf Gradle plugins tasks succeed (no missing google-services)
- [ ] [App Module] Validate resource resolving protections in MainActivity (NotFoundException guard)
- [ ] [App Module] Re-run app build profile; verify KSP tasks time reduced with incremental

- [ ] [CI/CD] Add GitHub Actions (or adapt existing) with matrix: JDK 17, cache Gradle
- [ ] [CI/CD] Job 1: Static checks (detekt, spotlessCheck)
- [ ] [CI/CD] Job 2: Unit tests (domain, data, app) with JUnit XML report upload
- [ ] [CI/CD] Job 3: Assemble APKs (plainDebug), attach artifacts
- [ ] [CI/CD] Job 4: Dependency audit (dependencyUpdates) artifact upload
- [ ] [CI/CD] Gate merges on all green checks

- [ ] [Admin Features] Add admin-only CRUD UI for students/lessons/groups/classes
- [ ] [Admin Features] Protect admin account from deletion and username changes (already partially implemented) — verify end-to-end
- [ ] [Admin Features] Add audit log for admin actions (create/update/delete with timestamp)
- [ ] [Admin Features] Add confirmation dialogs for destructive operations with explicit admin messaging
- [ ] [Admin Features] Add role checks on navigation and ViewModels (defense-in-depth)

- [ ] [UI/UX] Verify sliding drawer stability with simplified state management implementation
- [ ] [UI/UX] Ensure FAB does not overlap content across screens
- [ ] [UI/UX] Standardize paddings and icons for drawer items; verify header logo/name visible
- [ ] [UI/UX] Confirm MasterActionBox used only where intended; remove where excluded by spec
- [ ] [UI/UX] Fix top bar overlap on Student screen (EdgeToEdgeScaffold padding verified)

- [ ] [Security] Validate SQLCipher usage and key handling across encrypted flavor
- [ ] [Security] Ensure StrictMode detectAll() enabled only for debug builds
- [ ] [Security] Add basic tamper/integrity checks and logging (if not already)
- [ ] [Security] Confirm password reset flow (no full name/code), enforce strong password policy

- [ ] [Performance] Startup phase tracing (StartupPerformanceMonitor) — add markers for cold/warm
- [ ] [Performance] Pre-warm DB connection off main thread and validate timing improvements
- [ ] [Performance] Cache heavy images/resources if any
- [ ] [Performance] Re-profile build; target < 50% time reduction for KSP aggregate

- [ ] [Docs] Update CHANGELOG with build tooling changes and admin features
- [ ] [Docs] Update README with setup (JDK 17, Gradle, build flavors) and build commands
- [ ] [Docs] Add CONTRIBUTING with lint/test instructions and CI overview
- [ ] [Docs] Document Users screen (roles, protections, expected capabilities)

- [ ] [Release] Bump versionCode/versionName per CHANGELOG and project policy
- [ ] [Release] Tag release and attach artifacts (APKs, reports) to GitHub Release
- [ ] [Release] Smoke test install on device/emulator for plainDebug

- [ ] [Cleanup] Remove redundant Gradle blocks and comments left from prior attempts
- [ ] [Cleanup] Delete unused resources and images
- [ ] [Cleanup] Close stale branches/PRs related to superseded build changes
