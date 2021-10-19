package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test


internal class DatapakkeTest {

    @Test
    fun `Felter xAxis og yAxis i Option sstarter med en liten bokstav`() {
        val option = Option(
            legend = Legend(listOf("item1")),
            grid = Grid(),
            xAxis = Xaxis("The X axis", listOf("field 1", "field2", "field3")),
            yAxis = Yaxis("The Y axis"),
            tooltip = Tooltip("item"),
            series = listOf(Serie("My serie", listOf(1, 2, 3, 4, 5), "bar", "The Serie"))
        )

        val json = jacksonObjectMapper().writeValueAsString(option)

        Assertions.assertThat(json).isEqualTo(expectedJsonForOption())
    }

    @Test
    fun `Markdown Spec har et felt som heter 'markdown' (med liten 'm')`() {
        val spec = MarkdownSpec(
            markdown = "## This is a title"
        )
        val viewWithMarkdown = View(
            title = "The title", description = "A short description", specType = SpecType.markdown, spec = spec
        )

        val json = jacksonObjectMapper().writeValueAsString(viewWithMarkdown)

        Assertions.assertThat(json).isEqualTo("{" +
                  "\"title\":\"The title\"," +
                  "\"description\":\"A short description\"," +
                  "\"specType\":\"markdown\"," +
                  "\"spec\":{\"markdown\":\"## This is a title\"}" +
                "}")
    }


    private fun expectedJsonForOption(): String {
        return "{" +
                "\"legend\":{\"data\":[\"item1\"]}," +
                "\"xAxis\":{\"type\":\"The X axis\",\"data\":[\"field 1\",\"field2\",\"field3\"]}," +
                "\"yAxis\":{\"type\":\"The Y axis\"}," +
                "\"tooltip\":{\"trigger\":\"item\"}," +
                "\"series\":" +
                "[" +
                "{\"name\":\"My serie\",\"data\":[1,2,3,4,5],\"type\":\"bar\",\"title\":\"The Serie\"}" +
                "]" +
                "}"
    }
}
