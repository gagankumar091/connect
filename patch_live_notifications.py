import os

base_dir = "/home/randomx/Videos/mitron"
backend_dir = os.path.join(base_dir, "backend")
android_dir = os.path.join(base_dir, "app/src/main/java/com/mitron/connect")

# --- 1. BACKEND: setup.js ---
setup_path = os.path.join(backend_dir, "setup.js")
with open(setup_path, "r") as f:
    setup_content = f.read()
if "DROP TABLE IF EXISTS notifications" not in setup_content:
    setup_content = setup_content.replace(
        'await connection.query(`DROP TABLE IF EXISTS briefing_items`);',
        'await connection.query(`DROP TABLE IF EXISTS briefing_items`);\n        await connection.query(`DROP TABLE IF EXISTS notifications`);'
    )
    setup_content = setup_content.replace(
        'await connection.query(`CREATE TABLE connections',
        'await connection.query(`CREATE TABLE notifications (id VARCHAR(36) PRIMARY KEY, user_id VARCHAR(36), title VARCHAR(255), description TEXT, type VARCHAR(50), action_id VARCHAR(36), is_read BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);`);\n        await connection.query(`CREATE TABLE connections'
    )
    with open(setup_path, "w") as f: f.write(setup_content)

# --- 2. BACKEND: index.js ---
index_path = os.path.join(backend_dir, "index.js")
with open(index_path, "r") as f:
    index_content = f.read()
if "/api/notifications" not in index_content:
    patch = """
app.get('/api/notifications/:userId', authenticate, async (req, res) => {
    try { const [rows] = await pool.query('SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC', [req.params.userId]); res.json(rows); } 
    catch(e) { res.status(500).json({error: e.message}) }
});
app.post('/api/notifications/:id/read', authenticate, async (req, res) => {
    try { await pool.query('UPDATE notifications SET is_read = TRUE WHERE id = ?', [req.params.id]); res.json({success: true}); } 
    catch(e) { res.status(500).json({error: e.message}) }
});
"""
    with open(index_path, "a") as f: f.write(patch)

# --- 3. ANDROID: RetrofitClient.kt ---
retrofit_path = os.path.join(android_dir, "data/RetrofitClient.kt")
with open(retrofit_path, "r") as f:
    retrofit_content = f.read()
if "getNotifications" not in retrofit_content:
    retrofit_content = retrofit_content.replace(
        '    @GET("api/briefings")',
        '    @GET("api/notifications/{userId}")\n    suspend fun getNotifications(@Path("userId") userId: String): List<com.mitron.connect.data.model.AppNotification>\n\n    @POST("api/notifications/{id}/read")\n    suspend fun markNotificationRead(@Path("id") id: String): AuthResponse\n\n    @GET("api/briefings")'
    )
    with open(retrofit_path, "w") as f: f.write(retrofit_content)

# --- 4. ANDROID: AppNotification Model ---
model_path = os.path.join(android_dir, "data/model/AppNotification.kt")
with open(model_path, "w") as f:
    f.write("""package com.mitron.connect.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class NotificationType { CONNECTION_REQUEST, NEW_MESSAGE, NEW_EVENT }

@Serializable
data class AppNotification(
    val id: String,
    val user_id: String,
    val title: String,
    val description: String,
    val type: NotificationType,
    val action_id: String,
    val is_read: Boolean = false
)
""")

# --- 5. ANDROID: VercelRepository.kt ---
repo_path = os.path.join(android_dir, "data/VercelRepository.kt")
with open(repo_path, "r") as f:
    repo_content = f.read()
if "getNotifications" not in repo_content:
    repo_content = repo_content.replace(
        '    suspend fun getBriefingItems(): List<BriefingItem> = withContext(Dispatchers.IO) {',
        '    suspend fun getNotifications(): List<com.mitron.connect.data.model.AppNotification> = withContext(Dispatchers.IO) {\n        try {\n            val uid = getCurrentUserId() ?: return@withContext emptyList()\n            api.getNotifications(uid)\n        } catch(e:Exception){ emptyList() }\n    }\n\n    suspend fun markNotificationRead(id: String) = withContext(Dispatchers.IO) {\n        try { api.markNotificationRead(id) } catch(e:Exception){}\n    }\n\n    suspend fun getBriefingItems(): List<BriefingItem> = withContext(Dispatchers.IO) {'
    )
    with open(repo_path, "w") as f: f.write(repo_content)

