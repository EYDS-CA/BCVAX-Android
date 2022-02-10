package ca.bc.gov.shcdecoder.repository.impl

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ca.bc.gov.shcdecoder.model.Cache
import ca.bc.gov.shcdecoder.model.Expiry
import ca.bc.gov.shcdecoder.repository.PreferenceRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [PreferenceRepository]
 *
 * @author Pinakin Kansara
 */
private val Context.dataStore by preferencesDataStore("shc_decoder")

class PreferenceRepositoryImpl(
    private val context: Context
) : PreferenceRepository {

    companion object {
        val CACHED_TIME_STAMP = longPreferencesKey("CACHED_TIME_STAMP")
        val CACHE_CUSTOM_EXPIRY_TIME = stringPreferencesKey("CACHED_CUSTOM_EXPIRY_TIME")
    }

    override val defaultTimeStamp: Flow<Long> = context.dataStore.data.map { preference ->
        preference[CACHED_TIME_STAMP] ?: 0
    }

    override suspend fun setDefaultTimeStamp(timeStamp: Long) = context.dataStore.edit { preference ->
        preference[CACHED_TIME_STAMP] = timeStamp
    }

    override val customExpiryTime: Flow<Expiry> = context.dataStore.data.map { preference ->
        val json = preference[CACHE_CUSTOM_EXPIRY_TIME]
        Gson().fromJson(json, Expiry::class.java)
    }

    override suspend fun setCustomExpiryTime(expiry: Expiry) = context.dataStore.edit { preference ->
        preference[CACHE_CUSTOM_EXPIRY_TIME] = Gson().toJson(expiry)
    }
}
