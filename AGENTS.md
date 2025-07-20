# AGENTS.md – TutorBillingApp Guidance for Autonomous Coding Agents
<!-- Keep this file <300 lines so agents can parse it on every run. -->

## 1. Project Overview
TutorBillingApp is a Jetpack-Compose Android app that tracks tutoring sessions and issues invoices.  
Tech stack: **Kotlin 2.1.10**, **Android Gradle Plugin 8.8.0**, **Room**, **Hilt**, **DataStore**, **Robolectric** for JVM tests.
Agents must prioritise reproducible builds, test-first commits, and Jetpack security best practices.

## 2. Environment Setup
```bash
# 1 · Install JDK 17 (or higher) on PATH.
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

*Instrumentation tests* are not wired yet; skip `connectedAndroidTest` unless an emulator is available.

## 4. Code Standards

* **Language level**: Kotlin JVM 17 (`kotlin.jvm.target=17` in *gradle.properties*).
* **Formatting**: Use `ktfmt` or IntelliJ default; no tabs; 120-char line cap.
* **Compose**: Prefer `@Stable` data classes; pass `Modifier` as first optional param.
* **Room**: DAO methods return `Flow<>`; migrations handled via `autoMigrations`.
* **Dependency-Injection**: All ViewModels live under `gr.tsambala.tutorbilling.ui.*` and are Hilt-annotated.

## 5. Directory & Naming Conventions

| ------------------------------ | ------------------------------------------------------ |
| `/app/src/main`                | Production code (namespace `gr.tsambala.tutorbilling`) |
| `/app/src/test`                | JVM unit tests (Robolectric)                           |
| `/data/schemas`                 | Room JSON schemas (auto-generated; keep under VC)      |
| `build/`, `.gradle/`, `.idea/` | **Ignored** – see `.gitignore`                         |
| `local.properties`             | **Ignored** – SDK path, never commit                   |
| `CHANGELOG.md`                 | Project changelog; update the Unreleased section |

## 6. Pull-Request Messaging Template

```
[feat|fix|refactor](scope): short summary

- WHAT changed
- WHY it matters
- HOW to test (`./gradlew test`)
```

Checklist:
* [ ] `CHANGELOG.md` updated

* [ ] Unit tests passing
* [ ] Lint shows **0** new warnings
* [ ] No TODOs or commented-out code left
* [ ] `./gradlew assemble` succeeds on CI

## 7. Common Pitfalls

1. **ANDROID_HOME not set** – always source the profile written by `setup-android-sdk.sh`.
2. **Out-of-date Gradle wrapper** – update with `./gradlew wrapper --gradle-version 8.10.2` when bumping AGP.
3. **Room schema drift** – run `./gradlew test` after changing entities to auto-regenerate `/data/schemas`.
4. **Accidentally committed build output** – confirm `.gitignore` still excludes `build/`, `.gradle/`, `.idea/`.
5. **Robolectric memory leaks** – never keep global state in test classes; use `@Config` with `sdk = 34`.

## 8. Changelog
Update `CHANGELOG.md` under the repository root with a bullet for each pull request under the **Unreleased** section.

---

*End of AGENTS.md*
