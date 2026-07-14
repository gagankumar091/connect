package com.mitron.connect.core.data.repository

import com.mitron.connect.data.model.Contact
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the relationship intelligence data operations.
 * This abstracts away the implementation details (Vercel, Room) from the UI layer.
 */
interface ContactRepository {
    suspend fun getContacts(): List<Contact>
    suspend fun getContactById(id: String): Contact?
    suspend fun updateContactRelationshipScore(id: String, newScore: Int)
}
