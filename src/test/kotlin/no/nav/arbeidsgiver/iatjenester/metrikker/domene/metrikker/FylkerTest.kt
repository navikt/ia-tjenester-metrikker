package no.nav.arbeidsgiver.iatjenester.metrikker.domene.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Fylke
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.alleFylkerAlfabetisk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FylkerTest {
    @Test
    fun `alleFylkerAlfabetisk returnerer navnet på fylker i alfabetisk rekkefølge`() {
        val fylker = alleFylkerAlfabetisk()

        assertThat(fylker.first() == "Agder")
        assertThat(fylker[1] == "Innlandet")
        assertThat(fylker[2] == "Vestfold og Telemark")
        // ...
        assertThat(fylker.last() == "Viken")
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Ukjent for kommunenumre som ikke eksisterer`() {
        val kommunenumre = setOf(
            "0000",
            "9999",
        )

        kommunenumre.forEach {
            val fylke = Fylke.fraKommunenummer(it)
            assertThat(fylke.navn).isEqualTo("Ukjent")
            assertThat(fylke.nummer).isEqualTo("Ukjent")
        }
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Ukjent for kommunenumre som ikke har gyldig nummer`() {
        val kommunenumre = setOf(
            "",
            "9",
            "03",
            "30",
            "030",
            "9999",
            "03010",
        )

        kommunenumre.forEach {
            val fylke = Fylke.fraKommunenummer(it)
            assertThat(fylke.navn).isEqualTo("Ukjent")
            assertThat(fylke.nummer).isEqualTo("Ukjent")
        }
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Viken for tilhørende kommunenumre`() {
        val kommunenumre = setOf(
            "3001",
            "3090",
            "3005",
        )

        kommunenumre.forEach {
            val fylke = Fylke.fraKommunenummer(it)
            assertThat(fylke.navn).isEqualTo("Viken")
            assertThat(fylke.nummer).isEqualTo("30")
        }
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Oslo for Oslo kommune`() {
        val fylke = Fylke.fraKommunenummer("0301")

        assertThat(fylke.navn).isEqualTo("Oslo")
        assertThat(fylke.nummer).isEqualTo("03")
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Innlandet for tilhørende kommunenumre`() {
        val kommunenumre = setOf(
            "3401",
            "3405",
        )

        kommunenumre.forEach {
            val fylke = Fylke.fraKommunenummer(it)
            assertThat(fylke.navn).isEqualTo("Innlandet")
            assertThat(fylke.nummer).isEqualTo("34")
        }
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Vestfold og Telemark for tilhørende kommunenumre`() {
        val kommunenumre = setOf(
            "3804",
            "3806",
        )

        kommunenumre.forEach {
            val fylke = Fylke.fraKommunenummer(it)
            assertThat(fylke.navn).isEqualTo("Vestfold og Telemark")
            assertThat(fylke.nummer).isEqualTo("38")
        }
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Agder for tilhørende kommunenumre`() {
        val kommunenumre = setOf(
            "4201",
            "4204",
        )

        kommunenumre.forEach {
            val fylke = Fylke.fraKommunenummer(it)
            assertThat(fylke.navn).isEqualTo("Agder")
            assertThat(fylke.nummer).isEqualTo("42")
        }
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Rogaland for Eigersund kommune`() {
        val fylke = Fylke.fraKommunenummer("1101")

        assertThat(fylke.navn).isEqualTo("Rogaland")
        assertThat(fylke.nummer).isEqualTo("11")
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Vestland for tilhørende kommunenumre`() {
        val kommunenumre = setOf(
            "4601",
            "4602",
        )

        kommunenumre.forEach {
            val fylke = Fylke.fraKommunenummer(it)
            assertThat(fylke.navn).isEqualTo("Vestland")
            assertThat(fylke.nummer).isEqualTo("46")
        }
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Møre og Romsdal for Molde kommune`() {
        val fylke = Fylke.fraKommunenummer("1506")

        assertThat(fylke.navn).isEqualTo("Møre og Romsdal")
        assertThat(fylke.nummer).isEqualTo("15")
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Nordland for Bodø kommune`() {
        val fylke = Fylke.fraKommunenummer("1804")

        assertThat(fylke.navn).isEqualTo("Nordland")
        assertThat(fylke.nummer).isEqualTo("18")
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Troms og Finnmark for tilhørende kommunenumre`() {
        val kommunenumre = setOf(
            "5401",
            "5404",
        )

        kommunenumre.forEach {
            val fylke = Fylke.fraKommunenummer(it)
            assertThat(fylke.navn).isEqualTo("Troms og Finnmark")
            assertThat(fylke.nummer).isEqualTo("54")
        }
    }

    @Test
    fun `Fylke_fraKommunenummer returnerer Trøndelag for tilhørende kommunenumre`() {
        val kommunenumre = setOf(
            "5001",
            "5006",
        )

        kommunenumre.forEach {
            val fylke = Fylke.fraKommunenummer(it)
            assertThat(fylke.navn).isEqualTo("Trøndelag")
            assertThat(fylke.nummer).isEqualTo("50")
        }
    }
}
