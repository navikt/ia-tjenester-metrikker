package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.DatakatalogData
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.EchartSpec
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Grid
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.ItemStyle
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Legend
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.MarkdownSpec
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Option
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Serie
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.SpecType
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Tooltip
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.View
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Xaxis
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Yaxis
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.alleFylkerAlfabetisk
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde.SAMTALESTØTTE
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde.SYKEFRAVÆRSSTATISTIKK
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.tilNorskTekstformat

class MottattIaTjenesterStatistikk(private val datagrunnlag: MottattIaTjenesterDatagrunnlag) :
        DatakatalogData {

    private val NAV_BLÅ: String = "#0067C5"
    private val NAV_GRØNN: String = "#06893A"
    private val NAV_ORANSJE: String = "#FF9100"

    override fun opprettViews() = listOf(
        View(
            title = "Mottatte digitale IA-tjenester",
            description = """                
                **Antall daglig bedriftsnumre:** 
                - **2021: ${datagrunnlag.antallUnikeBedrifterPerÅr[2021]}**
                - **2022: ${datagrunnlag.antallUnikeBedrifterPerÅr[2022]}**
                
                Tabellen under viser oversikt over antall leverte IA-tjenester i 2021 og 2022 per måned:""".trimIndent(),
            specType = SpecType.markdown,
            spec = MarkdownSpec(
                markdown = TabellOverLeverteIaTjenester(datagrunnlag).opprett()
            )
        ),
        View(
            title = "Mottatte digitale IA-tjenester ",
            description = "Antall digitale IA-tjenester mottatt per applikasjon per måned",
            specType = SpecType.echart,
            spec = lagEchartSpecForMottatteDigitaleIATjenesterPerMåned(),
        ),
        View(
            title = "Mottatte digitale IA-tjenester per bransje (${
                datagrunnlag.gjeldendeMåneder.first().tilNorskTekstformat()
            } - ${
                datagrunnlag.gjeldendeMåneder.last().tilNorskTekstformat()
            } ${datagrunnlag.gjeldendeÅr})",
            description = "Antall digitale IA-tjenester mottatt per applikasjon fordelt per bransje i bransjeprogram",
            specType = SpecType.echart,
            spec = lagEchartSpecForMottatteDigitaleIATjenesterFordeltPerBransje(),
        ),
        View(
            title = "Mottatte digitale IA-tjenester per fylke (${
                datagrunnlag.gjeldendeMåneder.first().tilNorskTekstformat()
            } - ${
                datagrunnlag.gjeldendeMåneder.last().tilNorskTekstformat()
            } ${datagrunnlag.gjeldendeÅr})",
            description = "Antall digitale IA-tjenester mottatt per applikasjon fordelt på fylke.",
            specType = SpecType.echart,
            spec = lagHistogramOverMottatteDigitaleIATjenesterPerFylke(),
        ),
    )

    private fun lagEchartSpecForMottatteDigitaleIATjenesterFordeltPerBransje(): EchartSpec {
        return EchartSpec(
            "",
            Option(
                Legend(
                    listOf(
                        "Samtalestøtte (innlogget)",
                        "Sykefraværsstatistikk"
                    )
                ),
                Grid(
                    left = "3%",
                    right = "4%",
                    bottom = "3%",
                    containLabel = true
                ),
                Xaxis(
                    "value"
                ),
                Yaxis(
                    "category",
                    data = datagrunnlag.bransjeListe.map { it.name }
                ),
                Tooltip("item"),
                listOf(
                    Serie(
                        name = "Samtalestøtte (innlogget)",
                        data = datagrunnlag
                            .mottatteIaTjenesterInnloggetPerBransjeOgKilde
                            .filter { it.key.first == SAMTALESTØTTE }
                            .values,
                        type = "bar",
                        title = "Samtalestøtte",
                        itemStyle = ItemStyle(color = NAV_ORANSJE)
                    ),
                    Serie(
                        name = "Sykefraværsstatistikk",
                        data = datagrunnlag
                            .mottatteIaTjenesterInnloggetPerBransjeOgKilde
                            .filter { it.key.first == SYKEFRAVÆRSSTATISTIKK }
                            .values,
                        type = "bar",
                        title = "Sykefraværsstatistikk",
                        itemStyle = ItemStyle(color = NAV_GRØNN)
                    )
                )
            )
        )
    }

    private fun lagEchartSpecForMottatteDigitaleIATjenesterPerMåned(): EchartSpec {
        return EchartSpec(
            "",
            Option(
                Legend(
                    listOf(
                        "Samtalestøtte (uinnlogget)",
                        "Samtalestøtte (innlogget)",
                        "Sykefraværsstatistikk (innlogget)",
                    )
                ),
                Grid(),
                Xaxis(
                    type = "category",
                    data = datagrunnlag.gjeldendeMåneder.map { it.tilNorskTekstformat() }
                ),
                Yaxis("value"),
                Tooltip("item"),
                listOf(
                    Serie(
                        name = "Samtalestøtte (uinnlogget)",
                        data =
                        datagrunnlag.beregnAntallMetrikkerPerMånedPerApp(
                            SAMTALESTØTTE, IaTjenesteTilgjengelighet.UINNLOGGET
                        ).values,
                        type = "bar",
                        title = "Samtalestøtte",
                        stack = "Samtalestøtte",
                        itemStyle = ItemStyle(color = NAV_BLÅ)
                    ),
                    Serie(
                        name = "Samtalestøtte (innlogget)",
                        data = datagrunnlag.beregnAntallMetrikkerPerMånedPerApp(
                            SAMTALESTØTTE,
                            IaTjenesteTilgjengelighet.INNLOGGET
                        ).values,
                        type = "bar",
                        title = "Samtalestøtte",
                        stack = "Samtalestøtte",
                        itemStyle = ItemStyle(color = NAV_ORANSJE)
                    ),
                    Serie(
                        name = "Sykefraværsstatistikk (innlogget)",
                        data =
                        datagrunnlag.beregnAntallMetrikkerPerMånedPerApp(
                            SYKEFRAVÆRSSTATISTIKK,
                            IaTjenesteTilgjengelighet.INNLOGGET
                        )
                            .values,
                        type = "bar",
                        title = "Sykefraværsstatistikk",
                        itemStyle = ItemStyle(color = NAV_GRØNN)
                    )
                ),
            )
        )
    }

    private fun lagHistogramOverMottatteDigitaleIATjenesterPerFylke(): EchartSpec {
        return EchartSpec(
            "",
            Option(
                Legend(
                    listOf(
                        "Samtalestøtte (innlogget)",
                        "Sykefraværsstatistikk"
                    )
                ),
                Grid(),
                Xaxis(
                    "value"
                ),
                Yaxis(
                    "category",
                    data = alleFylkerAlfabetisk()
                ),
                Tooltip("item"),
                listOf(
                    Serie(
                        name = "Samtalestøtte (innlogget)",
                        data = datagrunnlag.beregnInnloggedeIaTjenesterPerFylke(fraApp = SAMTALESTØTTE),
                        type = "bar",
                        title = "Samtalestøtte",
                        stack = "app",
                        itemStyle = ItemStyle(color = NAV_ORANSJE)
                    ),
                    Serie(
                        name = "Sykefraværsstatistikk",
                        data = datagrunnlag.beregnInnloggedeIaTjenesterPerFylke(fraApp = SYKEFRAVÆRSSTATISTIKK),
                        type = "bar",
                        title = "Sykefraværsstatistikk",
                        stack = "app",
                        itemStyle = ItemStyle(color = NAV_GRØNN)
                    )
                )
            )
        )
    }
}
