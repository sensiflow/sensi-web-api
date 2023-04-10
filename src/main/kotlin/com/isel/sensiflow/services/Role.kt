package com.isel.sensiflow.services

enum class Role(vararg val children: Role) {
    USER,
    MODERATOR(USER),
    OWNER(MODERATOR, USER)
}

/**
 * Checks if the role has access to the given role.
 * @param role the role to check
 * @return true if the role has access to the given role, false otherwise
 */
fun Role.hasAccessTo(role: Role): Boolean {
    return this == role || this.children.contains(role)
}
