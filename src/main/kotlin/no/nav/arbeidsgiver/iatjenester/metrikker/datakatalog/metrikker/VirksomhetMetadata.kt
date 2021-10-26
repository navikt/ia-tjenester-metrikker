package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Fylke
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