package com.isel.sensiflow.services

/**
 * Throws an [InputConstraintViolationException] if the [constraint] is not satisfied.
 */
inline fun requireConstraint(constraint: Boolean, lazyMessage: () -> String) {
    if (!constraint) {
        throw InputConstraintViolationException(lazyMessage())
    }
}

/**
 * Throws an [InputConstraintViolationException] if the [param] is null or blank.
 */
fun requireNotBlank(param: String?, lazyMessage: () -> String) {
    if (param != null && param.isBlank()) {
        throw InputConstraintViolationException(lazyMessage())
    }
}

/**
 * Throws an [InputConstraintViolationException] if the [param] is null.
 */
fun requireParameter(param: String?, lazyMessage: () -> String): String {
    if (param == null) {
        throw InputConstraintViolationException(lazyMessage())
    }
    requireNotBlank(param, lazyMessage)
    return param
}

