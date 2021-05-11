package no.nav.arbeidsgiver.iatjenester.metrikker.utils

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Kilde
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.TypeIATjeneste
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
        assertTrue(sjekkDataKvalitet(getGyldigTestInnloggetIATjeneste()))

    }

    @Test
    fun `sjekkDataKvalitet skal logge og retunere false ved ugyldig orgnr`() {
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)
        assertFalse(sjekkDataKvalitet(getUgyldigOrgNrTestInnloggetIATjeneste()))
        verify(System.out).write(captor.capture())
        val value = captor.value.toString(Charsets.UTF_8)
        Assert.assertTrue(value.contains("Ugyldig orgnr mottatt fra innlogget tjeneste, avslutter registrering"))
    }

    @Test
    fun `sjekkDataKvalitet skal logge og retunere false ved ugyldig næringskode5siffer`() {
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)
        assertFalse(sjekkDataKvalitet(getUgyldigNæringskode5SifferInnloggetIATjeneste()))
        verify(System.out).write(captor.capture())
        val value = captor.value.toString(Charsets.UTF_8)
        Assert.assertTrue(value.contains("Ugyldig næringskode5siffer mottatt fra innlogget tjeneste, avslutter registrering"))
    }

    @Test
    fun `sjekkDataKvalitet skal logge og retunere false ved ugyldig fylkesnummer`() {
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)
        assertFalse(sjekkDataKvalitet(getUgyldigFylkesnummerInnloggetIATjeneste()))
        verify(System.out).write(captor.capture())
        val value = captor.value.toString(Charsets.UTF_8)
        Assert.assertTrue(value.contains("Ugyldig fylkesnummer mottatt fra innlogget tjeneste, avslutter registrering"))
    }

    @Test
    fun `sjekkDataKvalitet skal logge og retunere false ved ugyldig kommunenr`() {
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)
        assertFalse(sjekkDataKvalitet(getUgyldigKommuneNrInnloggetIATjeneste()))
        verify(System.out).write(captor.capture())
        val value = captor.value.toString(Charsets.UTF_8)
        Assert.assertTrue(value.contains("Ugyldig kommunenummer mottatt fra innlogget tjeneste, avslutter registrering"))
    }

    @Test
    fun `sjekkDataKvalitet skal logge og retunere false ved ugyldig SSB sektorkode`() {
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)
        assertFalse(sjekkDataKvalitet(getUgyldigSSBSektorkodeInnloggetIATjeneste()))
        verify(System.out).write(captor.capture())
        val value = captor.value.toString(Charsets.UTF_8)
        Assert.assertTrue(value.contains("Ugyldig SSB sektorkode mottatt fra innlogget tjeneste, avslutter registrering"))
    }

    @Test
    fun `sjekkDataKvalitet skal logge og retunere false ved for lang næringskode5Siffer beskrivelse`() {
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)
        assertFalse(sjekkDataKvalitet(getForLangNæringskode5SifferBeskrivelseInnloggetIATjeneste()))
        verify(System.out).write(captor.capture())
        val value = captor.value.toString(Charsets.UTF_8)
        Assert.assertTrue(
            value.contains(
                "For lang beskrivelse for næringskode 5 siffer felt fra innlogget tjeneste, avslutter registrering"
            )
        )
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
            "54",
            "Oslo",
            "5444",
            "Oslo"
        )
    }

    fun getUgyldigOrgNrTestInnloggetIATjeneste(): InnloggetIaTjeneste {
        return InnloggetIaTjeneste(
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
            "54",
            "Oslo",
            "5444",
            "Oslo"
        )
    }

    fun getUgyldigNæringskode5SifferInnloggetIATjeneste(): InnloggetIaTjeneste {
        return InnloggetIaTjeneste(
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
            "55",
            "Feil fylke ",
            "5445",
            "Feil kommune"
        )
    }

    fun getUgyldigFylkesnummerInnloggetIATjeneste(): InnloggetIaTjeneste {
        return InnloggetIaTjeneste(
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
            "55",
            "Feil fylke ",
            "544",
            "Feil kommune"
        )
    }

    fun getUgyldigKommuneNrInnloggetIATjeneste(): InnloggetIaTjeneste {
        return InnloggetIaTjeneste(
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
            "54",
            "fylke ",
            "5445",
            "Feil kommune"
        )
    }

    fun getUgyldigSSBSektorkodeInnloggetIATjeneste(): InnloggetIaTjeneste {
        return InnloggetIaTjeneste(
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
            "54",
            "fylke ",
            "5444",
            "Feil kommune"
        )
    }

    fun getForLangNæringskode5SifferBeskrivelseInnloggetIATjeneste(): InnloggetIaTjeneste {
        var i = 0
        var tekst = ""
        do {
            tekst += "K"
            i++
        } while (i <= 513)
        return InnloggetIaTjeneste(
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
            "54",
            "fylke ",
            "5444",
            "Feil kommune"
        )
    }
}
