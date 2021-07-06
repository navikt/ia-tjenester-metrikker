package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import java.net.URI

open class DatakatalogKlient(
    val restTemplate: RestTemplate,
    private val rootUrl: String,
    private val datapakkeId: String
) {
    open fun sendDatapakke(datapakke: Datapakke) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val nyDatapakkeTilOppdatering: HttpEntity<Datapakke> = HttpEntity(datapakke, headers)

        val uri = URI.create(rootUrl+datapakkeId)

        val response = restTemplate.exchange(uri, HttpMethod.PUT, nyDatapakkeTilOppdatering, DatakatalogResponse::class.java)
        println("Response er: $response")
    }

    data class DatakatalogResponse (val id: String, val status: String)
}