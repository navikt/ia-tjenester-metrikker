package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.*
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde.SAMTALESTØTTE
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde.SYKEFRAVÆRSSTATISTIKK
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.*


class MottattIaTjenesterStatistikk(private val datagrunnlag: MottattIaTjenesterDatagrunnlag) :
    DatakatalogData {

    private var startDato: LocalDate = datagrunnlag.startDate
    private val NAV_BLÅ: String = "#0067C5"
    private val NAV_GRØNN: String = "#06893A"
    private val NAV_ORANSJE: String = "#FF9100"

    override fun opprettViews() = listOf(
        View(
            title = "Mottatte digitale IA-tjenester",
            description = "Antall digitale IA-tjenester siden ${startDato.dayOfMonth}. ${
                startDato.month.tilNorskTekstformat(kortform = false)
            }",
            specType = SpecType.markdown,
            spec = lagMottatteDigitaleIATjenesterMarkdownSpec(datagrunnlag),
        ),
        View(
            title = "Mottatte digitale IA-tjenester ",
            description = "Antall digitale IA-tjenester mottatt per applikasjon per måned",
            specType = SpecType.echart,
            spec = lagEchartSpecForMottatteDigitaleIATjenesterPerMåned(datagrunnlag),
        ),
        View(
            title = "Mottatte digitale IA-tjenester per bransje (${
                datagrunnlag.gjeldendeMåneder.first().tilNorskTekstformat()
            } - ${
                datagrunnlag.gjeldendeMåneder.last().tilNorskTekstformat()
            } ${datagrunnlag.gjeldendeÅr})",
            description = "Antall digitale IA-tjenester mottatt per applikasjon fordelt per bransje i bransjeprogram",
            specType = SpecType.echart,
            spec = lagEchartSpecForMottatteDigitaleIATjenesterFordeltPerBransje(datagrunnlag),
        ),
        View(
            title = "Mottatte digitale IA-tjenester per fylke (${
                datagrunnlag.gjeldendeMåneder.first().tilNorskTekstformat()
            } - ${
                datagrunnlag.gjeldendeMåneder.last().tilNorskTekstformat()
            } ${datagrunnlag.gjeldendeÅr})",
            description = "Antall digitale IA-tjenester mottatt per applikasjon fordelt på fylke.",
            specType = SpecType.echart,
            spec = lagHistogramOverMottatteDigitaleIATjenesterPerFylke(datagrunnlag),
        )
    )

    private fun lagEchartSpecForMottatteDigitaleIATjenesterFordeltPerBransje(
        datagrunnlag: MottattIaTjenesterDatagrunnlag
    ): EchartSpec {
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

    private fun lagEchartSpecForMottatteDigitaleIATjenesterPerMåned(datagrunnlag: MottattIaTjenesterDatagrunnlag): EchartSpec {
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
                    data = datagrunnlag.gjeldendeMåneder.map { måned -> måned.tilNorskTekstformat() }
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

    private fun lagHistogramOverMottatteDigitaleIATjenesterPerFylke(datagrunnlag: MottattIaTjenesterDatagrunnlag): EchartSpec {
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

    private fun Month.tilNorskTekstformat(kortform: Boolean = true): String {
        return getDisplayName(
            if (kortform) TextStyle.SHORT else TextStyle.FULL,
            Locale("no", "NO", "NB")
        )
    }

    private fun lagMottatteDigitaleIATjenesterMarkdownSpec(datagrunnlag: MottattIaTjenesterDatagrunnlag): MarkdownSpec {
        return MarkdownSpec(
            "## Samtalestøtte (uinnlogget)\n **${datagrunnlag.totalUinnloggetMetrikker}**\n " +
                    "## Samtalestøtte (innlogget)\n **${datagrunnlag.totalInnloggetMetrikkerPerApp(SAMTALESTØTTE)}** \n " +
                    "## Sykefraværsstatistikk (innlogget)\n **${
                        datagrunnlag.totalInnloggetMetrikkerPerApp(
                            SYKEFRAVÆRSSTATISTIKK
                        )
                    }** \n " +
                    "### Antall unike bedriftsnummer \n **${datagrunnlag.totalUnikeBedrifterPerDag}**"
        )
    }
}
