package com.pafoid.kpoker.network

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val driverClassName = "org.sqlite.JDBC"
        val jdbcURL = "jdbc:sqlite:./kpoker.db"
        val database = Database.connect(jdbcURL, driverClassName)
        
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

object Users : Table() {
    val id = varchar("id", 128)
    val username = varchar("username", 128).uniqueIndex()
    val passwordHash = varchar("passwordHash", 128)
    val bankroll = long("bankroll")

    override val primaryKey = PrimaryKey(id)
}
