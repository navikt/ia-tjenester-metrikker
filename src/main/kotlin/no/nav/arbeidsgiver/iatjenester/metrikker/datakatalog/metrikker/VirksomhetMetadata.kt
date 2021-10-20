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
    val kommune: String,
    val kommunenummer: String,
    val fylke: Fylke,
    val antallAnsatte: Int
) : Virksomhet

data class InstitusjonellSektorkode(val kode: String, val beskrivelse: String)

enum class Fylke(val navn: String, val nummer: String) {
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
    TROMS_OG_FINNMARK("Troms og Finnmark", "54"),

    UKJENT("Ukjent", "Ukjent");

    companion object {
        fun fraKommunenummer(kommunenummer: String): Fylke {
            if (kommunenummer.length != 4) return UKJENT
            return values().find { it.nummer == kommunenummer.substring(0, 2) } ?: UKJENT
        }
    }
}

fun alleFylkerAlfabetisk() = Fylke.values().sortedBy { it.navn }.map { it.navn }
