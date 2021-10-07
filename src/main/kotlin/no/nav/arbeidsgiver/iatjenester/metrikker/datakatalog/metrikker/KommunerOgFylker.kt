package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

data class Fylke(val nummer: String, val navn: String)
data class Kommune(val nummer: String, val navn: String)

class KommunerOgFylker {

    companion object {
        fun mapTilFylke(kommune: Kommune): Fylke {

            val utleddFylkenummer: String =
                if (kommune.nummer.isNullOrBlank() || kommune.nummer.length != 5)
                    "IKKE_GYLDIG_KOMMUNENUMMER"
                else
                    kommune.nummer.substring(0, 2)

            return when (utleddFylkenummer) {
                "42" -> Fylke("42", "Agder")
                "34" -> Fylke("34", "Innlandet")
                "15" -> Fylke("15", "Møre og Romsdal")
                "18" -> Fylke("18", "Nordland")
                "03" -> Fylke("03", "Oslo")
                "11" -> Fylke("11", "Rogaland")
                "54" -> Fylke("54", "Troms og Finnmark")
                "50" -> Fylke("50", "Trøndelag")
                "38" -> Fylke("38", "Vestfold og Telemark")
                "46" -> Fylke("46", "Vestland")
                "30" -> Fylke("30", "Viken")
                else -> Fylke("UKJENT", "UKJENT")
            }
        }
    }
}