package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import no.nav.arbeidsgiver.iatjenester.metrikker.Cluster
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate

internal class DatakatalogStatistikkTest {

    private lateinit var dummyIaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository;
    private val mockJdbcTemplate = Mockito.mock(NamedParameterJdbcTemplate::class.java);
    private val dato = LocalDate.of(2021, 6, 29).atStartOfDay()


    init {
        dummyIaTjenesterMetrikkerRepository = object : IaTjenesterMetrikkerRepository(mockJdbcTemplate) {
            override fun hentUinnloggetMetrikker(målingerStartet: LocalDate?): List<MottattIaTjenesteMetrikk> {
                return listOf(
                    MottattIaTjenesteMetrikk(
                        false,
                        null,
                        dato
                    ),
                    MottattIaTjenesteMetrikk(
                        false,
                        null,
                        dato
                    )
                )
            }

            override fun hentInnloggetMetrikker(målingerStartet: LocalDate?): List<MottattIaTjenesteMetrikk> {
                return listOf(
                    MottattIaTjenesteMetrikk(
                        true,
                        "7777777777",
                        dato
                    ),
                    MottattIaTjenesteMetrikk(
                        true,
                        "999999998",
                        dato
                    )
                )
            }
        }
    }

    @Test
    fun `kjør DatakatalogStatistikk`() {

        val datakatalogStatistikk = DatakatalogStatistikk(
            iaTjenesterMetrikkerRepository = dummyIaTjenesterMetrikkerRepository, DatakatalogKlient(
                DatakatalogUrl(Cluster.LOKAL)
            ), dagensDato = { LocalDate.now() })

        datakatalogStatistikk.run()

        Assertions.assertThat(true).isEqualTo(true)
    }
}
