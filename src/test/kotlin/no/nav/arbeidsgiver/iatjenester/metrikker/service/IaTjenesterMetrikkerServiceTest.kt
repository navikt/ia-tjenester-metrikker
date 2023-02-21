package no.nav.arbeidsgiver.iatjenester.metrikker.service

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
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
    fun `noe med tidsstempel`() {
        val levertIaTjenesteFraInnlandet =
            TestUtils.vilkårligIaTjeneste().apply { kommunenummer = "3403" }

        val persisterteData = IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository, SimpleMeterRegistry())
            .sjekkOgPersister(levertIaTjenesteFraInnlandet)

        assertThat(persisterteData.fylke).isEqualTo("Innlandet")
    }

    @Test
    fun `Fylke blir utledet fra kommunenummer før innlogget IA-tjeneste persisteres`() {
        val levertIaTjenesteFraInnlandet =
            TestUtils.vilkårligIaTjeneste().apply { kommunenummer = "3403" }

        val persisterteData = IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository, SimpleMeterRegistry())
            .sjekkOgPersister(levertIaTjenesteFraInnlandet)

        assertThat(persisterteData.fylke).isEqualTo("Innlandet")
    }
}

