package ca.bc.gov.shcdecoder.revocations.impl

import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.cache.impl.CacheManagerImpl
import ca.bc.gov.shcdecoder.revocations.RevocationManager
import java.util.Date

class RevocationManagerImpl(
    private val fileManager: FileManager
) : RevocationManager {

    override suspend fun getRevocations(iss: String, kid: String): List<Pair<String, Date?>> {
        val url = getRevocationsUrl(iss, kid)
        return fileManager.getRevocations(url)?.getRidsInPairs().orEmpty()
    }

    companion object RevocationManager {
        private const val REVOCATION_JSON_PATH = "/.well-known/crl/"

        fun getRevocationsUrl(iss: String, kid: String): String {
            return iss.removeSuffix(CacheManagerImpl.SUFFIX_ISSUER_JSON).let { formattedIss ->
                "$formattedIss${REVOCATION_JSON_PATH}$kid.json"
            }
        }
    }
}
