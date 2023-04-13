package com.isel.sensiflow.services.dto

import com.isel.sensiflow.Constants

/**
 * Pagination information
 * @param page The page number
 * @param size The page size
 */
class PageableDTO(page: Int?, size: Int?) {
    val page: Int = page ?: Constants.Pagination.DEFAULT_PAGE
    val size: Int = size?.coerceAtMost(Constants.Pagination.MAX_PAGE_SIZE) ?: Constants.Pagination.DEFAULT_PAGE_SIZE
}
