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

    fun updateProfile(username: String, email: String, name: String, title: String, company: String, linkedin: String, website: String, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var avatarUrl: String? = null
                if (imageUri != null) {
                    avatarUrl = repository.uploadProfilePicture(imageUri)
                }
                val success = repository.updateUserProfile(username, email, name, title, company, linkedin, website, avatarUrl)
                _isSuccess.value = success
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
