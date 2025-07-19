# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]
- Add rebuild walkthrough in `rebuild.md` describing detailed steps.
- Add domain and data modules for clean architecture foundation.
- Move Room and repository code into `data` module and business utilities into `domain`.
- Fix launcher icon references in the AndroidManifest.
- Add CI workflow to run clean, assemble, test and lint on every push.
- Upgrade to Android Gradle Plugin 8.8.0 and Gradle 8.10.2.
- Enable Play App Signing release builds with keystore placeholders.
- Make release signingConfig conditional on keystore properties.
- Integrate Firebase Crashlytics for runtime crash reporting.
- Optional Google Sign-In with account persisted in DataStore.
- In-app privacy policy screen linked from Settings.
- Provide Hilt modules for DAOs and repositories.
- Expose domain use-cases and inject them into ViewModels.

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
