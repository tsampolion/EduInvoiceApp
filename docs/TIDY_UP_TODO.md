# EduInvoiceApp Tidy-Up Plan and To‑Do (Executable Tracking)

This is the single source of truth for the tidy-up/refactor effort. Use the checkboxes below to track progress. Keep PRs small and incremental; each section is intended to be one PR unless noted.

Conventions
- [ ] = open, [x] = done
- PR gates: run exactly these after each PR: `./gradlew clean`, `./gradlew assemble`, `./gradlew test`, `./gradlew lintDebug`

---

## 0) Pre-flight (create tracking branch)
- [ ] Create branch `refactor/tidy-up-foundation`
- [ ] Ensure a clean baseline build and tests pass

Acceptance
- [ ] `assemble`, `test`, `lintDebug` succeed on baseline

---

## 1) Remove duplicate app-layer “domain” types + fix invoice URI bug (Critical)
Scope
- Remove app duplicates that shadow domain contracts/types
- Fix `InvoiceScreen` URI usage to return a `content://` from `FileProvider`

Tasks
- [ ] Delete `app/src/main/java/gr/eduinvoice/utils/DomainInvoiceData.kt`
- [ ] Delete `app/src/main/java/gr/eduinvoice/utils/DomainPdfGenerator.kt`
- [ ] Search and replace imports so all code uses `gr.eduinvoice.domain.billing.DomainInvoiceData`
- [ ] In `app/src/main/java/gr/eduinvoice/ui/invoice/InvoiceScreen.kt`: on PDF success, convert the returned path to a `FileProvider` `content://` Uri; stop calling `Uri.parse(path)` and `toFile()`
- [ ] Collapse duplicate `catch (Exception)` blocks in `app/src/main/java/gr/eduinvoice/MainActivity.kt`
- [ ] Add an architecture unit test to forbid classes in `app` whose simple name starts with `Domain` (to prevent reintroduction)

Acceptance
- [ ] Build + tests run green
- [ ] Manual verification: invoice creation → Share and Print both work without `FileUriExposedException`

---

## 2) Consolidate PDF implementation to a single path (High)
Scope
- Keep a single implementation that satisfies the domain interface
- Remove legacy/parallel implementations that are unused

Tasks
- [ ] Keep: `app/src/main/java/gr/eduinvoice/utils/AndroidPdfGenerator.kt`
- [ ] Keep: `app/src/main/java/gr/eduinvoice/utils/AndroidPdfComponents.kt`
- [ ] Keep: `app/src/main/java/gr/eduinvoice/utils/PdfFilePrintAdapter.kt`
- [ ] Remove if unused (verify by grep before deleting):
  - [ ] `app/src/main/java/gr/eduinvoice/utils/PdfGenerator.kt`
  - [ ] `app/src/main/java/gr/eduinvoice/utils/ModernPdfGenerator.kt`
  - [ ] `app/src/main/java/gr/eduinvoice/utils/ModernPdfComponents.kt`
  - [ ] `app/src/main/java/gr/eduinvoice/utils/ModernPdfTheme.kt`
  - [ ] `app/src/main/java/gr/eduinvoice/utils/PdfThemeManager.kt`
  - [ ] `app/src/main/java/gr/eduinvoice/utils/OptimizedPdfGenerator.kt` and `GlobalPdfGenerator` (unless explicitly needed for chunked progress)
- [ ] Update/trim tests to exercise the consolidated `AndroidPdfGenerator`

Acceptance
- [ ] No references remain to removed PDF classes
- [ ] Invoice generation works as before; snapshot tests updated if needed

---

## 3) Replace global singletons with DI (High)
Scope
- Remove `GlobalBackgroundProcessor` and (if kept) `GlobalPdfGenerator`
- Provide instances via Hilt and inject where needed

Tasks
- [x] Introduce an app DI module (e.g., `app/src/main/java/gr/eduinvoice/di/AppModule.kt`) that:
  - [x] `@Provides @Singleton` binds `DomainPdfGenerator` to `AndroidPdfGenerator`
  - [x] `@Provides @Singleton` provides `BackgroundProcessor` (if still used)
- [x] Replace usages of `GlobalBackgroundProcessor` in `MainActivity` (no explicit initialization)
- [x] Remove the `GlobalPdfGenerator` object (and references) from code paths we own
- [x] Replace any remaining `GlobalBackgroundProcessor` usages with injected instance or `viewModelScope` (removed `OptimizedPdfGenerator` which depended on it)

