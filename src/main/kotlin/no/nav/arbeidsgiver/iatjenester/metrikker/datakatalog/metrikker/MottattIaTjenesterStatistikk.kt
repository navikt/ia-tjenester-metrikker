package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import kscience.plotly.Plotly
import kscience.plotly.toJsonString
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.*
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log


class MottattIaTjenesterStatistikk(private val datagrunnlag: MottattIaTjenesterDatagrunnlag) : DatakatalogData {

    companion object {
        private const val filnavnIaTjenesterAntallMottatt: String = "iaTjenesterAntallMottatt.json"
    }


    override fun views() = listOf(
        View(
            title = "Antall mottatt ia-tjenester",
            description = "Vise antall mottatt ia-tjenester",
            specType = "plotly",
            spec = Spec(
                url = filnavnIaTjenesterAntallMottatt
            )
        )
    )

    override fun plotlyFiler() =
        datagrunnlag.let { datagrunnlag ->
            listOf(
                filnavnIaTjenesterAntallMottatt to lagPlotAntallHullPresentert(datagrunnlag).toJsonString()
            )
        }

    private fun lagPlotAntallHullPresentert(datagrunnlag: MottattIaTjenesterDatagrunnlag) = Plotly.plot {
        log.info("Skal lage diagram for antall ia-tjenester mottatt (innlogget og uinnlogget)")
        lagBar("Antall mottatt ia-tjenester innlogget", datagrunnlag.gjeldendeDatoer()) { datagrunnlag.hentAntallMottatInnlogget(it) }
        lagBar("Antall pmottatt ia-tjenester uinnlogget", datagrunnlag.gjeldendeDatoer()) { datagrunnlag.hentAntallMottatUinnlogget(it) }
        getLayout("Antall")
    }
}