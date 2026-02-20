package com.example.mapapp.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen() {
    val context = LocalContext.current

    // variable that stores if user granted access their location
    var isLocationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // request permission launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        isLocationPermissionGranted = isGranted
    }

     // Automatically request location permission when app open
     LaunchedEffect(Unit) {
        if (!isLocationPermissionGranted) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
     }

    if (!isLocationPermissionGranted) {
        LocationPermissionRequest {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    } else {
        ApplicationMap()
    }
}

@Composable
private fun LocationPermissionRequest(
    onRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = onRequest,
            shape = RectangleShape,
            colors = ButtonDefaults
                .buttonColors(
                    containerColor = Color(0xff00b4d8)
                )
        ) {
            Text("Grant location permission")
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun LocationPermissionRequestPreview() {
    LocationPermissionRequest(
        onRequest = {}
    )
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun ApplicationMap() {
    val context = LocalContext.current

    // Client to interact with the Google Play Services location API
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var userLatLng by remember {
        mutableStateOf(LatLng(0.0, 0.0))
    }

    // Set up the initial camera position (looking at a general area first)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLatLng,
            2f
        )
    }

    LaunchedEffect(Unit) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        userLatLng = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position = CameraPosition
                            .fromLatLngZoom(userLatLng, 15f)
                    }
                }
        } catch (e: SecurityException) {
            Toast.makeText(
                context,
                "Some error occurred :(",
                Toast.LENGTH_LONG
            ).show()
            println(e.printStackTrace())
        }
    }

    // Google Map component
    GoogleMap(
        modifier = Modifier
            .fillMaxSize(),
        cameraPositionState = cameraPositionState,
        // Properties allow us to enable the "Blue Dot" (My Location layer)
//        properties = MapProperties(
//            isMyLocationEnabled = true, // This shows the blue dot on the map
//        ),
//        // UI Settings allow us to show/hide standard buttons
//        uiSettings = MapUiSettings(
//            myLocationButtonEnabled = true // Shows the button to center the map on the user
//        )
    ) {
        Marker(
            state = MarkerState(position = userLatLng),
            title = "You are here",
            snippet = "Coordinates: ${userLatLng.latitude}, ${userLatLng.longitude}"
        )
    }
}

