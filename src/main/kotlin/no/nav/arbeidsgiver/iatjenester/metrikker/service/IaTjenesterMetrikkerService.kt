package no.nav.arbeidsgiver.iatjenester.metrikker.service

import arrow.core.Either
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.Fylke
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjenesteMedVirksomhetGrunndata
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.MottattIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.UinnloggetMottattIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now
import io.micrometer.core.instrument.MeterRegistry

@Component
class IaTjenesterMetrikkerService(private val iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository, private val meterRegistry: MeterRegistry) {

    fun sjekkOgPersister(innloggetIaTjeneste: InnloggetMottattIaTjenesteMedVirksomhetGrunndata): Either<IaTjenesterMetrikkerValideringException, MottattIaTjeneste> {
        val iaTjenesteSjekkResultat = validerMottakelsesdato(innloggetIaTjeneste)

        if (iaTjenesteSjekkResultat is Either.Right) {

            innloggetIaTjeneste.fylke =
                Fylke.fraKommunenummer(innloggetIaTjeneste.kommunenummer).navn

            iaTjenesterMetrikkerRepository.persister(innloggetIaTjeneste)
            log("sjekkOgPersister()").info(
                "IA Tjeneste av type '${innloggetIaTjeneste.type.name}' " +
                        "fra kilde '${innloggetIaTjeneste.kilde.name}' " +
                        "og sektor '${innloggetIaTjeneste.SSBSektorKodeBeskrivelse}' " +
                        "opprettet"
            )
        }

        meterRegistry.counter("counted.innlogget.mottatt.iatjeneste").increment()
        return iaTjenesteSjekkResultat
    }

    fun sjekkOgPersister(uinnloggetIaTjeneste: UinnloggetMottattIaTjeneste): Either<IaTjenesterMetrikkerValideringException, MottattIaTjeneste> {
        val iaTjenesteSjekkResultat = validerMottakelsesdato(uinnloggetIaTjeneste)

        if (iaTjenesteSjekkResultat is Either.Right) {
            iaTjenesterMetrikkerRepository.persister(uinnloggetIaTjeneste)
            log("sjekkOgPersister()").info(
                "IA Tjeneste (uinnlogget) av type '${uinnloggetIaTjeneste.type.name}' " +
                        "fra kilde '${uinnloggetIaTjeneste.kilde.name}' " +
                        "opprettet"
            )
        }

        meterRegistry.counter("counted.uinnlogget.mottatt.iatjeneste").increment()
        return iaTjenesteSjekkResultat
    }


    private fun validerMottakelsesdato(mottattIaTjeneste: MottattIaTjeneste): Either<IaTjenesterMetrikkerValideringException, MottattIaTjeneste> {
        val muligTidsforskjellMellomServerneiMinutter: Long = 1

        return if (mottattIaTjeneste.tjenesteMottakkelsesdato.toLocalDateTime()
                .isAfter(now().plusMinutes(muligTidsforskjellMellomServerneiMinutter))
        ) {
            Either.Left(IaTjenesterMetrikkerValideringException("tjenesteMottakkelsesdato kan ikke være i fremtiden"))
        } else {
            Either.Right(mottattIaTjeneste)
        }
    }
}
