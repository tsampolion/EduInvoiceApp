# TutorBillingApp

TutorBillingApp is an Android application for managing tutoring sessions and invoices. It uses Jetpack Compose for the UI and Room as the local database.

## Features

- Manage students grouped by class
- Track lessons with billing information
- Generate PDF invoices from selected lessons
- Monitor revenue and past invoices
- Theme and preference settings via DataStore

## Prerequisites

- **JDK 17 or newer** installed and available on your `PATH`.
- **Android SDK** with API level 35 and build tools. The repository provides a helper script that installs the required SDK packages.

## Mandatory Android SDK setup

The project cannot be built until the Android SDK is installed. Use the provided setup script to download the required packages and configure the environment:

```bash
bash setup-android-sdk.sh
source ~/.profile
java -version
```

Running the script once will download the command line tools and create a `local.properties` file pointing Gradle to the SDK. Make sure to source your profile before invoking any Gradle tasks so that the `ANDROID_HOME` variables are available.

## Building the project

Use the Gradle wrapper to build, lint and test the app. Run the commands in this
order so caches and schemas are refreshed correctly:

```bash
./gradlew clean
./gradlew assemble
./gradlew test
./gradlew lintDebug
```

## Modules overview

The project is structured as a set of Gradle modules so each layer can be tested
in isolation:

- **app** – Compose UI and navigation.
- **domain** – business logic and use-cases.
- **data** – Room database, repositories, DataStore.

## Database migrations

Room is configured with `autoMigrations` for database version upgrades. The generated schema files are stored under `data/schemas` via the `room.schemaLocation` Gradle argument. Manual SQL scripts under `app/src/main/assets/migrations` are no longer required.

### Updating `AutoMigrationSpec` classes

When you modify an entity schema:

1. Bump the `version` in `TutorBillingDatabase`.
2. Update the existing `AutoMigrationSpec` in `AutoMigrations.kt` or create a new one.
   - Annotate the class with helpers such as `@RenameColumn` or `@DeleteColumn`.
   - Override `onPostMigrate` for SQL statements that Room cannot generate.
3. Register the spec in the `autoMigrations` array of `TutorBillingDatabase`.
4. Rebuild the project so that Room outputs the updated JSON schema under `data/schemas`.

## Changelog

A high level summary of changes lives in [`CHANGELOG.md`](CHANGELOG.md). Update the **Unreleased** section with a bullet point when opening a pull request.
