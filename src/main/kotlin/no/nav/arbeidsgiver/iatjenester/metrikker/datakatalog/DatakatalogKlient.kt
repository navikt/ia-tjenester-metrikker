package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import java.net.URI

open class DatakatalogKlient(
    val restTemplate: RestTemplate,
    private val rootUrl: String,
    private val datapakkeId: String,
    private val erUtsendingAktivert: Boolean = false
) {
    open fun sendDatapakke(datapakke: Datapakke) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val nyDatapakkeTilOppdatering: HttpEntity<Datapakke> = HttpEntity(datapakke, headers)

        val uri = URI.create(rootUrl + datapakkeId)

        if (erUtsendingAktivert) {
            val response: ResponseEntity<DatakatalogResponse> =
                restTemplate.exchange(uri, HttpMethod.PUT, nyDatapakkeTilOppdatering, DatakatalogResponse::class.java)

            log("DatakatalogKlient")
                .info("Datapakke sendt til Datakatalog, med response " +
                        "(statusCode: '${response.statusCode.value()}', body: '${response.body}')")
        } else {
            log("DatakatalogKlient")
                .info("Utsending til Datakatalog er ikke aktivert i dette miljøet")
        }
    }

    data class DatakatalogResponse(val id: String, val status: String)
}