import os

# --- 1. Backend: POST /api/connections ---
index_path = "/home/randomx/Videos/mitron/backend/index.js"
with open(index_path, "r") as f:
    index_content = f.read()

if "/api/connections" not in index_content:
    patch = """
app.post('/api/connections', authenticate, async (req, res) => {
    try {
        const { senderId, receiverId } = req.body;
        const id = 'conn_' + Date.now();
        await pool.query('INSERT INTO connections (id, sender_id, receiver_id) VALUES (?, ?, ?)', [id, senderId, receiverId]);
        
        const notifId = 'notif_' + Date.now();
        await pool.query('INSERT INTO notifications (id, user_id, title, description, type, action_id) VALUES (?, ?, ?, ?, ?, ?)', 
            [notifId, receiverId, 'New Connection Request', 'Someone wants to connect with you.', 'CONNECTION_REQUEST', senderId]);
        
        res.json({ success: true });
    } catch(e) { res.status(500).json({ error: e.message }); }
});
"""
    with open(index_path, "a") as f: f.write(patch)

# --- 2. Android: Add Dependency ---
build_path = "/home/randomx/Videos/mitron/app/build.gradle.kts"
with open(build_path, "r") as f:
    build_content = f.read()

if "play-services-location" not in build_content:
    build_content = build_content.replace(
        'implementation("com.squareup.okhttp3:okhttp:4.12.0")',
        'implementation("com.google.android.gms:play-services-location:21.3.0")\n    implementation("com.squareup.okhttp3:okhttp:4.12.0")'
    )
    with open(build_path, "w") as f: f.write(build_content)

# --- 3. Android: HomeViewModel Location ---
hvm_path = "/home/randomx/Videos/mitron/app/src/main/java/com/mitron/connect/ui/screens/home/HomeViewModel.kt"
with open(hvm_path, "r") as f: hvm_content = f.read()

if "fun updateGpsLocation" not in hvm_content:
    hvm_content = hvm_content.replace(
        'val userLat = 37.7749 // Hardcoded San Francisco for demo\n    val userLng = -122.4194',
        'var userLat: Double? = null\n    var userLng: Double? = null'
    )
    hvm_content = hvm_content.replace(
        '    init {\n        startRealTimePolling()\n    }',
        '    init {\n        startRealTimePolling()\n    }\n\n    fun updateGpsLocation(lat: Double, lng: Double) {\n        userLat = lat\n        userLng = lng\n        refreshData(true)\n    }'
    )
    hvm_content = hvm_content.replace(
        'repository.updateLocation(userLat, userLng)',
        'if (userLat != null && userLng != null) repository.updateLocation(userLat!!, userLng!!)'
    )
    with open(hvm_path, "w") as f: f.write(hvm_content)

# --- 4. Android: HomeScreen Live Location ---
home_path = "/home/randomx/Videos/mitron/app/src/main/java/com/mitron/connect/ui/screens/home/HomeScreen.kt"
with open(home_path, "r") as f: home_content = f.read()

if "LocationServices" not in home_content:
    home_content = home_content.replace(
        'import androidx.compose.ui.Modifier\nimport androidx.compose.ui.graphics.Color',
        'import androidx.compose.ui.Modifier\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.platform.LocalContext\nimport com.google.android.gms.location.LocationServices'
    )
    # Inside HomeScreen composable
    home_content = home_content.replace(
        'var hasLocationPermission by remember { mutableStateOf(false) }',
        'val context = LocalContext.current\n    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }\n    var hasLocationPermission by remember { mutableStateOf(false) }'
    )
    home_content = home_content.replace(
        'hasLocationPermission = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true',
        """hasLocationPermission = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) viewModel.updateGpsLocation(loc.latitude, loc.longitude)
                }
            } catch (e: SecurityException) { }
        }"""
    )
    # Also trigger fetch if already has permission (on launch)
    home_content = home_content.replace(
        '        locationPermissionLauncher.launch(',
        """        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) viewModel.updateGpsLocation(loc.latitude, loc.longitude)
            }
        } else {
            locationPermissionLauncher.launch("""
    )
    home_content = home_content.replace(
        ')\n    }',
        ')\n        }\n    }'
    )
    
    with open(home_path, "w") as f: f.write(home_content)

print("Backend and Android location patched.")
