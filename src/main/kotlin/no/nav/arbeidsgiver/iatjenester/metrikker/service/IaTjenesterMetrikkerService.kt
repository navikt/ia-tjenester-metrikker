package no.nav.arbeidsgiver.iatjenester.metrikker.service

import arrow.core.Either
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.InnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.UinnloggetIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now

@Component
class IaTjenesterMetrikkerService(private val iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository) {

    fun sjekkOgOpprett(innloggetIaTjeneste: InnloggetIaTjeneste): Either<IaTjenesterMetrikkerValideringException, IaTjeneste> {
        val iaTjenesteSjekkResultat = sjekk(innloggetIaTjeneste)

        if(iaTjenesteSjekkResultat is Either.Right) {
            iaTjenesterMetrikkerRepository.opprett(innloggetIaTjeneste)
            log("sjekkOgOpprett()").info(
                "IA Tjeneste av type '${innloggetIaTjeneste.type.name}' " +
                        "fra kilde '${innloggetIaTjeneste.kilde.name}' " +
                        "og sektor '${innloggetIaTjeneste.SSBSektorKodeBeskrivelse}' " +
                        "opprettet"
            )
        }

        return iaTjenesteSjekkResultat
    }

    fun sjekkOgOpprett(uinnloggetIaTjeneste: UinnloggetIaTjeneste): Either<IaTjenesterMetrikkerValideringException, IaTjeneste> {
        val iaTjenesteSjekkResultat = sjekk(uinnloggetIaTjeneste)

        if(iaTjenesteSjekkResultat is Either.Right) {
            iaTjenesterMetrikkerRepository.opprett(uinnloggetIaTjeneste)
            log("sjekkOgOpprett()").info(
                "IA Tjeneste (uinnlogget) av type '${uinnloggetIaTjeneste.type.name}' " +
                        "fra kilde '${uinnloggetIaTjeneste.kilde.name}' " +
                        "opprettet"
            )
        }

        return iaTjenesteSjekkResultat
    }


    private fun sjekk(iaTjeneste: IaTjeneste): Either<IaTjenesterMetrikkerValideringException, IaTjeneste> {
        val muligDeltaMellomServerneiMinutter: Long = 1

        return if (iaTjeneste.tjenesteMottakkelsesdato.toLocalDateTime()
                .isAfter(now().plusMinutes(muligDeltaMellomServerneiMinutter))
        ) {
            Either.Left(IaTjenesterMetrikkerValideringException("tjenesteMottakkelsesdato kan ikke være i fremtiden"))
        } else {
            Either.Right(iaTjeneste)
        }
    }
}