package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import arrow.core.Either
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class TilgangskontrollServiceTest {
    @Test
    @Throws(Exception::class)
    fun `Sjekker at bruker har tilgang til en bedrift`() {
        val bruker = InnloggetBruker(Fnr("01019912345"))
        bruker.organisasjoner = listOf(AltinnOrganisasjon(organizationNumber = "9876543210"))
        val sjekkResult = TilgangskontrollService.sjekkTilgangTilOrgnr(Orgnr("9876543210"), bruker)

        Assertions.assertThat(sjekkResult is Either.Right).isEqualTo(true)
    }

    @Test
    fun `Returnerer exception i en Either dersom bruker ikke har tilgang til en bedrift`() {
        val bruker = InnloggetBruker(Fnr("01019912345"))
        bruker.organisasjoner = listOf(AltinnOrganisasjon(organizationNumber = "9876543210"))

        val sjekkResult = TilgangskontrollService.sjekkTilgangTilOrgnr(Orgnr("99999999999"), bruker)

        Assertions.assertThat(sjekkResult is Either.Left).isEqualTo(true)
    }
}
