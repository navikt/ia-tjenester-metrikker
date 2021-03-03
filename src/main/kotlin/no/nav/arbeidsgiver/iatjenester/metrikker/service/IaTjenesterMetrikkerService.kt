package no.nav.arbeidsgiver.iatjenester.metrikker.service

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now

@Component
class IaTjenesterMetrikkerService(private val iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository) {

    fun sjekkOgOpprett(iaTjeneste: IaTjeneste): Boolean {
        sjekk(iaTjeneste)
        iaTjenesterMetrikkerRepository.opprett(iaTjeneste)
        log("sjekkOgOpprett()").info(
            "IA Tjeneste av type '${iaTjeneste.type.name}' " +
                    "fra kilde '${iaTjeneste.kilde.name}' " +
                    "og sektor '${iaTjeneste.SSBSektorKodeBeskrivelse}' " +
                    "opprettet"
        )
        return true
    }

    private fun sjekk(iaTjeneste: IaTjeneste) {
        val muligDeltaMellomServerneiMinutter: Long = 1

        if (iaTjeneste.tjenesteMottakkelsesdato.toLocalDateTime()
                .isAfter(now().plusMinutes(muligDeltaMellomServerneiMinutter))
        ) {
            throw IaTjenesterMetrikkerValideringException("tjenesteMottakkelsesdato kan ikke være i fremtiden")
        }
    }

}