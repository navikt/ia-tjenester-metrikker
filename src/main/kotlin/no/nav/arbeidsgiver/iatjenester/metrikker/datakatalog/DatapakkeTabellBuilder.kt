package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

data class TabellHeader(val navn: String, val colspan: Int = 1)

class DatapakkeTabellBuilder(private val headere: List<TabellHeader>) {

    private val rader = mutableListOf<String>()

    fun leggTilRad(rad: List<Any>, uthevet: Boolean = false): DatapakkeTabellBuilder {
        val enkeltradHtml = rad.joinToString(
            prefix = "<td>",
            separator = "</td><td>",
            postfix = "</td>"
        )

        rader.add(
            """<tr ${if (uthevet) "class=\"uthevet-rad\"" else ""}> $enkeltradHtml </tr>"""
        )
        return this
    }

    fun leggTilRader(rader: List<List<Any>>): DatapakkeTabellBuilder {
        rader.forEach { leggTilRad(it) }
        return this
    }

    private fun headereTilHtml() =
        headere.joinToString("") { """<th colspan="${it.colspan}"> ${it.navn} </td>""" }

    private fun raderTilHtml() =
        rader.joinToString(" ").replace("/n", " ")

    fun build() = style + tabellHtml(headereTilHtml(), raderTilHtml())
}

val style = """
    <style>
        .datapakke-tabell {
            border-collapse: collapse;
            margin: 25px 0;
            font-size: 0.9em;
            font-family: sans-serif;
            width: 100%;
            box-shadow: 0 0 20px rgba(0, 0, 0, 0.15);
        }
        
        .datapakke-tabell thead tr {
            background-color: #009879;
            color: #ffffff;
            text-align: left;
        }

        .datapakke-tabell th,
        .datapakke-tabell td {
            padding: 12px 15px;
        }
        
        .datapakke-tabell tbody tr {
            border-bottom: 1px solid #dddddd;
        }

        .datapakke-tabell tbody tr:nth-of-type(even) {
            background-color: #f3f3f3;
        }

        .datapakke-tabell tbody tr:last-of-type {
            border-bottom: 2px solid #009879;
        }
        
        .datapakke-tabell tbody tr.uthevet-rad {
            font-weight: bold;
            color: #009879;
        }

    </style>

""".trimIndent()

fun tabellHtml(headers: String, rows: String) = """
        <table class="datapakke-tabell">
          <thead>
            <tr>
              $headers
            </tr>
          </thead>
          <tbody>
            $rows
          </tbody>
        </table>
""".trimIndent()

