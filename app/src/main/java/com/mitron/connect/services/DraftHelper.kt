package com.mitron.connect.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mitron.connect.data.model.TimelineEvent

object DraftHelper {
    fun draftFollowUpEmail(context: Context, email: String, name: String, recentEvents: List<TimelineEvent>) {
        val lastMeeting = recentEvents.firstOrNull { it.isMeeting }
        val subject = "Follow up from our recent meeting"
        
        val contextSummary = lastMeeting?.subtitle ?: "our last discussion"
        
        val body = """
            Hi $name,
            
            Great speaking with you recently! I wanted to quickly follow up regarding $contextSummary.
            
            Let me know if you have any further questions or if we should schedule a quick sync.
            
            Best,
        """.trimIndent()
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Send Follow-up"))
    }
}
