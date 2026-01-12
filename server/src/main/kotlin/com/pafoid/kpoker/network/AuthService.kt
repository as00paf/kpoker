package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class AuthService {

    suspend fun register(username: String, password: String): Pair<Boolean, String?> = DatabaseFactory.dbQuery {
        val lowerUsername = username.lowercase()
        val existing = Users.select(Users.id).where { Users.username eq lowerUsername }.singleOrNull()
        if (existing != null) {
            return@dbQuery false to "Username already exists"
        }
        
        val id = UUID.randomUUID().toString()
        Users.insert {
            it[Users.id] = id
            it[Users.username] = lowerUsername
            it[Users.passwordHash] = hashPassword(password)
        }
        true to id
    }

    suspend fun login(username: String, password: String): Pair<Boolean, String?> = DatabaseFactory.dbQuery {
        val lowerUsername = username.lowercase()
        val user = Users.selectAll().where { Users.username eq lowerUsername }.singleOrNull()
            ?: return@dbQuery false to "User not found"
            
        if (user[Users.passwordHash] == hashPassword(password)) {
            return@dbQuery true to user[Users.id]
        }
        false to "Invalid password"
    }

    suspend fun changePassword(userId: String, newPassword: String): Boolean = DatabaseFactory.dbQuery {
        Users.update({ Users.id eq userId }) {
            it[Users.passwordHash] = hashPassword(newPassword)
        } > 0
    }

    suspend fun changeUsername(userId: String, newUsername: String): Pair<Boolean, String> = DatabaseFactory.dbQuery {
        val lowerUsername = newUsername.lowercase()
        val existing = Users.select(Users.id).where { Users.username eq lowerUsername }.singleOrNull()
        if (existing != null) {
            return@dbQuery false to "Username already exists"
        }

        val updated = Users.update({ Users.id eq userId }) {
            it[Users.username] = lowerUsername
        } > 0
        
        if (updated) true to "Username updated" else false to "User not found"
    }

    private fun hashPassword(password: String): String {
        return "hashed_$password"
    }
}
