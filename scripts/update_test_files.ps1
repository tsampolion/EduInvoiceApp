# PowerShell script to update test files with missing pagination methods

$testFiles = @(
    "app/src/test/java/gr/eduinvoice/ui/home/HomeMenuViewModelTest.kt",
    "app/src/test/java/gr/eduinvoice/ui/invoice/InvoiceViewModelTest.kt",
    "app/src/test/java/gr/eduinvoice/ui/lesson/LessonScreenTest.kt",
    "app/src/test/java/gr/eduinvoice/ui/lesson/LessonViewModelGroupTest.kt",
    "app/src/test/java/gr/eduinvoice/ui/lessons/LessonsScreenTest.kt",
    "app/src/test/java/gr/eduinvoice/ui/lessons/LessonsViewModelTest.kt",
    "app/src/test/java/gr/eduinvoice/ui/revenue/RevenueViewModelTest.kt",
    "app/src/test/java/gr/eduinvoice/ui/student/StudentViewModelTest.kt"
)

$studentDaoMethods = @"
        override suspend fun getStudentsPaginated(userId: Long, limit: Int, offset: Int): List<Student> {
            return flow.value
                .filter { it.ownerId == userId }
                .sortedBy { it.name }
                .drop(offset)
                .take(limit)
        }
        
        override suspend fun searchStudentsPaginated(userId: Long, searchQuery: String, limit: Int, offset: Int): List<Student> {
            return flow.value
                .filter { 
                    it.ownerId == userId &&
                    (it.name.contains(searchQuery, true) || it.className.contains(searchQuery, true))
                }
                .sortedBy { it.name }
                .drop(offset)
                .take(limit)
        }
"@

$lessonDaoMethods = @"
        override suspend fun getLessonsWithStudentsPaginated(userId: Long, limit: Int, offset: Int): List<LessonWithStudent> {
            return lessonWithStudentFlow.value
                .filter { it.lesson.ownerId == userId }
                .sortedWith(compareByDescending<LessonWithStudent> { it.lesson.date }
                    .thenByDescending { it.lesson.startTime })
                .drop(offset)
                .take(limit)
        }
"@

foreach ($file in $testFiles) {
    if (Test-Path $file) {
        Write-Host "Processing $file"
        
        $content = Get-Content $file -Raw
        
        # Add StudentDao pagination methods
        if ($content -match "override suspend fun classNameExists\(name: String, userId: Long\): Int") {
            $content = $content -replace "override suspend fun classNameExists\(name: String, userId: Long\): Int.*?\n", "override suspend fun classNameExists(name: String, userId: Long): Int = flow.value.count { it.className.equals(name, true) }`n`n$studentDaoMethods`n"
        }
        
        # Add LessonDao pagination methods
        if ($content -match "override suspend fun insertGroupLessons\(lessons: List<Lesson>\): List<Long>") {
            $content = $content -replace "override suspend fun insertGroupLessons\(lessons: List<Lesson>\): List<Long>.*?\n    }\n", "override suspend fun insertGroupLessons(lessons: List<Lesson>): List<Long> {`n        val ids = mutableListOf<Long>()`n        lessons.forEach { lesson ->`n            val id = insert(lesson)`n            ids.add(id)`n        }`n        return ids`n    }`n`n$lessonDaoMethods`n"
        }
        
        Set-Content $file $content -NoNewline
        Write-Host "Updated $file"
    } else {
        Write-Host "File not found: $file"
    }
}

Write-Host "Test files update completed!" 