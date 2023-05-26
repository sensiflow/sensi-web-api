package com.isel.sensiflow.services.dto.output

import com.isel.sensiflow.services.dto.PageableDTO
import org.springframework.data.domain.Page
import kotlin.math.ceil

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

/**
 * Converts a [List] to a [PageDTO].
 * @param T The type of the items in the list
 */
fun <T> List<T>.toPageDTO(paginationModel: PageableDTO): PageDTO<T> {
    val numElementsUntilCurrentPage = paginationModel.page * paginationModel.size
    val numElementUntilNextPage = paginationModel.size * (paginationModel.page + 1)
    val items =
        if (this.size <= paginationModel.size)
            this
        else
            this.slice(numElementsUntilCurrentPage until numElementUntilNextPage)

    val ceilTotalPages =
        if (paginationModel.size == 0)
            1
        else
            ceil((this.size.toDouble() / paginationModel.size)).toInt()

    val totalPages =
        if (paginationModel.size == 0)
            0
        else
            ceilTotalPages

    return PageDTO(
        totalElements = this.size.toLong(),
        totalPages = totalPages,
        isFirst = paginationModel.page == 0,
        isLast = totalPages == paginationModel.page + 1,
        items = items
    )
}
