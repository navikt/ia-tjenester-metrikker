package no.nav.arbeidsgiver.iatjenester.metrikker.service

import arrow.core.Either
import io.mockk.every
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjenesteMedVirksomhetGrunndata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.event.annotation.BeforeTestMethod
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.ZonedDateTime.now

@ExtendWith(SpringExtension::class)
internal class IaTjenesterMetrikkerServiceTest {

    @MockBean
    lateinit var iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository

    @BeforeTestMethod
    fun setupMocks() {
        every {
            iaTjenesterMetrikkerRepository
                .persister(any<InnloggetMottattIaTjenesteMedVirksomhetGrunndata>())
        } returns Unit
    }

    @Test
    @Throws(Exception::class)
    fun `Tester at sjekkOgPersister validerer gyldig IA-tjeneste OK`() {

        val sjekkOgOpprett =
            IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository)
                .sjekkOgPersister(TestUtils.vilkårligIaTjeneste())

        assertThat(sjekkOgOpprett is Either.Right).isEqualTo(true)
    }

    @Test
    @Throws(Exception::class)
    fun `Skal ikke godkjenne datoer i fremtiden`() {

        val iaTjenesteMedDatoIFremtiden = TestUtils.vilkårligIaTjeneste()
        iaTjenesteMedDatoIFremtiden.tjenesteMottakkelsesdato = now().plusMinutes(2)

        val iaSjekk = IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository).sjekkOgPersister(
            iaTjenesteMedDatoIFremtiden
        )

        assertThat(iaSjekk is Either.Left).isEqualTo(true)
        assertThat((iaSjekk as Either.Left).value.årsak)
            .isEqualTo("tjenesteMottakkelsesdato kan ikke være i fremtiden")
    }

    @Test
    fun `Test at fylke blir utledet fra kommunenummer før innlogget IA-tjeneste persisteres`() {
        val levertIaTjenesteFraInnlandet =
            TestUtils.vilkårligIaTjeneste().apply { kommunenummer = "3403" }

        val resultat = IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository).sjekkOgPersister(
            levertIaTjenesteFraInnlandet
        )

        val persisterteData = (resultat as Either.Right).value
                as InnloggetMottattIaTjenesteMedVirksomhetGrunndata

        assertThat(persisterteData.fylke).isEqualTo("Innlandet")
    }
}

