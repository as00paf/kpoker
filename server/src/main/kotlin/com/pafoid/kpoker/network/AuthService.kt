package com.pafoid.kpoker.network

import com.pafoid.kpoker.domain.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class AuthService {

    suspend fun register(username: String, password: String): Pair<Boolean, String?> = DatabaseFactory.dbQuery {
        val existing = Users.select(Users.id).where { Users.username eq username }.singleOrNull()
        if (existing != null) {
            return@dbQuery false to "Username already exists"
        }
        
        val id = UUID.randomUUID().toString()
        Users.insert {
            it[Users.id] = id
            it[Users.username] = username
            it[Users.passwordHash] = hashPassword(password)
        }
        true to id
    }

    suspend fun login(username: String, password: String): Pair<Boolean, String?> = DatabaseFactory.dbQuery {
        val user = Users.selectAll().where { Users.username eq username }.singleOrNull()
            ?: return@dbQuery false to "User not found"
            
        if (user[Users.passwordHash] == hashPassword(password)) {
            return@dbQuery true to user[Users.id]
        }
        false to "Invalid password"
    }

    private fun hashPassword(password: String): String {
        return "hashed_$password"
    }
}
