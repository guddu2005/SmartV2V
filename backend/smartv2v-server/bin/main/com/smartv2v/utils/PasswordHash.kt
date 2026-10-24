package com.smartv2v.utils

import org.mindrot.jbcrypt.BCrypt

object PasswordHash {
    fun hash(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    fun verify(password: String, hashed: String): Boolean {
        return try {
            BCrypt.checkpw(password, hashed)
        } catch (e: Exception) {
            false
        }
    }
}
