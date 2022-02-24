package ca.bc.gov.shcdecoder.model

import ca.bc.gov.shcdecoder.utils.epochToDate
import java.util.Date

data class RevocationsResponse(
    val kid: String,
    val method: String?,
    val ctr: Long?,
    val rids: List<String>
) {
    fun getRidsInPairs(): List<Pair<String, Date?>> {
        return rids.map { rid ->
            if (rid.contains(".") && (rid.startsWith(".").not() && rid.endsWith(".").not())) {
                val ridSplit = rid.split(".")
                ridSplit.first() to ridSplit[1].epochToDate()
            } else {
                rid to null
            }
        }.orEmpty()
    }
}
