package com.isel.sensiflow.services.dto

import com.isel.sensiflow.services.InvalidParameterException
import java.sql.Timestamp

/**
 * DTO that represents a request to retrieve metrics from the repository.
 */
class MetricRequestDTO(
    startTimeInput: String? = null,
    endTimeInput: String? = null
) {

    val startTime: Timestamp? = safeExtractTimestamp(startTimeInput)
    val endTime: Timestamp? = safeExtractTimestamp(endTimeInput)

    init {
        startTime?.let {
            endTime?.let {
                if (startTime.after(endTime)) {
                    throw InvalidParameterException("Start time must be before end time.")
                }
            }
        }
    }

    private fun safeExtractTimestamp(timestamp: String?): Timestamp? {
        return timestamp?.let {
            try {
                Timestamp.valueOf(timestamp)
            } catch (e: IllegalArgumentException) {
                throw InvalidParameterException("Invalid timestamp format: $timestamp. Expected format: yyyy-mm-dd hh:mm:ss")
            }
        }
    }

    /**
     * The type of request to be made to the repository.
     */
    enum class RequestType {
        ALL,
        BETWEEN,
        AFTER,
        BEFORE
    }

    /**
     * The type of request based on the given timestamps.
     */
    val requestType: RequestType = when {
        startTime != null && endTime != null -> RequestType.BETWEEN
        startTime != null -> RequestType.AFTER
        endTime != null -> RequestType.BEFORE
        else -> RequestType.ALL
    }
}
