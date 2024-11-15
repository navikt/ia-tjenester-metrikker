package no.nav.arbeidsgiver.iatjenester.metrikker.utils

import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.MottattIaTjenesteMedVirksomhetGrunndata

private const val MAKSIMUM_KOMMUNE_NR = 9999
private const val MAKSIMUM_SSBSEKTORKODE = 9000
private const val MAKSIMUM_ANTALL_KARAKTERER_TILLATT = 512

fun erOrgnrGyldig(innloggetIaTjenesteMedVirksomhetGrunndata: MottattIaTjenesteMedVirksomhetGrunndata): Boolean {
    if (innloggetIaTjenesteMedVirksomhetGrunndata.orgnr.length != 9) {
        log("")
            .warn("Ugyldig orgnr mottatt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjenesteMedVirksomhetGrunndata.næringKode5Siffer.length != 5 ||
        innloggetIaTjenesteMedVirksomhetGrunndata.næringKode5Siffer.toIntOrNull() == null
    ) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("Ugyldig næringskode5siffer mottatt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjenesteMedVirksomhetGrunndata.kommunenummer.toIntOrNull() == null ||
        innloggetIaTjenesteMedVirksomhetGrunndata.kommunenummer.toInt() > MAKSIMUM_KOMMUNE_NR
    ) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("Ugyldig kommunenummer mottatt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjenesteMedVirksomhetGrunndata.SSBSektorKode.toIntOrNull() == null ||
        innloggetIaTjenesteMedVirksomhetGrunndata.SSBSektorKode.toInt() > MAKSIMUM_SSBSEKTORKODE
    ) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("Ugyldig SSB sektorkode mottatt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (
        innloggetIaTjenesteMedVirksomhetGrunndata.næringskode5SifferBeskrivelse.length > MAKSIMUM_ANTALL_KARAKTERER_TILLATT
    ) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("For lang beskrivelse for næringskode 5 siffer felt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjenesteMedVirksomhetGrunndata.næring2SifferBeskrivelse.length > MAKSIMUM_ANTALL_KARAKTERER_TILLATT) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("For lang beskrivelse for næringskode 2siffer felt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjenesteMedVirksomhetGrunndata.SSBSektorKodeBeskrivelse.length > MAKSIMUM_ANTALL_KARAKTERER_TILLATT) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("For lang beskrivelse for SSB sektorkode felt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjenesteMedVirksomhetGrunndata.fylke.length > MAKSIMUM_ANTALL_KARAKTERER_TILLATT) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("For lang beskrivelse for fylke felt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    if (innloggetIaTjenesteMedVirksomhetGrunndata.kommune.length > MAKSIMUM_ANTALL_KARAKTERER_TILLATT) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("For lang beskrivelse for kommune felt fra innlogget tjeneste, avslutter registrering")
        return false
    }

    return true
}

fun erOrgnrGyldig(innloggetIaTjeneste: InnloggetMottattIaTjeneste): Boolean {
    if (innloggetIaTjeneste.orgnr.length != 9) {
        log("IaTjenesterMetrikkerInnloggetController")
            .warn("Ugyldig orgnr mottatt fra innlogget tjeneste, avslutter registrering")
        return false
    }
    return true
}
