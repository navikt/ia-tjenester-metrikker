package no.nav.arbeidsgiver.iatjenester.metrikker.tilgangskontroll

import com.fasterxml.jackson.annotation.JsonValue
import java.util.*
import java.util.stream.Collectors

class InnloggetBruker(val fnr: Fnr) {

    var organisasjoner: List<AltinnOrganisasjon> = ArrayList<AltinnOrganisasjon>()


    fun harTilgang(orgnr: Orgnr): Boolean {
        val orgnumreBrukerHarTilgangTil = organisasjoner
            .stream()
            .filter { obj: AltinnOrganisasjon? ->
                Objects.nonNull(
                    obj
                )
            }
            .map { org: AltinnOrganisasjon -> org.organizationNumber }
            .collect(Collectors.toList<Any>())
        return orgnumreBrukerHarTilgangTil.contains(orgnr.verdi)
    }

}

data class Orgnr(val verdi: String)

data class AltinnOrganisasjon(
    var name: String? = null,
    var parentOrganizationNumber: String? = null,
    var organizationNumber: String? = null,
    var organizationForm: String? = null,
    var status: String? = null,
    var type: String? = null
)

class Fnr(verdi: String) {
    private val verdi: String

    @JsonValue
    fun asString(): String {
        return verdi
    }

    companion object {
        fun erGyldigFnr(fnr: String): Boolean {
            return fnr.matches(Regex("^[0-9]{11}$"))
        }
    }

    init {
        if (!erGyldigFnr(verdi)) {
            throw RuntimeException("Ugyldig fødselsnummer. Må bestå av 11 tegn.")
        }
        this.verdi = verdi
    }
}

