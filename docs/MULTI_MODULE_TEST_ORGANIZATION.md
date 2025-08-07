# Multi-Module Test Organization

This document outlines the comprehensive test organization implemented across all modules of the EduInvoice Android application, maintaining the multi-module architecture while providing unified test infrastructure.

## Overview

The test suite is organized across multiple modules following Android best practices:
- **App Module**: UI tests, ViewModel tests, app-specific logic
- **Data Module**: Database tests, DAO tests, Repository tests, Concurrency tests
- **Domain Module**: Use case tests, Business logic tests
- **Android Tests**: Instrumented tests, UI automation, device testing

## Module-Specific Test Organization

### 1. App Module (`app/src/test/`)

**Purpose**: Unit tests for app-specific functionality, UI components, and ViewModels.

**Structure**:
```
app/src/test/java/gr/eduinvoice/
├── unit/                           # Pure unit tests
│   ├── utils/                      # Utility function tests
│   │   └── ValidationTest.kt       # Input validation tests
│   ├── security/                   # Security-related tests
│   │   └── SecurityTest.kt         # Authentication, encryption, data isolation
│   └── domain/                     # Domain logic tests
├── integration/                    # Integration tests
│   ├── database/                   # Database integration tests
│   ├── repository/                 # Repository integration tests
│   ├── usecase/                    # Use case integration tests
│   └── UserFlowIntegrationTest.kt  # End-to-end user flow tests
├── ui/                             # UI tests
│   ├── components/                 # UI component tests
│   ├── screens/                    # Screen-level tests
│   ├── navigation/                 # Navigation tests
│   └── accessibility/              # Accessibility tests
│       └── AccessibilityTest.kt    # Screen reader, keyboard navigation
├── performance/                    # Performance tests
│   └── PerformanceTest.kt          # Large dataset, memory usage tests
├── stress/                         # Stress tests
│   └── StressTest.kt               # Concurrent operations, extreme load
└── testinfrastructure/             # Test utilities and infrastructure
    ├── TestInfrastructure.kt       # Unified test infrastructure
    ├── BaseTest.kt                 # Standardized base test class
    └── TestConfiguration.kt        # Centralized test configuration
```

**Key Files**:
- `testinfrastructure/TestInfrastructure.kt` - Unified test infrastructure
- `testinfrastructure/BaseTest.kt` - Standardized base test class
- `testinfrastructure/TestConfiguration.kt` - Centralized configuration

### 2. Data Module (`data/src/test/`)

**Purpose**: Unit tests for data layer components including database operations, DAOs, repositories, and concurrency management.

**Structure**:
```
data/src/test/java/gr/eduinvoice/data/
├── dao/                           # DAO tests
│   ├── StudentDaoTest.kt          # Student DAO operations
│   ├── LessonDaoTest.kt           # Lesson DAO operations
│   ├── GroupDaoTest.kt            # Group DAO operations
│   └── UserDaoTest.kt             # User DAO operations
├── concurrency/                   # Concurrency management tests
│   ├── ConcurrencyControllerTest.kt
│   ├── TransactionManagerTest.kt
│   └── OperationQueueManagerTest.kt
├── user/                          # User-related data tests
│   ├── UserPreferencesRepositoryTest.kt
│   └── PassphraseCryptoTest.kt
├── utils/                         # Data utilities tests
│   ├── NetworkMonitorTest.kt
│   └── ExponentialBackoffTest.kt
├── testinfrastructure/            # Data-specific test infrastructure
│   ├── DataTestInfrastructure.kt  # Data layer test utilities
│   └── DataBaseTest.kt            # Data layer base test class
└── [Database/Repository tests]    # Database and repository tests
```

**Key Files**:
- `testinfrastructure/DataTestInfrastructure.kt` - Data layer test infrastructure
- `testinfrastructure/DataBaseTest.kt` - Data layer base test class

### 3. Domain Module (`domain/src/test/`)

**Purpose**: Unit tests for domain layer components including use cases and business logic.

**Structure**:
```
domain/src/test/java/gr/eduinvoice/domain/
├── student/                       # Student use case tests
│   ├── InsertUpdateStudentTest.kt
│   └── ArchiveRestoreStudentTest.kt
├── lesson/                        # Lesson use case tests
│   ├── AddLessonIntegrationTest.kt
│   ├── AddGroupLessonIntegrationTest.kt
│   ├── GetStudentLessonsTest.kt
│   └── UpdateLessonTest.kt
├── user/                          # User use case tests
│   ├── AuthenticateUserTest.kt
│   └── ResetPasswordTest.kt
├── utils/                         # Domain utilities tests
│   └── StudentExtensionsTest.kt
├── testinfrastructure/            # Domain-specific test infrastructure
│   ├── DomainTestInfrastructure.kt # Domain layer test utilities
│   └── DomainBaseTest.kt          # Domain layer base test class
└── BouncyCastleTestRunner.kt      # Domain test runner
```

