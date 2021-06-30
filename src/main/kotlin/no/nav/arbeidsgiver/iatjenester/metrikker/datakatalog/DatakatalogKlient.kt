package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log

private fun datapakkeHttpClient() = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = JacksonSerializer {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }
}

class DatakatalogKlient(private val httpClient: HttpClient = datapakkeHttpClient(),
                        private val url: DatakatalogUrl
) {
    fun sendDatapakke(lagDatapakke: Datapakke) {
        runBlocking {
            val response: HttpResponse = httpClient
                .put(url.datapakke()) {
                    body = lagDatapakke
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                }
            log.info("Svar fra datakatalog datapakke api $response")
        }
    }
}