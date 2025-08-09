# Test Support Module

This module provides centralized test fixtures, builders, fakes, and utilities for the EduInvoice app testing.

## Features

- **Test Builders**: Fluent API builders for creating test data objects
- **Fake Repositories**: In-memory implementations of repositories for testing
- **Extension Functions**: Convenient shortcuts for common test data creation
- **Test Utilities**: Common test setup and teardown utilities
- **No-op Implementations**: Simplified implementations for testing complex components

## Usage

### Test Builders

Use the fluent API builders to create test data with sensible defaults:

```kotlin
// Create a test student
val student = TestStudentBuilder()
    .withId(1L)
    .withName("Alice")
    .withRate(30.0)
    .withOwnerId(1L)
    .build()

// Create a test lesson
val lesson = TestLessonBuilder()
    .withStudentId(1L)
    .withDate("2024-01-01")
    .withDurationMinutes(60)
    .build()

// Create a test group
val group = TestGroupBuilder()
    .withName("Math Group")
    .withDescription("Advanced mathematics")
    .build()
```

### Extension Functions

Use convenient extension functions for quick test data creation:

```kotlin
// Create test objects with defaults
val student = createTestStudent(name = "Bob", rate = 25.0)
val lesson = createTestLesson(studentId = 1L, durationMinutes = 90)
val group = createTestGroup(name = "Science Group")

// Create multiple test objects
val students = createTestStudents(5, ownerId = 1L)
val lessons = createTestLessonsForStudent(1L, 3, ownerId = 1L)
val groups = createTestGroups(2, ownerId = 1L)

// Create complete test dataset
val dataset = createCompleteTestDataset(
    studentCount = 3,
    groupCount = 2,
    ownerId = 1L
)
```

### Fake Repositories

Use fake repositories for in-memory testing:

```kotlin
val fakeRepository = FakeLessonRepository()

// Add test data
val lesson = createTestLesson(studentId = 1L)
val lessonId = fakeRepository.addLesson(lesson)

// Query data
val retrievedLesson = fakeRepository.getLessonById(1L, 1L).first()
```

### No-op Concurrency Controller

Use the no-op concurrency controller for testing without concurrency overhead:

```kotlin
val controller = NoopConcurrencyController.create()

val result = controller.executeSafeOperation(
    operation = { "test result" },
    operationType = OperationType.READ,
    resourceId = "test"
)
```

### Test Environment

Use the complete test environment for integration testing:

```kotlin
TestUtils.runTestWithCleanup { environment ->
    // Use all repositories and use cases
    val student = createTestStudent(name = "Test Student")
    val studentId = environment.studentUseCases.insertStudent(student)
    
    val retrievedStudent = environment.studentUseCases.getStudentById(studentId).first()
    assertEquals("Test Student", retrievedStudent!!.name)
}
```

## Module Dependencies

Add the test fixtures to your module's `build.gradle`:

```gradle
dependencies {
    // For unit tests
    testImplementation(testFixtures(project(":test-support")))
    
    // For Android tests
    androidTestImplementation(testFixtures(project(":test-support")))
}
```

## Migration Guide

### From Old Test Infrastructure

Replace old test data creation:

```kotlin
// Old way
val student = TestInfrastructure.createTestStudent(id = 1, name = "Alice", rate = 10.0)

// New way
val student = createTestStudent(id = 1L, name = "Alice", rate = 10.0)
```

Replace old repository mocks:

```kotlin
// Old way
private val lessonDao = object : LessonDao {
    override suspend fun insert(lesson: Lesson): Long = 0L
    // ... many more overrides
}

// New way
private val fakeRepository = FakeLessonRepository()
```

## Best Practices

1. **Use builders for complex test data**: When you need specific test scenarios
2. **Use extension functions for simple cases**: When defaults are sufficient
3. **Use fake repositories for unit tests**: Avoid database dependencies
4. **Use test environment for integration tests**: Test the full stack
5. **Clean up after tests**: Use `TestUtils.runTestWithCleanup` for automatic cleanup

## Examples

See `TestFixturesExampleTest.kt` for comprehensive usage examples.
