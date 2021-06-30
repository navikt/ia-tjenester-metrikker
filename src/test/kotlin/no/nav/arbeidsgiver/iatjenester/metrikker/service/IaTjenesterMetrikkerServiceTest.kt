package no.nav.arbeidsgiver.iatjenester.metrikker.service

import arrow.core.Either
import com.zaxxer.hikari.HikariDataSource
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.ZonedDateTime.now

internal class IaTjenesterMetrikkerServiceTest {

    @Test
    @Throws(Exception::class)
    fun `Sjekker at mottatt datoen er gyldig`() {

        var iaTjenesterMetrikkerRepository =
            object : IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(HikariDataSource())) {
                override fun opprett(iatjeneste: InnloggetIaTjeneste) {
                    /* Do nothing */
                }
            }

        val sjekkOgOpprett =
            IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository).sjekkOgOpprett(TestUtils.vilkårligIaTjeneste())
        Assertions.assertThat(sjekkOgOpprett is Either.Right).isEqualTo(true)
    }

    @Test
    @Throws(Exception::class)
    fun `Skal ikke godkjenne datoer i fremtiden`() {

        var iaTjenesterMetrikkerRepository =
            object : IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(HikariDataSource())) {
                override fun opprett(iatjeneste: InnloggetIaTjeneste) {
                    /* Do nothing */
                }
            }
        val iaTjenesteMedDatoIFremtiden = TestUtils.vilkårligIaTjeneste()
        iaTjenesteMedDatoIFremtiden.tjenesteMottakkelsesdato = now().plusMinutes(2)

        val iaSjekk = IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository).sjekkOgOpprett(iaTjenesteMedDatoIFremtiden)

        Assertions.assertThat(iaSjekk is Either.Left).isEqualTo(true)
        Assertions.assertThat((iaSjekk as Either.Left).value.årsak).isEqualTo("tjenesteMottakkelsesdato kan ikke være i fremtiden")
    }
}

