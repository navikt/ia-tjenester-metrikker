package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class TilgangskontrollServiceTest {

    @Test
    @Throws(Exception::class)
    fun `Sjekker at bruker har tilgang til en bedrift`() {

        val bruker = InnloggetBruker(Fnr("01019912345"))
        bruker.organisasjoner = listOf(AltinnOrganisasjon(organizationNumber = "9876543210"))
        TilgangskontrollService.sjekkTilgangTilOrgnr(Orgnr("9876543210"), bruker)

        Assertions.assertThatNoException()
    }

    @Test
    @Throws(Exception::class)
    fun `Kaster Exception dersom bruker ikke har tilgang til en bedrift`() {
        val bruker = InnloggetBruker(Fnr("01019912345"))
        bruker.organisasjoner = listOf(AltinnOrganisasjon(organizationNumber = "9876543210"))

        Assertions.assertThatExceptionOfType(TilgangskontrollException::class.java).isThrownBy {
            TilgangskontrollService.sjekkTilgangTilOrgnr(Orgnr("99999999999"), bruker)
        }
    }
}


