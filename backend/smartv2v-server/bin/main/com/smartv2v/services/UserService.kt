package com.smartv2v.services

import com.smartv2v.db.UsersTable
import com.smartv2v.dto.AuthResponse
import com.smartv2v.dto.LoginRequest
import com.smartv2v.dto.RegisterRequest
import com.smartv2v.dto.UserDto
import com.smartv2v.utils.JwtConfig
import com.smartv2v.utils.PasswordHash
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class UserService {

    fun register(request: RegisterRequest): AuthResponse {
        val existing = transaction {
            UsersTable.select { UsersTable.email eq request.email.lowercase().trim() }.singleOrNull()
        }

        if (existing != null) {
            throw IllegalArgumentException("User with email '${request.email}' already exists.")
        }

        val hashedPassword = PasswordHash.hash(request.password)

        val userId = transaction {
            UsersTable.insert {
                it[name] = request.name
                it[email] = request.email.lowercase().trim()
                it[passwordHash] = hashedPassword
                it[createdAt] = LocalDateTime.now()
            } get UsersTable.id
        }

        val userDto = UserDto(
            id = userId,
            name = request.name,
            email = request.email.lowercase().trim(),
            createdAt = LocalDateTime.now().toString()
        )

        val token = JwtConfig.generateToken(userId, userDto.email)
        return AuthResponse(token = token, user = userDto)
    }

    fun login(request: LoginRequest): AuthResponse {
        val userRow = transaction {
            UsersTable.select { UsersTable.email eq request.email.lowercase().trim() }.singleOrNull()
        } ?: throw IllegalArgumentException("Invalid email or password.")

        val storedHash = userRow[UsersTable.passwordHash]
        if (!PasswordHash.verify(request.password, storedHash)) {
            throw IllegalArgumentException("Invalid email or password.")
        }

        val userId = userRow[UsersTable.id]
        val name = userRow[UsersTable.name]
        val email = userRow[UsersTable.email]
        val createdAt = userRow[UsersTable.createdAt].toString()

        val userDto = UserDto(id = userId, name = name, email = email, createdAt = createdAt)
        val token = JwtConfig.generateToken(userId, email)

        return AuthResponse(token = token, user = userDto)
    }

    fun getUserById(userId: Int): UserDto? {
        return transaction {
            UsersTable.select { UsersTable.id eq userId }.map {
                UserDto(
                    id = it[UsersTable.id],
                    name = it[UsersTable.name],
                    email = it[UsersTable.email],
                    createdAt = it[UsersTable.createdAt].toString()
                )
            }.singleOrNull()
        }
    }
}