**Key Files**:
- `testinfrastructure/DomainTestInfrastructure.kt` - Domain layer test infrastructure
- `testinfrastructure/DomainBaseTest.kt` - Domain layer base test class

### 4. Android Tests (`app/src/androidTest/`)

**Purpose**: Instrumented tests for UI automation, device testing, and integration testing requiring Android context.

**Structure**:
```
app/src/androidTest/java/gr/eduinvoice/
├── ui/                           # UI automation tests
│   ├── components/               # UI component automation
│   ├── screens/                  # Screen automation
│   └── navigation/               # Navigation automation
├── integration/                  # Device integration tests
├── testinfrastructure/           # Android test infrastructure
│   ├── AndroidTestInfrastructure.kt # Android test utilities
│   └── AndroidBaseTest.kt        # Android base test class
└── FakeUserProvider.kt           # Android test utilities
```

**Key Files**:
- `testinfrastructure/AndroidTestInfrastructure.kt` - Android test infrastructure
- `testinfrastructure/AndroidBaseTest.kt` - Android base test class

## Test Infrastructure Components

### 1. Module-Specific Test Infrastructure

Each module has its own test infrastructure tailored to its specific needs:

#### App Module Infrastructure
```kotlin
// app/src/test/java/gr/eduinvoice/testinfrastructure/
object TestInfrastructure {
    fun createTestEnvironment(database: EduInvoiceDatabase): TestEnvironment
    object TestDataFactory { /* App-specific test data */ }
    object TestValidation { /* App-specific validation */ }
    object PerformanceUtils { /* App-specific performance */ }
}
```

#### Data Module Infrastructure
```kotlin
// data/src/test/java/gr/eduinvoice/data/testinfrastructure/
object DataTestInfrastructure {
    fun createDataTestEnvironment(database: EduInvoiceDatabase): DataTestEnvironment
    object DataTestDataFactory { /* Data-specific test data */ }
    object DataTestValidation { /* Data-specific validation */ }
    object DataPerformanceUtils { /* Data-specific performance */ }
}
```

#### Domain Module Infrastructure
```kotlin
// domain/src/test/java/gr/eduinvoice/domain/testinfrastructure/
object DomainTestInfrastructure {
    fun createDomainTestEnvironment(database: EduInvoiceDatabase): DomainTestEnvironment
    object DomainTestDataFactory { /* Domain-specific test data */ }
    object DomainTestValidation { /* Domain-specific validation */ }
    object DomainPerformanceUtils { /* Domain-specific performance */ }
}
```

#### Android Test Infrastructure
```kotlin
// app/src/androidTest/java/gr/eduinvoice/testinfrastructure/
object AndroidTestInfrastructure {
    fun createAndroidTestEnvironment(database: EduInvoiceDatabase): AndroidTestEnvironment
    object AndroidTestDataFactory { /* Android-specific test data */ }
    object UiTestUtils { /* UI automation utilities */ }
    object UiPerformanceUtils { /* UI performance utilities */ }
    object AccessibilityTestUtils { /* Accessibility utilities */ }
}
```

### 2. Base Test Classes

Each module has a standardized base test class:

#### App Module Base Test
```kotlin
abstract class BaseTest {
    protected lateinit var testEnvironment: TestInfrastructure.TestEnvironment
    // App-specific utilities and setup
}
```

#### Data Module Base Test
```kotlin
abstract class DataBaseTest {
    protected lateinit var dataTestEnvironment: DataTestInfrastructure.DataTestEnvironment
    // Data-specific utilities and setup
}
```

#### Domain Module Base Test
```kotlin
abstract class DomainBaseTest {
    protected lateinit var domainTestEnvironment: DomainTestInfrastructure.DomainTestEnvironment
    // Domain-specific utilities and setup
}
```

#### Android Base Test
```kotlin
abstract class AndroidBaseTest {
    protected lateinit var androidTestEnvironment: AndroidTestInfrastructure.AndroidTestEnvironment
    // Android-specific utilities and setup
}
```

## Usage Examples

### Writing a Data Module Test

```kotlin
class StudentDaoTest : DataBaseTest() {

    @Test
    fun `test student insertion`() = runTest {
        val student = createTestStudent()
        val studentId = dataTestEnvironment.studentDao.insert(student)
        
        assertTrue(studentId > 0)
        
        val retrievedStudent = dataTestEnvironment.studentDao.getStudentById(studentId)
        assertNotNull(retrievedStudent)
        assertEquals(student.name, retrievedStudent!!.name)
    }

    @Test
    fun `test database performance`() = runTest {
        val time = measureDatabaseOperationTime {
            repeat(100) {
                dataTestEnvironment.studentDao.insert(createTestStudent())
            }
        }
        
        assertTrue(time < TestConfiguration.Performance.maxInsertionTime)
    }
}
```

### Writing a Domain Module Test

