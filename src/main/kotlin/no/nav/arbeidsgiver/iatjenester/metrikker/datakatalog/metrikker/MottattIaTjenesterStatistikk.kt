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
                markdown = style2() + html()
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
}

fun style() = """
    <style>
        table.datapakke-tabell {
            width: 100%;
            background-color: #FFFFFF;
            border-collapse: collapse;
            border-width: 2px;
            border-color: #FFFFFF;
            border-style: solid;
            color: #000000;
        }

        table.datapakke-tabell td, table.datapakke-tabell th {
            border-width: 2px;
            border-color: #FFFFFF;
            border-style: solid;
            padding: 5px;
        }

        table.datapakke-tabell thead {
            background-color: #60C4FC;
        }
    </style>

""".trimIndent()

fun style2() = """
    <style>
        .datapakke-tabell {
            border-collapse: collapse;
            margin: 25px 0;
            font-size: 0.9em;
            font-family: sans-serif;
            width: 100%;
            box-shadow: 0 0 20px rgba(0, 0, 0, 0.15);
        }
        
        .datapakke-tabell thead tr {
            background-color: #009879;
            color: #ffffff;
            text-align: left;
        }

        .datapakke-tabell th,
        .datapakke-tabell td {
            padding: 12px 15px;
        }
        
        .datapakke-tabell tbody tr {
            border-bottom: 1px solid #dddddd;
        }

        .datapakke-tabell tbody tr:nth-of-type(even) {
            background-color: #f3f3f3;
        }

        .datapakke-tabell tbody tr:last-of-type {
            border-bottom: 2px solid #009879;
        }
        
        .datapakke-tabell tbody tr.active-row {
            font-weight: bold;
            color: #009879;
        }

    </style>

""".trimIndent()

fun html() = """
        <table class="datapakke-tabell">
          <thead>
            <tr>
              <th></th>
              <th colspan=2>Sykefraværsstatistikk</th>
              <th colspan=2>Samtalestøtte (uinnlogget)</th>
              <th colspan=2>Samtalestøtte (innlogget)</th>
            </tr>
          </thead>
          <tbody>
              <tr class="active-row">
                <td> </td>
                <td>2021</td>
                <td>2022</td>
                <td>2021</td>
                <td>2022</td>
                <td>2021</td>
                <td>2022</td>
              </tr>
            <tr>
              <td>Jan</td>
              <td>Row 2, Cell 2</td>
              <td>Row 2, Cell 3</td>
              <td>Row 2, Cell 4</td>
              <td>Row 2, Cell 5</td>
              <td>Row 2, Cell 6</td>
              <td>Row 2, Cell 7</td>
            </tr>
            <tr>
              <td>Feb</td>
              <td>Row 2, Cell 2</td>
              <td>Row 2, Cell 3</td>
              <td>Row 2, Cell 4</td>
              <td>Row 2, Cell 5</td>
              <td>Row 2, Cell 6</td>
              <td>Row 2, Cell 7</td>
            </tr>
            <tr>
              <td>Mar</td>
              <td>Row 3, Cell 2</td>
              <td>Row 3, Cell 3</td>
              <td>Row 3, Cell 4</td>
              <td>Row 3, Cell 5</td>
              <td>Row 3, Cell 6</td>
              <td>Row 3, Cell 7</td>
            </tr>
          </tbody>
        </table>
""".trimIndent()