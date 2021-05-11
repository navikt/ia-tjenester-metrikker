package no.nav.arbeidsgiver.iatjenester.metrikker.utils

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.test.assertTrue

internal class DataKvalitetSjekkerKtTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun `sjekkDataKvalitet skal returnere true hvis deata er gyldig`() {
        assertTrue(sjekkDataKvalitet(getGyldigTestInnloggetIATjeneste()))

    }

    fun getGyldigTestInnloggetIATjeneste(): InnloggetIaTjeneste {
        return InnloggetIaTjeneste(
            "999999999",
            "85000",
            TypeIATjeneste.DIGITAL_IA_TJENESTE,
            Kilde.SYKEFRAVÆRSSTATISTIKK,
            ZonedDateTime.now(),
            10,
            "barnehage",
            "sosial og omsørgstjeneste uten bo tilbud",
            "3001",
            "Offenltig",
            "0231",
            "Oslo",
            "0300",
            "Oslo"
        )
    }
}
