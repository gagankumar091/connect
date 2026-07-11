package com.mitron.connect.services

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CallManager {
    private const val APP_ID = "d5d98935c72d46a68385409d52c31ad9"
    var rtcEngine: RtcEngine? = null
        private set

    private val _incomingCall = MutableStateFlow<IncomingCallData?>(null)
    val incomingCall: StateFlow<IncomingCallData?> = _incomingCall.asStateFlow()

    private val _isCallActive = MutableStateFlow(false)
    val isCallActive: StateFlow<Boolean> = _isCallActive.asStateFlow()

    private val _remoteUid = MutableStateFlow<Int?>(null)
    val remoteUid: StateFlow<Int?> = _remoteUid.asStateFlow()

    private val _isVideoEnabled = MutableStateFlow(false)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            _isCallActive.value = true
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            // Remote user joined
            _remoteUid.value = uid
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            if (_remoteUid.value == uid) {
                _remoteUid.value = null
            }
            endCall()
        }
    }

    fun initAgora(context: Context) {
        if (rtcEngine != null) return
        try {
            val config = RtcEngineConfig()
            config.mContext = context.applicationContext
            config.mAppId = APP_ID
            config.mEventHandler = mRtcEventHandler
            rtcEngine = RtcEngine.create(config)
            rtcEngine?.enableAudio()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setIncomingCall(data: IncomingCallData) {
        _incomingCall.value = data
    }

    fun clearIncomingCall() {
        _incomingCall.value = null
    }

    fun joinCall(token: String, channelName: String, isVideo: Boolean = false) {
        rtcEngine?.let { engine ->
            if (isVideo) {
                engine.enableVideo()
                _isVideoEnabled.value = true
            } else {
                engine.disableVideo()
                engine.enableAudio()
                _isVideoEnabled.value = false
            }
            engine.joinChannel(token, channelName, "Extra Optional Data", 0)
        }
    }

    fun toggleMute() {
        val newMuted = !_isMuted.value
        rtcEngine?.muteLocalAudioStream(newMuted)
        _isMuted.value = newMuted
    }

    fun toggleVideo() {
        val newVideo = !_isVideoEnabled.value
        if (newVideo) {
            rtcEngine?.enableVideo()
        } else {
            rtcEngine?.disableVideo()
        }
        _isVideoEnabled.value = newVideo
    }
    
    fun switchCamera() {
        rtcEngine?.switchCamera()
    }

    fun endCall() {
        rtcEngine?.leaveChannel()
        _isCallActive.value = false
        _incomingCall.value = null
        _isMuted.value = false
        _isVideoEnabled.value = false
        _remoteUid.value = null
    }

    fun destroy() {
        RtcEngine.destroy()
        rtcEngine = null
    }
}

data class IncomingCallData(
    val channelName: String,
    val callerId: String,
    val callerName: String,
    val callerAvatar: String?,
    val isVideo: Boolean
)
