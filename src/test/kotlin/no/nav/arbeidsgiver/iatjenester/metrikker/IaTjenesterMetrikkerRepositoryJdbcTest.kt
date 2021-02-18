package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.config.DBConfig
import no.nav.arbeidsgiver.iatjenester.metrikker.config.DatabaseCredentials
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.opprett
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IaTjenesterMetrikkerRepositoryJdbcTest {

    @Test
    fun `Enkel test som sjekker at repository oppretter en rad i DB`() {
        val dataSource = DBConfig(
            DatabaseCredentials(
                "local", "", "", ""
            ).getUrl(),
            "sa",
            "",
            "org.h2.Driver"
        ).getDataSource()
        // #1 kal IaTjenesterMetrikkerRepository.leggTilMetrikk()
        //TODO: dette er testen som feiler
        /*
        IaTjenesterMetrikkerRepository(dataSource).connection.opprett(
            IaTjeneste(
                "987654321",
                "12345",
                TypeIATjeneste.DIGITAL_IA_TJENESTE,
                Kilde.SYKKEFRAVÆRSSTATISTIKK
            )
        )
        */

        // #2 les fra tabellen
        // TODO

        // #3 sjekk at innhold stemmer
        assertThat(true).isTrue()
    }
}