Acceptance
- [ ] No references to `GlobalBackgroundProcessor` or `GlobalPdfGenerator`
- [ ] Hilt graph compiles; UI and PDF flows still work

---

## 4) Move `SearchHistoryRepository` to the data layer (High)
Scope
- Re-home DataStore-backed repository out of `app/utils`

Tasks
- [x] Create `domain` interface (e.g., `gr.eduinvoice.domain.user.SearchHistoryRepository`) with the minimal API used by UI
- [x] Move implementation to `data/src/main/java/.../repository/SearchHistoryRepository.kt`
- [x] Bind via Hilt: `@Binds @Singleton` from data implementation to domain interface
- [x] Update UI imports to depend on the domain interface only

Acceptance
- [ ] App compiles; history feature behaviour unchanged
- [ ] No DataStore references in `app/` except DI consumption

---

## 5) Convert `MainActivity` to full Compose scaffold (High)
Scope
- Remove hybrid XML + Compose; use Compose `ModalNavigationDrawer` and Compose menu

Tasks
- [x] Replace `activity_main.xml` usage with a Compose-only layout
- [x] Remove `NavigationView` and XML `Toolbar` dependencies
- [x] Implement drawer/menu in Compose (mirroring `menu_drawer` entries)
- [ ] Delete or archive `activity_main.xml` and unused resources
- [x] Remove code that sets/reads XML toolbar visibility

Acceptance
- [ ] UI parity validated manually; navigation/drawer function with Compose-only
- [ ] No XML layout inflation in `MainActivity`

---

## 6) Right-size `utils/` by concern and layer (Medium)
Scope
- Move files to appropriate packages or modules; keep `utils/` minimal

Tasks
- [ ] Move analytics-like helpers to `app/src/main/java/gr/eduinvoice/analytics/`
- [ ] If `BackgroundProcessor` is retained, consider move to `app/core/concurrency/` (or delete in favor of `viewModelScope`)
- [ ] Move cache-like/data helpers to `data/` when not UI-bound
- [ ] Introduce `app/pdf/` package for remaining PDF classes

Acceptance
- [ ] `utils/` contains only generic, UI-bound helpers

---

## 7) Make `domain/` a pure Kotlin module (Medium)
Scope
- Enforce no Android dependencies; speed up builds

Tasks
- [ ] Switch `domain/build.gradle` to Kotlin/JVM plugin
- [ ] Remove AndroidX test libraries in `domain`
- [ ] Ensure tests compile and run as JVM tests

Acceptance
- [ ] `domain` builds/tests without Android plugin

---

## 8) Version catalog + plugin management (Medium)
Scope
- Centralize dependency versions

Tasks
- [ ] Introduce `gradle/libs.versions.toml` and migrate dependencies
- [ ] Move plugin versions to `settings.gradle` `pluginManagement` where applicable
- [ ] Align Firebase and Compose via BoMs only

Acceptance
- [ ] Root and modules build using cataloged versions; CI green

---

## 9) Architecture tests hardening (Medium)
Scope
- Prevent regressions on boundaries

Tasks
- [ ] Test to forbid `app` importing from `data.model`/`data.database` (already present) — keep
- [ ] Add test to forbid classes in `app` package starting with `Domain`
- [ ] Add test to flag `object` singletons named `Global*` in `app`

Acceptance
- [ ] New tests fail on the old state, pass on the refactored state

---

## 10) Root cleanup (Low)
Scope
- Repo hygiene

Tasks
- [ ] Ensure `test-reports/` is ignored (or removed if committed)
- [ ] Update `README.md` with the new architecture notes and PDF path
- [ ] Optionally add a short `docs/ARCHITECTURE_TIDY_UP.md` summarizing the changes

Acceptance
- [ ] Clean repo; docs updated

---

## Running list of decisions (to be updated)
- [ ] Keep only `AndroidPdfGenerator` as the PDF engine; remove legacy PDF classes (unless a real need for chunked generation emerges)
- [ ] Prefer `viewModelScope` over a custom `BackgroundProcessor`; if kept, DI-provide it
- [ ] Move `SearchHistoryRepository` behind a domain interface and house the implementation in `data`

---

## Rollback notes
- Each PR should be revertible independently
- File deletions are safe; keep them in separate commits from logic edits


