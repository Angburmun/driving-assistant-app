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

@Composable
fun OsmMapFullScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // SharedPreferences "modernas", sin PreferenceManager deprecado
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

                // Ejemplo: Madrid
                val startPoint = GeoPoint(40.4168, -3.7038)
                mapController.setCenter(startPoint)
            }
        }
    )
}
