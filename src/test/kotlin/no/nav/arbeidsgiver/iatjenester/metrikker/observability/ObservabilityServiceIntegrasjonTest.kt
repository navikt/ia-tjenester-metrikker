package no.nav.arbeidsgiver.iatjenester.metrikker.observability

import arrow.core.Either
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.arbeidsgiver.iatjenester.metrikker.IaTjenesteRad
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.cleanTable
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.opprettInnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.TestUtils.Companion.opprettUinnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.UinnloggetIaTjenesteRad
import no.nav.arbeidsgiver.iatjenester.metrikker.config.AltinnConfigProperties
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.TypeIATjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.Month
import kotlin.test.assertTrue

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(value = [AltinnConfigProperties::class])
@EnableMockOAuth2Server
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWireMock(port = 0)
internal class ObservabilityServiceIntegrasjonTest {
    @Autowired
    private lateinit var iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository
    @Autowired
    private lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    private lateinit var observabilityServiceMedDato: ObservabilityService
    private var antallErrorMeldingerSendt = 0


    @BeforeAll
    fun setUpClassUnderTestWithInjectedAndDummyBeans() {
        observabilityServiceMedDato = object : ObservabilityService(
            iaTjenesterMetrikkerRepository
        ) {
            override fun dagensDato(): LocalDate {
                return LocalDate.of(2022, Month.JUNE, 18)
            }

            override fun skrivErrorLog(typeMetrikk: String, fraDato: LocalDate) {
                super.skrivErrorLog(typeMetrikk, fraDato)
                antallErrorMeldingerSendt++
            }
        }
        antallErrorMeldingerSendt = 0
    }

    @Test
    fun `henter data og skriver ERROR log dersom ingen metrikker mottatt i perioden`() {
        val _14_JUNE_2022 = LocalDate.of(2022, Month.JUNE, 14)
        opprettTestDataIDB(namedParameterJdbcTemplate, _14_JUNE_2022)

        observabilityServiceMedDato.run()

        Assertions.assertThat(antallErrorMeldingerSendt).isEqualTo(2)
    }

    @Test
    fun `henter data og IKKE skriver ERROR log dersom noen metrikker er mottatt i perioden`() {
        val _17_JUNE_2022 = LocalDate.of(2022, Month.JUNE, 17)
        opprettTestDataIDB(namedParameterJdbcTemplate, _17_JUNE_2022)

        observabilityServiceMedDato.run()

        Assertions.assertThat(antallErrorMeldingerSendt).isEqualTo(0)
    }


    private fun opprettTestDataIDB(namedParameterJdbcTemplate: NamedParameterJdbcTemplate, fraDato: LocalDate) {
        namedParameterJdbcTemplate.jdbcTemplate.dataSource?.connection?.cleanTable("metrikker_ia_tjenester_innlogget")
        namedParameterJdbcTemplate.jdbcTemplate.dataSource?.connection?.cleanTable("metrikker_ia_tjenester_uinnlogget")

        opprettInnloggetIaTjenester(
            listOf(
                Date.valueOf(fraDato)
            )
        )

        opprettUinnloggetIaTjenester(
            listOf(
                Date.valueOf(fraDato)
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
                    tjeneste_mottakkelsesdato = Timestamp.valueOf(date.toLocalDate().atStartOfDay()),
                    opprettet = Date.valueOf(LocalDate.now())
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
                    fylke = "",
                    kommunenummer = "",
                    kommune = "",
                    opprettet = Date.valueOf(LocalDate.now())
                )
            )
        }
    }
}
