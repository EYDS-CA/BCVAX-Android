package ca.bc.gov.shcdecoder.cache.impl

import ca.bc.gov.shcdecoder.SHCConfig
import ca.bc.gov.shcdecoder.cache.CacheManager
import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.repository.PreferenceRepository
import ca.bc.gov.shcdecoder.revocations.getRevocationsUrl
import ca.bc.gov.shcdecoder.rule.RulesManager
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

internal class CacheManagerImpl(
    private val shcConfig: SHCConfig,
    private val preferenceRepository: PreferenceRepository,
    private val fileManager: FileManager,
    private val rulesManager: RulesManager
) : CacheManager {

    companion object {
        const val SUFFIX_JWKS_JSON = "/.well-known/jwks.json"
        const val SUFFIX_ISSUER_JSON = "issuers.json"
        private const val MILLIS_IN_MINUTE = 60000L
    }

    override suspend fun fetch() {
        val customExpiryTimes = rulesManager.getRule(shcConfig.issuerEndPoint)

        val rulesExpiryTime = customExpiryTimes?.cache?.expiry?.rules?.minutesToMillis()
        val issuersExpiryTime = customExpiryTimes?.cache?.expiry?.issuers?.minutesToMillis()
        val revocationsExpiryTime = customExpiryTimes?.cache?.expiry?.revocations?.minutesToMillis()

        fetchRules(rulesExpiryTime)
        fetchIssuers(issuersExpiryTime)
        fetchRevocations(revocationsExpiryTime)

        preferenceRepository.setGeneralTimeStamp(Calendar.getInstance().timeInMillis)
    }

    private suspend fun fetchRules(rulesExpiryTime: Long?) {
        if (
            isCacheExpired(
                preferenceRepository.rulesTimeStamp.firstOrNull(),
                rulesExpiryTime
            )
        ) {
            fileManager.downloadFile(shcConfig.rulesEndPoint)

            if (rulesExpiryTime != null) {
                preferenceRepository.setRulesTimeStamp(Calendar.getInstance().timeInMillis)
            }
        }
    }

    private suspend fun fetchIssuers(issuersExpiryTime: Long?) {
        if (
            isCacheExpired(
                preferenceRepository.issuersTimeStamp.firstOrNull(),
                issuersExpiryTime
            )
        ) {
            fileManager.downloadFile(shcConfig.issuerEndPoint)
            fetchKeys()

            if (issuersExpiryTime != null) {
                preferenceRepository.setIssuersTimeStamp(Calendar.getInstance().timeInMillis)
            }
        }
    }

    private suspend fun fetchKeys() {
        fileManager.getIssuers(shcConfig.issuerEndPoint).forEach {
            val keyUrl = getKeyUrl(it.iss)

            fileManager.downloadFile(keyUrl)
        }
    }

    private suspend fun fetchRevocations(revocationsExpiryTime: Long?) {
        if (
            isCacheExpired(
                preferenceRepository.revocationsTimeStamp.firstOrNull(),
                revocationsExpiryTime
            )
        ) {
            fileManager.getIssuers(shcConfig.issuerEndPoint).forEach { issuer ->
                val keyUrl = getKeyUrl(issuer.iss)

                fileManager.getKeys(keyUrl).forEach { key ->
                    val revocationURL = getRevocationsUrl(issuer.iss, key.kid)
                    fileManager.downloadFile(revocationURL)
                }
            }

            if (revocationsExpiryTime != null) {
                preferenceRepository.setRevocationsTimeStamp(Calendar.getInstance().timeInMillis)
            }
        }
    }

    private fun getKeyUrl(iss: String): String {
        return if (iss.endsWith(SUFFIX_JWKS_JSON)) {
            iss
        } else {
            "$iss$SUFFIX_JWKS_JSON"
        }
    }

    private suspend fun isCacheExpired(
        timeStamp: Long?,
        timeExpiry: Long?
    ): Boolean {
        val finalTimeStamp = timeStamp ?: preferenceRepository.generalTimeStamp.firstOrNull() ?: Long.MIN_VALUE
        val finalTimeExpiry = timeExpiry ?: shcConfig.cacheExpiryTimeInMilli

        val currentTime = Calendar.getInstance()
        val previousTime = Calendar.getInstance()

        previousTime.timeInMillis = finalTimeStamp + finalTimeExpiry
        return (currentTime >= previousTime)
    }

    private fun String?.minutesToMillis(): Long? {
        return this?.toLongOrNull()?.let {
            it * MILLIS_IN_MINUTE
        }
    }

}
