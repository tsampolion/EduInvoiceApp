# Rebuild Walkthrough

This document describes a step-by-step process for performing a complete rebuild of **EduInvoiceApp**. It is intended for contributors who need to restructure the project while keeping builds reproducible and tests passing.

## 1. Environment preparation

1. Install **JDK 17** or higher and ensure it is available on your `PATH`.
2. Run the helper script to install the Android SDK and create `local.properties`:

   ```bash
   bash setup-android-sdk.sh
   source ~/.bashrc         # or the profile printed by the script
   sdkmanager --version
   ```
3. Verify a clean build of the existing project:

   ```bash
   ./gradlew clean
   ./gradlew assemble
   ./gradlew test
   ./gradlew lintDebug
   ```

These Gradle commands match the required build sequence defined in `AGENTS.md`.

## 2. Create a dedicated rebuild branch

Work on a separate branch so the current `main` branch remains stable.

```bash
git checkout -b rebuild
```

## 3. Introduce a modular structure

1. Add new Gradle modules to separate concerns:
   - `:app` – presentation layer with screens, view models and navigation.
   - `:domain` – pure Kotlin module for use-cases and business logic.
   - `:data` – Room database, repositories and DataStore.
2. Update `settings.gradle` to include the modules and create basic `build.gradle` files for each.
3. Move existing source files into the appropriate modules. Build after each move to ensure the project still compiles.

## 4. Dependency injection

Keep using Hilt but provide modules in the data layer to expose DAOs and repositories. View models in the app module depend only on domain use-cases, not directly on DAOs.

## 5. Database revisions

1. Review the migration history in `EduInvoiceDatabase` and flatten earlier migrations into a single schema representing the latest version.
2. Keep `AutoMigrationSpec` classes for users upgrading from an older version.
3. Store generated schema JSON under `data/schemas` so it is tracked in version control.

## 6. Compose UI refactoring

1. Break large screens into smaller composables. Each composable should accept `Modifier` as its first optional parameter.
2. Provide a design system with consistent colors, typography and shapes.

## 7. Domain use-cases and testing

1. Implement use-case classes in the domain module such as `AddLesson`, `GetStudentLessons` and `UpdateLesson`.
2. Update view models to invoke these use-cases.
3. Expand unit tests:
   - Use in-memory Room databases for DAO tests in the data module.
   - Test each use-case in the domain module.
   - Test view models with state flows using `turbine` or `collectAsState`.

Run the full build after adding tests:

```bash
./gradlew clean
./gradlew assemble
./gradlew test
./gradlew lintDebug
```

All tests and lint checks must pass before committing.

## 8. Documentation

1. Update `README.md` to reflect the new module layout and the build commands above.
2. Document any additional project rules in `AGENTS.md` if required.
3. Mention this rebuild guide in `CHANGELOG.md` under the appropriate version entry.

## 9. Final verification

After all modules compile and tests pass, run the app on an emulator to verify that the rebuild behaves as expected. Once satisfied, create a pull request back to `main` with a summary of the changes and instructions to run the build commands.

