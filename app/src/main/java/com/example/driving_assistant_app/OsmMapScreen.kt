package com.example.driving_assistant_app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun OsmMapFullScreen(
    modifier: Modifier = Modifier,
    showMyLocation: Boolean = false
) {
    val context = LocalContext.current

    val prefs = context.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
    Configuration.getInstance().load(context, prefs)
    Configuration.getInstance().userAgentValue = context.packageName

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                val mapController = controller
                mapController.setZoom(15.0)

                // Punto inicial (por si no hay GPS todavía)
                val startPoint = GeoPoint(40.4168, -3.7038) // Madrid
                mapController.setCenter(startPoint)

                if (showMyLocation) {
                    val locationOverlay = MyLocationNewOverlay(
                        GpsMyLocationProvider(ctx),
                        this
                    ).apply {
                        enableMyLocation()      // Empieza a escuchar ubicación
                        enableFollowLocation()  // Mueve la cámara a tu posición
                    }
                    overlays.add(locationOverlay)
                }
            }
        }
    )
}
