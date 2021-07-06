package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log

open class DatakatalogKlient(
    private val url: DatakatalogUrl
) {
    open fun sendDatapakke(datapakkeTilUtsending: Datapakke) {
        println("================> Datapakke: $datapakkeTilUtsending")
        log.info("TODO: implement me!")
    }
}