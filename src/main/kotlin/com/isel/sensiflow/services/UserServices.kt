package com.isel.sensiflow.services

import com.isel.sensiflow.data.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository : UserRepository) {

    @Transactional( isolation = Isolation.DEFAULT)
    fun example(): Long {
        return userRepository.count()

    }


}