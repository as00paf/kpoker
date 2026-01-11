package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.model.User
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AuthService {
    private val users = ConcurrentHashMap<String, User>() // username to User

    fun register(username: String, password: String): Pair<Boolean, String?> {
        if (users.containsKey(username)) {
            return false to "Username already exists"
        }
        val id = UUID.randomUUID().toString()
        val user = User(id, username, hashPassword(password))
        users[username] = user
        return true to id
    }

    fun login(username: String, password: String): Pair<Boolean, String?> {
        val user = users[username] ?: return false to "User not found"
        if (user.passwordHash == hashPassword(password)) {
            return true to user.id
        }
        return false to "Invalid password"
    }

    private fun hashPassword(password: String): String {
        // Simple placeholder for hashing
        return "hashed_$password"
    }
}
