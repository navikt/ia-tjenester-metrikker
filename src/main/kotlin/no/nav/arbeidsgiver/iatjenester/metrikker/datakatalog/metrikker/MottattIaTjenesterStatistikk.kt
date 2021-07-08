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
                        "Samtalestøtte (uinnlogget)",
                        "Sykefraværsstatistikk (innlogget)"
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

    private fun lagMarkdownSpec(datagrunnlag: MottattIaTjenesterDatagrunnlag): MarkdownSpec {
        return MarkdownSpec(
            "## Samtalestøtte (uinnlogget)\n **${datagrunnlag.totalUinnloggetMetrikker}**\n " +
                    "## Sykefraværsstatistikk (innlogget)\n **${datagrunnlag.totalInnloggetMetrikker}** \n " +
                    "### Antall unike bedriftsnummer \n **${datagrunnlag.totalUnikeBedrifterPerDag}**"
        )
    }

    // TODO lag en PR på denne og legg til en View med specType = SpecType.markdown, title = "Hva er en digital IA-tjeneste?", uten description
    private fun lagHvaErEnDigitatlIaTjenesteSpec(): MarkdownSpec {
        return MarkdownSpec(
            "I samtalestøtte registreres en digital IA-tjeneste når brukeren\n" +
                    " 1. finner informasjon om når eller hvordan de skal gjennomføre en samtale\n " +
                    " 2. benytter seg av veiledningen til systematisk arbeid\n " +
                    "\n" +
                    "I sykefraværsstatistikk registreres en digital IA-tjeneste dersom brukerene\n" +
                    " 1. ser sine og bransjens/næringens sykefraværtall og/eller\n " +
                    " 2. trykker seg videre til en ressurs inne på siden eller ut til eksterne lenker\n "
        )
    }
}