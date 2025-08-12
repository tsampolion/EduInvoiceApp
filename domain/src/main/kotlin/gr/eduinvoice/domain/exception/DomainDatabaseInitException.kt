package gr.eduinvoice.domain.exception

/**
 * Domain-level exception for database initialization errors.
 * This allows the app module to handle database errors without exposing data layer details.
 * 
 * Added during app→domain migration. Replaces data layer DatabaseInitException.
 */
class DomainDatabaseInitException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
