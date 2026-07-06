package com.mitron.connect.data

import com.mitron.connect.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VercelRepository {
    private val api = RetrofitClient.apiService
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    suspend fun getCurrentUserId(): String? = withContext(Dispatchers.IO) {
        SessionManager.getUserId()
    }

    suspend fun login(usernameOrEmail: String, pass: String): String? = withContext(Dispatchers.IO) {
        try {
            val res = api.login(com.mitron.connect.data.LoginRequest(usernameOrEmail, pass))
            if (res.success && res.userId != null) {
                SessionManager.saveUserId(res.userId)
                res.userId
            } else null
        } catch(e:Exception){e.printStackTrace(); null}
    }

    suspend fun register(username: String, pass: String, name: String, email: String): String? = withContext(Dispatchers.IO) {
        try {
            val res = api.register(com.mitron.connect.data.RegisterRequest(username, pass, name, email))
            if (res.success && res.userId != null) {
                SessionManager.saveUserId(res.userId)
                res.userId
            } else null
        } catch(e:Exception){e.printStackTrace(); null}
    }


    suspend fun getContacts(lat: Double? = null, lng: Double? = null): List<Contact> = withContext(Dispatchers.IO) {
        try {
            val allUsers = api.getUsers(lat, lng)
            val currentUserId = getCurrentUserId()
            allUsers.filter { it.id != currentUserId }.map {
                it.copy(initials = it.name.take(2).uppercase())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getContactById(id: String): Contact? = withContext(Dispatchers.IO) {
        try {
            api.getUsers(null, null).find { it.id == id }?.let { contact ->
                contact.copy(initials = contact.name.take(2).uppercase())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getCompanies(): List<Company> = withContext(Dispatchers.IO) {
        try {
            api.getCompanies()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCompanyById(id: String): Company? = withContext(Dispatchers.IO) {
        getCompanies().find { it.id == id }
    }

    suspend fun getChats(): List<ChatPreview> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = getCurrentUserId() ?: return@withContext emptyList()
            api.getChats(currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getTimelineEvents(contactId: String): List<TimelineEvent> = withContext(Dispatchers.IO) {
        try {
            api.getTimelineEvents(contactId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getMeetingSummary(contactId: String): MeetingSummary? = withContext(Dispatchers.IO) {
        null // We can add this later, not critical yet
    }

    suspend fun getNotifications(): List<com.mitron.connect.data.model.AppNotification> = withContext(Dispatchers.IO) {
        try {
            val uid = getCurrentUserId() ?: return@withContext emptyList()
            api.getNotifications(uid)
        } catch(e:Exception){ emptyList() }
    }

    suspend fun markNotificationRead(id: String) = withContext(Dispatchers.IO) {
        try { api.markNotificationRead(id) } catch(e:Exception){}
    }

    suspend fun getBriefingItems(): List<BriefingItem> = withContext(Dispatchers.IO) {
        try {
            api.getBriefingItems()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getChatMessages(chatId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        val currentUserId = getCurrentUserId() ?: return@withContext emptyList()
        try {
            api.getChatMessages(chatId, currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getContactsByCompany(companyId: String): List<Contact> = withContext(Dispatchers.IO) {
        try {
            val company = getCompanyById(companyId) ?: return@withContext emptyList()
            val allUsers = api.getUsers(null, null)
            val currentUserId = getCurrentUserId()
            allUsers.filter { it.company == company.name && it.id != currentUserId }.map {
                it.copy(initials = it.name.take(2).uppercase())
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendConnectionRequest(targetContactId: String): Boolean = withContext(Dispatchers.IO) {
        val currentUserId = getCurrentUserId() ?: return@withContext false
        try {
            val response = api.sendConnectionRequest(ConnectionRequestDto(currentUserId, targetContactId))
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun acceptConnectionRequest(senderId: String): Boolean = withContext(Dispatchers.IO) {
        val currentUserId = getCurrentUserId() ?: return@withContext false
        try {
            val response = api.acceptConnectionRequest(ConnectionRequestDto(senderId, currentUserId))
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun rejectConnectionRequest(senderId: String): Boolean = withContext(Dispatchers.IO) {
        val currentUserId = getCurrentUserId() ?: return@withContext false
        try {
            val response = api.rejectConnectionRequest(ConnectionRequestDto(senderId, currentUserId))
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun sendMessage(chatId: String, text: String): Boolean = withContext(Dispatchers.IO) {
        val currentUserId = getCurrentUserId() ?: return@withContext false
        try {
            val response = api.sendMessage(chatId, SendMessageRequest(currentUserId, text))
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getEvents(lat: Double? = null, lng: Double? = null): List<Event> = withContext(Dispatchers.IO) {
        try {
            api.getEvents(lat, lng)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateLocation(lat: Double, lng: Double) = withContext(Dispatchers.IO) {
        try { val uid = getCurrentUserId() ?: return@withContext; api.updateLocation(com.mitron.connect.data.LocationRequest(uid, lat, lng)) } catch(e:Exception){}
    }

    suspend fun updateFcmToken(token: String) = withContext(Dispatchers.IO) {
        val currentUserId = getCurrentUserId() ?: return@withContext
        try { api.updateFcmToken(FcmTokenRequest(currentUserId, token)) } catch(e: Exception) { e.printStackTrace() }
    }

    suspend fun getRecentCompanies(): List<Company> = withContext(Dispatchers.IO) {
        val currentUserId = getCurrentUserId() ?: return@withContext emptyList()
        try { api.getRecentCompanies(currentUserId) } catch(e: Exception) { e.printStackTrace(); emptyList() }
    }

    suspend fun getUserProfile(): Contact? = withContext(Dispatchers.IO) {
        val currentUserId = getCurrentUserId() ?: return@withContext null
        try { api.getUserProfile(currentUserId) } catch(e: Exception) { e.printStackTrace(); null }
    }

    suspend fun markMessagesRead(chatId: String) = withContext(Dispatchers.IO) {
        val currentUserId = getCurrentUserId() ?: return@withContext
        try { api.markMessagesRead(MarkMessagesReadRequest(chatId, currentUserId)) } catch(e: Exception) { e.printStackTrace() }
    }

    suspend fun uploadProfilePicture(uri: android.net.Uri): String? = withContext(Dispatchers.IO) {
        null
    }

    suspend fun updateUserProfile(username: String, email: String, name: String, title: String, company: String, linkedin: String, website: String, avatarUrl: String?): Boolean = withContext(Dispatchers.IO) {
        val currentUserId = getCurrentUserId() ?: return@withContext false
        try {
            val res = api.updateUserProfile(currentUserId, UpdateProfileRequest(username, email, name, title, company, linkedin, website))
            res.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
