package com.isel.sensiflow.amqp

/**
 * Represents a possible action to be performed by the Instance Manager Scheduler on processor instances.
 */
enum class SchedulerNotification {
    UPDATED_INSTANCE,
    REMOVED_INSTANCE;

    companion object {

        fun fromString(value: String): SchedulerNotification? {
            return try {
                SchedulerNotification.valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}