package no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret

import arrow.core.Either
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.OverordnetEnhet
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Underenhet
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr
import org.springframework.stereotype.Component
import java.security.InvalidParameterException

@Component
class EnhetsregisteretOpplysningerService(private val enhetsregisteretClient: EnhetsregisteretClient) {

    fun hentOpplysningerForUnderenhet(orgnrUnderenhet: Orgnr): Either<EnhetsregisteretException, Underenhet> {

        return try {
            val underenhet: Underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnrUnderenhet)
            Either.Right(underenhet)
        } catch (e: Exception) {
            Either.Left(EnhetsregisteretException(e.message, e))
        }
    }

    fun hentOpplysningerForOverordnetEnhet(orgnrOverordnetEnhet: Orgnr?):
            Either<EnhetsregisteretException, OverordnetEnhet> {
        orgnrOverordnetEnhet?.let {
            return try {
                val overordnetEnhet: OverordnetEnhet =
                    enhetsregisteretClient.hentInformasjonOmEnhet(orgnrOverordnetEnhet)
                Either.Right(overordnetEnhet)
            } catch (e: Exception) {
                Either.Left(EnhetsregisteretException(e.message, e))
            }
        } ?: run {
            val melding = "Parameter 'orgnr overordnet enhet' er null"
            return Either.Left(EnhetsregisteretException(melding, InvalidParameterException(melding)))
        }
    }

}