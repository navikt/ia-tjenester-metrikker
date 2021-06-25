package statistikkapi.datakatalog.alder

import kscience.plotly.Plotly
import statistikkapi.datakatalog.*

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
