package com.example.indra.data

import com.example.indra.db.DatabaseProvider
import com.example.indra.auth.AuthApi

interface PropertyRepository {
    suspend fun getUserProperties(): List<Property>
    suspend fun addProperty(property: Property)
    suspend fun updateProperty(property: Property)
    suspend fun deleteProperty(propertyId: String)
    suspend fun getProperty(propertyId: String): Property?
}

class FirebasePropertyRepository : PropertyRepository {
    private val database = DatabaseProvider.database()

    override suspend fun getUserProperties(): List<Property> {
        return try {
            val currentUser = AuthApi.currentUser()
            val uid = currentUser?.uid ?: return emptyList()
            database.getUserProperties(uid)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addProperty(property: Property) {
        val currentUser = AuthApi.currentUser()
        val uid = currentUser?.uid ?: return
        database.addProperty(uid, property)
    }

    override suspend fun updateProperty(property: Property) {
        val currentUser = AuthApi.currentUser()
        val uid = currentUser?.uid ?: return
        database.updateProperty(uid, property)
    }

    override suspend fun deleteProperty(propertyId: String) {
        val currentUser = AuthApi.currentUser()
        val uid = currentUser?.uid ?: return
        database.deleteProperty(uid, propertyId)
    }

    override suspend fun getProperty(propertyId: String): Property? {
        val currentUser = AuthApi.currentUser()
        val uid = currentUser?.uid ?: return null
        // Note: DatabaseApi doesn't have getProperty, so we'll return null for now
        return null
    }
}

object PropertyRepositoryProvider {
    private var _repository: PropertyRepository? = null
    
    fun repository(): PropertyRepository {
        if (_repository == null) {
            _repository = FirebasePropertyRepository()
        }
        return _repository!!
    }
}
