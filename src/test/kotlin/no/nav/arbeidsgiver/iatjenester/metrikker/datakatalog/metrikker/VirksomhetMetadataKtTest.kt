package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class VirksomhetMetadataKtTest {

    @Test
    fun `Test at mapToFylke returnerer UKJENT for kommuner som ikke eksisterer`() {

        val kommuner = setOf(
            Kommune("0000", "Bare tull"),
            Kommune("9999", "Rulleski"),
        )

        kommuner.forEach {
            val fylke = mapTilFylke(it)
            Assertions.assertThat(fylke.navn).isEqualTo("UKJENT")
            Assertions.assertThat(fylke.nummer).isEqualTo("UKJENT")
        }

    }

    @Test
    fun `Test at mapToFylke returnerer IKKE_GYLDIG_KOMMUNENUMMER for kommuner som ikke har gyldig nummer`() {

        val kommuner = setOf(
            Kommune("", ""),
            Kommune("9", "Rulleski"),
            Kommune("03", "Oslo"),
            Kommune("30", "Oslo"),
            Kommune("030", "Oslo"),
            Kommune("03010", "Oslo"),
        )

        kommuner.forEach {
            val fylke = mapTilFylke(it)
            Assertions.assertThat(fylke.navn).isEqualTo("UKJENT")
            Assertions.assertThat(fylke.nummer).isEqualTo("UKJENT")
        }

    }

    @Test
    fun `Test at mapToFylke returnerer Viken for tilhørende kommuner`() {

        val kommuner = setOf(
            Kommune("3001", "Halden"),
            Kommune("3090", "Vestby"),
            Kommune("3005", "Drammen")
        )

        kommuner.forEach {
            val fylke = mapTilFylke(it)
            Assertions.assertThat(fylke.navn).isEqualTo("Viken")
            Assertions.assertThat(fylke.nummer).isEqualTo("30")
        }

    }

    @Test
    fun `Test at mapToFylke returnerer Oslo for Oslo kommune`() {

        val fylke = mapTilFylke(Kommune("0301", "Oslo"))

        Assertions.assertThat(fylke.navn).isEqualTo("Oslo")
        Assertions.assertThat(fylke.nummer).isEqualTo("03")
    }

    @Test
    fun `Test at mapToFylke returnerer Innlandet for tilhørende kommuner`() {

        val kommuner = setOf(
            Kommune("3401", "Kongsvinger"),
            Kommune("3405", "Lillehammer")
        )

        kommuner.forEach {
            val fylke = mapTilFylke(it)
            Assertions.assertThat(fylke.navn).isEqualTo("Innlandet")
            Assertions.assertThat(fylke.nummer).isEqualTo("34")
        }
    }

    @Test
    fun `Test at mapToFylke returnerer Vestfold og Telemark for tilhørende kommuner`() {

        val kommuner = setOf(
            Kommune("3804", "Sandefjord"),
            Kommune("3806", "Porsgrunn")
        )

        kommuner.forEach {
            val fylke = mapTilFylke(it)
            Assertions.assertThat(fylke.navn).isEqualTo("Vestfold og Telemark")
            Assertions.assertThat(fylke.nummer).isEqualTo("38")
        }
    }

    @Test
    fun `Test at mapToFylke returnerer Agder for tilhørende kommuner`() {

        val kommuner = setOf(
            Kommune("4201", "Risør"),
            Kommune("4204", "Kristiansand")
        )

        kommuner.forEach {
            val fylke = mapTilFylke(it)
            Assertions.assertThat(fylke.navn).isEqualTo("Agder")
            Assertions.assertThat(fylke.nummer).isEqualTo("42")
        }
    }

    @Test
    fun `Test at mapToFylke returnerer Rogaland for Eigersund kommune`() {

        val fylke = mapTilFylke(Kommune("1101", "Eigersund"))

        Assertions.assertThat(fylke.navn).isEqualTo("Rogaland")
        Assertions.assertThat(fylke.nummer).isEqualTo("11")
    }

    @Test
    fun `Test at mapToFylke returnerer Vestland for tilhørende kommuner`() {

        val kommuner = setOf(
            Kommune("4601", "Bergen"),
            Kommune("4602", "Kinn")
        )

        kommuner.forEach {
            val fylke = mapTilFylke(it)
            Assertions.assertThat(fylke.navn).isEqualTo("Vestland")
            Assertions.assertThat(fylke.nummer).isEqualTo("46")
        }
    }

    @Test
    fun `Test at mapToFylke returnerer Møre og Romsdal for Molde kommune`() {

        val fylke = mapTilFylke(Kommune("1506", "Molde"))

        Assertions.assertThat(fylke.navn).isEqualTo("Møre og Romsdal")
        Assertions.assertThat(fylke.nummer).isEqualTo("15")
    }

    @Test
    fun `Test at mapToFylke returnerer Nordland for Bodø kommune`() {

        val fylke = mapTilFylke(Kommune("1804", "Bodø"))

        Assertions.assertThat(fylke.navn).isEqualTo("Nordland")
        Assertions.assertThat(fylke.nummer).isEqualTo("18")
    }

    @Test
    fun `Test at mapToFylke returnerer Troms og Finnmark for tilhørende kommuner`() {

        val kommuner = setOf(
            Kommune("5401", "Tromsø"),
            Kommune("5404", "Vardø")
        )

        kommuner.forEach {
            val fylke = mapTilFylke(it)
            Assertions.assertThat(fylke.navn).isEqualTo("Troms og Finnmark")
            Assertions.assertThat(fylke.nummer).isEqualTo("54")
        }
    }

    @Test
    fun `Test at mapToFylke returnerer Trøndelag for tilhørende kommuner`() {

        val kommuner = setOf(
            Kommune("5001", "Trondheim"),
            Kommune("5006", "Steinkjer")
        )

        kommuner.forEach {
            val fylke = mapTilFylke(it)
            Assertions.assertThat(fylke.navn).isEqualTo("Trøndelag")
            Assertions.assertThat(fylke.nummer).isEqualTo("50")
        }
    }
}