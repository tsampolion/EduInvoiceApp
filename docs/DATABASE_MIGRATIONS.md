# Database Migrations (Development)

## Current policy: Re-baselined to version 1

During active development, the database has been re-baselined to `@Database(version = 1)` with no migrations. Debug builds enable destructive migration so schema edits recreate the database automatically. Schema snapshots are exported to `data/schemas/.../1.json` on build.

When preparing a release, reintroduce migrations starting from 1→2, 2→3, ... and commit each new schema JSON.

## Adding migrations post re-baseline (when releasing)

1) Bump the Room version in `EduInvoiceDatabase.kt` from 1 to 2.
2) Prefer AutoMigrations. If custom logic is needed, use `AutoMigrationSpec`.
3) Build to export `2.json` and commit it.
4) Test upgrades using a pre-1 database if applicable, otherwise start from 1.

## Build-time protection

Detekt rule `NoManualMigrations` forbids manual `Migration` classes and `.addMigrations()` usage. Use AutoMigrations or re-baseline while unshipped.

## Notes

- Because the app is unshipped, older schema JSONs and migration specs were removed.
- Before shipping, freeze v1 as the baseline and add proper migrations for subsequent versions.
