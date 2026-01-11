package com.pafoid.kpoker.network

import kotlinx.coroutines.runBlocking
import kotlin.test.*

class AuthServiceTest {

    private val authService = AuthService()

    @BeforeTest
    fun setup() {
        DatabaseFactory.init()
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
