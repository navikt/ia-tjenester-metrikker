package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log

class DatakatalogKlient(
    private val url: DatakatalogUrl
) {
    fun sendDatapakke(lagDatapakke: Datapakke) {
        println("================> Datapakke: $lagDatapakke")
        log.info("TODO: implement me!")
    }

    fun sendPlotlyFilTilDatavarehus(plotlyJsons: List<Pair<String, String>>) {
        println("================> plotlyJsons: $plotlyJsons")
        log.info("TODO: implement me!")
    }

}