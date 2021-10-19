package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næringskode5Siffer
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr


interface Virksomhet {
    val orgnr: Orgnr
    val navn: String
    val næringskode: Næringskode5Siffer
}

data class OverordnetEnhet(
    override var orgnr: Orgnr,
    override val navn: String,
    override val næringskode: Næringskode5Siffer,
    val institusjonellSektorkode: InstitusjonellSektorkode,
    val antallAnsatte: Int
) : Virksomhet

data class Underenhet(
    override var orgnr: Orgnr,
    override val navn: String,
    override val næringskode: Næringskode5Siffer,
    val overordnetEnhetOrgnr: Orgnr,
    val kommune: Kommune,
    val fylke: Fylke,
    val antallAnsatte: Int
) : Virksomhet

data class InstitusjonellSektorkode(val kode: String, val beskrivelse: String)

data class Fylke(val nummer: String, val navn: String)
data class Kommune(val nummer: String, val navn: String)

enum class Fylker(val navn: String, val nummer: String) {
    OSLO("Oslo", "03"),
    ROGALAND("Rogaland", "11"),
    MØRE_OG_ROMSDAL("Møre og Romsdal", "15"),
    NORDLAND("Nordland", "18"),
    VIKEN("Viken", "30"),
    INNLANDET("Innlandet", "34"),
    VESTFOLD_OG_TELEMARK("Vestfold og Telemark", "38"),
    AGDER("Agder", "42"),
    VESTLAND("Vestland", "46"),
    TRØNDELAG("Trøndelag", "50"),
    TROMS_OG_FINNMARK("Troms og Finnmark", "54")
}

fun alleFylkerAlfabetisk() = Fylker.values().sortedBy { it.navn }.map { it.navn }

fun mapTilFylke(kommune: Kommune): Fylke {

    val utledetFylkesnummer: String =
        if (kommune.nummer.length != 4)
            "IKKE_GYLDIG_KOMMUNENUMMER"
        else
            kommune.nummer.substring(0, 2)

    return when (utledetFylkesnummer) {
        "03" -> Fylke("03", "Oslo")
        "11" -> Fylke("11", "Rogaland")
        "15" -> Fylke("15", "Møre og Romsdal")
        "18" -> Fylke("18", "Nordland")
        "30" -> Fylke("30", "Viken")
        "34" -> Fylke("34", "Innlandet")
        "38" -> Fylke("38", "Vestfold og Telemark")
        "42" -> Fylke("42", "Agder")
        "46" -> Fylke("46", "Vestland")
        "50" -> Fylke("50", "Trøndelag")
        "54" -> Fylke("54", "Troms og Finnmark")

        else -> Fylke("UKJENT", "UKJENT")
    }
}
