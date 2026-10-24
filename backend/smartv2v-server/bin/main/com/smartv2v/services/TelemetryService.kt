package com.smartv2v.services

import com.smartv2v.db.TelemetryTable
import com.smartv2v.dto.TelemetryRequest
import com.smartv2v.dto.TelemetryResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class TelemetryService(private val vehicleService: VehicleService) {

    fun saveTelemetry(userId: Int, vehicleId: Int, request: TelemetryRequest): TelemetryResponse {
        // Enforce ownership verification
        if (!vehicleService.verifyVehicleOwnership(userId, vehicleId)) {
            throw SecurityException("Unauthorized: You do not own vehicle $vehicleId.")
        }

        val telemetryId = transaction {
            TelemetryTable.insert {
                it[this.vehicleId] = vehicleId
                it[latitude] = request.latitude
                it[longitude] = request.longitude
                it[speed] = request.speed
                it[heading] = request.heading
                it[accelerationX] = request.accelerationX
                it[accelerationY] = request.accelerationY
                it[accelerationZ] = request.accelerationZ
                it[gyroscopeX] = request.gyroscopeX
                it[gyroscopeY] = request.gyroscopeY
                it[gyroscopeZ] = request.gyroscopeZ
                it[timestamp] = LocalDateTime.now()
            } get TelemetryTable.id
        }

        return getLatestTelemetry(vehicleId)
            ?: throw IllegalStateException("Failed to retrieve inserted telemetry.")
    }

    fun getLatestTelemetry(vehicleId: Int): TelemetryResponse? {
        return transaction {
            TelemetryTable.select { TelemetryTable.vehicleId eq vehicleId }
                .orderBy(TelemetryTable.timestamp to SortOrder.DESC)
                .limit(1)
                .map { toTelemetryResponse(it) }
                .singleOrNull()
        }
    }

    fun getRecentTelemetryHistory(vehicleId: Int, limit: Int = 100): List<TelemetryResponse> {
        return transaction {
            TelemetryTable.select { TelemetryTable.vehicleId eq vehicleId }
                .orderBy(TelemetryTable.timestamp to SortOrder.DESC)
                .limit(limit)
                .map { toTelemetryResponse(it) }
        }
    }

    private fun toTelemetryResponse(row: ResultRow): TelemetryResponse {
        return TelemetryResponse(
            id = row[TelemetryTable.id],
            vehicleId = row[TelemetryTable.vehicleId],
            latitude = row[TelemetryTable.latitude],
            longitude = row[TelemetryTable.longitude],
            speed = row[TelemetryTable.speed],
            heading = row[TelemetryTable.heading],
            accelerationX = row[TelemetryTable.accelerationX],
            accelerationY = row[TelemetryTable.accelerationY],
            accelerationZ = row[TelemetryTable.accelerationZ],
            gyroscopeX = row[TelemetryTable.gyroscopeX],
            gyroscopeY = row[TelemetryTable.gyroscopeY],
            gyroscopeZ = row[TelemetryTable.gyroscopeZ],
            timestamp = row[TelemetryTable.timestamp].toString()
        )
    }
}
