package no.nav.arbeidsgiver.iatjenester.metrikker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DummyTest {

    @Test
    fun `Denne testen sjekker at test konfig er på plass`() {
        assertThat(2 + 2).isEqualTo(4)
    }

}