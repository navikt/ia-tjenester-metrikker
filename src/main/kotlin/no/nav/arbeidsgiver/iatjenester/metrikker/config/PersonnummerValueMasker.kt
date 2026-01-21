package no.nav.arbeidsgiver.iatjenester.metrikker.config

import net.logstash.logback.mask.ValueMasker
import tools.jackson.core.TokenStreamContext

class PersonnummerValueMasker : ValueMasker {
    override fun mask(
        context: TokenStreamContext?,
        value: Any?,
    ): Any? {
        if (value is CharSequence) {
            return value.replace(Regex("\\d{11}"), "***********")
        }
        return null
    }
}
