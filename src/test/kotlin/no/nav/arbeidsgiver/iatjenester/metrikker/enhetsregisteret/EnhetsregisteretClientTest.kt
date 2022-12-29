package no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret


import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate

@ExtendWith(MockitoExtension::class)
class EnhetsregisteretClientTest {
    private var enhetsregisteretClient: EnhetsregisteretClient? = null

    @Mock
    private val restTemplate: RestTemplate? = null
    @BeforeEach
    fun setup() {
        enhetsregisteretClient = EnhetsregisteretClient(restTemplate!!, "")
    }

    @Test
    fun hentInformasjonOmEnhet__skal_hente_riktige_felter() {
        mockRespons(gyldigEnhetRespons("999263550"))
        val overordnetEnhet: OverordnetEnhet? = enhetsregisteretClient!!.hentOverordnetEnhet(Orgnr("999263550"))
        assertThat(overordnetEnhet?.orgnr?.verdi).isEqualTo("999263550")
        assertThat(overordnetEnhet?.navn).isEqualTo("NAV ARBEID OG YTELSER")
        assertThat(overordnetEnhet?.næringskode?.kode).isEqualTo("84300")
        assertThat(
            overordnetEnhet?.næringskode?.beskrivelse
        ).isEqualTo("Trygdeordninger underlagt offentlig forvaltning")
        assertThat(overordnetEnhet?.institusjonellSektorkode?.kode).isEqualTo("6100")
        assertThat(overordnetEnhet?.institusjonellSektorkode?.beskrivelse).isEqualTo("Statsforvaltningen")
        assertThat(overordnetEnhet?.antallAnsatte).isEqualTo(40)
    }

    @Test
    fun hentInformasjonOmEnhet__skal_feile_hvis_server_returnerer_5xx_server_returnerer_5xx() {
        Mockito.`when`(
            restTemplate!!.getForObject(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any<Class<Any>>()
            )
        ).thenThrow(
            HttpServerErrorException(HttpStatus.BAD_GATEWAY)
        )
        assertThrows(EnhetsregisteretException::class.java) {
            enhetsregisteretClient!!.hentOverordnetEnhet(
                Orgnr("971800534")
            )
        }
    }

    @Test
    fun hentInformasjonOmEnhet__skal_feile_hvis_et_felt_mangler() {
        val responsMedManglendeFelt = gyldigEnhetRespons("999263550")
        responsMedManglendeFelt.remove("institusjonellSektorkode")
        mockRespons(responsMedManglendeFelt)
        assertThrows(EnhetsregisteretMappingException::class.java) {
            enhetsregisteretClient!!.hentOverordnetEnhet(
                Orgnr("971800534")
            )
        }
    }

    @Test
    fun hentInformasjonOmEnhet__skal_feile_hvis_returnert_orgnr_ikke_matcher_med_medsendt_orgnr() {
        val responsMedFeilOrgnr = gyldigEnhetRespons("999263550")
        mockRespons(responsMedFeilOrgnr)
        assertThrows(
            IllegalStateException::class.java
        ) { enhetsregisteretClient!!.hentOverordnetEnhet(Orgnr("777777777")) }
    }

    @Test
    fun hentInformasjonOmUnderenhet__skal_hente_riktige_felter() {
        mockRespons(gyldigUnderenhetRespons("971800534"))
        val underenhet: Underenhet? = enhetsregisteretClient!!.hentUnderenhet(Orgnr("971800534"))
        assertThat(underenhet?.orgnr?.verdi).isEqualTo("971800534")
        assertThat(underenhet?.overordnetEnhetOrgnr?.verdi).isEqualTo("999263550")
        assertThat(underenhet?.navn).isEqualTo("NAV ARBEID OG YTELSER AVD OSLO")
        assertThat(underenhet?.næringskode?.kode).isEqualTo("84300")
        assertThat(
            underenhet?.næringskode?.beskrivelse
        ).isEqualTo("Trygdeordninger underlagt offentlig forvaltning")
        assertThat(underenhet?.kommunenummer).isEqualTo("3005")
        assertThat(underenhet?.kommune).isEqualTo("DRAMMEN")
        assertThat(underenhet?.fylke?.nummer).isEqualTo("30")
        assertThat(underenhet?.fylke?.navn).isEqualTo("Viken")
        assertThat(underenhet?.antallAnsatte).isEqualTo(40)
    }

    @Test
    fun hentInformasjonOmUnderenhet__skal_feile_hvis_server_returnerer_5xx() {
        Mockito.`when`(
            restTemplate!!.getForObject(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any<Class<Any>>()
            )
        ).thenThrow(
            HttpServerErrorException(HttpStatus.BAD_GATEWAY)
        )
        assertThrows(
            EnhetsregisteretIkkeTilgjengeligException::class.java
        ) { enhetsregisteretClient!!.hentUnderenhet(Orgnr("971800534")) }
    }

    @Test
    fun hentInformasjonOmUnderenhet__skal_feile_hvis_et_felt_mangler() {
        val responsMedManglendeFelt = gyldigUnderenhetRespons("822565212")
        responsMedManglendeFelt.remove("navn")
        mockRespons(responsMedManglendeFelt)
        assertThrows(
            EnhetsregisteretMappingException::class.java
        ) { enhetsregisteretClient!!.hentUnderenhet(Orgnr("971800534")) }
    }

    @Test
    fun hentInformasjonOmUnderenhet__skal_feile_hvis_returnert_orgnr_ikke_matcher_med_medsendt_orgnr() {
        val responsMedFeilOrgnr = gyldigUnderenhetRespons("822565212")
        mockRespons(responsMedFeilOrgnr)
        assertThrows(
            IllegalStateException::class.java
        ) { enhetsregisteretClient!!.hentUnderenhet(Orgnr("777777777")) }
    }

    private fun mockRespons(node: JsonNode) {
        Mockito.`when`(restTemplate!!.getForObject(ArgumentMatchers.anyString(), ArgumentMatchers.any<Class<Any>>()))
            .thenReturn(
                objectMapper.writeValueAsString(node)
            )
    }

    private fun gyldigUnderenhetRespons(orgnr: String): ObjectNode {
        val str = """{
              "organisasjonsnummer": "$orgnr",
              "navn": "NAV ARBEID OG YTELSER AVD OSLO",
              "naeringskode1": {
                "beskrivelse": "Trygdeordninger underlagt offentlig forvaltning",
                "kode": "84.300"
              },
              "antallAnsatte": 40,
              "overordnetEnhet": "999263550",
              "beliggenhetsadresse": {
                "land": "Norge",
                "landkode": "NO",
                "postnummer": "3036",
                "poststed": "DRAMMEN",
                "adresse": [
                  "Testenekjørerveien 13"
                ],
                "kommune": "DRAMMEN",
                "kommunenummer": "3005"
              }
            }"""
        return objectMapper.readTree(str) as ObjectNode
    }

    private fun gyldigEnhetRespons(orgnr: String): ObjectNode {
        val str = """{
              "organisasjonsnummer": "$orgnr",
              "navn": "NAV ARBEID OG YTELSER",  
              "naeringskode1": {
                "beskrivelse": "Trygdeordninger underlagt offentlig forvaltning",
                "kode": "84.300"
              },
              "institusjonellSektorkode": {
                  "kode": "6100",
                  "beskrivelse": "Statsforvaltningen"
              },
              "antallAnsatte": 40
        }"""
        return objectMapper.readTree(str) as ObjectNode
    }

    companion object {
        private val objectMapper = ObjectMapper()
    }
}

