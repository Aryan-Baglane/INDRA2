package com.example.indra.data

import com.example.indra.auth.AuthApi
import com.example.indra.db.DatabaseProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReportRepository {
    private val database = DatabaseProvider.database()

    suspend fun getUserReports(): List<Report> {
        val uid = AuthApi.currentUser()?.uid ?: return emptyList()
        return database.getUserReports(uid)
    }

    suspend fun addReport(report: Report): Boolean {
        val uid = AuthApi.currentUser()?.uid ?: return false
        return try {
            database.addReport(uid, report)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateReport(report: Report): Boolean {
        val uid = AuthApi.currentUser()?.uid ?: return false
        return try {
            database.updateReport(uid, report)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteReport(reportId: String): Boolean {
        val uid = AuthApi.currentUser()?.uid ?: return false
        return try {
            database.deleteReport(uid, reportId)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getReport(reportId: String): Report? {
        val uid = AuthApi.currentUser()?.uid ?: return null
        return database.getReport(uid, reportId)
    }

    fun getUserReportsFlow(): Flow<List<Report>> = flow {
        emit(getUserReports())
    }
}

object ReportRepositoryProvider {
    fun repository(): ReportRepository = ReportRepository()
}
