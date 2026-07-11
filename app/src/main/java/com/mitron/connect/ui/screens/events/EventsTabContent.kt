package com.mitron.connect.ui.screens.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.mitron.connect.ui.components.EventCard
import com.mitron.connect.ui.screens.home.HomeViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.blur

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsTabContent(
    modifier: Modifier = Modifier,
    onOpenEvent: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val events by viewModel.events.collectAsState()
    
    // Default to San Francisco or user's location
    val defaultLocation = LatLng(37.7749, -122.4194)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
    }
    
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 160.dp,
        sheetContainerColor = Color.White.copy(alpha = 0.75f),
        sheetShape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        sheetContent = {
            Box(modifier = Modifier.fillMaxHeight(0.8f).blur(16.dp)) {
                // Glass effect blur box
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxHeight(0.8f).padding(top = 16.dp)
            ) {
                item {
                    Text(
                        "Explore Events",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(events) { event ->
                    var isAttending by remember { mutableStateOf(false) }
                    var attendeeCount by remember { mutableStateOf((20..150).random()) }
                    
                    EventCard(
                        title = event.title,
                        date = event.date,
                        location = event.location,
                        imageUrl = null,
                        isAttending = isAttending,
                        attendeeCount = attendeeCount,
                        onRsvpClick = {
                            isAttending = !isAttending
                            attendeeCount += if (isAttending) 1 else -1
                        },
                        onClick = {
                            val lat = event.lat ?: defaultLocation.latitude
                            val lng = event.lng ?: defaultLocation.longitude
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15f),
                                    durationMs = 1000
                                )
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        GoogleMap(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false)
        ) {
            events.forEach { event ->
                val lat = event.lat ?: (defaultLocation.latitude + (Math.random() - 0.5) * 0.1)
                val lng = event.lng ?: (defaultLocation.longitude + (Math.random() - 0.5) * 0.1)
                Marker(
                    state = MarkerState(position = LatLng(lat, lng)),
                    title = event.title,
                    snippet = event.location,
                    onClick = {
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLng(LatLng(lat, lng)), 500)
                            scaffoldState.bottomSheetState.expand()
                        }
                        true
                    }
                )
            }
        }
    }
}
