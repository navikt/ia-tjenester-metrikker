package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.kilde

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.DatakatalogData
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Spec
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.View

class AntallMottattIATjenesteStatistikk(private val antallMottattIATjenesteDatagrunnlag: AntallMottattIATjenesteDatagrunnlag) :
    DatakatalogData {

    companion object {
        private val filnavnAntallMottattIATjeneste: String = "alderAntallPresentert.json"
        private val filnavnAntallVirksomheterMottattIATjeneste: String = "alderAntallFåttJobben.json"
    }

    override fun views() = listOf(
        View(
            title = "Antall mottatt IA tjeneste",
            description = "Vise antall ",
            specType = "markdown",
            spec = Spec(
                url = filnavnAntallMottattIATjeneste
            )
        ),
        View(
            title = "Antall virksomheter",
            description = "Vise antall virksomheter mottatt IA tjeneste",
            specType = "markdown",
            spec = Spec(
                url = filnavnAntallVirksomheterMottattIATjeneste
            )
        ),
    )

    override fun plotlyFiler() =
        antallMottattIATjenesteDatagrunnlag.let { datagrunnlag ->
            listOf(
                filnavnAntallMottattIATjeneste to datagrunnlag.antallMottattIATjeneste.toString(),
                filnavnAntallVirksomheterMottattIATjeneste to datagrunnlag.antallVirksomheterMottattIATjenester.toString()
            )
        }
}
