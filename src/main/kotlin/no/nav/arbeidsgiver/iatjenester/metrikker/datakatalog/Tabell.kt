package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

class Tabell {
    private val style = style2()
    private val html = html()

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

fun html() = """
        <table class="datapakke-tabell">
          <thead>
            <tr>
              <th></th>
              <th colspan=2>Sykefraværsstatistikk</th>
              <th colspan=2>Samtalestøtte (uinnlogget)</th>
              <th colspan=2>Samtalestøtte (innlogget)</th>
            </tr>
          </thead>
          <tbody>
              <tr class="active-row">
                <td> </td>
                <td>2021</td>
                <td>2022</td>
                <td>2021</td>
                <td>2022</td>
                <td>2021</td>
                <td>2022</td>
              </tr>
            <tr>
              <td>Jan</td>
              <td>Row 2, Cell 2</td>
              <td>Row 2, Cell 3</td>
              <td>Row 2, Cell 4</td>
              <td>Row 2, Cell 5</td>
              <td>Row 2, Cell 6</td>
              <td>Row 2, Cell 7</td>
            </tr>
            <tr>
              <td>Feb</td>
              <td>Row 2, Cell 2</td>
              <td>Row 2, Cell 3</td>
              <td>Row 2, Cell 4</td>
              <td>Row 2, Cell 5</td>
              <td>Row 2, Cell 6</td>
              <td>Row 2, Cell 7</td>
            </tr>
            <tr>
              <td>Mar</td>
              <td>Row 3, Cell 2</td>
              <td>Row 3, Cell 3</td>
              <td>Row 3, Cell 4</td>
              <td>Row 3, Cell 5</td>
              <td>Row 3, Cell 6</td>
              <td>Row 3, Cell 7</td>
            </tr>
          </tbody>
        </table>
""".trimIndent()

