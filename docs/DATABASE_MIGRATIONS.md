# Database Migrations Guide

## Overview

EduInvoiceApp uses **AutoMigrations exclusively** for all database schema changes. Manual migrations are **strictly forbidden** and will cause build failures.

## Migration Strategy

### ✅ **Allowed: AutoMigrations Only**
- **Room AutoMigrations**: Simple schema changes (add/remove columns, tables)
- **Custom AutoMigrationSpec**: Complex migrations requiring custom SQL logic
- **Schema Export**: Automatic schema generation for version control

### ❌ **Forbidden: Manual Migrations**
- `Migration` classes
- `.addMigrations()` calls
- Manual SQL execution in migration classes
- Custom migration logic outside AutoMigrationSpec

## Current AutoMigrations

| Version | Type | Description |
|---------|------|-------------|
| 5→6 | Custom | Recreate lessons table with constraints |
| 6→7 | Auto | Simple schema changes |
| 7→8 | Auto | Simple schema changes |
| 8→9 | Auto | Simple schema changes |
| 9→10 | Auto | Simple schema changes |
| 10→11 | Auto | Simple schema changes |
| 11→12 | Auto | Simple schema changes |
| 12→13 | Custom | Add ownerId columns for multi-user support |
| 13→14 | Auto | Simple schema changes |
| 14→15 | Custom | Add lastModified columns |
| 15→16 | Custom | Add group fields (className, rate, rateType) |
| 16→17 | Custom | Add isActive to groups |
| 17→18 | Custom | Create group lesson master and absences tables |
| 18→19 | Custom | Add masterId to lessons |
| 19→20 | Custom | Create invoice_master table |
| 20→21 | Custom | Add payment batch and reschedule tables |
| 21→22 | Custom | Add role column to users table |

## Adding New Migrations

### 1. **Simple Schema Changes**
Room will automatically handle:
- Adding/removing columns
- Adding/removing tables
- Simple data type changes

### 2. **Complex Migrations**
Create a custom `AutoMigrationSpec`:

```kotlin
class AutoMigration22To23 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        // Custom SQL logic here
        db.execSQL("ALTER TABLE table_name ADD COLUMN new_column TEXT")
    }
}
```

### 3. **Update Database Configuration**
Add to `EduInvoiceDatabase.kt`:

```kotlin
@Database(
    // ... other config
    autoMigrations = [
        // ... existing migrations
        AutoMigration(from = 22, to = 23, spec = AutoMigration22To23::class)
    ]
)
```

### 4. **Bump Version**
Update `version = 23` in the `@Database` annotation.

## Build-Time Protection

### Detekt Rules
The build system includes custom rules that:
- Detect manual migration classes
- Prevent `.addMigrations()` calls
- Fail builds if manual migrations are detected

### Error Messages
```
Manual migrations are banned. Use AutoMigrationSpec instead.
```

## Best Practices

### ✅ **Do**
- Use Room's automatic schema detection
- Create custom `AutoMigrationSpec` for complex changes
- Test migrations with sample data
- Document complex migration logic
- Keep migrations atomic and focused

### ❌ **Don't**
- Create `Migration` classes
- Use `.addMigrations()` in database builders
- Execute raw SQL outside `AutoMigrationSpec`
- Skip schema export
- Create migrations that can't be rolled back

## Testing Migrations

### 1. **Unit Tests**
```kotlin
@Test
fun testMigration22To23() {
    val db = Room.inMemoryDatabaseBuilder(
        context,
        TestDatabase::class.java
    ).build()
    
    // Test migration logic
    db.close()
}
```

### 2. **Integration Tests**
- Test with real database files
- Verify data integrity after migration
- Test rollback scenarios

## Troubleshooting

### Common Issues

1. **Migration Conflicts**
   - Ensure version numbers are sequential
   - Check for duplicate migration specs
   - Verify schema compatibility

2. **Build Failures**
   - Check for manual migration classes
   - Verify AutoMigrationSpec syntax
   - Ensure all imports are correct

3. **Runtime Errors**
   - Test migrations with sample data
   - Verify database state before migration
   - Check for data type mismatches

## Migration History

### Version 0.29.0
- **Consolidated to AutoMigrations only**
- **Removed all manual migrations**
- **Added build-time protection**
- **Updated documentation**

### Previous Versions
- Used hybrid approach (AutoMigrations + Manual)
- Caused confusion and potential conflicts
- Required manual maintenance of migration files

## Support

For migration-related issues:
1. Check this documentation
2. Review existing AutoMigrationSpec examples
3. Consult Room documentation
4. Create issue with migration details

---

**Remember**: Manual migrations are banned. Always use AutoMigrations for database schema changes.
