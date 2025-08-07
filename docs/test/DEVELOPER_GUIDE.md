# Testing Developer Guide

## Quick Start

### Prerequisites
```bash
# Set up Android environment
powershell -ExecutionPolicy Bypass -File setup_android_env.ps1
```

### Running Tests
```bash
# All tests
./gradlew test

# Specific categories
./gradlew test --tests "*UnitTest*"
./gradlew test --tests "*IntegrationTest*"
./gradlew test --tests "*PerformanceTest*"
./gradlew test --tests "*StressTest*"

# UI tests (requires device/emulator)
./gradlew connectedAndroidTest --tests "*UiAutomationTest*"

# With coverage
./gradlew testDebugUnitTestCoverage
```

## Test Categories

### Unit Tests
- **Location**: `app/src/test/java/gr/eduinvoice/`
- **Purpose**: Test individual components in isolation
- **Execution**: `./gradlew test --tests '*UnitTest*'`

### Integration Tests
- **Location**: `app/src/test/java/gr/eduinvoice/integration/`
- **Purpose**: Test component interactions and data flow
- **Key Files**: `UserFlowIntegrationTest.kt`, `ErrorHandlingIntegrationTest.kt`
- **Execution**: `./gradlew test --tests '*IntegrationTest*'`

### Performance Tests
- **Location**: `app/src/test/java/gr/eduinvoice/performance/`
- **Purpose**: Validate performance under load
- **Key Files**: `PerformanceTest.kt`, `SimplePerformanceTest.kt`
- **Execution**: `./gradlew test --tests '*PerformanceTest*'`

### Stress Tests
- **Location**: `app/src/test/java/gr/eduinvoice/stress/`
- **Purpose**: Test system stability under extreme conditions
- **Key Files**: `StressTest.kt`
- **Execution**: `./gradlew test --tests '*StressTest*'`

### UI Tests
- **Location**: `app/src/androidTest/java/gr/eduinvoice/ui/`
- **Purpose**: Test user interface interactions
- **Key Files**: `UiAutomationTest.kt`
- **Execution**: `./gradlew connectedAndroidTest --tests '*UiAutomationTest*'`

## Test Infrastructure

### TestDatabaseContainer
```kotlin
@get:Rule
val databaseContainer = TestDatabaseContainer()

// Create isolated test database
val database = databaseContainer.createTestDatabase()

// Populate test data
databaseContainer.populateTestData(database, userId)

// Create large dataset for performance testing
databaseContainer.populateLargeDataset(database, userId, 1000)
```

### TestConfiguration
```kotlin
// Performance thresholds
TestConfiguration.Performance.MAX_INSERTION_TIME_MS // 5000ms
TestConfiguration.Performance.MAX_MEMORY_USAGE_BYTES // 100MB

// Test data sizes
TestConfiguration.DataSize.LARGE_DATASET // 10000 records
```

## Writing Tests

### Unit Test Template
```kotlin
class MyComponentTest : BaseTest() {
    @Test
    fun testFeature() = runTest {
        // Arrange
        val testData = createTestData()
        
        // Act
        val result = useCase.performOperation(testData)
        
        // Assert
        assertNotNull(result)
        assertEquals(expectedValue, result.value)
    }
}
```

### Integration Test Template
```kotlin
class MyIntegrationTest : BaseTest() {
    @Test
    fun testUserFlow() = runTest {
        // Setup
        val user = createTestUser()
        
        // Execute flow
        val result = executeUserFlow(user)
        
        // Verify
        assertTrue(result.isSuccess)
        verifyExpectedOutcome(result)
    }
}
```

## Test Execution Options

### Parallel Execution
```bash
./gradlew test --parallel --max-workers=4
```

### Test Filtering
```bash
# Run tests matching pattern
./gradlew test --tests "*Student*"

# Exclude specific tests
./gradlew test --tests "*Test*" --exclude "*SlowTest*"
```

### Debug Mode
```bash
./gradlew test --debug --tests "*SpecificTest*"
```

## Automated Test Script

### Using the PowerShell Script
```powershell
# Run all tests
.\scripts\run_comprehensive_tests.ps1

# Run specific test category
.\scripts\run_comprehensive_tests.ps1 -TestCategory "performance"

# Run with verbose output
.\scripts\run_comprehensive_tests.ps1 -Verbose
```

## Test Reports

### HTML Reports
- **Location**: `app/build/reports/tests/`
- **Coverage Reports**: `app/build/reports/coverage/`
- **Performance Metrics**: JSON format for analysis

### Interpreting Results
- **Unit Tests**: 100% pass rate expected
- **Integration Tests**: >95% pass rate expected
- **Performance Tests**: All thresholds must be met
- **Stress Tests**: >90% success rate expected

## Troubleshooting

### Common Issues

#### SQLCipher Test Failures
- **Problem**: Database encryption conflicts in tests
- **Solution**: Use `TestDatabaseContainer` for isolated databases

#### Memory Issues
- **Problem**: Tests causing memory leaks
- **Solution**: Proper cleanup in test teardown

#### Flaky Tests
- **Problem**: Tests failing intermittently
- **Solution**: Add retry mechanisms and proper synchronization

#### Slow Test Execution
- **Problem**: Tests taking too long
- **Solution**: Optimize test data size and database operations

### Debug Mode
```bash
./gradlew test --debug --tests "*SpecificTest*"
```

## Best Practices

### Test Design
1. **Test Independence**: Each test should be independent
2. **Test Clarity**: Clear test names and descriptive assertions
3. **Performance**: Optimize test execution time
4. **Cleanup**: Proper setup and teardown

### Test Data
1. **Realistic Data**: Use realistic test data covering edge cases
2. **Data Management**: Clean up test data and use isolated datasets
3. **Avoid Conflicts**: Prevent data conflicts between tests

## Performance Benchmarks

### Database Operations
- **Student Retrieval**: < 1 second for 1000 students
- **Paginated Queries**: < 500ms for 50 records
- **Search Operations**: < 1 second for complex queries
- **Bulk Operations**: < 5 seconds for 100 records

### Memory Usage
- **Normal Operation**: < 100MB
- **Large Dataset**: < 200MB increase
- **Memory Pressure**: < 50MB increase during operations

### Concurrent Operations
- **Concurrent Users**: 10+ simultaneous users
- **Operation Success Rate**: > 85%
- **Response Time**: < 5 seconds under load
- **Error Rate**: < 15% under stress
