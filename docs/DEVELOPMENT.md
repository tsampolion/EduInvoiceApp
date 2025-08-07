# Development Guide

This guide provides comprehensive information for developers working on EduInvoiceApp, including architecture, coding standards, and development workflows.

## 🏗️ Architecture Overview

EduInvoiceApp follows Clean Architecture principles with a modular structure:

### Architecture Layers

```
┌─────────────────────────────────────┐
│              UI Layer               │
│  (Compose Screens, ViewModels)      │
├─────────────────────────────────────┤
│           Domain Layer              │
│     (Use Cases, Entities)           │
├─────────────────────────────────────┤
│            Data Layer               │
│  (Database, Repositories, DAOs)     │
└─────────────────────────────────────┘
```

### Module Structure

- **`app/`** - Android application module
  - UI components (Compose screens)
  - ViewModels
  - Navigation
  - Error handling components
  - Background processing

- **`domain/`** - Pure Kotlin business logic
  - Use cases
  - Entities
  - Business rules
  - Domain services

- **`data/`** - Data access layer
  - Room database
  - DAOs (Data Access Objects)
  - Repositories
  - Data models
  - Network services

## 🛠️ Development Setup

### Prerequisites

1. **Java Development Kit (JDK) 17+**
2. **Android Studio Hedgehog (2023.1.1)+**
3. **Android SDK API 35**
4. **Git**

### Environment Setup

```bash
# Clone repository
git clone https://github.com/your-username/EduInvoiceApp.git
cd EduInvoiceApp

# Setup Android SDK
bash setup-android-sdk.sh
source ~/.bashrc

# Verify setup
java -version
sdkmanager --version
```

### IDE Configuration

#### Android Studio
1. **Import Project:**
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to EduInvoiceApp directory

2. **Configure Kotlin:**
   - File → Settings → Languages & Frameworks → Kotlin
   - Ensure Kotlin version matches project (2.1.10)

3. **Configure Gradle:**
   - File → Settings → Build, Execution, Deployment → Gradle
   - Set Gradle JDK to version 17

#### VS Code (Alternative)
Install extensions:
- Kotlin Language
- Android
- Gradle for Java
- Material Icon Theme

## 📝 Coding Standards

### Kotlin Standards

```kotlin
// Use camelCase for variables and functions
val studentName = "John Doe"
fun calculateRevenue(): Double { ... }

// Use PascalCase for classes and objects
class StudentViewModel { ... }
object Constants { ... }

// Use UPPER_SNAKE_CASE for constants
const val MAX_STUDENTS = 1000

// Prefer val over var when possible
val immutableList = listOf(1, 2, 3)
var mutableList = mutableListOf(1, 2, 3)
```

### Compose Standards

```kotlin
// Prefer @Stable for data classes
@Stable
data class Student(
    val id: Long,
    val name: String,
    val email: String
)

// Pass Modifier as first optional parameter
@Composable
fun StudentCard(
    student: Student,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) { ... }

// Use remember and derivedStateOf for state management
val students by viewModel.students.collectAsStateWithLifecycle()
val filteredStudents by remember(students, searchQuery) {
    derivedStateOf { students.filter { it.name.contains(searchQuery) } }
}
```

### Room Standards

```kotlin
// DAO methods return Flow for reactive data
@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE ownerId = :ownerId")
    fun getAllStudents(ownerId: Long): Flow<List<Student>>
    
    @Insert
    suspend fun insertStudent(student: Student): Long
    
    @Update
    suspend fun updateStudent(student: Student)
    
    @Delete
    suspend fun deleteStudent(student: Student)
}
```

### Error Handling Standards

```kotlin
// Use ErrorBoundary for UI error handling
ErrorBoundary(
    onError = { error ->
        errorReporter.reportError(error, "StudentScreen")
    }
) {
    StudentScreen()
}

// Use ErrorHandler for centralized error handling
val errorHandler = ErrorHandler(context)
val result = errorHandler.handle {
    repository.saveStudent(student)
}
```

## 🔧 Development Workflow

### 1. Feature Development

