# Architecture Boundaries

This document defines the strict boundaries between layers in the EduInvoice app to maintain clean architecture principles.

## Layer Dependencies

```
App Layer → Domain Layer → Data Layer
```

### App Layer (`app/`)
- **Can depend on**: Domain layer, external libraries, Android framework
- **Cannot depend on**: Data layer (models, database, repositories, DAOs, mappers)
- **Must use**: Domain models for all business logic

### Domain Layer (`domain/`)
- **Can depend on**: External libraries, Kotlin standard library
- **Cannot depend on**: Data layer, App layer
- **Contains**: Business logic, use cases, domain models

### Data Layer (`data/`)
- **Can depend on**: Domain layer, external libraries
- **Cannot depend on**: App layer
- **Contains**: Database entities, repositories, DAOs, mappers

## Enforcement Mechanisms

### 1. Detekt Rules
The `config/detekt/detekt.yml` file contains forbidden import rules that prevent:
- `gr.eduinvoice.data.model.*`
- `gr.eduinvoice.data.database.*`
- `gr.eduinvoice.data.adapter.*`
- `gr.eduinvoice.data.concurrency.*`
- `gr.eduinvoice.data.repository.*`
- `gr.eduinvoice.data.dao.*`
- `gr.eduinvoice.data.mapper.*`

### 2. Architecture Tests
- `AppLayerDependenciesTest`: Ensures app layer doesn't import data packages
- `DomainLayerUsageTest`: Verifies proper use of domain models
- `ArchitectureValidationTest`: Comprehensive validation of all boundaries

### 3. Data Layer Visibility
All data layer classes are marked as `internal` to prevent accidental usage from other modules:
- Data models (`Lesson`, `Student`, `User`, etc.)
- Database classes (`LessonWithStudent`, `DatabaseConstants`, etc.)
- Exceptions (`DatabaseInitException`)

## Migration Guide

When you need to use data from the data layer in the app layer:

1. **Create/Use Domain Models**: Use `DomainLesson`, `DomainStudent`, etc.
2. **Use Use Cases**: Implement business logic in the domain layer
3. **Use Repositories**: Access data through repository interfaces in the domain layer
4. **Never Import Data Models Directly**: Always go through the domain layer

## Running Architecture Tests

```bash
# Run all architecture tests
./gradlew :app:testDebugUnitTest --tests "architecture.*"

# Run specific test
./gradlew :app:testDebugUnitTest --tests "architecture.AppLayerDependenciesTest"
```

## Common Violations to Fix

- ❌ `import gr.eduinvoice.data.model.Lesson`
- ❌ `import gr.eduinvoice.data.database.LessonWithStudent`
- ❌ Using data models in UI components
- ❌ Using data models in ViewModels

## Correct Patterns

- ✅ `import gr.eduinvoice.domain.model.DomainLesson`
- ✅ Using domain models in UI components
- ✅ Using domain models in ViewModels
- ✅ Accessing data through repository interfaces

## Benefits

1. **Maintainability**: Clear separation of concerns
2. **Testability**: Easy to mock domain layer for testing
3. **Flexibility**: Data layer can change without affecting app layer
4. **Scalability**: Easy to add new features without breaking existing code
5. **Team Development**: Different teams can work on different layers independently
