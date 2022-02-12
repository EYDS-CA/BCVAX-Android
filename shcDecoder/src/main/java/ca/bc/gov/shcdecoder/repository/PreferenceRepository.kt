package ca.bc.gov.shcdecoder.repository

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    val generalTimeStamp: Flow<Long>
    suspend fun setGeneralTimeStamp(timeStamp: Long): Preferences

    val rulesTimeStamp: Flow<Long>
    suspend fun setRulesTimeStamp(timeStamp: Long): Preferences

    val issuersTimeStamp: Flow<Long>
    suspend fun setIssuersTimeStamp(timeStamp: Long): Preferences

    val revocationsTimeStamp: Flow<Long>
    suspend fun setRevocationsTimeStamp(timeStamp: Long): Preferences
}
