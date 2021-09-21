package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository

class VirksomhetMetadata(
    val orgnr: String,
    val næringskode2Siffer: IaTjenesterMetrikkerRepository.Næringskode2Siffer,
    val næringskode5Siffer: IaTjenesterMetrikkerRepository.Næringskode5Siffer,
    val bransje: ArbeidstilsynetBransje
) {

}