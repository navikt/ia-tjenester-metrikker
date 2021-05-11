package no.nav.arbeidsgiver.iatjenester.metrikker.config

import com.fasterxml.jackson.core.JsonStreamContext
import net.logstash.logback.mask.ValueMasker

class PersonnummerValueMasker : ValueMasker {
    override fun mask(context: JsonStreamContext?, value: Any?): Any? {
        if(value is CharSequence) {
            return value.replace(Regex("\\d{11}"), "***********")
        }
        return null
    }
}
