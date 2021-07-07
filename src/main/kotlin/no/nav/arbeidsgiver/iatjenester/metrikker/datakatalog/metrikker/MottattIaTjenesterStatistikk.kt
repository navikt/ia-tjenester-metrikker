package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.*
import java.time.format.TextStyle
import java.util.*

class MottattIaTjenesterStatistikk(private val datagrunnlag: MottattIaTjenesterDatagrunnlag) : DatakatalogData {


    override fun views() = listOf(
        View(
            title = "Antall mottatt ia-tjenester",
            description = "Vise antall mottatt ia-tjenester",
            specType = "echart",
            spec = lagSpec(datagrunnlag),
        )
    )


    private fun lagSpec(datagrunnlag: MottattIaTjenesterDatagrunnlag): Spec {
        return Spec(
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
                        .map { month -> month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) }
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
}