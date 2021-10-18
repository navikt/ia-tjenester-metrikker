package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.DatakatalogData
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.EchartSpec
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Grid
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Legend
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.MarkdownSpec
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Option
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Serie
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.SpecType
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Tooltip
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.View
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Xaxis
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Yaxis
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde.SAMTALESTØTTE
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde.SYKEFRAVÆRSSTATISTIKK
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*


class MottattIaTjenesterStatistikk(private val datagrunnlag: MottattIaTjenesterDatagrunnlag) :
        DatakatalogData {

    private var NORSK_BOKMÅL = Locale("no", "NO", "NB")
    private var startDato: LocalDate = datagrunnlag.startDate

    override fun opprettViews() = listOf(
        View(
            title = "Mottatte digitale IA-tjenester",
            description = "Antall digitale IA-tjenester siden ${startDato.dayOfMonth}. ${
                startDato.month.getDisplayName(
                    TextStyle.FULL,
                    NORSK_BOKMÅL
                )
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
                datagrunnlag.gjeldendeMåneder.first().getDisplayName(
                    TextStyle.SHORT,
                    NORSK_BOKMÅL
                )
            } - ${
                datagrunnlag.gjeldendeMåneder.last().getDisplayName(
                    TextStyle.SHORT,
                    NORSK_BOKMÅL
                )
            } ${datagrunnlag.gjeldendeÅr})",
            description = "Antall digitale IA-tjenester mottatt per applikasjon fordelt per bransje i bransjeprogram",
            specType = SpecType.echart,
            spec = lagEchartSpecForMottatteDigitaleIATjenesterFordeltPerBransje(datagrunnlag),
        ),
        View(
            title = "Mottatte digitale IA-tjenester per fylke (${
                datagrunnlag.gjeldendeMåneder.first().getDisplayName(
                    TextStyle.SHORT,
                    NORSK_BOKMÅL
                )
            } - ${
                datagrunnlag.gjeldendeMåneder.last().getDisplayName(
                    TextStyle.SHORT,
                    NORSK_BOKMÅL
                )
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
                        "Samtalestøtte (innlogget)",
                        datagrunnlag
                            .mottatteIaTjenesterInnloggetPerBransjeOgKilde
                            .filter { it.key.first == SAMTALESTØTTE }
                            .values
                            .toList(),
                        "bar",
                        "Samtalestøtte"
                    ),
                    Serie(
                        "Sykefraværsstatistikk",
                        datagrunnlag
                            .mottatteIaTjenesterInnloggetPerBransjeOgKilde
                            .filter { it.key.first == SYKEFRAVÆRSSTATISTIKK }
                            .values
                            .toList(),
                        "bar",
                        "Sykefraværsstatistikk"
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
                        "Sykefraværsstatistikk (innlogget)"
                    )
                ),
                Xaxis(
                    "category",
                    data = datagrunnlag.gjeldendeMåneder
                        .map { month -> month.getDisplayName(TextStyle.SHORT, NORSK_BOKMÅL) }
                ),
                Yaxis("value"),
                Tooltip("item"),
                listOf(
                    Serie(
                        "Samtalestøtte (uinnlogget)",
                        datagrunnlag.antallUinnloggetMetrikkerPerMåned.values.toList(),
                        "bar",
                        "Samtalestøtte"
                    ),
                    Serie(
                        "Sykefraværsstatistikk (innlogget)",
                        datagrunnlag.antallInnloggetMetrikkerPerMåned.values.toList(),
                        "bar",
                        "Sykefraværsstatistikk"
                    )
                )
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
//                Grid(
//                    left = "3%",
//                    right = "4%",
//                    bottom = "3%",
//                    containLabel = true
//                ),
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
                        "Samtalestøtte (innlogget)",
                        datagrunnlag.beregnInnloggedeIaTjenesterPerFylke(
                            fraApp = SAMTALESTØTTE
                        ),
                        "bar",
                        "Samtalestøtte"
                    ),
                    Serie(
                        "Sykefraværsstatistikk",
                        datagrunnlag.beregnInnloggedeIaTjenesterPerFylke(
                            fraApp = SYKEFRAVÆRSSTATISTIKK
                        ),
                        "bar",
                        "Sykefraværsstatistikk"
                    )
                )
            )
        )
    }


    private fun lagMottatteDigitaleIATjenesterMarkdownSpec(datagrunnlag: MottattIaTjenesterDatagrunnlag): MarkdownSpec {
        return MarkdownSpec(
            "## Samtalestøtte (uinnlogget)\n **${datagrunnlag.totalUinnloggetMetrikker}**\n " +
                    "## Sykefraværsstatistikk (innlogget)\n **${datagrunnlag.totalInnloggetMetrikker}** \n " +
                    "### Antall unike bedriftsnummer \n **${datagrunnlag.totalUnikeBedrifterPerDag}**"
        )
    }
}