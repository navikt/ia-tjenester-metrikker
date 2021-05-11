package no.nav.arbeidsgiver.iatjenester.metrikker

import org.junit.Assert
import org.junit.jupiter.api.*
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.PrintStream


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LogbackMaskeringTest {
    private val log: Logger = LoggerFactory.getLogger(LogbackMaskeringTest::class.java)
    private val mockOut = Mockito.mock(PrintStream::class.java);
    private lateinit var stdOut: PrintStream;

    @BeforeAll
    fun setup() {
        stdOut = System.out
        System.setOut(mockOut);
    }

    @AfterEach
    fun afterEach() {
        Mockito.reset(System.out);
    }

    @AfterAll
    fun tearDown() {
        System.setOut(stdOut);
    }

    @Test
    fun masksElevenDigitNumbersWhenInfoLogging(){
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)

        log.info("fnr:12345678901");
        verify(System.out).write(captor.capture())

        val value = captor.value.toString(Charsets.UTF_8)

        Assert.assertTrue(value.contains("fnr:**********"));
        Assert.assertFalse(value.contains("fnr:12345678901"));
    }

    @Test
    fun masksElevenDigitNumbersWhenWarnLogging(){
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)

        log.warn("fnr:12345678901");
        verify(System.out).write(captor.capture())

        val value = captor.value.toString(Charsets.UTF_8)

        Assert.assertTrue(value.contains("fnr:**********"));
        Assert.assertFalse(value.contains("fnr:12345678901"));
    }

    @Test
    fun masksElevenDigitNumbersWhenErrorLogging(){
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)

        log.error("fnr:12345678901");
        verify(System.out).write(captor.capture())

        val value = captor.value.toString(Charsets.UTF_8)

        Assert.assertTrue(value.contains("fnr:**********"));
        Assert.assertFalse(value.contains("fnr:12345678901"));
    }

    @Test
    fun shouldBlockMultipleSensitiveNumbers() {
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)

        log.info("fnr:12345678901, fnr:12345678902");
        verify(System.out).write(captor.capture())

        val value = captor.value.toString(Charsets.UTF_8)

        Assert.assertTrue(value.contains("fnr:**********"));
        Assert.assertFalse(value.contains("fnr:12345678901"));
        Assert.assertFalse(value.contains("fnr:12345678902"));
    }

    @Test
    fun shouldNotMaskWhenLoggingNonSensitiveNumbers(){
        val captor = ArgumentCaptor.forClass(ByteArray::class.java)

        log.info("fnr:1111111111");
        log.warn("fnr:2222222222");
        log.error("fnr:3333333333");
        log.info("fnr:444444444444");
        log.warn("fnr:555555555555");
        log.error("fnr:666666666666");

        verify(System.out, times(6)).write(captor.capture())

        val values = captor.allValues.map { value -> value.toString(Charsets.UTF_8) }

        Assert.assertEquals(6, values.size);

        Assert.assertTrue(values[0].contains("fnr:1111111111"))
        Assert.assertFalse(values[0].contains("fnr:**********"))

        Assert.assertTrue(values[1].contains("fnr:2222222222"))
        Assert.assertFalse(values[1].contains("fnr:**********"))

        Assert.assertTrue(values[2].contains("fnr:3333333333"))
        Assert.assertFalse(values[2].contains("fnr:**********"))

        // Disse er en svakhet ved maskerings-regex
        Assert.assertTrue(values[3].contains("fnr:***********4"))
        Assert.assertTrue(values[4].contains("fnr:***********5"))
        Assert.assertTrue(values[5].contains("fnr:***********6"))
    }
}