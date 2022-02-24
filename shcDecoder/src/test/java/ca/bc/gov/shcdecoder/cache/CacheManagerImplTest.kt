package ca.bc.gov.shcdecoder.cache

import android.util.Log
import ca.bc.gov.shcdecoder.TEST_ISS
import ca.bc.gov.shcdecoder.TEST_ISS_WITH_SUFFIX
import ca.bc.gov.shcdecoder.cache.impl.CacheManagerImpl
import ca.bc.gov.shcdecoder.config
import ca.bc.gov.shcdecoder.defaultKey
import ca.bc.gov.shcdecoder.defaultRule
import ca.bc.gov.shcdecoder.model.Cache
import ca.bc.gov.shcdecoder.model.Expiry
import ca.bc.gov.shcdecoder.model.Issuer
import ca.bc.gov.shcdecoder.repository.PreferenceRepository
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.internal.verification.Times
import org.mockito.junit.MockitoJUnitRunner
import java.util.Calendar

@RunWith(MockitoJUnitRunner::class)
class CacheManagerImplTest {

    private lateinit var sut: CacheManager

    @Mock
    private lateinit var preferenceRepository: PreferenceRepository

    @Mock
    private lateinit var fileManager: FileManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockkStatic(Log::class)
        sut = CacheManagerImpl(
            config,
            preferenceRepository,
            fileManager
        )
    }

    @Test
    fun `given fetch when all cache is not expired then do nothing`(): Unit = runBlocking {
        prepareDependencies(
            isRuleCacheExpired = false,
            isIssuersCacheExpired = false,
            isRevocationsCacheExpired = false
        )
        prepareCacheExpiryTimes()
        sut.fetch()
        verify(fileManager, Times(0)).downloadFile(anyString())
    }

    @Test
    fun `given fetch when rules cache is expired then download rules`(): Unit = runBlocking {
        prepareDependencies(
            isRuleCacheExpired = true,
            isIssuersCacheExpired = false,
            isRevocationsCacheExpired = false
        )
        prepareCacheExpiryTimes()
        sut.fetch()
        verify(fileManager, Times(1)).downloadFile(anyString())
    }

    @Test
    fun `given fetch when issuers cache is expired then download issuers`(): Unit = runBlocking {
        prepareDependencies(
            isRuleCacheExpired = false,
            isIssuersCacheExpired = true,
            isRevocationsCacheExpired = false
        )
        prepareCacheExpiryTimes()
        sut.fetch()
        verify(fileManager, Times(2)).downloadFile(anyString())
    }

    @Test
    fun `given fetch when revocations cache is expired then download revocations`(): Unit = runBlocking {
        prepareDependencies(
            isRuleCacheExpired = false,
            isIssuersCacheExpired = false,
            isRevocationsCacheExpired = true
        )
        prepareCacheExpiryTimes()
        sut.fetch()
        verify(fileManager, Times(1)).downloadFile(anyString())
    }

    @Test
    fun `given fetch when general time stamp is set then file downloaded`(): Unit = runBlocking {
        prepareDependencies()
        prepareCacheExpiryTimes()
        sut.fetch()
        verify(fileManager, atLeast(3)).downloadFile(anyString())
    }

    @Test
    fun `given fetch when issuers are correct and have defined suffix then time stamp is set and file downloaded`(): Unit = runBlocking {
        prepareDependencies()
        prepareCacheExpiryTimes()
        sut.fetch()
        verify(fileManager, atLeast(3)).downloadFile(anyString())
    }

    private fun prepareDependencies(
        isIssuerWithSuffix: Boolean = false,
        isRuleCacheExpired: Boolean? = null,
        isIssuersCacheExpired: Boolean? = null,
        isRevocationsCacheExpired: Boolean? = null
    ): Unit = runBlocking {
        doReturn(
            prepareTimeStampFlow(isRuleCacheExpired)
        ).`when`(preferenceRepository).rulesTimeStamp

        doReturn(
            prepareTimeStampFlow(isIssuersCacheExpired)
        ).`when`(preferenceRepository).issuersTimeStamp

        doReturn(
            prepareTimeStampFlow(isRevocationsCacheExpired)
        ).`when`(preferenceRepository).revocationsTimeStamp

        doReturn(
            listOf(
                defaultKey
            )
        ).`when`(fileManager).getKeys(anyString())

        doReturn(
            false
        ).`when`(fileManager).exists(anyString())

        doReturn(
            listOf(
                Issuer(
                    iss = if (isIssuerWithSuffix) TEST_ISS_WITH_SUFFIX else TEST_ISS,
                    name = "Dev Freshworks"
                )
            )
        ).`when`(fileManager).getIssuers(anyString())

        doReturn(
            listOf(
                defaultKey
            )
        ).`when`(fileManager).getKeys(anyString())

        every { Log.e(any(), any(), any()) } returns 0
    }

    private fun prepareTimeStampFlow(isExpired: Boolean?): Flow<Long?> {
        return flow {
            emit(
                isExpired?.let {
                    Calendar.getInstance().apply {
                        if (isExpired) {
                            set(2000, 1, 1)
                        } else {
                            set(999999, 1, 1)
                        }
                    }.timeInMillis
                }
            )
        }
    }

    private fun prepareCacheExpiryTimes(
        rulesExpiryMinutes: String = "360",
        issuersExpiryMinutes: String = "360",
        revocationsExpiryMinutes: String = "360"
    ): Unit = runBlocking {

        doReturn(
            listOf(
                defaultRule.copy(
                    cache = Cache(
                        expiry = Expiry(
                            rules = rulesExpiryMinutes,
                            issuers = issuersExpiryMinutes,
                            revocations = revocationsExpiryMinutes
                        )
                    )
                )
            )
        ).`when`(fileManager).getRule(anyString())
    }
}
