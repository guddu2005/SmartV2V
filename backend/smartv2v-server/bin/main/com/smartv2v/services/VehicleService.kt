package com.smartv2v.services

import com.smartv2v.db.VehiclesTable
import com.smartv2v.dto.CreateVehicleRequest
import com.smartv2v.dto.UpdateVehicleRequest
import com.smartv2v.dto.VehicleResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class VehicleService {

    fun createVehicle(userIdParam: Int, request: CreateVehicleRequest): VehicleResponse {
        val vehicleId = transaction {
            VehiclesTable.insert {
                it[vehicleNumber] = request.vehicleNumber
                it[deviceId] = request.deviceId
                it[active] = true
                it[createdAt] = LocalDateTime.now()
                it[userId] = userIdParam
            } get VehiclesTable.id
        }

        return getVehicleById(vehicleId) ?: throw IllegalStateException("Vehicle creation failed.")
    }

    fun getVehiclesByUserId(userId: Int): List<VehicleResponse> {
        return transaction {
            VehiclesTable.select { VehiclesTable.userId eq userId }
                .map { toVehicleResponse(it) }
        }
    }

    fun getVehicleById(vehicleId: Int): VehicleResponse? {
        return transaction {
            VehiclesTable.select { VehiclesTable.id eq vehicleId }
                .map { toVehicleResponse(it) }
                .singleOrNull()
        }
    }

    fun updateVehicle(userId: Int, vehicleId: Int, request: UpdateVehicleRequest): VehicleResponse {
        val vehicle = getVehicleById(vehicleId)
            ?: throw IllegalArgumentException("Vehicle with ID $vehicleId not found.")

        if (vehicle.userId != userId) {
            throw SecurityException("Unauthorized: You do not own this vehicle.")
        }

        transaction {
            VehiclesTable.update({ VehiclesTable.id eq vehicleId }) {
                request.vehicleNumber?.let { num -> it[vehicleNumber] = num }
                request.deviceId?.let { dev -> it[deviceId] = dev }
                request.active?.let { act -> it[active] = act }
            }
        }

        return getVehicleById(vehicleId)!!
    }

    fun deleteVehicle(userId: Int, vehicleId: Int): Boolean {
        val vehicle = getVehicleById(vehicleId)
            ?: throw IllegalArgumentException("Vehicle with ID $vehicleId not found.")

        if (vehicle.userId != userId) {
            throw SecurityException("Unauthorized: You do not own this vehicle.")
        }

        val deletedRows = transaction {
            VehiclesTable.deleteWhere { VehiclesTable.id eq vehicleId }
        }

        return deletedRows > 0
    }

    fun verifyVehicleOwnership(userId: Int, vehicleId: Int): Boolean {
        val vehicle = getVehicleById(vehicleId) ?: return false
        return vehicle.userId == userId
    }

    private fun toVehicleResponse(row: ResultRow): VehicleResponse {
        return VehicleResponse(
            id = row[VehiclesTable.id],
            vehicleNumber = row[VehiclesTable.vehicleNumber],
            deviceId = row[VehiclesTable.deviceId],
            active = row[VehiclesTable.active],
            userId = row[VehiclesTable.userId],
            createdAt = row[VehiclesTable.createdAt].toString()
        )
    }
}
