package no.nav.arbeidsgiver.iatjenester.metrikker.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.arbeidsgiver.iatjenester.metrikker.domene.Fylke
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.InnloggetMottattIaTjenesteMedVirksomhetGrunndata
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.UinnloggetMottattIaTjeneste
import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.stereotype.Component

@Component
class IaTjenesterMetrikkerService(
    private val iaTjenesterMetrikkerRepository: IaTjenesterMetrikkerRepository,
    private val meterRegistry: MeterRegistry
) {

    fun sjekkOgPersister(innloggetIaTjeneste: InnloggetMottattIaTjenesteMedVirksomhetGrunndata): InnloggetMottattIaTjenesteMedVirksomhetGrunndata {

        innloggetIaTjeneste.fylke =
            Fylke.fraKommunenummer(innloggetIaTjeneste.kommunenummer).navn

        iaTjenesterMetrikkerRepository.persister(innloggetIaTjeneste)
        log("sjekkOgPersister()").info(
            "IA Tjeneste av type '${innloggetIaTjeneste.type.name}' " +
                "fra kilde '${innloggetIaTjeneste.kilde.name}' " +
                "og sektor '${innloggetIaTjeneste.SSBSektorKodeBeskrivelse}' " +
                "opprettet"
        )

        meterRegistry.counter("counted.innlogget.mottatt.iatjeneste").increment()
        return innloggetIaTjeneste;
    }

    fun persister(uinnloggetIaTjeneste: UinnloggetMottattIaTjeneste) {
        iaTjenesterMetrikkerRepository.persister(uinnloggetIaTjeneste)
        with(uinnloggetIaTjeneste) {
            log("sjekkOgPersister()").info(
                "IA Tjeneste (uinnlogget) av type '${type.name}' fra kilde '${kilde.name}' opprettet"
            )
        }

        meterRegistry.counter("counted.uinnlogget.mottatt.iatjeneste").increment()
    }
}
