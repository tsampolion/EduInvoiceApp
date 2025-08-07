# Test Quick Reference

## Module Test Distribution

### ✅ **App Module** (`app/src/test/`)
- **Purpose**: Unit tests for app-specific functionality, UI components, ViewModels
- **Infrastructure**: `TestInfrastructure.kt`, `BaseTest.kt`, `TestConfiguration.kt`
- **Categories**: Unit, Integration, UI, Performance, Stress, Accessibility, Security, Validation
- **Status**: ✅ **Organized**

### ✅ **Data Module** (`data/src/test/`)
- **Purpose**: Database tests, DAO tests, Repository tests, Concurrency tests
- **Infrastructure**: `DataTestInfrastructure.kt`, `DataBaseTest.kt`
- **Categories**: DAO, Repository, Concurrency, Database, User, Utils
- **Status**: ✅ **Organized**

### ✅ **Domain Module** (`domain/src/test/`)
- **Purpose**: Use case tests, Business logic tests
- **Infrastructure**: `DomainTestInfrastructure.kt`, `DomainBaseTest.kt`
- **Categories**: Student, Lesson, User, Utils
- **Status**: ✅ **Organized**

### ✅ **Android Tests** (`app/src/androidTest/`)
- **Purpose**: Instrumented tests, UI automation, device testing
- **Infrastructure**: `AndroidTestInfrastructure.kt`, `AndroidBaseTest.kt`
- **Categories**: UI, Integration, Accessibility
- **Status**: ✅ **Organized**

## Key Infrastructure Components

### 1. **Module-Specific Test Infrastructure**
Each module has its own tailored test infrastructure:

| Module | Infrastructure File | Base Test Class | Purpose |
|--------|-------------------|-----------------|---------|
| App | `TestInfrastructure.kt` | `BaseTest.kt` | App-specific utilities and setup |
| Data | `DataTestInfrastructure.kt` | `DataBaseTest.kt` | Data layer utilities and setup |
| Domain | `DomainTestInfrastructure.kt` | `DomainBaseTest.kt` | Domain layer utilities and setup |
| Android | `AndroidTestInfrastructure.kt` | `AndroidBaseTest.kt` | UI automation and device testing |

### 2. **Centralized Configuration**
- **File**: `app/src/test/java/gr/eduinvoice/infrastructure/TestConfiguration.kt`
- **Purpose**: Shared configuration across all modules
- **Categories**: Performance, Stress, DataSize, Database, UI, Security, Accessibility, Validation

### 3. **Test Data Factories**
Each module has its own test data factory:
- `TestInfrastructure.TestDataFactory` (App)
- `DataTestInfrastructure.DataTestDataFactory` (Data)
- `DomainTestInfrastructure.DomainTestDataFactory` (Domain)
- `AndroidTestInfrastructure.AndroidTestDataFactory` (Android)

## Test Categories by Module

### App Module Categories
```
app/src/test/java/gr/eduinvoice/
├── unit/                    # Pure unit tests
│   ├── utils/              # Utility function tests
│   ├── security/           # Security-related tests
│   └── domain/             # Domain logic tests
├── integration/            # Integration tests
│   ├── database/           # Database integration
│   ├── repository/         # Repository integration
│   ├── usecase/            # Use case integration
│   └── UserFlowIntegrationTest.kt
├── ui/                     # UI tests
│   ├── components/         # UI component tests
│   ├── screens/            # Screen-level tests
│   ├── navigation/         # Navigation tests
│   └── accessibility/      # Accessibility tests
├── performance/            # Performance tests
├── stress/                 # Stress tests
└── testinfrastructure/     # Test utilities
```

### Data Module Categories
```
data/src/test/java/gr/eduinvoice/data/
├── dao/                    # DAO tests
├── concurrency/            # Concurrency management tests
├── user/                   # User-related data tests
├── utils/                  # Data utilities tests
├── testinfrastructure/     # Data-specific test infrastructure
└── [Database/Repository]   # Database and repository tests
```

