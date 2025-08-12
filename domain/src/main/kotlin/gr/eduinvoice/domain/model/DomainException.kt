package gr.eduinvoice.domain.model

/**
 * Domain-level exception for handling application errors without exposing data layer details.
 * This allows the app module to handle errors while maintaining clean architecture boundaries.
 */
sealed class DomainException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    
    /**
     * Database-related errors that have been translated to domain level
     */
    data class DatabaseError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainException(message, cause)
    
    /**
     * Network-related errors
     */
    data class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainException(message, cause)
    
    /**
     * Validation errors
     */
    data class ValidationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainException(message, cause)
    
    /**
     * Generic application errors
     */
    data class ApplicationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainException(message, cause)
}
