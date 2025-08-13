# Contributing to EduInvoiceApp

Thank you for your interest in contributing to EduInvoiceApp! This document provides comprehensive guidelines and information for contributors.

## 🚀 Quick Start

1. **Fork** the repository
2. **Clone** your fork locally
3. **Create** a feature branch
4. **Make** your changes
5. **Test** thoroughly
6. **Submit** a pull request

## 📋 Prerequisites

- **JDK 17 or newer**
- **Android Studio** (latest stable version)
- **Android SDK** with API level 35
- **Git** for version control

## 🛠️ Development Setup

### 1. Environment Setup

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/EduInvoiceApp.git
cd EduInvoiceApp

# Setup Android SDK
bash setup-android-sdk.sh
source ~/.profile

# Build the project
./gradlew clean assemble
```

### 2. IDE Configuration

- Use **Android Studio** (recommended) or **IntelliJ IDEA**
- Install the **Kotlin** plugin
- Configure **ktfmt** for code formatting
- Enable **Android Lint** integration

## 📝 Code Standards

### Kotlin Style Guide

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use **ktfmt** for consistent formatting
- Maximum line length: **120 characters**
- Use **4 spaces** for indentation (no tabs)

### Architecture Guidelines

- Follow **Clean Architecture** principles
- Use **MVVM** pattern for UI layer
- Implement **Repository Pattern** for data access
- Use **Dependency Injection** with Hilt

### Naming Conventions

- **Classes**: PascalCase (`StudentRepository`)
- **Functions**: camelCase (`getStudentById`)
- **Variables**: camelCase (`studentName`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_STUDENTS`)
- **Files**: PascalCase (`StudentViewModel.kt`)

### Code Quality

- Write **comprehensive tests** for new features
- Maintain **test coverage** above 80%
- Use **meaningful commit messages**
- Add **documentation** for public APIs

#### Static Analysis Ratchet Policy

- Spotless formatting uses a ratchet from `origin/main` to prevent unrelated reformatting.
- Detekt baseline stays as-is. When you touch a file, fix its Detekt issues or add a local suppression with a short rationale. Do NOT add new issues to the baseline.
- Optional CI may run Detekt only on changed files as a fail-fast gate.

Example local suppression (use sparingly, with rationale):

```kotlin
@file:Suppress("LongMethod") // rationale: Legacy method; will be refactored in follow-up PR
```

## 🧪 Testing Guidelines

### Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests StudentRepositoryTest

# Run with coverage
./gradlew test jacocoTestReport
```

### Instrumentation Tests

```bash
# Run instrumentation tests (requires emulator)
./gradlew connectedAndroidTest
```

### Test Writing Standards

- Use **descriptive test names**
- Follow **AAA pattern** (Arrange, Act, Assert)
- Mock **external dependencies**
- Test **edge cases** and error scenarios

## 🔄 Pull Request Process

### 1. Before Submitting

- [ ] Code follows style guidelines
- [ ] All tests pass (`./gradlew test`)
- [ ] Lint shows no warnings (`./gradlew lintDebug`)
- [ ] Documentation is updated
- [ ] Version is bumped in `CHANGELOG.md`

### 2. Creating a Pull Request

1. **Update** your branch with latest main
2. **Squash** commits if necessary
3. **Write** a clear description
4. **Link** related issues
5. **Request** review from maintainers

### 3. Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] Version bumped
```

## 🐛 Bug Reports

### Before Reporting

1. **Search** existing issues
2. **Test** on latest version
3. **Reproduce** the issue
4. **Gather** relevant information

### Bug Report Template

```markdown
## Bug Description
Clear description of the issue

## Steps to Reproduce
1. Step 1
2. Step 2
3. Step 3

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- OS: [e.g., Windows 10]
- Android Studio: [version]
- Device/Emulator: [details]

## Additional Information
Screenshots, logs, etc.
```

## 💡 Feature Requests

### Before Requesting

1. **Check** if feature already exists
2. **Search** existing discussions
3. **Consider** implementation complexity
4. **Think** about user impact

### Feature Request Template

```markdown
## Feature Description
Clear description of the feature

## Use Case
Why this feature is needed

## Proposed Solution
How it could be implemented

## Alternatives Considered
Other approaches

## Additional Information
Mockups, examples, etc.
```

## 🔒 Security

### Reporting Security Issues

**Do NOT** create public issues for security vulnerabilities. Instead:

1. **Email** security@eduinvoice.com
2. **Include** detailed description
3. **Provide** reproduction steps
4. **Wait** for response

### Security Guidelines

- **Never** commit sensitive data
- **Use** environment variables for secrets
- **Validate** all user inputs
- **Follow** OWASP guidelines

## 📚 Documentation

### Documentation Standards

- Write **clear, concise** documentation
- Use **code examples** where appropriate
- Keep **README.md** updated
- Document **API changes**

### Documentation Structure

```
docs/
├── API_REFERENCE.md
├── DEVELOPMENT.md
├── INSTALLATION.md
├── USER_MANUAL.md
└── TESTING_STRATEGY.md
```

## 🏷️ Version Management

### Version Bumping

- **Major**: Breaking changes
- **Minor**: New features
- **Patch**: Bug fixes

### Changelog Updates

- Update `CHANGELOG.md` for all changes
- Use **conventional commit** format
- Group changes by type
- Include **breaking changes** section

## 🤝 Community Guidelines

### Code of Conduct

- **Be respectful** and inclusive
- **Help others** learn and grow
- **Provide constructive** feedback
- **Follow** project guidelines

### Communication

- Use **GitHub Issues** for discussions
- Be **patient** with responses
- **Ask questions** when unsure
- **Share knowledge** with others

## 🎯 Getting Help

### Resources

- **[Documentation](.)** - Comprehensive guides
- **[Issues](https://github.com/your-username/EduInvoiceApp/issues)** - Search existing discussions
- **[Discussions](https://github.com/your-username/EduInvoiceApp/discussions)** - Community forum

### Contact

- **Email**: support@eduinvoice.com
- **GitHub**: [@your-username](https://github.com/your-username)

## 📄 License

By contributing to EduInvoiceApp, you agree that your contributions will be licensed under the [MIT License](LICENSE.md).

---

Thank you for contributing to EduInvoiceApp! 🚀