```kotlin
class InsertStudentTest : DomainBaseTest() {

    @Test
    fun `test student insertion use case`() = runTest {
        val student = createTestStudent()
        val studentId = domainTestEnvironment.studentUseCases.insertStudent(student)
        
        assertTrue(studentId > 0)
        
        val retrievedStudent = domainTestEnvironment.studentUseCases.getStudentById(studentId, student.ownerId).first()
        assertNotNull(retrievedStudent)
        assertEquals(student.name, retrievedStudent!!.name)
    }

    @Test
    fun `test business logic performance`() = runTest {
        val time = measureUseCaseExecutionTime {
            repeat(50) {
                domainTestEnvironment.studentUseCases.insertStudent(createTestStudent())
            }
        }
        
        assertTrue(time < TestConfiguration.Performance.maxInsertionTime)
    }
}
```

### Writing an App Module Test

```kotlin
class StudentViewModelTest : BaseTest() {

    @Test
    fun `test student list loading`() = runTest {
        val viewModel = StudentViewModel(testEnvironment.studentUseCases)
        
        viewModel.loadStudents(1L)
        
        val students = viewModel.students.first()
        assertNotNull(students)
        assertTrue(students.isNotEmpty())
    }

    @Test
    fun `test validation logic`() = runTest {
        val validEmail = "test@example.com"
        val invalidEmail = "invalid-email"
        
        assertTrue(isValidEmail(validEmail))
        assertFalse(isValidEmail(invalidEmail))
    }
}
```

### Writing an Android Test

```kotlin
class StudentScreenTest : AndroidBaseTest() {

    @Test
    fun `test student screen navigation`() {
        composeTestRule.setContent {
            StudentScreen(
                onNavigateToAddStudent = {},
                onNavigateToStudentDetails = {}
            )
        }

        // Test UI interactions
        performClick(composeTestRule, "Add Student")
        assertElementExists(composeTestRule, "Add Student")
        
        // Test performance
        val loadTime = measureScreenLoadTime(composeTestRule) {
            // Screen loading operation
        }
        assertTrue(loadTime < TestConfiguration.UI.maxUiLoadTime)
        
        // Test accessibility
        assertClickableElement(composeTestRule, "Add Student")
        assertAccessibilityLabel(composeTestRule, "Add Student", "Add new student button")
    }
}
```

## Test Configuration

### Centralized Configuration

All modules use the centralized `TestConfiguration` object for consistent parameters:

```kotlin
// app/src/test/java/gr/eduinvoice/infrastructure/TestConfiguration.kt
object TestConfiguration {
    object Performance { /* Performance thresholds */ }
    object Stress { /* Stress test parameters */ }
    object DataSize { /* Data size configurations */ }
    object Database { /* Database settings */ }
    object UI { /* UI testing parameters */ }
    object Security { /* Security parameters */ }
    object Accessibility { /* Accessibility standards */ }
    // ... more categories
}
```

### Module-Specific Configuration

Each module can extend the configuration with module-specific parameters:

```kotlin
// Data module specific configuration
object DataTestConfiguration {
    const val maxDatabaseOperations = 1000
    const val maxConcurrentTransactions = 10
    const val databaseTimeout = 5000L
}

// Domain module specific configuration
object DomainTestConfiguration {
    const val maxUseCaseExecutionTime = 3000L
    const val maxBusinessLogicComplexity = 100
}

// Android test specific configuration
object AndroidTestConfiguration {
    const val maxUiResponseTime = 1000L
    const val maxScreenLoadTime = 3000L
    const val minTouchTargetSize = 48
}
```

## Running Tests

### Run All Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Run Module-Specific Tests
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

### Run Specific Test Categories
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

## Best Practices

### 1. Module Isolation
- Keep tests within their respective modules
- Use module-specific test infrastructure
- Avoid cross-module dependencies in tests

### 2. Test Organization
- Group related tests in appropriate categories
- Use descriptive test names with backticks
- Follow the established directory structure

### 3. Test Data Management
- Use module-specific test data factories
- Avoid hardcoded test data
- Use appropriate data sizes for different test types

### 4. Performance Testing
- Use centralized performance thresholds
- Measure both time and memory usage
- Test with realistic data sizes

### 5. Infrastructure Reuse
- Extend base test classes for common functionality
- Use module-specific test infrastructure
- Share common utilities through infrastructure objects

## Maintenance

### Adding New Tests
1. Choose the appropriate module and test category
2. Extend the module-specific base test class
3. Use the module-specific test infrastructure
4. Follow the established naming conventions

### Updating Infrastructure
1. Modify module-specific infrastructure as needed
2. Update documentation for new utilities
3. Ensure changes are reflected across all test categories

### Test Data Management
1. Use module-specific test data factories
2. Add new factory methods as needed
3. Maintain realistic test data patterns

## Conclusion

This multi-module test organization provides:
- **Module Isolation**: Each module tests its own functionality
- **Infrastructure Consistency**: Unified patterns across modules
- **Maintainability**: Clear structure and reusable components
- **Performance**: Efficient test execution and CI integration
- **Quality**: Consistent testing patterns and validation
- **Scalability**: Supports application growth while maintaining test quality

The structure maintains the benefits of multi-module architecture while providing comprehensive test coverage and developer productivity.
