package no.nav.arbeidsgiver.iatjenester.metrikker.utils

import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import java.util.UUID

private val DEFAULT_CALLID_HEADER = "Nav-CallId"
private val ALTERNATIVE_CALLID_HEADER = "Nav-Call-Id"

fun setNavCallid(headers: HttpHeaders) {
    val defaultCallid = headers.getFirst(DEFAULT_CALLID_HEADER)
    val altCallid = headers.getFirst(ALTERNATIVE_CALLID_HEADER)
    val uuid = UUID.randomUUID().toString()

    when {
        defaultCallid != null -> putCallidInMDC(defaultCallid)
        altCallid != null -> putCallidInMDC(altCallid)
        else -> putCallidInMDC(uuid)
    }
}

fun clearNavCallid() {
    MDC.remove(DEFAULT_CALLID_HEADER)
    MDC.remove(ALTERNATIVE_CALLID_HEADER)
}

private fun putCallidInMDC(callid: String) {
    MDC.put(DEFAULT_CALLID_HEADER, callid)
    MDC.put(ALTERNATIVE_CALLID_HEADER, callid)
}