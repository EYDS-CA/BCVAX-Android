package ca.bc.gov.shcdecoder.cache.impl

import android.content.Context
import android.util.Log
import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.model.Issuer
import ca.bc.gov.shcdecoder.model.Jwks
import ca.bc.gov.shcdecoder.model.JwksKey
import ca.bc.gov.shcdecoder.model.RevocationsResponse
import ca.bc.gov.shcdecoder.model.Rule
import ca.bc.gov.shcdecoder.model.TrustedIssuersResponse
import ca.bc.gov.shcdecoder.model.ValidationRuleResponse
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class FileManagerImpl(
    context: Context
) : FileManager {

    private val downloadDir: File = File(context.filesDir, "Decoder")

    companion object {
        private const val TAG = "FileManagerImpl"
    }

    init {
        if (!downloadDir.exists()) {
            downloadDir.mkdir()
        }
    }

    override suspend fun downloadFile(url: String) {
        try {
            val fileName = getFileNameFromUrl(url)

            val tempFileName = "temp_$fileName"

            val destFile = File(downloadDir, tempFileName)

            if (destFile.exists()) {
                destFile.delete()
            }

            URL(url).openStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val actualFile = File(downloadDir, fileName)

            destFile.copyTo(actualFile, overwrite = true)

            destFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }

    override suspend fun getIssuers(url: String): List<Issuer> {
        return if (url.endsWith(CacheManagerImpl.SUFFIX_ISSUER_JSON)) {
            val issuerResponse = getDataFromFile(url, TrustedIssuersResponse::class.java)
            issuerResponse?.trustedIssuers.orEmpty()
        } else {
            listOf(
                Issuer(url, url)
            )
        }
    }

    override suspend fun getKeys(url: String): List<JwksKey> {
        val keyResponse = getDataFromFile(url, Jwks::class.java)
        return keyResponse?.keys.orEmpty()
    }

    override suspend fun getRule(url: String): List<Rule> {
        val validationRulesResponse = getDataFromFile(url, ValidationRuleResponse::class.java)
        return validationRulesResponse?.ruleSet.orEmpty()
    }

    override suspend fun getRevocations(url: String): RevocationsResponse? {
        return getDataFromFile(url, RevocationsResponse::class.java)
    }

    override suspend fun exists(url: String) =
        File(downloadDir, getFileNameFromUrl(url)).exists()

    private fun <T> getDataFromFile(url: String, classType: Class<T>): T? {
        return try {
            val fileName = getFileNameFromUrl(url)
            val file = File(downloadDir, fileName)
            val bufferedReader = file.bufferedReader()
            val json = bufferedReader.use { it.readText() }
            Gson().fromJson(json, classType)
        } catch (exception: Exception) {
            exception.printStackTrace()
            return null
        }
    }

    private fun getFileNameFromUrl(url: String) = url.removePrefix("https://")
        .replace("/", "~")
}
