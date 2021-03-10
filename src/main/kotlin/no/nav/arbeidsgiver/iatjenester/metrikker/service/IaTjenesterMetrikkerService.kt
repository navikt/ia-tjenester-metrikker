package no.nav.arbeidsgiver.iatjenester.metrikker.service

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.UinnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now

@Component
class IaTjenesterMetrikkerService(private val iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository) {

    fun sjekkOgOpprett(innloggetIaTjeneste: InnloggetIaTjeneste): Boolean {
        sjekk(innloggetIaTjeneste)
        iaTjenesterMetrikkerRepository.opprett(innloggetIaTjeneste)
        log("sjekkOgOpprett()").info(
            "IA Tjeneste av type '${innloggetIaTjeneste.type.name}' " +
                    "fra kilde '${innloggetIaTjeneste.kilde.name}' " +
                    "og sektor '${innloggetIaTjeneste.SSBSektorKodeBeskrivelse}' " +
                    "opprettet"
        )

        return true
    }

    fun sjekkOgOpprett(uinnloggetIaTjeneste: UinnloggetIaTjeneste): Boolean {
        sjekk(uinnloggetIaTjeneste)
        iaTjenesterMetrikkerRepository.opprett(uinnloggetIaTjeneste)
        log("sjekkOgOpprett()").info(
            "IA Tjeneste (uinnlogget) av type '${uinnloggetIaTjeneste.type.name}' " +
                    "fra kilde '${uinnloggetIaTjeneste.kilde.name}' " +
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