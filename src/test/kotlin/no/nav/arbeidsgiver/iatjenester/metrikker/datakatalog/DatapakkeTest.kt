package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test


internal class DatapakkeTest {

    @Test
    fun `Felter xAxis og yAxis i Option skal ha en stor 'X'`() {
        val option = Option(
            legend = Legend(listOf("item1")),
            xAxis = Xaxis("The X axis", listOf("field 1", "field2", "field3")),
            yAxis = Yaxis("The Y axis"),
            series = listOf(Serie("My serie", listOf(1, 2, 3, 4, 5), "bar", "The Serie"))
        )

        val json = jacksonObjectMapper().writeValueAsString(option)

        Assertions.assertThat(json).isEqualTo(expectedJson())
    }

    fun expectedJson(): String {
        return "{" +
                "\"legend\":{\"data\":[\"item1\"]}," +
                "\"xAxis\":{\"type\":\"The X axis\",\"data\":[\"field 1\",\"field2\",\"field3\"]}," +
                "\"yAxis\":{\"type\":\"The Y axis\"}," +
                "\"series\":" +
                "[" +
                "{\"name\":\"My serie\",\"data\":[1,2,3,4,5],\"type\":\"bar\",\"title\":\"The Serie\"}" +
                "]" +
                "}"
    }
}
