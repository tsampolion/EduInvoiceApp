# AGENTS.md – EduInvoice Guidance for Autonomous Coding Agents
<!-- Keep this file <300 lines so agents can parse it on every run. -->

## 1. Project Overview
EduInvoice is a Jetpack-Compose Android app that tracks tutoring sessions and issues invoices.  
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
* **Dependency-Injection**: All ViewModels live under `gr.eduinvoice.ui.*` and are Hilt-annotated.

## 5. Directory & Naming Conventions

| ------------------------------ | ------------------------------------------------------ |
| `/app`                         | Android application module |
| `/domain`                      | Pure Kotlin business logic |
| `/data`                        | Database and repository layer |
| `/app/src/main`                | Production code (namespace `gr.eduinvoice`) |
| `/app/src/test`                | JVM unit tests (Robolectric) |
| `/data/schemas`                | Room JSON schemas (auto-generated; keep under VC) |
| `build/`, `.gradle/`, `.idea/` | **Ignored** – see `.gitignore`                         |
| `local.properties`             | **Ignored** – SDK path, never commit                   |
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

## 7. Common Pitfalls

1. **ANDROID_HOME not set** – always source the profile written by `setup-android-sdk.sh`.
2. **Out-of-date Gradle wrapper** – update with `./gradlew wrapper --gradle-version 8.10.2` when bumping AGP.
3. **Room schema drift** – run `./gradlew test` after changing entities to auto-regenerate `/data/schemas`.
4. **Accidentally committed build output** – confirm `.gitignore` still excludes `build/`, `.gradle/`, `.idea/`.
5. **Robolectric memory leaks** – never keep global state in test classes; use `@Config` with `sdk = 34`.

## 8. Changelog
From version `0.22.0` onward, changelog entries use the full
`[MAJOR.MINOR.PATCH]` format. Gather changes under the next release
heading and bump `versionName` in `app/build.gradle` only when cutting a
release (patch/minor/major). The current version is `0.22.0`.

## 9. Guiding Principles for Task Stub Generation

This document provides a set of guiding principles for all agents, including AI assistants like Codex, when creating new task stubs for this project. The goal is to ensure all tasks are thorough, actionable, and verifiable. All generated task stubs **must** adhere to the following principles.

### 1. The Principle of Decomposition

Large features must be broken down into smaller, logical, and atomic task stubs. Avoid creating single, monolithic tasks for complex features. A good task represents a single, verifiable unit of work that can be completed in one pull request.

* **DO:** Create separate tasks for UI layout, business logic, data models, and resource creation.
* **DON'T:** Create a single task called "Implement Login Screen."

### 2. The Principle of Specificity

Tasks must be unambiguous and provide concrete details. The agent is expected to analyze the existing codebase to provide accurate file paths, class names, and method signatures.

* **DO:** Include specific file paths (e.g., `app/src/main/res/layout/activity_main.xml`).
* **DO:** Suggest specific component IDs, class names, or method names.
* **DO:** Include short, illustrative code snippets in XML or Kotlin where appropriate.
* **DON'T:** Use vague instructions like "update the layout" or "add the logic."

### 3. The Principle of Verifiability

Every task must include a clear set of acceptance criteria. This is a checklist that a developer or QA engineer can use to confirm that the task is 100% complete and correct.

* **DO:** List 2-5 specific, testable outcomes.
* **DON'T:** Finish a task description without defining what "done" means.

---

### The Golden Standard: Task Stub Template

All generated stubs must follow this Markdown template:

**Task Title:** [A concise, one-line summary of the work]

* **File(s) to Create/Modify:**
    * `[path/to/file_one.kt]`
    * `[path/to/file_two.xml]`
* **Description of Work:**
    * A detailed, step-by-step explanation of the required changes.
    * Use bullet points for clarity.
    * Reference specific IDs, methods, and components.
    * *Example Snippet (if helpful):*
        ```kotlin
        // A short code example
        ```
* **Acceptance Criteria:**
    * [ ] The first condition of "done."
    * [ ] The second condition of "done."
    * [ ] The third condition of "done."

### Good Example

**Task Title:** Update Primary Button Style

* **File(s) to Create/Modify:**
    * `app/src/main/res/values/styles.xml`
    * `app/src/main/res/layout/fragment_login.xml`
* **Description of Work:**
    * In `styles.xml`, create a new style named `AppTheme.Button.Primary`.
    * This style should set the `backgroundColor` to `@color/colorPrimary` and `textColor` to `@color/white`.
    * In `fragment_login.xml`, apply this new style to the button with the ID `@+id/login_button`.
* **Acceptance Criteria:**
    * [ ] A new button style `AppTheme.Button.Primary` exists in `styles.xml`.
    * [ ] The login button in the running app now has the new primary color background.
    * [ ] The login button text is white.

---

*End of AGENTS.md*
