package no.nav.arbeidsgiver.iatjenester.metrikker.utils

import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjenesteMedVirksomhetGrunndata
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.TypeIATjeneste
import org.junit.Assert
import org.junit.jupiter.api.*
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.PrintStream
import java.time.ZonedDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DataKvalitetSjekkerKtTest {
    private val log: Logger = LoggerFactory.getLogger(DataKvalitetSjekkerKtTest::class.java)
    private val mockOut = Mockito.mock(PrintStream::class.java);
    private lateinit var stdOut: PrintStream;

    @BeforeEach
    fun setUp() {
        stdOut = System.out
        System.setOut(mockOut)
    }

    @AfterEach
    fun afterEach() {
        Mockito.reset(System.out)
    }

    @AfterAll
    fun tearDown() {
        System.setOut(stdOut)
    }

    @Test
    fun `sjekkDataKvalitet skal returnere true hvis deata er gyldig`() {
        assertTrue(erOrgnrGyldig(getGyldigTestInnloggetIATjeneste()))

    }

    @Test
    fun `sjekkDataKvalitet skal logge og retunere false ved ugyldig orgnr`() {
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)
        assertFalse(erOrgnrGyldig(getUgyldigOrgNrTestInnloggetIATjeneste()))
        verify(System.out).write(captor.capture())
        val value = captor.value.toString(Charsets.UTF_8)
        Assert.assertTrue(value.contains("Ugyldig orgnr mottatt fra innlogget tjeneste, avslutter registrering"))
    }

    @Test
    fun `sjekkDataKvalitet skal logge og retunere false ved ugyldig næringskode5siffer`() {
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)
        assertFalse(erOrgnrGyldig(getUgyldigNæringskode5SifferInnloggetIATjeneste()))
        verify(System.out).write(captor.capture())
        val value = captor.value.toString(Charsets.UTF_8)
        Assert.assertTrue(value.contains("Ugyldig næringskode5siffer mottatt fra innlogget tjeneste, avslutter registrering"))
    }

    @Test
    fun `sjekkDataKvalitet skal logge og retunere false ved ugyldig kommunenr`() {
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)
        assertFalse(erOrgnrGyldig(getUgyldigKommuneNrInnloggetIATjeneste()))
        verify(System.out).write(captor.capture())
        val value = captor.value.toString(Charsets.UTF_8)
        Assert.assertTrue(value.contains("Ugyldig kommunenummer mottatt fra innlogget tjeneste, avslutter registrering"))
    }

    @Test
    fun `sjekkDataKvalitet skal logge og retunere false ved ugyldig SSB sektorkode`() {
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)
        assertFalse(erOrgnrGyldig(getUgyldigSSBSektorkodeInnloggetIATjeneste()))
        verify(System.out).write(captor.capture())
        val value = captor.value.toString(Charsets.UTF_8)
        Assert.assertTrue(value.contains("Ugyldig SSB sektorkode mottatt fra innlogget tjeneste, avslutter registrering"))
    }

    @Test
    fun `sjekkDataKvalitet skal logge og retunere false ved for lang næringskode5Siffer beskrivelse`() {
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)
        assertFalse(erOrgnrGyldig(getForLangNæringskode5SifferBeskrivelseInnloggetIATjeneste()))
        verify(System.out).write(captor.capture())
        val value = captor.value.toString(Charsets.UTF_8)
        Assert.assertTrue(
            value.contains(
                "For lang beskrivelse for næringskode 5 siffer felt fra innlogget tjeneste, avslutter registrering"
            )
        )
    }

    fun getGyldigTestInnloggetIATjeneste(): InnloggetMottattIaTjenesteMedVirksomhetGrunndata {
        return InnloggetMottattIaTjenesteMedVirksomhetGrunndata(
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
            "Oslo",
            "5444",
            "Oslo"
        )
    }

    fun getUgyldigOrgNrTestInnloggetIATjeneste(): InnloggetMottattIaTjenesteMedVirksomhetGrunndata {
        return InnloggetMottattIaTjenesteMedVirksomhetGrunndata(
            "959595",
            "85000",
            TypeIATjeneste.DIGITAL_IA_TJENESTE,
            Kilde.SYKEFRAVÆRSSTATISTIKK,
            ZonedDateTime.now(),
            10,
            "barnehage",
            "sosial og omsørgstjeneste uten bo tilbud",
            "3001",
            "Offenltig",
            "Oslo",
            "5444",
            "Oslo"
        )
    }

    fun getUgyldigNæringskode5SifferInnloggetIATjeneste(): InnloggetMottattIaTjenesteMedVirksomhetGrunndata {
        return InnloggetMottattIaTjenesteMedVirksomhetGrunndata(
            "123456789",
            "85525000",
            TypeIATjeneste.DIGITAL_IA_TJENESTE,
            Kilde.SYKEFRAVÆRSSTATISTIKK,
            ZonedDateTime.now(),
            0,
            "feilnæringskodeognæring",
            "feil næring beskrivelse for 2 siffer",
            "9001",
            "feil ssbsektor kode",
            "Feil fylke ",
            "5445",
            "Feil kommune"
        )
    }

    fun getUgyldigKommuneNrInnloggetIATjeneste(): InnloggetMottattIaTjenesteMedVirksomhetGrunndata {
        return InnloggetMottattIaTjenesteMedVirksomhetGrunndata(
            "123456789",
            "25000",
            TypeIATjeneste.DIGITAL_IA_TJENESTE,
            Kilde.SYKEFRAVÆRSSTATISTIKK,
            ZonedDateTime.now(),
            0,
            "besk",
            "besk",
            "9000",
            "besk",
            "fylke ",
            "5445",
            "Feil kommune"
        )
    }

    fun getUgyldigSSBSektorkodeInnloggetIATjeneste(): InnloggetMottattIaTjenesteMedVirksomhetGrunndata {
        return InnloggetMottattIaTjenesteMedVirksomhetGrunndata(
            "123456789",
            "25000",
            TypeIATjeneste.DIGITAL_IA_TJENESTE,
            Kilde.SYKEFRAVÆRSSTATISTIKK,
            ZonedDateTime.now(),
            0,
            "besk",
            "besk",
            "9001",
            "besk",
            "fylke ",
            "5444",
            "Feil kommune"
        )
    }

    fun getForLangNæringskode5SifferBeskrivelseInnloggetIATjeneste(): InnloggetMottattIaTjenesteMedVirksomhetGrunndata {
        var i = 0
        var tekst = ""
        do {
            tekst += "K"
            i++
        } while (i <= 513)
        return InnloggetMottattIaTjenesteMedVirksomhetGrunndata(
            "123456789",
            "25000",
            TypeIATjeneste.DIGITAL_IA_TJENESTE,
            Kilde.SYKEFRAVÆRSSTATISTIKK,
            ZonedDateTime.now(),
            0,
            tekst,
            "besk",
            "9000",
            "besk",
            "fylke ",
            "5444",
            "Feil kommune"
        )
    }
}
