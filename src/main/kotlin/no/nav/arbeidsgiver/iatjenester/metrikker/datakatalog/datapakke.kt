package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

data class Datapakke(
    val title: String,
    val type: String,
    val description: String,
    val name: String,
    val uri: String,
    val url: String,
    val team: String,
    val views: List<View>
)

data class View(
    val title: String,
    val description: String,
    val specType: String,
    val spec: Spec // TODO vi har to forskjellige specType: Markdown og echart
)

data class Spec ( // Denne er av type 'echart'
    val url: String,
    val option: Option
)

data class Option(
    val legend: Legend,
    val xAxis: Xaxis,
    val yAxis: Yaxis,
    val series: List<Serie>
)

data class Legend(
    val data: List<String>
)

data class Xaxis(
    val type: String,
    val data: List<String>
)

data class Yaxis(
    val type: String
)

data class Serie(
    val name: String,
    val data: List<*>,
    val type: String,
    val title: String
)
