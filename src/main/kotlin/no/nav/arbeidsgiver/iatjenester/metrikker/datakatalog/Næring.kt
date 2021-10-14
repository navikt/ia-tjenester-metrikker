package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

/*
  Næringskode 2 siffer og næringskode 5 siffer er standard for næringsgruppering (SN) fra SSB:
  Ref: https://www.ssb.no/klass/klassifikasjoner/6
*/
data class Næring(val kode5Siffer: String, val kode5SifferBeskrivelse: String, val kode2SifferBeskrivelse: String) {

    fun getKode2siffer(): String = if (kode5Siffer.length > 1) kode5Siffer.substring(0, 2) else ""

    fun getArbeidstilsynetBransje(): ArbeidstilsynetBransje {
        when (getKode2siffer()) {
            "10" -> return ArbeidstilsynetBransje.NÆRINGSMIDDELINDUSTRI
            "41" -> return ArbeidstilsynetBransje.BYGG
            "42" -> return ArbeidstilsynetBransje.ANLEGG
        }

        when (kode5Siffer) {
            "88911" -> return ArbeidstilsynetBransje.BARNEHAGER
            "86101", "86102", "86104", "86105", "86106", "86107" -> return ArbeidstilsynetBransje.SYKEHUS
            "87101", "87102" -> return ArbeidstilsynetBransje.SYKEHJEM
            "49100", "49311", "49391", "49392" -> return ArbeidstilsynetBransje.TRANSPORT
        }

        return ArbeidstilsynetBransje.ANDRE_BRANSJER
    }

    enum class ArbeidstilsynetBransje {
        BARNEHAGER,
        NÆRINGSMIDDELINDUSTRI,
        SYKEHUS,
        SYKEHJEM,
        TRANSPORT,
        BYGG,
        ANLEGG,
        ANDRE_BRANSJER
    }
}

data class Næringskode5Siffer(var kode: String?, val beskrivelse: String) {
    init {
        if (kode.isNullOrBlank()) {
            throw IllegalArgumentException("Kode for næring kan IKKE være null")
        }
        val næringskodeUtenPunktum: String = kode!!.replace(".", "")

        if (erGyldigNæringskode(næringskodeUtenPunktum)) {
            this.kode = næringskodeUtenPunktum
        } else {
            throw IllegalArgumentException("Kode for næring skal være 5 siffer")
        }
    }

    private fun erGyldigNæringskode(verdi: String): Boolean = verdi.matches(Regex("^[0-9]{5}$"))
}

data class Næringsbeskrivelse(val kode: String, val beskrivelse: String)

class Næringsbeskrivelser {

