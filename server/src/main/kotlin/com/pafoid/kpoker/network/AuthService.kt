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
            it[Users.bankroll] = 10000L // Initialize bankroll
        }
        true to id
    }

    suspend fun login(username: String, password: String): Pair<Boolean, Pair<String, Long>?> = DatabaseFactory.dbQuery {
        val lowerUsername = username.lowercase()
        val user = Users.selectAll().where { Users.username eq lowerUsername }.singleOrNull()
            ?: return@dbQuery false to null // Return null for user and bankroll if not found
            
        if (user[Users.passwordHash] == hashPassword(password)) {
            return@dbQuery true to (user[Users.id] to user[Users.bankroll]) // Return ID and bankroll
        }
        false to null // Return null for user and bankroll if password is invalid
    }

    suspend fun getUserBankroll(userId: String): Long? = DatabaseFactory.dbQuery {
        Users.select(Users.bankroll).where { Users.id eq userId }.singleOrNull()?.get(Users.bankroll)
    }

    suspend fun updateUserBankroll(userId: String, amountChange: Long): Boolean = DatabaseFactory.dbQuery {
        val currentBankroll = Users.select(Users.bankroll).where { Users.id eq userId }.singleOrNull()?.get(Users.bankroll)
        if (currentBankroll == null) {
            return@dbQuery false // User not found
        }
        
        // Ensure bankroll doesn't go below zero (optional, but good practice for game currency)
        val newBankroll = (currentBankroll + amountChange).coerceAtLeast(0L)
        
        Users.update({ Users.id eq userId }) {
            it[Users.bankroll] = newBankroll
        } > 0
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
