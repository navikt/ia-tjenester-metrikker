package no.nav.arbeidsgiver.iatjenester.metrikker.utils

import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetIaTjenesteKunOrgnr

private val MAKSIMUM_KOMMUNE_NR = 5444
private val MAKSIMUM_FYLKE_NR = 54
private val MAKSIMUM_SSBSEKTORKODE = 9000
private val MAKSIMUM_ANTALL_KARAKTERERTILLATT = 512

fun sjekkDataKvalitet(innloggetIaTjeneste: InnloggetIaTjeneste)
        : Boolean {
    if (innloggetIaTjeneste.orgnr.length != 9) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("Ugyldig orgnr mottatt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjeneste.næringKode5Siffer.length != 5 ||
        innloggetIaTjeneste.næringKode5Siffer.toIntOrNull() == null
    ) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("Ugyldig næringskode5siffer mottatt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjeneste.kommunenummer.toIntOrNull() == null ||
        innloggetIaTjeneste.kommunenummer.toInt() > MAKSIMUM_KOMMUNE_NR
    ) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("Ugyldig kommunenummer mottatt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjeneste.SSBSektorKode.toIntOrNull() == null ||
        innloggetIaTjeneste.SSBSektorKode.toInt() > MAKSIMUM_SSBSEKTORKODE
    ) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("Ugyldig SSB sektorkode mottatt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (
        innloggetIaTjeneste.næringskode5SifferBeskrivelse.length > MAKSIMUM_ANTALL_KARAKTERERTILLATT) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("For lang beskrivelse for næringskode 5 siffer felt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjeneste.næring2SifferBeskrivelse.length > MAKSIMUM_ANTALL_KARAKTERERTILLATT) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("For lang beskrivelse for næringskode 2siffer felt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjeneste.SSBSektorKodeBeskrivelse.length > MAKSIMUM_ANTALL_KARAKTERERTILLATT) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("For lang beskrivelse for SSB sektorkode felt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjeneste.fylke.length > MAKSIMUM_ANTALL_KARAKTERERTILLATT) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("For lang beskrivelse for fylke felt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjeneste.kommune.length > MAKSIMUM_ANTALL_KARAKTERERTILLATT) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("For lang beskrivelse for kommune felt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    return true
}

fun sjekkDataKvalitet (innloggetIaTjenesteKunOrgnr: InnloggetIaTjenesteKunOrgnr): Boolean {
    if (innloggetIaTjenesteKunOrgnr.orgnr.length != 9) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("Ugyldig orgnr mottatt fra innlogget tjeneste, avslutter registrering")
        return false
    }
return true
}
