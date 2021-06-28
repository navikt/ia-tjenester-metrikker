package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.View

interface DatakatalogData {

    fun views(): List<View>
    fun plotlyFiler(): List<Pair<String, String>>
}
