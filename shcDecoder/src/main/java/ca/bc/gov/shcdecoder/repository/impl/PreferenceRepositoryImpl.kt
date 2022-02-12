package ca.bc.gov.shcdecoder.repository.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ca.bc.gov.shcdecoder.repository.PreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.UnsupportedOperationException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * [PreferenceRepository]
 *
 * @author Pinakin Kansara
 */

private val Context.dataStore by preferencesDataStore("shc_decoder")

private fun <T> Context.getFromStore(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
    return dataStore.data.map { preference ->
        preference[key] ?: defaultValue
    }
}

private suspend fun <T> Context.saveToStore(key: Preferences.Key<T>, value: T): Preferences {
    return dataStore.edit { preference ->
        preference[key] = value
    }
}

class PreferenceRepositoryImpl(
    private val context: Context
) : PreferenceRepository {

    private companion object PreferenceKeys {
        private val CACHED_TIME_STAMP = longPreferencesKey("CACHED_TIME_STAMP")
        private val CACHED_RULES_TIME_STAMP = longPreferencesKey("CACHED_RULES_TIME_STAMP")
        private val CACHED_ISSUERS_TIME_STAMP = longPreferencesKey("CACHED_ISSUERS_TIME_STAMP")
        private val CACHED_REVOCATIONS_TIME_STAMP = longPreferencesKey("CACHED_REVOCATIONS_TIME_STAMP")
    }

    override val generalTimeStamp: Flow<Long> = context.getFromStore(CACHED_TIME_STAMP, 0)

    override val rulesTimeStamp: Flow<Long> = context.getFromStore(CACHED_RULES_TIME_STAMP, 0)

    override val issuersTimeStamp: Flow<Long> = context.getFromStore(CACHED_ISSUERS_TIME_STAMP, 0)

    override val revocationsTimeStamp: Flow<Long> = context.getFromStore(CACHED_REVOCATIONS_TIME_STAMP, 0)

    override suspend fun setGeneralTimeStamp(timeStamp: Long) = context.saveToStore(CACHED_TIME_STAMP, timeStamp)

    override suspend fun setRulesTimeStamp(timeStamp: Long) = context.saveToStore(CACHED_RULES_TIME_STAMP, timeStamp)

    override suspend fun setIssuersTimeStamp(timeStamp: Long) = context.saveToStore(CACHED_ISSUERS_TIME_STAMP, timeStamp)

    override suspend fun setRevocationsTimeStamp(timeStamp: Long) = context.saveToStore(CACHED_REVOCATIONS_TIME_STAMP, timeStamp)

}