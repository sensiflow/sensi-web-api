package com.isel.sensiflow.http.entities.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validates that the annotated string is not blank.
 * If the string is null, it is considered as valid.
 * Can be used on nullable fields
 */
@Constraint(validatedBy = [NotBlankNullableValidator::class])
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.VALUE_PARAMETER,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotBlankNullable(
    val message: String = "This field cannot be blank",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class NotBlankNullableValidator : ConstraintValidator<NotBlankNullable, String?> {
    override fun isValid(value: String?, cxt: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        return value.isNotBlank()
    }
}
