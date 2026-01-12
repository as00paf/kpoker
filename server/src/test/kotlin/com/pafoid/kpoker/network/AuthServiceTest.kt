package com.pafoid.kpoker.network

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import kotlin.test.*

class AuthServiceTest {

    private val authService = AuthService()

    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
        runBlocking {
            DatabaseFactory.dbQuery {
                Users.deleteAll()
            }
        }
    }

    @Test
    fun testRegisterAndLogin() = runBlocking {
        val (regSuccess, userId) = authService.register("alice", "password123")
        assertTrue(regSuccess)
        assertNotNull(userId)

        val (loginSuccess, loginUserId) = authService.login("alice", "password123")
        assertTrue(loginSuccess)
        assertEquals(userId, loginUserId)
    }

    @Test
    fun testCaseInsensitiveUsername() = runBlocking {
        authService.register("UserX", "pass")
        val (login1, _) = authService.login("userx", "pass")
        val (login2, _) = authService.login("USERX", "pass")
        assertTrue(login1, "Should login with lowercase")
        assertTrue(login2, "Should login with uppercase")
        
        val (regSuccess, _) = authService.register("userx", "other")
        assertFalse(regSuccess, "Should not allow duplicate register with different case")
    }

    @Test
    fun testDuplicateRegister() = runBlocking {
        authService.register("bob", "pass")
        val (success, message) = authService.register("bob", "other")
        assertFalse(success)
        assertEquals("Username already exists", message)
    }

    @Test
    fun testInvalidLogin() = runBlocking {
        authService.register("charlie", "secret")
        
        val (wrongPassSuccess, _) = authService.login("charlie", "wrong")
        assertFalse(wrongPassSuccess)
        
        val (notFoundSuccess, _) = authService.login("nobody", "pass")
        assertFalse(notFoundSuccess)
    }
}