```bash
# Create feature branch
git checkout -b feature/new-feature

# Make changes and test
./gradlew test
./gradlew lintDebug

# Commit with conventional commit message
git commit -m "feat(student): add student search functionality

- Add search bar to student list
- Implement real-time search filtering
- Add search history tracking"

# Push and create PR
git push origin feature/new-feature
```

### 2. Testing Strategy

#### Unit Tests
```kotlin
@RunWith(RobolectricTestRunner::class)
class StudentViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @Test
    fun `when student is saved, it appears in list`() = runTest {
        // Given
        val student = Student(name = "John Doe")
        
        // When
        viewModel.saveStudent(student)
        
        // Then
        assertThat(viewModel.students.value).contains(student)
    }
}
```

#### Integration Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class StudentRepositoryIntegrationTest {
    
    @get:Rule
    val databaseRule = TestDatabaseContainer()
    
    @Test
    fun saveAndRetrieveStudent() = runTest {
        // Test database operations
    }
}
```

### 3. Code Quality

#### Linting
```bash
# Run Android Lint
./gradlew lintDebug

# Fix auto-fixable issues
./gradlew lintFix
```

#### Code Formatting
```bash
# Format code with ktfmt
./gradlew ktfmtFormat

# Check formatting
./gradlew ktfmtCheck
```

#### Static Analysis
```bash
# Run Detekt
./gradlew detekt

# Generate report
./gradlew detektMain
```

## 🏗️ Architecture Patterns

### MVVM with Clean Architecture

```kotlin
// ViewModel (UI Layer)
@HiltViewModel
class StudentViewModel @Inject constructor(
    private val getStudentsUseCase: GetStudentsUseCase,
    private val saveStudentUseCase: SaveStudentUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StudentUiState())
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()
    
    fun loadStudents() {
        viewModelScope.launch {
            getStudentsUseCase().collect { students ->
                _uiState.value = _uiState.value.copy(students = students)
            }
        }
    }
}

// Use Case (Domain Layer)
class GetStudentsUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    operator fun invoke(): Flow<List<Student>> {
        return studentRepository.getAllStudents()
    }
}

// Repository (Data Layer)
class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao,
    private val concurrencyController: ConcurrencyController
) : StudentRepository {
    
    override fun getAllStudents(): Flow<List<Student>> {
        return concurrencyController.executeRead {
            studentDao.getAllStudents()
        }
    }
}
```

### Dependency Injection with Hilt

```kotlin
// Module definition
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EduInvoiceDatabase {
        return Room.databaseBuilder(
            context,
            EduInvoiceDatabase::class.java,
            "eduinvoice.db"
        ).build()
    }
    
    @Provides
    fun provideStudentDao(database: EduInvoiceDatabase): StudentDao {
        return database.studentDao()
    }
}

// Repository binding
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    abstract fun bindStudentRepository(
        studentRepositoryImpl: StudentRepositoryImpl
    ): StudentRepository
}
```

## 🔒 Security Implementation

### Database Encryption

```kotlin
// SQLCipher configuration
@Database(
    entities = [Student::class, Lesson::class],
    version = 15,
    autoMigrations = [/* ... */]
)
abstract class EduInvoiceDatabase : RoomDatabase() {
    
    companion object {
        fun create(context: Context, passphrase: String): EduInvoiceDatabase {
            return Room.databaseBuilder(
                context,
                EduInvoiceDatabase::class.java,
                "eduinvoice.db"
            )
            .openHelperFactory(SQLiteOpenHelperFactory(passphrase))
            .build()
        }
    }
}
```

### Password Security

```kotlin
// BCrypt password hashing
class PasswordManager @Inject constructor() {
    
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
    
    fun verifyPassword(password: String, hash: String): Boolean {
        return BCrypt.checkpw(password, hash)
    }
}
```

## ⚡ Performance Optimization

### Background Processing

```kotlin
// Background processor for heavy operations
class BackgroundProcessor @Inject constructor() {
    
