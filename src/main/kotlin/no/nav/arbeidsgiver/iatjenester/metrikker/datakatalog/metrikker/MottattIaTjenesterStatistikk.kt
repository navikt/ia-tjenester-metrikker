package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.*
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde.SAMTALESTØTTE
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde.SYKEFRAVÆRSSTATISTIKK
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.*


class MottattIaTjenesterStatistikk(private val datagrunnlag: MottattIaTjenesterDatagrunnlag) :
    DatakatalogData {

    private var startDato: LocalDate = datagrunnlag.startDate

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
                        "Samtalestøtte (uinnlogget)",
                        datagrunnlag.antallUinnloggetMetrikkerPerMåned.values.toList(),
                        "bar",
                        "Samtalestøtte",
                        stack = "Samtalestøtte"
                    ),
                    Serie(
                        "Samtalestøtte (innlogget)",
                        datagrunnlag.beregnAntallMetrikkerPerMånedPerApp(datagrunnlag.gjeldendeMåneder, SAMTALESTØTTE)
                            .values,
                        "bar",
                        "Samtalestøtte",
                        stack = "Samtalestøtte"
                    ),
                    Serie(
                        "Sykefraværsstatistikk (innlogget)",
                        datagrunnlag.beregnAntallMetrikkerPerMånedPerApp(
                            datagrunnlag.gjeldendeMåneder,
                            SYKEFRAVÆRSSTATISTIKK
                        )
                            .values,
                        "bar",
                        "Sykefraværsstatistikk"
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
                        "Samtalestøtte (innlogget)",
                        datagrunnlag.beregnInnloggedeIaTjenesterPerFylke(
                            fraApp = SAMTALESTØTTE
                        ),
                        "bar",
                        "Samtalestøtte",
                        "app"
                    ),
                    Serie(
                        "Sykefraværsstatistikk",
                        datagrunnlag.beregnInnloggedeIaTjenesterPerFylke(
                            fraApp = SYKEFRAVÆRSSTATISTIKK
                        ),
                        "bar",
                        "Sykefraværsstatistikk",
                        "app"
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
