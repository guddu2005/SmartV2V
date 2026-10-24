package com.smartv2v.services

import com.smartv2v.dto.MLPredictionRequest
import com.smartv2v.dto.MLPredictionResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class MLClientService(private val mlServiceUrl: String = "http://localhost:8000/predict") {

    private val logger = LoggerFactory.getLogger(MLClientService::class.java)

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getCollisionRiskPrediction(request: MLPredictionRequest): MLPredictionResponse {
        return try {
            val response = client.post(mlServiceUrl) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.isSuccess()) {
                response.body<MLPredictionResponse>()
            } else {
                logger.error("ML service returned status: ${response.status}")
                fallbackPrediction(request)
            }
        } catch (e: Exception) {
            logger.warn("Unable to reach ML FastAPI service at $mlServiceUrl: ${e.message}")
            fallbackPrediction(request)
        }
    }

    /**
     * Fallback calculation if Python ML service is offline or unreachable.
     */
    private fun fallbackPrediction(req: MLPredictionRequest): MLPredictionResponse {
        val highRisk = req.ttc in 0.01..3.0 || (req.distance < 15.0 && req.relativeSpeed > 10.0)
        val medRisk = req.ttc in 3.01..6.0 || (req.distance < 35.0 && req.relativeSpeed > 5.0)

        val prob = when {
            highRisk -> 0.85
            medRisk -> 0.45
            else -> 0.10
        }

        val level = when {
            prob >= 0.70 -> "HIGH"
            prob >= 0.30 -> "MEDIUM"
            else -> "LOW"
        }

        return MLPredictionResponse(
            prediction = if (prob >= 0.50) 1 else 0,
            riskProbability = prob,
            riskPercentage = (prob * 100).toInt(),
            riskLevel = level
        )
    }
}
