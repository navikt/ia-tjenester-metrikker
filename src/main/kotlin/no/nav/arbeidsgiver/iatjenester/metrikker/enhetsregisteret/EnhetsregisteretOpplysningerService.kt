package no.nav.arbeidsgiver.iatjenester.metrikker.enhetsregisteret

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.metrikker.Underenhet
import no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll.Orgnr
import org.springframework.stereotype.Component

@Component
class EnhetsregisteretOpplysningerService(private val enhetsregisteretClient: EnhetsregisteretClient) {

    fun hentOpplysninger(orgnr: Orgnr): Either<EnhetsregisteretException, Underenhet> {

        return try {
            val underenhet: Underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr)
            Either.Right(underenhet)
        } catch (e: Exception) {
            Either.Left(EnhetsregisteretException(e.message, e))
        }
    }

}