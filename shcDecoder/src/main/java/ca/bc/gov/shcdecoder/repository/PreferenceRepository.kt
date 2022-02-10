package ca.bc.gov.shcdecoder.repository

import androidx.datastore.preferences.core.Preferences
import ca.bc.gov.shcdecoder.model.Cache
import ca.bc.gov.shcdecoder.model.Expiry
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    val defaultTimeStamp: Flow<Long>
    suspend fun setDefaultTimeStamp(timeStamp: Long): Preferences

    val customExpiryTime: Flow<Expiry>
    suspend fun setCustomExpiryTime(expiry: Expiry): Preferences
}
