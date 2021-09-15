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
    val specType: SpecType,
    val spec: Spec
)

enum class SpecType {
    markdown,
    echart
}

interface Spec
data class EchartSpec (
    val url: String,
    val option: Option
) : Spec

data class MarkdownSpec (
    val markdown: String
) : Spec

data class Option(
    val legend: Legend,
    @JvmField
    val xAxis: Xaxis,
    @JvmField
    val yAxis: Yaxis,
    val tooltip: Tooltip,
    val series: List<Serie>
)

data class Legend(
    val data: List<String>
)

data class Tooltip(
    val trigger: String
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
