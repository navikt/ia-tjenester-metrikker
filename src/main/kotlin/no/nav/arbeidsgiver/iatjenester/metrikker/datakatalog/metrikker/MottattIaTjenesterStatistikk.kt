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
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Tabell
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Tooltip
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.View
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Xaxis
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Yaxis
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.alleFylkerAlfabetisk
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.månederOgÅrTil
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.IaTjenesteTilgjengelighet
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
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
        ),
        View(
            title = "Tabell test 5",
            description = "Tabell test description",
            specType = SpecType.markdown,
            spec = MarkdownSpec(
                markdown = Tabell(headers(), rows()).build()
            )
        ),
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
                    "## Samtalestøtte (innlogget)\n **${
                        datagrunnlag.totalInnloggetMetrikkerPerApp(
                            SAMTALESTØTTE
                        )
                    }** \n " +
                    "## Sykefraværsstatistikk (innlogget)\n **${
                        datagrunnlag.totalInnloggetMetrikkerPerApp(
                            SYKEFRAVÆRSSTATISTIKK
                        )
                    }** \n " +
                    "## Antall unike bedriftsnummer \n **${datagrunnlag.totalUnikeBedrifterPerDag}**"
        )
    }

    private fun headers() = """
        <th> </th>
              <th colspan=2>Sykefraværsstatistikk</th>
              <th colspan=2>Samtalestøtte (uinnlogget)</th>
    """.trimIndent() // TODO <th colspan=2>Samtalestøtte (innlogget)</th>

    private fun rows(): String {
        val tabellrader = mutableListOf<String>()

        tabellrader.add(
            listOf(
                "",
                "2021",
                "2022",
                "2021",
                "2022",
            ).joinToString(
                prefix = "<td>",
                separator = "</td><td>",
                postfix = "</td>"
            )
        ) // TODO: Legg til-htnl-kode inn i Tabell-klassen

        val førsteDag2021 = LocalDate.of(2021, 1, 1)
        val sisteDag2022 = LocalDate.of(2022, 12, 31)

        val kombinasjonerIDatagrunnlaget = datagrunnlag.innloggetMetrikker.groupingBy {
            Grupperingsverdier(
                kilde = it.kilde,
                år = it.tidspunkt.year,
                måned = it.tidspunkt.month,
            )
        }

        val alleKombinasjonerITabell =
            (førsteDag2021 månederOgÅrTil sisteDag2022).flatMap { månedOgÅr ->
                listOf(SYKEFRAVÆRSSTATISTIKK, SAMTALESTØTTE)
                    .map { kilde -> Grupperingsverdier(kilde, månedOgÅr.år, månedOgÅr.måned) }
            }.sortedWith(compareBy({ it.måned }, { it.kilde }))

        val opptelt =
            alleKombinasjonerITabell.associateWith { 0 } + kombinasjonerIDatagrunnlaget.eachCount()

        opptelt.toList()
            .groupBy { it.first.måned }
            .mapValues { (_, antallLeverteTjenesterPerMåned) -> antallLeverteTjenesterPerMåned.map { it.second } }
            .map { (måned, antallTjenesterDenMåneden) -> listOf(måned.tilNorskTekstformat()) + antallTjenesterDenMåneden }
            .forEach { tabellrader.add(it.somHtmlRad()) }

        return tabellrader.joinToString(prefix = "<tr>", separator = "</tr><tr>", postfix = "<tr>")
    }
}

fun List<Any>.somHtmlRad() = this.joinToString(
    prefix = "<td>",
    separator = "</td><td>",
    postfix = "</td>"
)

data class Grupperingsverdier(val kilde: Kilde, val år: Int, val måned: Month)

