package com.smartv2v.services

import com.smartv2v.db.CollisionRiskResultsTable
import com.smartv2v.dto.MLPredictionRequest
import com.smartv2v.dto.RiskResponse
import com.smartv2v.utils.GeoUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class RiskService(
    private val telemetryService: TelemetryService,
    private val mlClientService: MLClientService
) {

    suspend fun calculateAndStoreRisk(vehicleAId: Int, vehicleBId: Int): RiskResponse {
        val telemA = telemetryService.getLatestTelemetry(vehicleAId)
            ?: throw IllegalArgumentException("No telemetry recorded for Vehicle A (ID: $vehicleAId)")
        val telemB = telemetryService.getLatestTelemetry(vehicleBId)
            ?: throw IllegalArgumentException("No telemetry recorded for Vehicle B (ID: $vehicleBId)")

        // 1. Calculate features
        val distance = GeoUtils.calculateHaversineDistance(
            telemA.latitude, telemA.longitude,
            telemB.latitude, telemB.longitude
        )

        val relativeSpeed = GeoUtils.calculateRelativeSpeed(telemA.speed, telemB.speed)

        val accelMagA = GeoUtils.calculateMagnitude(telemA.accelerationX, telemA.accelerationY, telemA.accelerationZ)
        val accelMagB = GeoUtils.calculateMagnitude(telemB.accelerationX, telemB.accelerationY, telemB.accelerationZ)
        val relativeAccel = kotlin.math.abs(accelMagA - accelMagB)

        val headingDiff = GeoUtils.calculateHeadingDifference(telemA.heading, telemB.heading)

        val gyroMagA = GeoUtils.calculateMagnitude(telemA.gyroscopeX, telemA.gyroscopeY, telemA.gyroscopeZ)
        val gyroMagB = GeoUtils.calculateMagnitude(telemB.gyroscopeX, telemB.gyroscopeY, telemB.gyroscopeZ)
        val combinedGyroMag = (gyroMagA + gyroMagB) / 2.0

        val ttc = GeoUtils.calculateTTC(distance, telemA.speed, telemB.speed, headingDiff)

        // 2. Query ML Service
        val mlRequest = MLPredictionRequest(
            distance = distance,
            relativeSpeed = relativeSpeed,
            relativeAcceleration = relativeAccel,
            headingDifference = headingDiff,
            accelerationMagnitude = accelMagA,
            gyroscopeMagnitude = gyroMagA,
            ttc = ttc,
            speedA = telemA.speed,
            speedB = telemB.speed
        )

        val mlPrediction = mlClientService.getCollisionRiskPrediction(mlRequest)

        // 3. Store result in PostgreSQL
        val insertedId = transaction {
            CollisionRiskResultsTable.insert {
                it[this.vehicleAId] = vehicleAId
                it[this.vehicleBId] = vehicleBId
                it[this.distance] = distance
                it[this.relativeSpeed] = relativeSpeed
                it[this.relativeAcceleration] = relativeAccel
                it[this.headingDifference] = headingDiff
                it[this.accelerationMagnitude] = accelMagA
                it[this.gyroscopeMagnitude] = combinedGyroMag
                it[this.ttc] = ttc
                it[this.riskProbability] = mlPrediction.riskProbability
                it[this.riskLevel] = mlPrediction.riskLevel
                it[this.timestamp] = LocalDateTime.now()
            } get CollisionRiskResultsTable.id
        }

        return RiskResponse(
            id = insertedId,
            vehicleAId = vehicleAId,
            vehicleBId = vehicleBId,
            distance = distance,
            relativeSpeed = relativeSpeed,
            relativeAcceleration = relativeAccel,
            headingDifference = headingDiff,
            accelerationMagnitude = accelMagA,
            gyroscopeMagnitude = combinedGyroMag,
            ttc = ttc,
            riskProbability = mlPrediction.riskProbability,
            riskLevel = mlPrediction.riskLevel,
            timestamp = LocalDateTime.now().toString()
        )
    }

    fun getLatestRiskResult(vehicleAId: Int, vehicleBId: Int): RiskResponse? {
        return transaction {
            val isMatchAB = (CollisionRiskResultsTable.vehicleAId eq vehicleAId) and (CollisionRiskResultsTable.vehicleBId eq vehicleBId)
            val isMatchBA = (CollisionRiskResultsTable.vehicleAId eq vehicleBId) and (CollisionRiskResultsTable.vehicleBId eq vehicleAId)

            CollisionRiskResultsTable.select { isMatchAB or isMatchBA }
                .orderBy(CollisionRiskResultsTable.timestamp to SortOrder.DESC)
                .limit(1)
                .map { row ->
                    RiskResponse(
                        id = row[CollisionRiskResultsTable.id],
                        vehicleAId = row[CollisionRiskResultsTable.vehicleAId],
                        vehicleBId = row[CollisionRiskResultsTable.vehicleBId],
                        distance = row[CollisionRiskResultsTable.distance],
                        relativeSpeed = row[CollisionRiskResultsTable.relativeSpeed],
                        relativeAcceleration = row[CollisionRiskResultsTable.relativeAcceleration],
                        headingDifference = row[CollisionRiskResultsTable.headingDifference],
                        accelerationMagnitude = row[CollisionRiskResultsTable.accelerationMagnitude],
                        gyroscopeMagnitude = row[CollisionRiskResultsTable.gyroscopeMagnitude],
                        ttc = row[CollisionRiskResultsTable.ttc],
                        riskProbability = row[CollisionRiskResultsTable.riskProbability],
                        riskLevel = row[CollisionRiskResultsTable.riskLevel],
                        timestamp = row[CollisionRiskResultsTable.timestamp].toString()
                    )
                }
                .singleOrNull()
        }
    }
}
