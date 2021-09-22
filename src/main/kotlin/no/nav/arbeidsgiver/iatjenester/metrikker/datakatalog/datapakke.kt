package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val grid: Grid?,
    @JvmField
    val xAxis: Xaxis,
    @JvmField
    val yAxis: Yaxis,
    val tooltip: Tooltip,
    val series: List<Serie>
){
    constructor(legend: Legend, xAxis: Xaxis, yAxis: Yaxis, tooltip: Tooltip, series: List<Serie>) :
            this(legend, null, xAxis, yAxis, tooltip, series)
}

data class Legend(
    val data: List<String>
)

data class Tooltip(
    val trigger: String
)

data class Grid(
    val left: String,
    val right: String,
    val bottom: String,
    val containLabel: Boolean
)

data class Xaxis(
    val type: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val data: List<String>?
) {
    constructor(type: String) :
            this(type, null)
}

data class Yaxis(
    val type: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val data: List<String>?
) {
    constructor(type: String) :
            this(type, null)
}

data class Serie(
    val name: String,
    val data: List<*>,
    val type: String,
    val title: String
)
