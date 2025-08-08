package gr.eduinvoice.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusinessMetrics @Inject constructor(
    private val userAnalytics: UserAnalytics
) {
    
    fun trackInvoiceGenerated(amount: Double, lessonCount: Int) {
        userAnalytics.trackUserAction("invoice_generated", mapOf(
            "amount" to amount.toString(),
            "lesson_count" to lessonCount.toString()
        ))
    }
    
    fun trackStudentAdded(studentType: String) {
        userAnalytics.trackUserAction("student_added", mapOf(
            "student_type" to studentType
        ))
    }
    
    fun trackLessonCompleted(duration: Int, rate: Double) {
        userAnalytics.trackUserAction("lesson_completed", mapOf(
            "duration" to duration.toString(),
            "rate" to rate.toString()
        ))
    }
    
    fun trackRevenueGenerated(amount: Double, source: String) {
        userAnalytics.trackUserAction("revenue_generated", mapOf(
            "amount" to amount.toString(),
            "source" to source
        ))
    }
    
    fun trackBackupCreated(backupSize: Long) {
        userAnalytics.trackUserAction("backup_created", mapOf(
            "backup_size" to backupSize.toString()
        ))
    }
    
    fun trackDataRestored(restoreSize: Long) {
        userAnalytics.trackUserAction("data_restored", mapOf(
            "restore_size" to restoreSize.toString()
        ))
    }
    
    fun trackPDFGenerated(pdfSize: Long, pageCount: Int) {
        userAnalytics.trackUserAction("pdf_generated", mapOf(
            "pdf_size" to pdfSize.toString(),
            "page_count" to pageCount.toString()
        ))
    }
    
    fun trackSearchPerformed(query: String, resultCount: Int) {
        userAnalytics.trackUserAction("search_performed", mapOf(
            "query" to query,
            "result_count" to resultCount.toString()
        ))
    }
    
    fun trackFilterApplied(filterType: String, filterValue: String) {
        userAnalytics.trackUserAction("filter_applied", mapOf(
            "filter_type" to filterType,
            "filter_value" to filterValue
        ))
    }
    
    fun trackFeatureUsage(feature: String, usageCount: Int = 1) {
        userAnalytics.trackUserAction("feature_usage", mapOf(
            "feature" to feature,
            "usage_count" to usageCount.toString()
        ))
    }
    
    fun trackAppSession(duration: Long, screensViewed: Int) {
        userAnalytics.trackUserAction("app_session", mapOf(
            "duration" to duration.toString(),
            "screens_viewed" to screensViewed.toString()
        ))
    }
}
