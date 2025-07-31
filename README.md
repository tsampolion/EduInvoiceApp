# EduInvoice

EduInvoice is an Android application for managing tutoring sessions and invoices. It uses Jetpack Compose for the UI and Room as the local database.

## Features

- Manage students grouped by class
- Organize students into groups and schedule group lessons
- Track lessons with billing information
- Generate PDF invoices from selected lessons
- Monitor revenue and past invoices
- Theme and preference settings via DataStore
- Backup and restore your data to JSON
- SQLCipher-encrypted database
- Sliding navigation drawer for quick navigation between screens

## Prerequisites

- **JDK 17 or newer** installed and available on your `PATH`.
- **Android SDK** with API level 35 and build tools. The repository provides a helper script that installs the required SDK packages.
- SQLCipher native libraries are pulled via Gradle; no manual setup is needed.

## Mandatory Android SDK setup

The project cannot be built until the Android SDK is installed. Use the provided setup script to download the required packages and configure the environment:

```bash
bash setup-android-sdk.sh
source ~/.profile
java -version
```

Running the script once will download the command line tools and create a `local.properties` file pointing Gradle to the SDK. Make sure to source your profile before invoking any Gradle tasks so that the `ANDROID_HOME` variables are available.

## Firebase API key

The `app/google-services.json` file has the API key stripped out. Provide the key in your `local.properties` using the `FIREBASE_API_KEY` property so it is compiled into `BuildConfig`. Never commit actual keys to version control.

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

## Backup & Migration

Use the Settings screen to export your entire database to a JSON file and restore it later. When upgrading from versions prior to 0.21.9, create a backup first and restore it after updating so the new SQLCipher encrypted database is populated.

## Database migrations

Room is configured with `autoMigrations` for database version upgrades. The generated schema files are stored under `data/schemas` via the `room.schemaLocation` Gradle argument. Manual SQL scripts under `app/src/main/assets/migrations` are no longer required.

### Updating `AutoMigrationSpec` classes

When you modify an entity schema:

1. Bump the `version` in `EduInvoiceDatabase`.
2. Update the existing `AutoMigrationSpec` in `AutoMigrations.kt` or create a new one.
   - Annotate the class with helpers such as `@RenameColumn` or `@DeleteColumn`.
   - Override `onPostMigrate` for SQL statements that Room cannot generate.
3. Register the spec in the `autoMigrations` array of `EduInvoiceDatabase`.
4. Rebuild the project so that Room outputs the updated JSON schema under `data/schemas`.

## Changelog

A high level summary of changes lives in [`CHANGELOG.md`](CHANGELOG.md).
The current version is `0.22.0`. Starting with this release, EduInvoice
follows [Semantic Versioning](https://semver.org) in the form
`MAJOR.MINOR.PATCH`. Bump the version only when making a new release and
record changes under the corresponding heading in the changelog.