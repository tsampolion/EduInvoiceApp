package gr.eduinvoice.data.sync

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import gr.eduinvoice.data.model.Student
import gr.eduinvoice.data.model.Lesson
import gr.eduinvoice.data.model.StudentGroup
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves conflicts between local and remote data versions
 */
@Singleton
class ConflictResolver @Inject constructor() {
    private val gson = Gson()

    /**
     * Resolve a conflict between local and remote data
     */
    fun resolveConflict(localData: String, remoteData: String): ConflictResolution {
        return try {
            // Parse both data sets
            val localJson = gson.fromJson(localData, JsonObject::class.java)
            val remoteJson = gson.fromJson(remoteData, JsonObject::class.java)
            
            // Determine the best resolution strategy
            when {
                isLocalMoreRecent(localJson, remoteJson) -> {
                    ConflictResolution.UseLocal
                }
                isRemoteMoreRecent(localJson, remoteJson) -> {
                    ConflictResolution.UseRemote(remoteData)
                }
                canMerge(localJson, remoteJson) -> {
                    val mergedData = mergeJsonObjects(localJson, remoteJson)
                    ConflictResolution.Merge(gson.toJson(mergedData))
                }
                else -> {
                    // Default to local data if no clear resolution
                    ConflictResolution.UseLocal
                }
            }
        } catch (e: Exception) {
            // If parsing fails, use local data as fallback
            ConflictResolution.UseLocal
        }
    }

    /**
     * Merge local and remote data intelligently
     */
    fun mergeData(localData: String, remoteData: String): String {
        return try {
            val localJson = gson.fromJson(localData, JsonObject::class.java)
            val remoteJson = gson.fromJson(remoteData, JsonObject::class.java)
            val mergedJson = mergeJsonObjects(localJson, remoteJson)
            gson.toJson(mergedJson)
        } catch (e: Exception) {
            // Return local data if merge fails
            localData
        }
    }

    /**
     * Check if local data is more recent than remote data
     */
    private fun isLocalMoreRecent(localJson: JsonObject, remoteJson: JsonObject): Boolean {
        val localTimestamp = getTimestamp(localJson)
        val remoteTimestamp = getTimestamp(remoteJson)
        return localTimestamp > remoteTimestamp
    }

    /**
     * Check if remote data is more recent than local data
     */
    private fun isRemoteMoreRecent(localJson: JsonObject, remoteJson: JsonObject): Boolean {
        val localTimestamp = getTimestamp(localJson)
        val remoteTimestamp = getTimestamp(remoteJson)
        return remoteTimestamp > localTimestamp
    }

    /**
     * Check if data can be safely merged
     */
    private fun canMerge(localJson: JsonObject, remoteJson: JsonObject): Boolean {
        // Check if both objects have the same structure
        val localKeys = localJson.keySet()
        val remoteKeys = remoteJson.keySet()
        
        // If they have completely different keys, merging might be risky
        if (localKeys != remoteKeys) {
            return false
        }
        
        // Check for critical fields that shouldn't be merged
        val criticalFields = setOf("id", "ownerId", "createdAt")
        for (field in criticalFields) {
            if (localJson.has(field) && remoteJson.has(field)) {
                val localValue = localJson.get(field)
                val remoteValue = remoteJson.get(field)
                if (localValue != remoteValue) {
                    return false
                }
            }
        }
        
        return true
    }

    /**
     * Merge two JSON objects intelligently
     */
    private fun mergeJsonObjects(localJson: JsonObject, remoteJson: JsonObject): JsonObject {
        val merged = JsonObject()
        
        // Add all fields from local JSON
        for ((key, value) in localJson.entrySet()) {
            merged.add(key, value)
        }
        
        // Merge fields from remote JSON
        for ((key, remoteValue) in remoteJson.entrySet()) {
            if (merged.has(key)) {
                val localValue = merged.get(key)
                val mergedValue = mergeField(key, localValue, remoteValue)
                merged.add(key, mergedValue)
            } else {
                merged.add(key, remoteValue)
            }
        }
        
        // Update timestamp to current time
        merged.addProperty("lastModified", System.currentTimeMillis())
        
        return merged
    }

