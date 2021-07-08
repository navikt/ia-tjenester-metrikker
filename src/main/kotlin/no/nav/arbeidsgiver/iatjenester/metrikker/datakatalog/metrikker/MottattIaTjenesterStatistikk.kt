package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.*
import java.time.format.TextStyle
import java.util.Locale


class MottattIaTjenesterStatistikk(private val datagrunnlag: MottattIaTjenesterDatagrunnlag) : DatakatalogData {

    var NORSK_BOKMÅL = Locale("no", "NO", "NB")

    override fun views() = listOf(
        View(
            title = "Mottatte digitale IA-tjenester",
            description = "Tallene viser totalt antall digitale IA-tjenester fra ${
                datagrunnlag.gjeldendeMåneder.first().getDisplayName(
                    TextStyle.SHORT,
                    NORSK_BOKMÅL
                )
            } ${datagrunnlag.gjelendeÅr}",
            specType = SpecType.markdown,
            spec = lagMarkdownSpec(datagrunnlag),
        ),
        View(
            title = "Mottatte digitale IA-tjenester per måned (${
                datagrunnlag.gjeldendeMåneder.first().getDisplayName(
                    TextStyle.SHORT,
                    NORSK_BOKMÅL
                )
            } - ${
                datagrunnlag.gjeldendeMåneder.last().getDisplayName(
                    TextStyle.SHORT,
                    NORSK_BOKMÅL
                )
            } ${datagrunnlag.gjelendeÅr})",
            description = "Vise antall digitale IA-tjenester mottatt per applikasjon per måned",
            specType = SpecType.echart,
            spec = lagEchartSpec(datagrunnlag),
        )
    )


    private fun lagEchartSpec(datagrunnlag: MottattIaTjenesterDatagrunnlag): EchartSpec {
        return EchartSpec(
            "",
            Option(
                Legend(
                    listOf(
                        "Uinnlogget",
                        "Innlogget"
                    )
                ),
                Xaxis(
                    "category",
                    data = datagrunnlag.gjeldendeMåneder()
                        .map { month -> month.getDisplayName(TextStyle.SHORT, NORSK_BOKMÅL) }
                ),
                Yaxis("value"),
                listOf(
                    Serie(
                        "Uinnlogget",
                        datagrunnlag.antallUinnloggetMetrikkerPerMåned.values.toList(),
                        "bar",
                        "Samtalestøtte"
                    ),
                    Serie(
                        "Innlogget",
                        datagrunnlag.antallInnloggetMetrikkerPerMåned.values.toList(),
                        "bar",
                        "Sykefraværsstatistikk"
                    )
                )
            )
        )
    }

    private fun lagMarkdownSpec(datagrunnlag: MottattIaTjenesterDatagrunnlag): MarkdownSpec {
        return MarkdownSpec(
            "## Samtalestøtte (uinnlogget)\n **${datagrunnlag.totalUinnloggetMetrikker}**\n " +
                    "## Sykefraværsstatistikk (innlogget)\n **${datagrunnlag.totalInnloggetMetrikker}** \n " +
                    "### Antall unike bedriftsnummer \n **${datagrunnlag.totalUnikeBedrifterPerDag}**"
        )
    }

}