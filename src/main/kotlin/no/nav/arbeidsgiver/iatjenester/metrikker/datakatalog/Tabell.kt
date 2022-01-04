package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

class Tabell(headers: String, rows: String) {
    private val style = style2()

    private val html = html(headers, rows)

    fun build() = style + html

}

fun style2() = """
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
        
        .datapakke-tabell tbody tr.active-row {
            font-weight: bold;
            color: #009879;
        }

    </style>

""".trimIndent()

fun html(headers: String, rows: String) = """
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

