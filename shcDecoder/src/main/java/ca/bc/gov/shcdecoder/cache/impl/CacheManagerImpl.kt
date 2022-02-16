package ca.bc.gov.shcdecoder.cache.impl

import ca.bc.gov.shcdecoder.SHCConfig
import ca.bc.gov.shcdecoder.cache.CacheManager
import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.repository.PreferenceRepository
import ca.bc.gov.shcdecoder.revocations.getRevocationsUrl
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

internal class CacheManagerImpl(
    private val shcConfig: SHCConfig,
    private val preferenceRepository: PreferenceRepository,
    private val fileManager: FileManager
) : CacheManager {

    companion object {
        const val SUFFIX_JWKS_JSON = "/.well-known/jwks.json"
        const val SUFFIX_ISSUER_JSON = "issuers.json"
        private const val MILLIS_IN_MINUTE = 60000L
    }

    /**
     * This method downloads rules, issuers and revocations files
     * download is controlled by time stamps
     *
     * <p>
     *
     * If any of the expiry times is null
     * that value will be replaced by shcConfig.cacheExpiryTimeInMilli
     * So, when rule set doesn't provides any of these expiry times
     * It will always use shcConfig.cacheExpiryTimeInMilli by default for everything
     *
     * Also if any time stamp is null
     * isCacheExpired method will take the Long Min Value for timestamp milliseconds
     * this will make isCacheExpired return true by default
     *
     * @see isCacheExpired
     *
     */
    override suspend fun fetch() {
        val rules = fileManager.getRule(shcConfig.rulesEndPoint).firstOrNull()

        val rulesExpiryTime = rules?.cache?.expiry?.rules?.minutesToMillis()
        val issuersExpiryTime = rules?.cache?.expiry?.issuers?.minutesToMillis()
        val revocationsExpiryTime = rules?.cache?.expiry?.revocations?.minutesToMillis()

        fetchRules(rulesExpiryTime)
        fetchIssuers(issuersExpiryTime)
        fetchRevocations(revocationsExpiryTime)
    }

    private suspend fun fetchRules(rulesExpiryTime: Long?) {
        if (
            isCacheExpired(
                timeStamp = preferenceRepository.rulesTimeStamp.firstOrNull(),
                timeExpiry = rulesExpiryTime
            )
        ) {
            fileManager.downloadFile(shcConfig.rulesEndPoint)
            preferenceRepository.setRulesTimeStamp(Calendar.getInstance().timeInMillis)
        }
    }

    private suspend fun fetchIssuers(issuersExpiryTime: Long?) {
        if (
            isCacheExpired(
                timeStamp = preferenceRepository.issuersTimeStamp.firstOrNull(),
                timeExpiry = issuersExpiryTime
            )
        ) {
            fileManager.downloadFile(shcConfig.issuerEndPoint)
            fetchKeys()
            preferenceRepository.setIssuersTimeStamp(Calendar.getInstance().timeInMillis)
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
                timeStamp = preferenceRepository.revocationsTimeStamp.firstOrNull(),
                timeExpiry = revocationsExpiryTime
            )
        ) {
            fileManager.getIssuers(shcConfig.issuerEndPoint).forEach { issuer ->
                val keyUrl = getKeyUrl(issuer.iss)

                fileManager.getKeys(keyUrl).forEach { key ->
                    val revocationURL = getRevocationsUrl(issuer.iss, key.kid)
                    fileManager.downloadFile(revocationURL)
                }
            }
            preferenceRepository.setRevocationsTimeStamp(Calendar.getInstance().timeInMillis)
        }
    }

    private fun getKeyUrl(iss: String): String {
        return if (iss.endsWith(SUFFIX_JWKS_JSON)) {
            iss
        } else {
            "$iss$SUFFIX_JWKS_JSON"
        }
    }

    private fun isCacheExpired(
        timeStamp: Long?,
        timeExpiry: Long?
    ): Boolean {
        val finalTimeStamp = timeStamp ?: Long.MIN_VALUE
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
