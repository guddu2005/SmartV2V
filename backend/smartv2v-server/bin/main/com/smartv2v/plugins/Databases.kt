package com.smartv2v.plugins

import com.smartv2v.db.CollisionRiskResultsTable
import com.smartv2v.db.TelemetryTable
import com.smartv2v.db.UsersTable
import com.smartv2v.db.VehiclesTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.sql.DriverManager

fun Application.configureDatabases() {
    val logger = LoggerFactory.getLogger("DatabasesPlugin")

    val driverClass = environment.config.propertyOrNull("database.driver")?.getString() ?: "org.postgresql.Driver"
    val configUrl = environment.config.propertyOrNull("database.url")?.getString() ?: "jdbc:postgresql://localhost:5433/smartv2v_db"
    val dbUser = environment.config.propertyOrNull("database.user")?.getString() ?: "postgres"
    val dbPassword = environment.config.propertyOrNull("database.password")?.getString() ?: "password"

    // Candidate PostgreSQL URLs: 5433, 5432
    val candidateUrls = listOf(
        configUrl,
        "jdbc:postgresql://localhost:5433/smartv2v_db",
        "jdbc:postgresql://localhost:5432/smartv2v_db",
        "jdbc:postgresql://localhost:5433/postgres",
        "jdbc:postgresql://localhost:5432/postgres"
    ).distinct()

    var connected = false

    for (targetUrl in candidateUrls) {
        try {
            logger.info("Attempting connection to PostgreSQL database at $targetUrl...")
            val hikariConfig = HikariConfig().apply {
                this.driverClassName = driverClass
                this.jdbcUrl = targetUrl
                this.username = dbUser
                this.password = dbPassword
                this.maximumPoolSize = 10
                this.connectionTimeout = 3000
                this.isAutoCommit = false
                this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            }

            val dataSource = HikariDataSource(hikariConfig)
            Database.connect(dataSource)

            transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    UsersTable,
                    VehiclesTable,
                    TelemetryTable,
                    CollisionRiskResultsTable
                )
            }
            logger.info("Successfully connected to PostgreSQL at $targetUrl")
            connected = true
            break
        } catch (e: Exception) {
            logger.warn("Could not connect to $targetUrl: ${e.message}")
        }
    }

    if (!connected) {
        logger.error("All PostgreSQL connection attempts failed. Initializing fail-safe H2 In-Memory Database...")
        val h2Url = "jdbc:h2:mem:smartv2v_fallback;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
        val h2Driver = "org.h2.Driver"
        
        Database.connect(h2Url, driver = h2Driver)
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                UsersTable,
                VehiclesTable,
                TelemetryTable,
                CollisionRiskResultsTable
            )
        }
        logger.info("Fail-safe database initialized successfully!")
    }
}
