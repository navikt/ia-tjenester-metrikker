package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import no.nav.arbeidsgiver.iatjenester.metrikker.Cluster
import no.nav.arbeidsgiver.iatjenester.metrikker.IaTjenesteRad
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.cleanTable
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.opprettInnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.opprettUinnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.UinnloggetIaTjenesteRad
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(value = [AltinnConfigProperties::class])
@EnableMockOAuth2Server
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = ["wiremock.port=8686"])
internal class DatakatalogStatistikkIntTest {

    @Autowired
    private lateinit var iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository

    @Autowired
    private lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    private lateinit var datakatalogStatistikk: DatakatalogStatistikk
    private lateinit var datapakke: Datapakke

    private var datakatalogKlient: DatakatalogKlient = object : DatakatalogKlient(url = DatakatalogUrl(cluster = Cluster.LOKAL)) {
        override fun sendDatapakke(datapakkeTilUtsending: Datapakke) {
            datapakke = datapakkeTilUtsending
        }
    }

    private val målingFra = LocalDate.of(2021, 1, 1)


    @BeforeAll
    fun setUpClassUnderTestWithInjectedAndDummyBeans() {
        datakatalogStatistikk =
            DatakatalogStatistikk(iaTjenesterMetrikkerRepository, datakatalogKlient, dagensDato = { målingFra.plusMonths(6) })
    }


    @Test
    fun `kjør DatakatalogStatistikk`() {
        opprettTestDataIDB(namedParameterJdbcTemplate)

        datakatalogStatistikk.run()

        Assertions.assertThat(datapakke.views.size).isEqualTo(1)
        Assertions.assertThat(datapakke.views[0].spec.option.xAxis.data)
            .isEqualTo(listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"))
        Assertions.assertThat(datapakke.views[0].spec.option.series[0].name).isEqualTo("Uinnlogget")
        Assertions.assertThat(datapakke.views[0].spec.option.series[0].title).isEqualTo("Samtalestøtte")
        Assertions.assertThat(datapakke.views[0].spec.option.series[0].data)
            .isEqualTo(listOf(0, 1, 2, 2, 1, 0, 0))
        Assertions.assertThat(datapakke.views[0].spec.option.series[1].name).isEqualTo("Innlogget")
        Assertions.assertThat(datapakke.views[0].spec.option.series[1].title).isEqualTo("Sykefraværsstatistikk")
        Assertions.assertThat(datapakke.views[0].spec.option.series[1].data)
            .isEqualTo(listOf(1, 3, 1, 1, 1, 0, 1))
    }

    private fun opprettTestDataIDB(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {
        namedParameterJdbcTemplate.jdbcTemplate.dataSource?.connection?.cleanTable("metrikker_ia_tjenester_innlogget")
        namedParameterJdbcTemplate.jdbcTemplate.dataSource?.connection?.cleanTable("metrikker_ia_tjenester_uinnlogget")

        opprettInnloggetIaTjenester(
            listOf(
                Date.valueOf(målingFra),
                Date.valueOf(målingFra.plusMonths(1).plusDays(3)),
                Date.valueOf(målingFra.plusMonths(1).plusDays(12)),
                Date.valueOf(målingFra.plusMonths(1).plusDays(14)),
                Date.valueOf(målingFra.plusMonths(2).plusDays(1)),
                Date.valueOf(målingFra.plusMonths(3).plusDays(2)),
                Date.valueOf(målingFra.plusMonths(4).plusDays(3)),
                Date.valueOf(målingFra.plusMonths(6).plusDays(1)),
            )
        )

        opprettUinnloggetIaTjenester(
            listOf(
                Date.valueOf(målingFra.plusMonths(1).plusDays(3)),
                Date.valueOf(målingFra.plusMonths(2).plusDays(12)),
                Date.valueOf(målingFra.plusMonths(2).plusDays(14)),
                Date.valueOf(målingFra.plusMonths(3).plusDays(1)),
                Date.valueOf(målingFra.plusMonths(3).plusDays(22)),
                Date.valueOf(målingFra.plusMonths(4).plusDays(3)),
                Date.valueOf(målingFra.plusMonths(4).plusDays(3)),
            )
        )
    }

    private fun opprettUinnloggetIaTjenester(dates: List<Date> ) {
        dates.forEachIndexed() { index, date ->
            namedParameterJdbcTemplate.jdbcTemplate.dataSource?.connection?.opprettUinnloggetIaTjeneste(
                UinnloggetIaTjenesteRad(
                    id = index + 1,
                    type = TypeIATjeneste.DIGITAL_IA_TJENESTE,
                    kilde = Kilde.SAMTALESTØTTE,
                    Timestamp.valueOf(date.toLocalDate().atStartOfDay()),
                    Date.valueOf(LocalDate.now())
                )
            )
        }
    }

    private fun opprettInnloggetIaTjenester(dates: List<Date> ) {
        dates.forEachIndexed() { index, date ->
            namedParameterJdbcTemplate.jdbcTemplate.dataSource?.connection?.opprettInnloggetIaTjeneste(
                IaTjenesteRad(
                    id = index + 1,
                    type = TypeIATjeneste.DIGITAL_IA_TJENESTE,
                    kilde = Kilde.SYKEFRAVÆRSSTATISTIKK,
                    orgnr = (999999900 + index).toString(),
                    næringKode5Siffer = "",
                    næring2SifferBeskrivelse = "",
                    tjeneste_mottakkelsesdato = Timestamp.valueOf(date.toLocalDate().atStartOfDay()),
                    antallAnsatte = 5 + index,
                    næringskode5SifferBeskrivelse = "",
                    SSBSektorKode = "",
                    SSBSektorKodeBeskrivelse = "",
                    fylkesnummer = "",
                    fylke = "",
                    kommunenummer = "",
                    kommune = "",
                    opprettet = Date.valueOf(LocalDate.now())
                )
            )
        }
    }
}
