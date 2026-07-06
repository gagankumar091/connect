package com.mitron.connect.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mitron.connect.data.model.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface MitronApiService {
    @GET("api/users")
    suspend fun getUsers(@retrofit2.http.Query("lat") lat: Double? = null, @retrofit2.http.Query("lng") lng: Double? = null): List<Contact>

    @GET("api/companies")
    suspend fun getCompanies(): List<Company>

    @GET("api/events")
    suspend fun getEvents(@retrofit2.http.Query("lat") lat: Double? = null, @retrofit2.http.Query("lng") lng: Double? = null): List<Event>

    @retrofit2.http.POST("api/users/location")
    suspend fun updateLocation(@retrofit2.http.Body request: LocationRequest): AuthResponse

    @GET("api/chats/{userId}")
    suspend fun getChats(@Path("userId") userId: String): List<ChatPreview>

    @GET("api/timeline/{contactId}")
    suspend fun getTimelineEvents(@Path("contactId") contactId: String): List<TimelineEvent>

    @GET("api/notifications/{userId}")
    suspend fun getNotifications(@Path("userId") userId: String): List<com.mitron.connect.data.model.AppNotification>

    @POST("api/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): AuthResponse

    @GET("api/briefings")
    suspend fun getBriefingItems(): List<BriefingItem>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/connections")
    suspend fun sendConnectionRequest(@Body request: ConnectionRequestDto): ApiResponse

    @POST("api/connections/accept")
    suspend fun acceptConnectionRequest(@Body request: ConnectionRequestDto): ApiResponse

    @POST("api/connections/reject")
    suspend fun rejectConnectionRequest(@Body request: ConnectionRequestDto): ApiResponse

    @POST("api/messages/read")
    suspend fun markMessagesRead(@Body request: MarkMessagesReadRequest): ApiResponse

    @POST("api/users/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): ApiResponse

    @GET("api/recent-companies")
    suspend fun getRecentCompanies(@retrofit2.http.Query("userId") userId: String): List<Company>

    @GET("api/user-profile/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Contact

    @POST("api/events")
    suspend fun createEvent(@Body request: Event): ApiResponse

    @GET("api/chats/{chatId}/messages")
    suspend fun getChatMessages(@Path("chatId") chatId: String, @retrofit2.http.Query("userId") userId: String): List<ChatMessage>

    @POST("api/chats/{chatId}/messages")
    suspend fun sendMessage(@Path("chatId") chatId: String, @Body request: SendMessageRequest): ApiResponse

    @retrofit2.http.PUT("api/users/{id}")
    suspend fun updateUserProfile(@Path("id") id: String, @Body request: UpdateProfileRequest): ApiResponse
}


@kotlinx.serialization.Serializable
data class LoginRequest(val usernameOrEmail: String, val password: String)
@kotlinx.serialization.Serializable
data class RegisterRequest(val username: String, val password: String, val name: String, val email: String)
@kotlinx.serialization.Serializable
data class AuthResponse(val success: Boolean, val message: String? = null, val userId: String? = null)

@kotlinx.serialization.Serializable
data class LocationRequest(val userId: String, val lat: Double, val lng: Double)
@kotlinx.serialization.Serializable
data class ConnectionRequestDto(val senderId: String, val receiverId: String)

@kotlinx.serialization.Serializable
data class SendMessageRequest(val senderId: String, val text: String)
@kotlinx.serialization.Serializable
data class UpdateProfileRequest(val username: String, val email: String, val name: String, val title: String, val company: String, val linkedin: String, val website: String)

@kotlinx.serialization.Serializable
data class MarkMessagesReadRequest(val chatId: String, val userId: String)

@kotlinx.serialization.Serializable
data class FcmTokenRequest(val userId: String, val token: String)

@kotlinx.serialization.Serializable
data class ApiResponse(val success: Boolean, val message: String? = null)

object RetrofitClient {
    private const val BASE_URL = "https://connect-mitron.vercel.app/"
    
    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer MITRON_SECURE_KEY_2026")
                .build()
            chain.proceed(request)
        }
        .build()

    val apiService: MitronApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(MitronApiService::class.java)
    }
}
