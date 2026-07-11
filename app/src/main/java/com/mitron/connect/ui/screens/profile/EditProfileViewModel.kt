package com.mitron.connect.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.VercelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val repository: VercelRepository = VercelRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _profile = MutableStateFlow<com.mitron.connect.data.model.Contact?>(null)
    val profile: StateFlow<com.mitron.connect.data.model.Contact?> = _profile.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                val p = repository.getUserProfile()
                _profile.value = p
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateProfile(username: String, email: String, name: String, title: String, company: String, linkedin: String, website: String, phone: String?, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var finalAvatarUrl = _profile.value?.avatarUrl
                if (imageUri != null) {
                    finalAvatarUrl = repository.uploadProfilePicture(imageUri)
                }
                val success = repository.updateUserProfile(username, email, name, title, company, linkedin, website, phone, finalAvatarUrl)
                _isSuccess.value = success
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
