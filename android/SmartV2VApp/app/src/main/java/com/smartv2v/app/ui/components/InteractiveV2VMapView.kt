package com.smartv2v.app.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.smartv2v.app.ui.theme.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveV2VMapView(
    latA: Double,
    lonA: Double,
    headingA: Double,
    latB: Double,
    lonB: Double,
    headingB: Double,
    distanceMeters: Double,
    riskLevel: String,
    modifier: Modifier = Modifier
) {
    val riskHexColor = when (riskLevel) {
        "HIGH" -> "#E11D48"
        "MEDIUM" -> "#D97706"
        else -> "#16A34A"
    }

    // Fallback coordinates if GPS is 0.0 (e.g., inside building without satellite lock)
    val actualLatA = if (latA != 0.0) latA else 17.38504
    val actualLonA = if (lonA != 0.0) lonA else 78.48667
    val actualLatB = if (latB != 0.0) latB else actualLatA + 0.0003
    val actualLonB = if (lonB != 0.0) lonB else actualLonA + 0.0003

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // HTML / Leaflet OpenStreetMap payload
    val mapHtml = remember(actualLatA, actualLonA, actualLatB, actualLonB, riskHexColor) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #F8FAFC; }
                .car-icon {
                    font-size: 26px;
                    text-align: center;
                    line-height: 32px;
                    filter: drop-shadow(0px 2px 4px rgba(0,0,0,0.3));
                }
                .gap-pill {
                    background: #0F172A;
                    color: white;
                    font-weight: bold;
                    font-size: 11px;
                    padding: 3px 8px;
                    border-radius: 12px;
                    border: 1px solid $riskHexColor;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.25);
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { zoomControl: false, attributionControl: false });
                
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                }).addTo(map);

                var posA = [$actualLatA, $actualLonA];
                var posB = [$actualLatB, $actualLonB];

                // Host Car Marker (Vehicle A)
                var iconA = L.divIcon({
                    className: 'custom-icon',
                    html: '<div class="car-icon" style="transform: rotate(${headingA}deg);">🚗</div>',
                    iconSize: [32, 32],
                    iconAnchor: [16, 16]
                });
                var markerA = L.marker(posA, { icon: iconA }).addTo(map).bindPopup("<b>Host Vehicle (V1)</b>");

                // Target Lead Car Marker (Vehicle B)
                var iconB = L.divIcon({
                    className: 'custom-icon',
                    html: '<div class="car-icon" style="transform: rotate(${headingB}deg); color: $riskHexColor;">🚘</div>',
                    iconSize: [32, 32],
                    iconAnchor: [16, 16]
                });
                var markerB = L.marker(posB, { icon: iconB }).addTo(map).bindPopup("<b>Target Vehicle (V2)</b>");

                // Connecting V2V Hazard Ray Polyline
                var latlngs = [posA, posB];
                var polyline = L.polyline(latlngs, {
                    color: '$riskHexColor',
                    weight: 4,
                    opacity: 0.85,
                    dashArray: '6, 8'
                }).addTo(map);

                // Fit view to include both vehicles
                var group = new L.featureGroup([markerA, markerB]);
                map.fitBounds(group.getBounds().pad(0.35));
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, NeoCardBorder, RoundedCornerShape(20.dp))
            .background(NeoCardSurface)
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    loadDataWithBaseURL("https://openstreetmap.org", mapHtml, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = { view ->
                view.loadDataWithBaseURL("https://openstreetmap.org", mapHtml, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Map Header Pill
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = NeoCardSurface.copy(alpha = 0.94f),
            shadowElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = NeoObsidian,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "OpenStreetMap V2V Mesh",
                    color = NeoObsidian,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Overlay Live Gap Pill at Bottom Right
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NeoObsidian,
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .border(1.dp, Color(android.graphics.Color.parseColor(riskHexColor)), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = null,
                    tint = Color(android.graphics.Color.parseColor(riskHexColor)),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "%.1f m Gap".format(distanceMeters),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
