package com.isel.sensiflow.services.dto.output

import org.springframework.data.domain.Page

/**
 * Dto of a page of items.
 * @param T The type of the items in the page
 * @param totalElements The total number of elements
 * @param totalPages The total number of pages
 * @param isLast True if the page is the last page
 * @param isFirst True if the page is the first page
 * @param items The items in the page
 */
data class PageDTO<T>(
    val totalElements: Long,
    val totalPages: Int,
    val isLast: Boolean,
    val isFirst: Boolean,
    val items: List<T>
)

/**
 * Converts a [Page] to a [PageDTO].
 * @param T The type of the items in the page
 */
fun <T> Page<T>.toPageDTO(): PageDTO<T> {

    return PageDTO(
        totalElements = this.totalElements,
        totalPages = this.totalPages,
        isFirst = this.isFirst,
        isLast = this.isLast,
        items = this.content
    )
}
