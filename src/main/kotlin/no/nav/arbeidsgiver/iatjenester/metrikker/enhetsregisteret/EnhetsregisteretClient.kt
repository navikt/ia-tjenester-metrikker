package no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næringskode5Siffer
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.InstitusjonellSektorkode
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Kommune
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.OverordnetEnhet
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Underenhet
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.mapTilFylke
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.io.IOException


@Configuration
class EnhetsregisteretClient(val restTemplate: RestTemplate, val enhetsregisteretUrl: String) {
    private val objectMapper = jacksonObjectMapper()

    fun hentOverordnetEnhet(orgnrTilEnhet: Orgnr): OverordnetEnhet {
        val url = enhetsregisteretUrl + "enheter/" + orgnrTilEnhet.verdi
        try {
            val respons = restTemplate.getForObject(url, String::class.java)
            val overordnetEnhet: OverordnetEnhet = mapTilEnhet(respons)
            validerReturnertOrgnrLikInnsendtOrgnr(orgnrTilEnhet, overordnetEnhet.orgnr)
            return overordnetEnhet
        } catch (e: RestClientException) {
            throw EnhetsregisteretException("Feil ved kall til Enhetsregisteret", e)
        }
    }

    fun hentUnderenhet(orgnrTilUnderenhet: Orgnr): Underenhet {
        try {
            val url = enhetsregisteretUrl + "underenheter/" + orgnrTilUnderenhet.verdi
            val respons = restTemplate.getForObject(url, String::class.java)
            val underenhet: Underenhet = mapTilUnderenhet(respons)
            validerReturnertOrgnrLikInnsendtOrgnr(orgnrTilUnderenhet, underenhet.orgnr)
            return underenhet
        } catch (hsee: HttpServerErrorException) {
            throw EnhetsregisteretIkkeTilgjengeligException("Enhetsregisteret svarer ikke", hsee)
        } catch (e: RestClientException) {
            throw EnhetsregisteretException("Feil ved kall til Enhetsregisteret", e)
        }
    }


    private fun mapTilEnhet(jsonResponseFraEnhetsregisteret: String?): OverordnetEnhet {
        try {
            val enhetJson = objectMapper.readTree(jsonResponseFraEnhetsregisteret)
            val næringskodeJson = enhetJson["naeringskode1"]
            val sektorJson = enhetJson["institusjonellSektorkode"]
            val orgnr = Orgnr(enhetJson["organisasjonsnummer"].textValue())
            val navn = enhetJson["navn"].textValue()
            val næringskode: Næringskode5Siffer = objectMapper.readValue(næringskodeJson.toString())
            val institusjonellSektorkode: InstitusjonellSektorkode = objectMapper.readValue(sektorJson.toString())
            val antallAnsatte = enhetJson["antallAnsatte"].intValue()
            return OverordnetEnhet(
                orgnr,
                navn,
                næringskode,
                institusjonellSektorkode,
                antallAnsatte
            )
        } catch (e: Exception) {
            when (e) {
                is IOException, is NullPointerException, is IllegalArgumentException -> {
                    throw EnhetsregisteretMappingException(
                        "Feil ved kall til Enhetsregisteret. Kunne ikke parse respons til overordnet enhet.",
                        e
                    )
                }
                else -> {
                    throw e
                }
            }
        }
    }

    private fun validerReturnertOrgnrLikInnsendtOrgnr(innsendtOrgnr: Orgnr, returnertOrgnr: Orgnr) {
        if (!innsendtOrgnr.equals(returnertOrgnr)) {
            throw IllegalStateException(
                ("Orgnr hentet fra Enhetsregisteret samsvarer ikke med det innsendte orgnr. Request: "
                        + innsendtOrgnr.verdi
                        ) + ", response: "
                        + returnertOrgnr.verdi
            )
        }
    }

    private fun mapTilUnderenhet(jsonResponseFraEnhetsregisteret: String?): Underenhet {
        try {
            val enhetJson = objectMapper.readTree(jsonResponseFraEnhetsregisteret)
            val næringskodeJson = enhetJson["naeringskode1"]
                ?: throw IngenNæringException("Feil ved kall til Enhetsregisteret. Ingen næring for virksomhet.")

            val beliggenhetsadresseJson = enhetJson["beliggenhetsadresse"]
            val kommune = Kommune(
                beliggenhetsadresseJson["kommunenummer"].textValue(),
                beliggenhetsadresseJson["kommune"].textValue()
            )
            return Underenhet(
                Orgnr(enhetJson["organisasjonsnummer"].textValue()),
                enhetJson["navn"].textValue(),
                objectMapper.treeToValue(næringskodeJson, Næringskode5Siffer::class.java),
                Orgnr(enhetJson["overordnetEnhet"].textValue()),
                kommune,
                mapTilFylke(kommune),
                enhetJson["antallAnsatte"].intValue()
            )
        } catch (e: Exception) {
            when (e) {
                is IOException, is NullPointerException -> {
                    throw EnhetsregisteretMappingException(
                        "Feil ved kall til Enhetsregisteret. Kunne ikke parse respons til underenhet.",
                        e
                    )
                }
                else -> {
                    throw e
                }
            }
        }
    }
}
