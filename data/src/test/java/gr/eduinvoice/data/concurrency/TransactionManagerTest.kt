package gr.eduinvoice.data.concurrency

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import gr.eduinvoice.data.database.EduInvoiceDatabase
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.async
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class TransactionManagerTest {
    
    @Inject
    lateinit var transactionManager: TransactionManager
    
    private lateinit var database: EduInvoiceDatabase
    private lateinit var context: Context
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            EduInvoiceDatabase::class.java
        ).build()
        
        transactionManager = TransactionManager(database)
    }
    
    @Test
    fun `test successful transaction execution`() = runTest {
        val student = Student(
            id = 0,
            ownerId = 1L,
            name = "John",
            surname = "Doe",
            parentMobile = "1234567890",
            parentEmail = "john@example.com",
            className = "Math",
            rate = 50.0,
            rateType = "per_hour",
            isActive = true
        )
        
        val result = transactionManager.executeInTransaction(
            operation = {
                val studentId = database.studentDao().insert(student)
                database.studentDao().getStudentById(studentId, 1L).first()
            },
            transactionName = "test_successful_transaction"
        )
        
        assertTrue("Transaction should succeed", result.isSuccess)
        assertNotNull("Student should be returned", result.getOrNull())
        assertEquals("Student name should match", "John", result.getOrNull()?.name)
    }
    
    @Test
    fun `test transaction rollback on failure`() = runTest {
        val initialCount = database.studentDao().getAllActiveStudents(1L).first().size
        
        val result = transactionManager.executeInTransaction(
            operation = {
                // Insert a valid student
                val student = Student(
                    id = 0,
                    ownerId = 1L,
                    name = "Jane",
                    surname = "Doe",
                    parentMobile = "1234567890",
                    parentEmail = "jane@example.com",
                    className = "Math",
                    rate = 50.0,
                    rateType = "per_hour",
                    isActive = true
                )
                database.studentDao().insert(student)
                
                // This will cause the transaction to fail
                throw RuntimeException("Simulated failure")
            },
            transactionName = "test_rollback_transaction"
        )
        
        assertTrue("Transaction should fail", result.isFailure)
        assertTrue("Should contain error message", result.exceptionOrNull()?.message?.contains("Simulated failure") == true)
        
        // Verify rollback - no new student should be added
        val finalCount = database.studentDao().getAllActiveStudents(1L).first().size
        assertEquals("Database should be rolled back", initialCount, finalCount)
    }
    
    @Test
    fun `test read-only transaction`() = runTest {
        // First insert a student
        val student = Student(
            id = 0,
            ownerId = 1L,
            name = "Alice",
            surname = "Smith",
            parentMobile = "1234567890",
            parentEmail = "alice@example.com",
            className = "Math",
            rate = 50.0,
            rateType = "per_hour",
            isActive = true
        )
        database.studentDao().insert(student)
        
        val result = transactionManager.executeReadOnly(
            operation = {
                database.studentDao().getAllActiveStudents(1L).first()
            },
            transactionName = "test_readonly_transaction"
        )
        
        assertTrue("Read-only transaction should succeed", result.isSuccess)
        assertNotNull("Students should be returned", result.getOrNull())
        assertTrue("Should have at least one student", result.getOrNull()?.isNotEmpty() == true)
    }
    
    @Test
    fun `test batch transaction execution`() = runTest {
        val students = listOf(
            Student(id = 0, ownerId = 1L, name = "Student1", surname = "Test", parentMobile = "123", parentEmail = "test1@test.com", className = "Math", rate = 50.0, rateType = "per_hour", isActive = true),
            Student(id = 0, ownerId = 1L, name = "Student2", surname = "Test", parentMobile = "456", parentEmail = "test2@test.com", className = "Math", rate = 50.0, rateType = "per_hour", isActive = true),
            Student(id = 0, ownerId = 1L, name = "Student3", surname = "Test", parentMobile = "789", parentEmail = "test3@test.com", className = "Math", rate = 50.0, rateType = "per_hour", isActive = true)
        )
        
        val operations = students.map { student ->
            suspend { database.studentDao().insert(student) }
        }
        
        val result = transactionManager.executeBatchInTransaction(
            operations = operations,
            transactionName = "test_batch_transaction"
        )
        
        assertTrue("Batch transaction should succeed", result.isSuccess)
        val insertedIds = result.getOrNull()
        assertNotNull("Should return list of IDs", insertedIds)
        assertEquals("Should have 3 IDs", 3, insertedIds?.size)
        
        // Verify all students were inserted
        val allStudents = database.studentDao().getAllActiveStudents(1L).first()
        assertEquals("Should have 3 students", 3, allStudents.size)
    }
    
    @Test
    fun `test concurrent transactions`() = runTest {
        val counter = AtomicInteger(0)
        val results = mutableListOf<Result<Int>>()
        
        // Launch multiple concurrent transactions
        val jobs = (1..5).map { index ->
            async {
                transactionManager.executeInTransaction(
                    operation = {
                        counter.incrementAndGet()
                    },
                    transactionName = "concurrent_transaction_$index"
                )
            }
        }
        
        // Wait for all transactions to complete
        jobs.forEach { job ->
            results.add(job.await())
        }
        
        // All transactions should succeed
        results.forEach { result ->
            assertTrue("Each transaction should succeed", result.isSuccess)
        }
        
        // Counter should be incremented 5 times
        assertEquals("Counter should be 5", 5, counter.get())
    }
    
    @Test
    fun `test transaction statistics`() = runTest {
        val initialStats = transactionManager.transactionStats.value
        
        // Execute a successful transaction
        transactionManager.executeInTransaction(
            operation = { "success" },
            transactionName = "test_stats_success"
        )
        
        // Execute a failing transaction
        transactionManager.executeInTransaction(
            operation = { throw RuntimeException("Test failure") },
            transactionName = "test_stats_failure"
        )
        
        val finalStats = transactionManager.transactionStats.value
        
        assertEquals("Total transactions should increase by 2", initialStats.totalTransactions + 2, finalStats.totalTransactions)
        assertEquals("Successful transactions should increase by 1", initialStats.successfulTransactions + 1, finalStats.successfulTransactions)
        assertEquals("Failed transactions should increase by 1", initialStats.failedTransactions + 1, finalStats.failedTransactions)
        assertNotNull("Should have last error", finalStats.lastError)
    }
    
    @Test
    fun `test transaction isolation levels`() = runTest {
        val result = transactionManager.executeInTransaction(
            operation = { "test" },
            transactionName = "test_isolation_levels",
            isolationLevel = TransactionIsolationLevel.READ_COMMITTED
        )
        
        assertTrue("Transaction with custom isolation level should succeed", result.isSuccess)
    }
    
    @Test
    fun `test active transactions tracking`() = runTest {
        val initialActive = transactionManager.getActiveTransactions().size
        
        // Start a transaction but don't complete it immediately
        val deferred = CompletableDeferred<Result<String>>()
        
        val job = async {
            val result = transactionManager.executeInTransaction(
                operation = {
                    deferred.await() // Wait for external signal
                    "completed"
                },
                transactionName = "test_active_tracking"
            )
        }
        
        // Check that transaction is active
        val activeDuring = transactionManager.getActiveTransactions()
        assertTrue("Should have active transaction", activeDuring.isNotEmpty())
        
        // Complete the transaction
        deferred.complete(Result.success("test"))
        job.join()
        
        // Check that transaction is no longer active
        val finalActive = transactionManager.getActiveTransactions()
        assertEquals("Should have no active transactions", 0, finalActive.size)
    }
    
    @Test
    fun `test emergency cleanup`() = runTest {
        // Start some transactions
        val deferred = CompletableDeferred<Unit>()
        
        val job = async {
            transactionManager.executeInTransaction(
                operation = {
                    deferred.await() // Keep transaction open
                    "completed"
                },
                transactionName = "test_cleanup"
            )
        }
        
        // Verify transaction is active
        assertTrue("Should have active transaction", transactionManager.getActiveTransactions().isNotEmpty())
        
        // Perform emergency cleanup
        transactionManager.cancelAllTransactions()
        
        // Complete the deferred to avoid hanging
        deferred.complete(Unit)
        job.join()
        
        // Verify all transactions are cleaned up
        assertEquals("Should have no active transactions after cleanup", 0, transactionManager.getActiveTransactions().size)
    }
} 