    /**
     * Merge individual fields based on their type and importance
     */
    private fun mergeField(key: String, localValue: JsonElement, remoteValue: JsonElement): JsonElement {
        return when (key) {
            "name", "surname", "parentMobile", "parentEmail" -> {
                // For text fields, prefer non-empty values
                val localStr = localValue.asString ?: ""
                val remoteStr = remoteValue.asString ?: ""
                when {
                    localStr.isNotEmpty() -> localValue
                    remoteStr.isNotEmpty() -> remoteValue
                    else -> localValue
                }
            }
            "rate", "durationMinutes" -> {
                // For numeric fields, prefer the higher value (assuming it's more recent)
                val localNum = localValue.asDouble
                val remoteNum = remoteValue.asDouble
                if (localNum > remoteNum) localValue else remoteValue
            }
            "isArchived", "isActive" -> {
                // For boolean fields, prefer the more recent (true) value
                if (localValue.asBoolean) localValue else remoteValue
            }
            "lessons", "students" -> {
                // For arrays, merge them
                mergeArrays(localValue, remoteValue)
            }
            else -> {
                // For other fields, prefer local value
                localValue
            }
        }
    }

    /**
     * Merge arrays by combining unique elements
     */
    private fun mergeArrays(localArray: JsonElement, remoteArray: JsonElement): JsonElement {
        return try {
            val localList = gson.fromJson(localArray, List::class.java)
            val remoteList = gson.fromJson(remoteArray, List::class.java)
            
            val mergedList = (localList + remoteList).distinct()
            val mergedJson = gson.toJsonTree(mergedList)
            mergedJson
        } catch (e: Exception) {
            // If merging fails, return local array
            localArray
        }
    }

    /**
     * Extract timestamp from JSON object
     */
    private fun getTimestamp(json: JsonObject): Long {
        return when {
            json.has("lastModified") -> json.get("lastModified").asLong
            json.has("updatedAt") -> json.get("updatedAt").asLong
            json.has("timestamp") -> json.get("timestamp").asLong
            else -> 0L
        }
    }

    /**
     * Resolve conflicts for specific data types
     */
    fun resolveStudentConflict(localStudent: Student, remoteStudent: Student): ConflictResolution {
        return when {
            localStudent.lastModified > remoteStudent.lastModified -> {
                ConflictResolution.UseLocal
            }
            remoteStudent.lastModified > localStudent.lastModified -> {
                ConflictResolution.UseRemote(gson.toJson(remoteStudent))
            }
            else -> {
                // Merge if timestamps are equal
                val mergedStudent = mergeStudents(localStudent, remoteStudent)
                ConflictResolution.Merge(gson.toJson(mergedStudent))
            }
        }
    }

    /**
     * Merge two student objects
     */
    private fun mergeStudents(local: Student, remote: Student): Student {
        return local.copy(
            name = if (local.name.isNotEmpty()) local.name else remote.name,
            surname = if (local.surname.isNotEmpty()) local.surname else remote.surname,
            parentMobile = if (local.parentMobile.isNotEmpty()) local.parentMobile else remote.parentMobile,
            parentEmail = local.parentEmail ?: remote.parentEmail,
            rate = maxOf(local.rate, remote.rate),
            className = if (local.className.isNotEmpty()) local.className else remote.className,
            lastModified = System.currentTimeMillis()
        )
    }

    /**
     * Resolve conflicts for lessons
     */
    fun resolveLessonConflict(localLesson: Lesson, remoteLesson: Lesson): ConflictResolution {
        return when {
            localLesson.lastModified > remoteLesson.lastModified -> {
                ConflictResolution.UseLocal
            }
            remoteLesson.lastModified > localLesson.lastModified -> {
                ConflictResolution.UseRemote(gson.toJson(remoteLesson))
            }
            else -> {
                val mergedLesson = mergeLessons(localLesson, remoteLesson)
                ConflictResolution.Merge(gson.toJson(mergedLesson))
            }
        }
    }

    /**
     * Merge two lesson objects
     */
    private fun mergeLessons(local: Lesson, remote: Lesson): Lesson {
        return local.copy(
            date = maxOf(local.date, remote.date),
            durationMinutes = maxOf(local.durationMinutes, remote.durationMinutes),
            notes = local.notes ?: remote.notes,
            lastModified = System.currentTimeMillis()
        )
    }
}

/**
 * Represents the resolution of a conflict
 */
sealed class ConflictResolution {
    object UseLocal : ConflictResolution()
    data class UseRemote(val data: String) : ConflictResolution()
    data class Merge(val mergedData: String) : ConflictResolution()
} 