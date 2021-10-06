package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Næring
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr

class VirksomhetMetadata(
    val orgnr: String,
    val næring: Næring
)

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
    val overordnetEnhetOrgnr: Orgnr,
    override val navn: String,
    override val næringskode: Næringskode5Siffer,
    val antallAnsatte: Int
) : Virksomhet

data class InstitusjonellSektorkode(
    val kode: String,
    val beskrivelse: String
)