    companion object {

        val næringsbeskrivelser = listOf(
            Næringsbeskrivelse(
                kode = "01",
                beskrivelse = "Jordbruk og tjenester tilknyttet jordbruk, jakt og viltstell"
            ),
            Næringsbeskrivelse(
                kode = "01",
                beskrivelse = "Jordbruk og tjenester tilknyttet jordbruk, jakt og viltstell"
            ),
            Næringsbeskrivelse(kode = "02", beskrivelse = "Skogbruk og tjenester tilknyttet skogbruk"),
            Næringsbeskrivelse(kode = "03", beskrivelse = "Fiske, fangst og akvakultur"),
            Næringsbeskrivelse(kode = "05", beskrivelse = "Bryting av steinkull og brunkull"),
            Næringsbeskrivelse(kode = "06", beskrivelse = "Utvinning av råolje og naturgass"),
            Næringsbeskrivelse(kode = "07", beskrivelse = "Bryting av metallholdig malm"),
            Næringsbeskrivelse(kode = "08", beskrivelse = "Bryting og bergverksdrift ellers"),
            Næringsbeskrivelse(kode = "09", beskrivelse = "Tjenester tilknyttet bergverksdrift og utvinning"),
            Næringsbeskrivelse(kode = "10", beskrivelse = "Produksjon av nærings- og nytelsesmidler"),
            Næringsbeskrivelse(kode = "11", beskrivelse = "Produksjon av drikkevarer"),
            Næringsbeskrivelse(kode = "12", beskrivelse = "Produksjon av tobakksvarer"),
            Næringsbeskrivelse(kode = "13", beskrivelse = "Produksjon av tekstiler"),
            Næringsbeskrivelse(kode = "14", beskrivelse = "Produksjon av klær"),
            Næringsbeskrivelse(kode = "15", beskrivelse = "Produksjon av lær og lærvarer"),
            Næringsbeskrivelse(
                kode = "16",
                beskrivelse = "Produksjon av trelast og varer av tre, kork, strå og flettematerialer, unntatt møbler"
            ),
            Næringsbeskrivelse(kode = "17", beskrivelse = "Produksjon av papir og papirvarer"),
            Næringsbeskrivelse(kode = "18", beskrivelse = "Trykking og reproduksjon av innspilte opptak"),
            Næringsbeskrivelse(kode = "19", beskrivelse = "Produksjon av kull- og raffinerte petroleumsprodukter"),
            Næringsbeskrivelse(kode = "20", beskrivelse = "Produksjon av kjemikalier og kjemiske produkter"),
            Næringsbeskrivelse(kode = "21", beskrivelse = "Produksjon av farmasøytiske råvarer og preparater"),
            Næringsbeskrivelse(kode = "22", beskrivelse = "Produksjon av gummi- og plastprodukter"),
            Næringsbeskrivelse(kode = "23", beskrivelse = "Produksjon av andre ikke-metallholdige mineralprodukter"),
            Næringsbeskrivelse(kode = "24", beskrivelse = "Produksjon av metaller"),
            Næringsbeskrivelse(kode = "25", beskrivelse = "Produksjon av metallvarer, unntatt maskiner og utstyr"),
            Næringsbeskrivelse(
                kode = "26",
                beskrivelse = "Produksjon av datamaskiner og elektroniske og optiske produkter"
            ),
            Næringsbeskrivelse(kode = "27", beskrivelse = "Produksjon av elektrisk utstyr"),
            Næringsbeskrivelse(
                kode = "28",
                beskrivelse = "Produksjon av maskiner og utstyr til generell bruk, ikke nevnt annet sted"
            ),
            Næringsbeskrivelse(kode = "29", beskrivelse = "Produksjon av motorvogner og tilhengere"),
            Næringsbeskrivelse(kode = "30", beskrivelse = "Produksjon av andre transportmidler"),
            Næringsbeskrivelse(kode = "31", beskrivelse = "Produksjon av møbler"),
            Næringsbeskrivelse(kode = "32", beskrivelse = "Annen industriproduksjon"),
            Næringsbeskrivelse(kode = "33", beskrivelse = "Reparasjon og installasjon av maskiner og utstyr"),
            Næringsbeskrivelse(kode = "35", beskrivelse = "Elektrisitets-, gass-, damp- og varmtvannsforsyning"),
            Næringsbeskrivelse(kode = "36", beskrivelse = "Uttak fra kilde, rensing og distribusjon av vann"),
            Næringsbeskrivelse(kode = "37", beskrivelse = "Oppsamling og behandling av avløpsvann"),
            Næringsbeskrivelse(
                kode = "38",
                beskrivelse = "Innsamling, behandling, disponering og gjenvinning av avfall"
            ),
            Næringsbeskrivelse(kode = "39", beskrivelse = "Miljørydding, miljørensing og lignende virksomhet"),
            Næringsbeskrivelse(kode = "41", beskrivelse = "Oppføring av bygninger"),
            Næringsbeskrivelse(kode = "42", beskrivelse = "Anleggsvirksomhet"),
            Næringsbeskrivelse(kode = "43", beskrivelse = "Spesialisert bygge- og anleggsvirksomhet"),
            Næringsbeskrivelse(kode = "45", beskrivelse = "Handel med og reparasjon av motorvogner"),
            Næringsbeskrivelse(kode = "46", beskrivelse = "Agentur- og engroshandel, unntatt med motorvogner"),
            Næringsbeskrivelse(kode = "47", beskrivelse = "Detaljhandel, unntatt med motorvogner"),
            Næringsbeskrivelse(kode = "49", beskrivelse = "Landtransport og rørtransport"),
            Næringsbeskrivelse(kode = "50", beskrivelse = "Sjøfart"),
            Næringsbeskrivelse(kode = "51", beskrivelse = "Lufttransport"),
            Næringsbeskrivelse(kode = "52", beskrivelse = "Lagring og andre tjenester tilknyttet transport"),
            Næringsbeskrivelse(kode = "53", beskrivelse = "Post og distribusjonsvirksomhet"),
            Næringsbeskrivelse(kode = "55", beskrivelse = "Overnattingsvirksomhet"),
            Næringsbeskrivelse(kode = "56", beskrivelse = "Serveringsvirksomhet"),
            Næringsbeskrivelse(kode = "58", beskrivelse = "Forlagsvirksomhet"),
            Næringsbeskrivelse(
                kode = "59",
                beskrivelse = "Film-, video- og fjernsynsprogramproduksjon, utgivelse av musikk- og lydopptak"
            ),
            Næringsbeskrivelse(kode = "60", beskrivelse = "Radio- og fjernsynskringkasting"),
            Næringsbeskrivelse(kode = "61", beskrivelse = "Telekommunikasjon"),
            Næringsbeskrivelse(kode = "62", beskrivelse = "Tjenester tilknyttet informasjonsteknologi"),
            Næringsbeskrivelse(kode = "63", beskrivelse = "Informasjonstjenester"),
            Næringsbeskrivelse(kode = "64", beskrivelse = "Finansieringsvirksomhet"),
            Næringsbeskrivelse(
                kode = "65",
                beskrivelse = "Forsikringsvirksomhet og pensjonskasser, unntatt trygdeordninger underlagt offentlig forvaltning"
            ),

            Næringsbeskrivelse(
                kode = "66",
                beskrivelse = "Tjenester tilknyttet finansierings- og forsikringsvirksomhet"
            ),
            Næringsbeskrivelse(kode = "68", beskrivelse = "Omsetning og drift av fast eiendom"),
            Næringsbeskrivelse(kode = "69", beskrivelse = "Juridisk og regnskapsmessig tjenesteyting"),
            Næringsbeskrivelse(kode = "70", beskrivelse = "Hovedkontortjenester, administrativ rådgivning"),
            Næringsbeskrivelse(
                kode = "71",
                beskrivelse = "Arkitektvirksomhet og teknisk konsulentvirksomhet, og teknisk prøving og analyse"
            ),
            Næringsbeskrivelse(kode = "72", beskrivelse = "Forskning og utviklingsarbeid"),
            Næringsbeskrivelse(kode = "73", beskrivelse = "Annonse- og reklamevirksomhet og markedsundersøkelser"),
            Næringsbeskrivelse(kode = "74", beskrivelse = "Annen faglig, vitenskapelig og teknisk virksomhet"),
            Næringsbeskrivelse(kode = "75", beskrivelse = "Veterinærtjenester"),
            Næringsbeskrivelse(kode = "77", beskrivelse = "Utleie- og leasingvirksomhet"),
            Næringsbeskrivelse(kode = "78", beskrivelse = "Arbeidskrafttjenester"),
            Næringsbeskrivelse(
                kode = "79",
                beskrivelse = "Reisebyrå- og reisearrangørvirksomhet og tilknyttede tjenester"
            ),
            Næringsbeskrivelse(kode = "80", beskrivelse = "Vakttjeneste og etterforsking"),
            Næringsbeskrivelse(kode = "81", beskrivelse = "Tjenester tilknyttet eiendomsdrift"),
            Næringsbeskrivelse(kode = "82", beskrivelse = "Annen forretningsmessig tjenesteyting"),
            Næringsbeskrivelse(
                kode = "84",
                beskrivelse = "Offentlig administrasjon og forsvar, og trygdeordninger underlagt offentlig forvaltning"
            ),
            Næringsbeskrivelse(kode = "85", beskrivelse = "Undervisning"),
            Næringsbeskrivelse(kode = "86", beskrivelse = "Helsetjenester"),
            Næringsbeskrivelse(kode = "87", beskrivelse = "Pleie- og omsorgstjenester i institusjon"),
            Næringsbeskrivelse(kode = "88", beskrivelse = "Sosiale omsorgstjenester uten botilbud"),
            Næringsbeskrivelse(kode = "90", beskrivelse = "Kunstnerisk virksomhet og underholdningsvirksomhet"),
            Næringsbeskrivelse(
                kode = "91",
                beskrivelse = "Drift av biblioteker, arkiver, museer og annen kulturvirksomhet"
            ),
            Næringsbeskrivelse(kode = "92", beskrivelse = "Lotteri og totalisatorspill"),
            Næringsbeskrivelse(
                kode = "93",
                beskrivelse = "Sports- og fritidsaktiviteter og drift av fornøyelsesetablissementer"
            ),
            Næringsbeskrivelse(kode = "94", beskrivelse = "Aktiviteter i medlemsorganisasjoner"),
            Næringsbeskrivelse(
                kode = "95",
                beskrivelse = "Reparasjon av datamaskiner, husholdningsvarer og varer til personlig bruk"
            ),
            Næringsbeskrivelse(kode = "96", beskrivelse = "Annen personlig tjenesteyting"),
            Næringsbeskrivelse(kode = "97", beskrivelse = "Lønnet arbeid i private husholdninger"),
            Næringsbeskrivelse(kode = "99", beskrivelse = "Internasjonale organisasjoner og organer")
        )


        fun mapTilNæringsbeskrivelse(næringskode2siffer: String): String {
            return næringsbeskrivelser.find { beskrivelse -> beskrivelse.kode == næringskode2siffer }
                .let {
                    it?.beskrivelse
                } ?: "Ingen beskrivelse funnet for kode '$næringskode2siffer'"
        }

    }
}