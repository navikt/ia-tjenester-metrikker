package no.nav.arbeidsgiver.iatjenester.metrikker.service

import com.zaxxer.hikari.HikariDataSource
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.ZonedDateTime.now
import kotlin.test.assertFailsWith

internal class IaTjenesterMetrikkerServiceTest {

    @Test
    @Throws(Exception::class)
    fun `Sjekker at mottatt datoen er gyldig`() {


        var iaTjenesterMetrikkerRepository =
            object : IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(HikariDataSource())) {
                override fun opprett(iatjeneste: IaTjeneste) {
                    /* Do nothing */
                }
            }

        val sjekkOgOpprett =
            IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository).sjekkOgOpprett(TestUtils.vilkårligIaTjeneste())
        Assertions.assertThat(sjekkOgOpprett).isEqualTo(true)
    }

    @Test
    @Throws(Exception::class)
    fun `Skal ikke godkjenne datoer i frmetiden`() {

        var iaTjenesterMetrikkerRepository =
            object : IaTjenesterMetrikkerRepository(NamedParameterJdbcTemplate(HikariDataSource())) {
                override fun opprett(iatjeneste: IaTjeneste) {
                    /* Do nothing */
                }
            }
        val iaTjenesteMedDatoIFremtiden = TestUtils.vilkårligIaTjeneste()
        iaTjenesteMedDatoIFremtiden.tjenesteMottakkelsesdato = now().plusMinutes(2)

        val exception = assertFailsWith<IaTjenesterMetrikkerValideringException>
        { IaTjenesterMetrikkerService(iaTjenesterMetrikkerRepository).sjekkOgOpprett(iaTjenesteMedDatoIFremtiden) }

        Assertions.assertThat(exception.årsak).isEqualTo("tjenesteMottakkelsesdato kan ikke være i fremtiden")
    }
}

