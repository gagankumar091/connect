import os

repo_path = "/home/randomx/Videos/mitron/app/src/main/java/com/mitron/connect/data/FirebaseRepository.kt"
with open(repo_path, "r") as f:
    content = f.read()

# Fix 1: getCompanies ID mapping
old_companies = """    suspend fun getCompanies(): List<Company> = withContext(Dispatchers.IO) {
        try {
            db.collection("companies").get().await().toObjects(Company::class.java)
        } catch (e: Exception) {"""
new_companies = """    suspend fun getCompanies(): List<Company> = withContext(Dispatchers.IO) {
        try {
            db.collection("companies").get().await().documents.mapNotNull { doc ->
                doc.toObject(Company::class.java)?.apply { id = doc.id }
            }
        } catch (e: Exception) {"""
content = content.replace(old_companies, new_companies)

# Fix 2: getChats crash protection
old_chats = """                val contactId = doc.getString("contact_id") ?: ""
                val lastMessage = doc.getString("last_message") ?: ""
                val unreadCount = doc.getLong("unread_count") ?: 0
                val contactNameFallback = doc.getString("contact_name") ?: "Unknown"
                
                // We'll fetch contacts in parallel for better performance if there are many chats
                // But for now, simple sequential or a better way
                val contactDoc = db.collection("contacts").document(contactId).get().await()"""
new_chats = """                val contactId = doc.getString("contact_id") ?: ""
                if (contactId.isBlank()) return@map null
                
                val lastMessage = doc.getString("last_message") ?: ""
                val unreadCount = doc.getLong("unread_count") ?: 0
                val contactNameFallback = doc.getString("contact_name") ?: "Unknown"
                
                val contactDoc = db.collection("contacts").document(contactId).get().await()"""
content = content.replace(old_chats, new_chats)

# Also fix the map returning null in getChats
old_map = """            chatsSnapshot.documents.map { doc ->"""
new_map = """            chatsSnapshot.documents.mapNotNull { doc ->"""
content = content.replace(old_map, new_map)


with open(repo_path, "w") as f:
    f.write(content)

# Fix 3: Refresh Data on Tab Change
home_path = "/home/randomx/Videos/mitron/app/src/main/java/com/mitron/connect/ui/screens/home/HomeScreen.kt"
with open(home_path, "r") as f:
    home_content = f.read()

old_launched = """    androidx.compose.runtime.LaunchedEffect(Unit) {
        locationPermissionLauncher.launch("""
new_launched = """    androidx.compose.runtime.LaunchedEffect(selectedTab) {
        viewModel.refreshData()
    }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        locationPermissionLauncher.launch("""

if "LaunchedEffect(selectedTab)" not in home_content:
    home_content = home_content.replace(old_launched, new_launched)
    with open(home_path, "w") as f:
        f.write(home_content)

print("Applied Data Sync Fixes!")
