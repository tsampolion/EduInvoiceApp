# Testing Documentation

This directory contains comprehensive testing documentation organized for different audiences and use cases.

## Documentation Structure

### 🛠️ [Developer Guide](DEVELOPER_GUIDE.md)
**For**: Developers who need to run tests and write new tests
**Contains**:
- Quick start guide
- Test execution commands
- Writing test templates
- Troubleshooting common issues
- Performance benchmarks

### 🏗️ [Architecture Guide](ARCHITECTURE_GUIDE.md)
**For**: Architects and team leads who need to understand the testing strategy
**Contains**:
- Testing strategy overview
- Infrastructure design decisions
- Quality assurance approach
- CI/CD integration
- Future enhancements

### 📋 [Quick Reference](QUICK_REFERENCE.md)
**For**: Anyone who needs to quickly find test locations and understand organization
**Contains**:
- Module test distribution
- File structure overview
- Usage patterns
- Key test files
- Running test commands

## Quick Navigation

### I need to...
- **Run tests**: See [Developer Guide](DEVELOPER_GUIDE.md#quick-start)
- **Write a new test**: See [Developer Guide](DEVELOPER_GUIDE.md#writing-tests)
- **Understand test strategy**: See [Architecture Guide](ARCHITECTURE_GUIDE.md)
- **Find test files**: See [Quick Reference](QUICK_REFERENCE.md)
- **Troubleshoot issues**: See [Developer Guide](DEVELOPER_GUIDE.md#troubleshooting)

### Test Categories
- **Unit Tests**: `app/src/test/` - Individual component testing
- **Integration Tests**: `app/src/test/java/gr/eduinvoice/integration/` - Component interactions
- **Performance Tests**: `app/src/test/java/gr/eduinvoice/performance/` - Performance validation
- **Stress Tests**: `app/src/test/java/gr/eduinvoice/stress/` - System stability
- **UI Tests**: `app/src/androidTest/java/gr/eduinvoice/ui/` - User interface testing

### Key Files
- **TestDatabaseContainer**: `app/src/test/java/gr/eduinvoice/infrastructure/TestDatabaseContainer.kt`
- **TestConfiguration**: `app/src/test/java/gr/eduinvoice/infrastructure/TestConfiguration.kt`
- **ComprehensiveTestRunner**: `app/src/test/java/gr/eduinvoice/ComprehensiveTestRunner.kt`
- **Automation Script**: `scripts/run_comprehensive_tests.ps1`

## Related Documentation

- **Main Testing Doc**: [`../TESTING.md`](../TESTING.md) - Legacy comprehensive guide
- **Implementation Summary**: [`../TASK_1.7_IMPLEMENTATION_SUMMARY.md`](../TASK_1.7_IMPLEMENTATION_SUMMARY.md)
- **Performance Guide**: [`../PERFORMANCE.md`](../PERFORMANCE.md)
- **Security Guide**: [`../SECURITY.md`](../SECURITY.md)

## Migration Note

The original testing documentation has been restructured into this organized format. The original files (`TESTING.md`, `TESTING_STRATEGY.md`, `TEST_ORGANIZATION_SUMMARY.md`) are still available in the main `docs/` directory for reference, but this new structure provides better organization for different audiences.
