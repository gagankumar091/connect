package com.mitron.connect.data

import com.mitron.connect.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VercelRepository : com.mitron.connect.core.data.repository.ContactRepository {
    private val api = RetrofitClient.apiService


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

    suspend fun register(username: String, pass: String, name: String, email: String, phone: String? = null): String? = withContext(Dispatchers.IO) {
        try {
            val res = api.register(com.mitron.connect.data.RegisterRequest(username, pass, name, email, phone))
            if (res.success && res.userId != null) {
                SessionManager.saveUserId(res.userId)
                res.userId
            } else null
        } catch(e:Exception){e.printStackTrace(); null}
    }


    override suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        try {
            val allUsers = api.getUsers(null, null)
            val currentUserId = getCurrentUserId()
            allUsers.filter { it.id != currentUserId }.map { contact ->
                // PHASE 2: Relationship Health Algorithm (Mocked for API)
                // Recency of email + Meeting frequency - Follow-up gaps
                val meetingBonus = if (contact.mutualsCount > 0) contact.mutualsCount * 10 else 0
                val followUpGap = contact.daysSinceContact
                val calculatedScore = (80 + meetingBonus - (followUpGap * 2)).coerceIn(0, 100)
                
                contact.copy(
                    initials = contact.name.take(2).uppercase(),
                    score = calculatedScore
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getContactById(id: String): Contact? = withContext(Dispatchers.IO) {
        try {
            // Use direct user profile endpoint — faster than fetching all users
            api.getUserProfile(id).let { contact ->
                // PHASE 2: Relationship Health Algorithm
                val meetingBonus = if (contact.mutualsCount > 0) contact.mutualsCount * 10 else 0
                val followUpGap = contact.daysSinceContact
                val calculatedScore = (80 + meetingBonus - (followUpGap * 2)).coerceIn(0, 100)

                contact.copy(
                    initials = contact.name.take(2).uppercase(),
                    score = calculatedScore
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun updateContactRelationshipScore(id: String, newScore: Int) {
        // Implementation for API call to update score will go here
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

    suspend fun addTimelineEvent(event: TimelineEvent): Boolean = withContext(Dispatchers.IO) {
        try {
            val res = api.addTimelineEvent(event)
            res.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addMeetingSummary(summary: MeetingSummary): Boolean = withContext(Dispatchers.IO) {
        try {
            val res = api.addMeetingSummary(summary)
            res.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
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
            val currentUserId = getCurrentUserId()
            // Use the efficient by-company endpoint instead of fetching all users
            api.getUsersByCompany(company.name)
                .filter { it.id != currentUserId }
                .map { it.copy(initials = it.name.take(2).uppercase()) }
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
        try { 
            api.getUserProfile(currentUserId) 
        } catch(e: Exception) { 
            e.printStackTrace()
            // Fallback to getContactById if getUserProfile fails
            getContactById(currentUserId)
        }
    }

    suspend fun markMessagesRead(chatId: String) = withContext(Dispatchers.IO) {
        val currentUserId = getCurrentUserId() ?: return@withContext
        try { api.markMessagesRead(MarkMessagesReadRequest(chatId, currentUserId)) } catch(e: Exception) { e.printStackTrace() }
    }

    suspend fun uploadProfilePicture(uri: android.net.Uri): String? = withContext(Dispatchers.IO) {
        try {
            val context = com.mitron.connect.data.SessionManager.appContext
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return@withContext null

            // Resize to max 300x300 to save bandwidth
            val maxSize = 300
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = java.lang.Math.min(maxSize.toFloat() / width, maxSize.toFloat() / height)
            
            val scaledBitmap = if (scale < 1) {
                val matrix = android.graphics.Matrix()
                matrix.postScale(scale, scale)
                android.graphics.Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, true)
            } else {
                originalBitmap
            }

            // Compress to JPEG Base64
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64Str = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
            
            "data:image/jpeg;base64,$base64Str"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateUserProfile(username: String, email: String, name: String, title: String, company: String, linkedin: String, website: String, phone: String?, avatarUrl: String?): Boolean = withContext(Dispatchers.IO) {
        val currentUserId = getCurrentUserId() ?: return@withContext false
        try {
            val res = api.updateUserProfile(currentUserId, UpdateProfileRequest(username, email, name, title, company, linkedin, website, avatarUrl, phone))
            res.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ---- New methods ----

    suspend fun getEventById(eventId: String): Event? = withContext(Dispatchers.IO) {
        try { api.getEventById(eventId) } catch (e: Exception) { e.printStackTrace(); null }
    }

    suspend fun attendEvent(eventId: String): Boolean = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext false
        try { api.attendEvent(eventId, AttendEventRequest(uid)).success } catch (e: Exception) { false }
    }

    suspend fun getCompanyByIdDirect(companyId: String): Company? = withContext(Dispatchers.IO) {
        try { api.getCompanyById(companyId) } catch (e: Exception) {
            // Fall back to list search
            getCompanyById(companyId)
        }
    }

    suspend fun createCompany(name: String, industry: String?, headquarters: String?, website: String?, avatarUrl: String?, funding: String?, employeeRange: String?): String? = withContext(Dispatchers.IO) {
        try {
            val res = api.createCompany(CreateCompanyRequest(
                name = name, industry = industry, headquarters = headquarters,
                website = website, avatarUrl = avatarUrl, funding = funding, employeeRange = employeeRange
            ))
            if (res.success) res.message else null
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    suspend fun getReminders(): List<com.mitron.connect.data.model.AppNotification> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext emptyList()
        try { api.getReminders(uid) } catch (e: Exception) { emptyList() }
    }

    suspend fun createReminder(title: String, contactId: String?, dueDateIso: String?): Boolean = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext false
        try {
            api.createReminder(CreateReminderRequest(
                userId = uid, title = title, actionId = contactId, dueDate = dueDateIso
            )).success
        } catch (e: Exception) { false }
    }

    suspend fun getRelationshipScore(contactId: String): RelationshipScoreResponse? = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext null
        try { api.getRelationshipScore(contactId, uid) } catch (e: Exception) { null }
    }

    suspend fun initiateCall(receiverId: String, isVideo: Boolean): CallInitiatedResponse? = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext null
        try { 
            api.initiateCall(InitiateCallRequest(callerId = uid, receiverId = receiverId, isVideo = isVideo)) 
        } catch (e: Exception) { 
            e.printStackTrace()
            null
        }
    }
}