# --- 6. ANDROID: HomeViewModel.kt ---
hvm_path = os.path.join(android_dir, "ui/screens/home/HomeViewModel.kt")
with open(hvm_path, "r") as f:
    hvm_content = f.read()
if "_notifications" not in hvm_content:
    hvm_content = hvm_content.replace(
        'val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()',
        'val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()\n\n    private val _notifications = MutableStateFlow<List<com.mitron.connect.data.model.AppNotification>>(emptyList())\n    val notifications: StateFlow<List<com.mitron.connect.data.model.AppNotification>> = _notifications.asStateFlow()'
    )
    hvm_content = hvm_content.replace(
        '_events.value = repository.getEvents()',
        '_events.value = repository.getEvents()\n                _notifications.value = repository.getNotifications()'
    )
    hvm_content = hvm_content.replace(
        '    fun connectWithContact(contactId: String) {',
        '    fun markNotificationRead(id: String) { viewModelScope.launch { repository.markNotificationRead(id); refreshData(true) } }\n\n    fun connectWithContact(contactId: String) {'
    )
    with open(hvm_path, "w") as f: f.write(hvm_content)

# --- 7. ANDROID: HomeScreen.kt ---
home_path = os.path.join(android_dir, "ui/screens/home/HomeScreen.kt")
with open(home_path, "r") as f:
    home_content = f.read()
home_content = home_content.replace('Text("3")', 'Text("${viewModel.notifications.collectAsState().value.count { !it.is_read }}")')
with open(home_path, "w") as f: f.write(home_content)

# --- 8. ANDROID: NotificationsScreen.kt ---
notif_path = os.path.join(android_dir, "ui/screens/notifications/NotificationsScreen.kt")
with open(notif_path, "r") as f:
    notif_content = f.read()

notif_content = notif_content.replace(
    'import androidx.compose.runtime.*\nimport androidx.compose.ui.Alignment',
    'import androidx.compose.runtime.*\nimport androidx.compose.ui.Alignment\nimport com.mitron.connect.data.model.*\nimport androidx.lifecycle.viewmodel.compose.viewModel\nimport com.mitron.connect.ui.screens.home.HomeViewModel'
)
notif_content = notif_content.replace(
    'data class AppNotification(\n    val id: String,\n    val title: String,\n    val description: String,\n    val type: NotificationType,\n    val actionId: String\n)\n\nenum class NotificationType {\n    CONNECTION_REQUEST, NEW_MESSAGE, NEW_EVENT\n}',
    ''
)
notif_content = notif_content.replace(
    'onOpenEvent: (String) -> Unit\n) {',
    'onOpenEvent: (String) -> Unit,\n    viewModel: HomeViewModel = viewModel()\n) {'
)
notif_content = notif_content.replace(
    '    // Mocking notifications for now. This would come from VercelRepository in production.\n    val notifications = listOf(\n        AppNotification("1", "New Connection Request", "Tony Stark wants to connect with you.", NotificationType.CONNECTION_REQUEST, "user_stark"),\n        AppNotification("2", "New Message", "Steve Rogers: \'Are we still on for tomorrow?\'", NotificationType.NEW_MESSAGE, "chat_123"),\n        AppNotification("3", "New Event Nearby", "Tech Meetup 2026 is happening near you.", NotificationType.NEW_EVENT, "event_456")\n    )',
    '    val notifications by viewModel.notifications.collectAsState()'
)
notif_content = notif_content.replace('notification.actionId', 'notification.action_id')
notif_content = notif_content.replace('/* Accept */', 'viewModel.markNotificationRead(notification.id)')
notif_content = notif_content.replace('onClick()', 'viewModel.markNotificationRead(notification.id); onClick()')
with open(notif_path, "w") as f: f.write(notif_content)

print("Mock data removed, full Live Notifications stack implemented!")
