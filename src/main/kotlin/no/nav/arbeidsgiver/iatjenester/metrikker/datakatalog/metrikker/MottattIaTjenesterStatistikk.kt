package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.*

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
                        "Samtalestøtte",
                        "Sykefraværsstatistikk"
                    )
                ),
                Xaxis(
                    "category",
                    data = datagrunnlag.gjeldendeMåneder().map { toString() }
                ),
                Yaxis("value"),
                listOf(
                    Serie(
                        "Uinnlogget",
                        "total",
                        datagrunnlag.antallUinnloggetMetrikkerPerMåned.values.toList(),
                        "bar",
                        "Samtalestøtte"
                    ),
                    Serie(
                        "Innlogget",
                        "total",
                        datagrunnlag.antallInnloggetMetrikkerPerMåned.values.toList(),
                        "bar",
                        "Sykefraværsstatistikk"
                    )
                )
            )
        )
    }


}

/*
#1 hente metrikker fra DB:
    innlogget metrikk
    uinnlogget metrikk

   Mappe data til model (fra 1.1.2021 til dagensdato)
   -> skape liste av måneder
   -> sette sammen metrikker per måned

   Model -> Json

   Sende Model til endepunktet (ved bruk av RestTemplate)



 */