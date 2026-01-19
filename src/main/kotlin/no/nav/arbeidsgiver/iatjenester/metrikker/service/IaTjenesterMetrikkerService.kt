package no.nav.arbeidsgiver.iatjenester.metrikker.service

import arrow.core.Either
import arrow.core.left
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Fylke
import no.nav.arbeidsgiver.iatjenester.metrikker.observability.PrometheusMetrics
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.MottattIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.MottattIaTjenesteMedVirksomhetGrunndata
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now

@Component
class IaTjenesterMetrikkerService(
    private val iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository,
    private val prometheusMetrics: PrometheusMetrics,
) {
    fun sjekkOgPersister(
        innloggetIaTjeneste: MottattIaTjenesteMedVirksomhetGrunndata,
    ): Either<IaTjenesterMetrikkerValideringException, MottattIaTjeneste> {
        val iaTjenesteSjekkResultat = validerMottakelsesdato(innloggetIaTjeneste).onLeft {
            return it.left()
        }

        innloggetIaTjeneste.fylke =
            Fylke.fraKommunenummer(innloggetIaTjeneste.kommunenummer).navn

        iaTjenesterMetrikkerRepository.persister(innloggetIaTjeneste)
        log("sjekkOgPersister()").info(
            "IA Tjeneste av type '${innloggetIaTjeneste.type.name}' " +
                "fra kilde '${innloggetIaTjeneste.kilde.name}' " +
                "og sektor '${innloggetIaTjeneste.SSBSektorKodeBeskrivelse}' " +
                "opprettet",
        )

        prometheusMetrics.inkrementerInnloggedeMetrikkerPersistert(innloggetIaTjeneste.kilde)

        return iaTjenesteSjekkResultat
    }

    private fun validerMottakelsesdato(
        mottattIaTjeneste: MottattIaTjeneste,
    ): Either<IaTjenesterMetrikkerValideringException, MottattIaTjeneste> {
        val muligTidsforskjellMellomServerneiMinutter: Long = 1

        return if ((mottattIaTjeneste.tjenesteMottakkelsesdato?.toLocalDateTime() ?: now())
                .isAfter(now().plusMinutes(muligTidsforskjellMellomServerneiMinutter))
        ) {
            Either.Left(IaTjenesterMetrikkerValideringException("tjenesteMottakkelsesdato kan ikke være i fremtiden"))
        } else {
            Either.Right(mottattIaTjeneste)
        }
    }
}
