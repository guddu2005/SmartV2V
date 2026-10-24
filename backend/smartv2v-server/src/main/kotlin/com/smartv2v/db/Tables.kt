package com.smartv2v.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

object VehiclesTable : Table("vehicles") {
    val id = integer("id").autoIncrement()
    val vehicleNumber = varchar("vehicle_number", 50)
    val deviceId = varchar("device_id", 100)
    val active = bool("active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val userId = integer("user_id").references(UsersTable.id)

    override val primaryKey = PrimaryKey(id)
}

object TelemetryTable : Table("vehicle_telemetry") {
    val id = long("id").autoIncrement()
    val vehicleId = integer("vehicle_id").references(VehiclesTable.id)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val speed = double("speed")
    val heading = double("heading")
    val accelerationX = double("acceleration_x")
    val accelerationY = double("acceleration_y")
    val accelerationZ = double("acceleration_z")
    val gyroscopeX = double("gyroscope_x")
    val gyroscopeY = double("gyroscope_y")
    val gyroscopeZ = double("gyroscope_z")
    val timestamp = datetime("timestamp").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

object CollisionRiskResultsTable : Table("collision_risk_results") {
    val id = long("id").autoIncrement()
    val vehicleAId = integer("vehicle_a_id").references(VehiclesTable.id)
    val vehicleBId = integer("vehicle_b_id").references(VehiclesTable.id)
    val distance = double("distance")
    val relativeSpeed = double("relative_speed")
    val relativeAcceleration = double("relative_acceleration")
    val headingDifference = double("heading_difference")
    val accelerationMagnitude = double("acceleration_magnitude")
    val gyroscopeMagnitude = double("gyroscope_magnitude")
    val ttc = double("ttc")
    val riskProbability = double("risk_probability")
    val riskLevel = varchar("risk_level", 20)
    val timestamp = datetime("timestamp").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
