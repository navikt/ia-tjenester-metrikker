package no.nav.arbeidsgiver.iatjenester.metrikker.altinn

import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.AltinnOrganisasjon
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Component
class AltinnTilgangerKlient(
    @Qualifier("restTemplateAltinnTilganger")
    val restTemplate: RestTemplate,
    val altinnTilgangerApiUrl: String,
) {
    companion object {
        fun AltinnTilganger?.altinnOrganisasjonerVedkommendeHarTilgangTil(): List<AltinnOrganisasjon> =
            this?.hierarki?.flatMap {
                flatten(it) { altinnTilgang ->
                    AltinnOrganisasjon(
                        name = altinnTilgang.navn,
                        organizationNumber = altinnTilgang.orgnr,
                        organizationForm = altinnTilgang.organisasjonsform,
                        parentOrganizationNumber = this.finnOverordnetEnhet(altinnTilgang.orgnr) ?: "",
                    )
                }
            }?.toList() ?: emptyList()

        fun AltinnTilganger?.finnOverordnetEnhet(orgnr: String): String? {
            val listeAvAltinnTilgangPerOrgnr = this?.listeAvAltinnTilgangPerOrgnr()
            val filtrertMapPåOrgnr: Map<String, List<AltinnTilgang>>? =
                listeAvAltinnTilgangPerOrgnr
                    ?.filter { it.value.any { altinnTilgang -> altinnTilgang.orgnr == orgnr } }
            return if (filtrertMapPåOrgnr?.isEmpty() == true) {
                null
            } else {
                filtrertMapPåOrgnr?.keys?.first()
            }
        }

        private fun AltinnTilganger?.listeAvAltinnTilgangPerOrgnr(): Map<String, List<AltinnTilgang>> =
            this?.hierarki?.flatMap {
                flatten(it) { o: AltinnTilgang -> o.orgnr to o.underenheter }
            }?.toMap() ?: emptyMap()

        private fun <T> flatten(
            altinnTilgang: AltinnTilgang,
            mapFn: (AltinnTilgang) -> T,
        ): Set<T> =
            setOf(
                mapFn(altinnTilgang),
            ) + altinnTilgang.underenheter.flatMap { flatten(it, mapFn) }
    }

    fun hentAltinnOrganisasjoner(tokendingsToken: JwtToken): List<AltinnOrganisasjon> {
        try {
            val altinnTilganger = hentAltinnTilganger(tokendingsToken)
            return altinnTilganger.altinnOrganisasjonerVedkommendeHarTilgangTil()
        } catch (e: Exception) {
            log.error("Feil ved kall til Altinn tilganger", e)
            return emptyList()
        }
    }

    private fun hentAltinnTilganger(tokendingsToken: JwtToken): AltinnTilganger? {
        try {
            val response = restTemplate.exchange<AltinnTilganger>(
                altinnTilgangerApiUrl,
                HttpMethod.POST,
                HttpEntity("{}", httpHeaders(tokendingsToken)),
            )
            return response.body
        } catch (e: Exception) {
            log.error("Feil ved kall til Altinn tilganger", e)
            return null
        }
    }

    private fun httpHeaders(tokendingsToken: JwtToken) = HttpHeaders().apply {
        return HttpHeaders().apply {
            setBearerAuth(tokendingsToken.encodedToken)
            contentType = MediaType.APPLICATION_JSON
        }

    }

    data class AltinnTilgang(
        val orgnr: String,
        val altinn3Tilganger: Set<String>,
        val altinn2Tilganger: Set<String>,
        val underenheter: List<AltinnTilgang>,
        val navn: String,
        val organisasjonsform: String,
        val erSlettet: Boolean = false,
    )

    data class AltinnTilganger(
        val hierarki: List<AltinnTilgang>,
        val orgNrTilTilganger: Map<String, Set<String>>,
        val tilgangTilOrgNr: Map<String, Set<String>>,
        val isError: Boolean,
    )
}
