package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import no.nav.arbeidsgiver.iatjenester.metrikker.IaTjenesteRad
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.cleanTable
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.opprettInnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.opprettUinnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.UinnloggetIaTjenesteRad
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.mockserver.MockServer
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
import org.springframework.web.client.RestTemplate
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(value = [AltinnConfigProperties::class])
@EnableMockOAuth2Server
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = ["wiremock.port=8585"])
internal class DatakatalogStatistikkIntegrasjonTest {

    @Autowired
    private lateinit var mockServer: MockServer

    @Autowired
    private lateinit var iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository

    @Autowired
    private lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var datakatalogKlient: DatakatalogKlient


    private lateinit var datakatalogStatistikk: DatakatalogStatistikk
    private lateinit var datakatalogStatistikkSomSenderTilLokalMockServer: DatakatalogStatistikk
    private lateinit var produsertDatapakke: Datapakke

    private var mockDatakatalogKlient: DatakatalogKlient = object : DatakatalogKlient(
        RestTemplate(),
        "http://localhost/url/skal/ikke/brukes",
        "ikke_i_bruk"
    ) {
        override fun sendDatapakke(datapakke: Datapakke) {
            produsertDatapakke = datapakke
        }
    }

    private val målingFra = LocalDate.of(2021, 1, 1)


    @BeforeAll
    fun setUpClassUnderTestWithInjectedAndDummyBeans() {
        datakatalogStatistikk =
            DatakatalogStatistikk(
                iaTjenesterMetrikkerRepository,
                mockDatakatalogKlient
            )

        datakatalogStatistikkSomSenderTilLokalMockServer =
            DatakatalogStatistikk(
                iaTjenesterMetrikkerRepository,
                datakatalogKlient
            )
    }


    @Test
    fun `kjør DatakatalogStatistikk og verifiser innhold til datapakke`() {
        opprettTestDataIDB(namedParameterJdbcTemplate)

        datakatalogStatistikk.run()

        Assertions.assertThat(produsertDatapakke.views.size).isEqualTo(2)
        Assertions.assertThat(produsertDatapakke.views[0].spec).isInstanceOf(MarkdownSpec::class.java)

        val echartSpec: EchartSpec = produsertDatapakke.views[1].spec as EchartSpec
        Assertions.assertThat(echartSpec.option.xAxis.data)
            .isEqualTo(listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"))
        Assertions.assertThat(echartSpec.option.series[0].name).isEqualTo("Uinnlogget")
        Assertions.assertThat(echartSpec.option.series[0].title).isEqualTo("Samtalestøtte")
        Assertions.assertThat(echartSpec.option.series[0].data)
            .isEqualTo(listOf(0, 1, 2, 2, 1, 0, 0))
        Assertions.assertThat(echartSpec.option.series[1].name).isEqualTo("Innlogget")
        Assertions.assertThat(echartSpec.option.series[1].title)
            .isEqualTo("Sykefraværsstatistikk")
        Assertions.assertThat(echartSpec.option.series[1].data)
            .isEqualTo(listOf(1, 3, 1, 1, 1, 0, 1))
    }

    @Test
    fun `kjør DatakatalogStatistikk og send data til lokal mock datakatalog`() {
        opprettTestDataIDB(namedParameterJdbcTemplate)

        datakatalogStatistikkSomSenderTilLokalMockServer.run()

        mockServer.wireMockServer.verify(
            1,
            putRequestedFor(
                urlEqualTo("/lokal_datakatalog/ikke_en_ekte_datapakke_id")
            )
        )
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

    private fun opprettUinnloggetIaTjenester(dates: List<Date>) {
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

    private fun opprettInnloggetIaTjenester(dates: List<Date>) {
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
