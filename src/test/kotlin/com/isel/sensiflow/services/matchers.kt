package com.isel.sensiflow.services

import org.mockito.Mockito

/**
 * Workaround for the Mockito.any() method, which is not working with Kotlin
 * @param type The type of the argument to be matched
 *
 * @see <a href="https://stackoverflow.com/questions/59230041/argumentmatchers-any-must-not-be-null">Stack Overflow 59230041</a>
 * @see <a href="https://discuss.kotlinlang.org/t/how-to-use-mockito-with-kotlin/324">Kotlin Forum 324</a>
 */
fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