### Domain Module Categories
```
domain/src/test/java/gr/eduinvoice/domain/
├── student/                # Student use case tests
├── lesson/                 # Lesson use case tests
├── user/                   # User use case tests
├── utils/                  # Domain utilities tests
├── testinfrastructure/     # Domain-specific test infrastructure
└── BouncyCastleTestRunner.kt
```

### Android Test Categories
```
app/src/androidTest/java/gr/eduinvoice/
├── ui/                     # UI automation tests
│   ├── components/         # UI component automation
│   ├── screens/            # Screen automation
│   └── navigation/         # Navigation automation
├── integration/            # Device integration tests
├── testinfrastructure/     # Android test infrastructure
└── FakeUserProvider.kt     # Android test utilities
```

## Usage Patterns

### Writing Tests in Each Module

#### App Module Test
```kotlin
class StudentViewModelTest : BaseTest() {
    @Test
    fun `test student list loading`() = runTest {
        val viewModel = StudentViewModel(testEnvironment.studentUseCases)
        viewModel.loadStudents(1L)
        val students = viewModel.students.first()
        assertNotNull(students)
    }
}
```

#### Data Module Test
```kotlin
class StudentDaoTest : DataBaseTest() {
    @Test
    fun `test student insertion`() = runTest {
        val student = createTestStudent()
        val studentId = dataTestEnvironment.studentDao.insert(student)
        assertTrue(studentId > 0)
    }
}
```

#### Domain Module Test
```kotlin
class InsertStudentTest : DomainBaseTest() {
    @Test
    fun `test student insertion use case`() = runTest {
        val student = createTestStudent()
        val studentId = domainTestEnvironment.studentUseCases.insertStudent(student)
        assertTrue(studentId > 0)
    }
}
```

#### Android Test
```kotlin
class StudentScreenTest : AndroidBaseTest() {
    @Test
    fun `test student screen navigation`() {
        composeTestRule.setContent {
            StudentScreen(onNavigateToAddStudent = {}, onNavigateToStudentDetails = {})
        }
        performClick(composeTestRule, "Add Student")
        assertElementExists(composeTestRule, "Add Student")
    }
}
```

## Running Tests

### Module-Specific Commands
```bash
# App module tests
./gradlew :app:test

# Data module tests
./gradlew :data:test

# Domain module tests
./gradlew :domain:test

# Android tests
./gradlew :app:connectedAndroidTest
```

### Category-Specific Commands
```bash
# Unit tests
./gradlew test --tests "*UnitTest*"

# Integration tests
./gradlew test --tests "*IntegrationTest*"

# Performance tests
./gradlew test --tests "*PerformanceTest*"

# UI tests
./gradlew test --tests "*UiTest*"
```

## Key Test Files

### Infrastructure Files
- `app/src/test/java/gr/eduinvoice/infrastructure/TestDatabaseContainer.kt`
- `app/src/test/java/gr/eduinvoice/infrastructure/TestConfiguration.kt`
- `app/src/test/java/gr/eduinvoice/ComprehensiveTestRunner.kt`

### Test Categories
- `app/src/test/java/gr/eduinvoice/integration/UserFlowIntegrationTest.kt`
- `app/src/test/java/gr/eduinvoice/performance/PerformanceTest.kt`
- `app/src/test/java/gr/eduinvoice/stress/StressTest.kt`
- `app/src/androidTest/java/gr/eduinvoice/ui/UiAutomationTest.kt`

### Automation Scripts
- `scripts/run_comprehensive_tests.ps1`

## Benefits of This Organization

### ✅ **Module Isolation**
- Each module tests its own functionality
- No cross-module dependencies in tests
- Clear separation of concerns

### ✅ **Infrastructure Consistency**
- Unified patterns across modules
- Shared configuration and utilities
- Consistent test structure

### ✅ **Maintainability**
- Clear organization and structure
- Reusable components and utilities
- Easy to add new tests

### ✅ **Performance**
- Efficient test execution
- CI-friendly organization
- Parallel test execution support

### ✅ **Quality**
- Comprehensive test coverage
- Consistent testing patterns
- Validation and performance testing

### ✅ **Scalability**
- Supports application growth
- Easy to extend and modify
- Maintains test quality over time