    fun executeTask(
        task: suspend () -> Unit,
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                task()
                onComplete()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
```

### Pagination

```kotlin
// Paginated data loading
class PaginatedList<T>(
    private val pageSize: Int = 20,
    private val loadFunction: suspend (Int, Int) -> List<T>
) {
    
    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items.asStateFlow()
    
    suspend fun loadNextPage() {
        val currentSize = _items.value.size
        val newItems = loadFunction(currentSize, pageSize)
        _items.value = _items.value + newItems
    }
}
```

## 🧪 Testing Infrastructure

### Test Environment Setup

```kotlin
// Test database container
@RunWith(RobolectricTestRunner::class)
abstract class BaseTest {
    
    @get:Rule
    val databaseRule = TestDatabaseContainer()
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    protected lateinit var database: EduInvoiceDatabase
    protected lateinit var studentDao: StudentDao
    
    @Before
    fun setup() {
        database = databaseRule.database
        studentDao = database.studentDao()
    }
}
```

### MockK Integration

```kotlin
// Mocking final classes
@RunWith(RobolectricTestRunner::class)
class ConcurrencyControllerTest {
    
    @MockK
    private lateinit var mockConcurrencyController: ConcurrencyController
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }
    
    @Test
    fun `test concurrent operations`() {
        // Test implementation
    }
}
```

## 📊 Monitoring & Analytics

### Error Reporting

```kotlin
// Firebase Crashlytics integration
class ErrorReporter @Inject constructor(
    private val context: Context
) {
    
    fun reportError(error: Throwable, source: String) {
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("source", source)
            recordException(error)
        }
    }
}
```

### Performance Monitoring

```kotlin
// Memory monitoring
class MemoryMonitor @Inject constructor() {
    
    fun checkMemoryPressure(): MemoryPressure {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usagePercentage = (usedMemory.toDouble() / maxMemory) * 100
        
        return when {
            usagePercentage > 90 -> MemoryPressure.CRITICAL
            usagePercentage > 75 -> MemoryPressure.HIGH
            else -> MemoryPressure.NORMAL
        }
    }
}
```

## 🔄 Continuous Integration

### GitHub Actions Workflow

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Run lint
        run: ./gradlew lintDebug
```

## 📚 Documentation Standards

### Code Documentation

```kotlin
/**
 * Manages student data operations with thread-safe concurrency control.
 * 
 * This repository provides a safe interface for all student-related database
 * operations, ensuring data consistency and preventing race conditions.
 * 
 * @param studentDao Data access object for student operations
 * @param concurrencyController Controller for thread-safe operations
 */
class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao,
    private val concurrencyController: ConcurrencyController
) : StudentRepository {
    
    /**
     * Retrieves all students for the current user.
     * 
     * @return Flow of student lists that updates when data changes
     */
    override fun getAllStudents(): Flow<List<Student>> {
        return concurrencyController.executeRead {
            studentDao.getAllStudents()
        }
    }
}
```

### API Documentation

- Document all public APIs
- Include usage examples
- Specify parameter types and return values
- Document error conditions and exceptions

## 🚀 Release Process

### Version Management

```kotlin
// Version bumping
// 1. Update version in app/build.gradle
versionName "0.24.10"
versionCode 38

// 2. Update CHANGELOG.md
// 3. Create release tag
git tag -a v0.24.10 -m "Release version 0.24.10"
git push origin v0.24.10
```

### Release Checklist

- [ ] All tests passing
- [ ] Lint shows no warnings
- [ ] Documentation updated
- [ ] Version bumped
- [ ] Changelog updated
- [ ] Release notes prepared
- [ ] APK signed and tested
- [ ] Release tag created

## 🤝 Contributing Guidelines

### Pull Request Process

1. **Create Feature Branch:**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Changes:**
   - Follow coding standards
   - Write tests for new features
   - Update documentation

3. **Test Changes:**
   ```bash
   ./gradlew test
   ./gradlew lintDebug
   ```

4. **Submit PR:**
   - Use conventional commit messages
   - Include detailed description
   - Link related issues

### Code Review Process

- All PRs require review
- Address review comments promptly
- Ensure CI checks pass
- Update documentation if needed

---

*This development guide is maintained alongside the codebase and updated with each release.*
