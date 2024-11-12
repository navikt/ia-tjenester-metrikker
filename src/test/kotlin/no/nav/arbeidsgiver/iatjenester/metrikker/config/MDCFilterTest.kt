package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

class MDCFilterTest {
    @RestController
    private class TestController {
        @GetMapping("/test")
        fun test(): ResponseEntity<String> {
            if (MDC.get("Nav-CallId").isNullOrEmpty()) return ResponseEntity.internalServerError().build()
            if (MDC.get("correlationId").isNullOrEmpty()) return ResponseEntity.internalServerError().build()
            return ResponseEntity.ok("Test Controller")
        }

        @GetMapping("/testCallId")
        fun testCallId(
            @RequestParam callIdToTestAgainst: String,
        ): ResponseEntity<String> {
            if (!MDC.get("Nav-CallId").equals(callIdToTestAgainst)) return ResponseEntity.internalServerError().build()
            if (MDC.get("correlationId").isNullOrEmpty()) return ResponseEntity.internalServerError().build()
            return ResponseEntity.ok("Test Controller")
        }

        @GetMapping("/testCorrelationId")
        fun testCorrelationId(
            @RequestParam correlationIdToTestAgainst: String,
        ): ResponseEntity<String> {
            if (MDC.get("Nav-CallId").isNullOrEmpty()) return ResponseEntity.internalServerError().build()
            if (!MDC.get("correlationId").equals(correlationIdToTestAgainst)) return ResponseEntity.internalServerError().build()
            return ResponseEntity.ok("Test Controller")
        }
    }

    @Test
    fun `skal generere correlation og callId i MDC hvis det ikke kommer med headers`() {
        val mockMvc = MockMvcBuilders
            .standaloneSetup(TestController())
            .addFilter<StandaloneMockMvcBuilder>(MDCFilter())
            .build()

        mockMvc
            .perform(MockMvcRequestBuilders.get("/test"))
            .andExpect(status().isOk)
    }

    @Test
    fun `skal legge til callID i MDC hvis det kommer med headers`() {
        val mockMvc = MockMvcBuilders
            .standaloneSetup(TestController())
            .addFilter<StandaloneMockMvcBuilder>(MDCFilter())
            .build()

        val uuid = UUID.randomUUID().toString()
        val differentUuid = UUID.randomUUID().toString()

        mockMvc
            .perform(MockMvcRequestBuilders.get("/testCallId?callIdToTestAgainst=$uuid").header("Nav-CallId", uuid))
            .andExpect(status().isOk)

        mockMvc
            .perform(MockMvcRequestBuilders.get("/testCallId?callIdToTestAgainst=$uuid").header("Nav-CallId", differentUuid))
            .andExpect(status().isInternalServerError)
    }

    @Test
    fun `skal legge til correlationId i MDC hvis det kommer med headers`() {
        val mockMvc = MockMvcBuilders
            .standaloneSetup(TestController())
            .addFilter<StandaloneMockMvcBuilder>(MDCFilter())
            .build()

        val uuid = UUID.randomUUID().toString()
        val differentUuid = UUID.randomUUID().toString()

        mockMvc
            .perform(MockMvcRequestBuilders.get("/testCorrelationId?correlationIdToTestAgainst=$uuid").header("X-Correlation-ID", uuid))
            .andExpect(status().isOk)

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/testCorrelationId?correlationIdToTestAgainst=$uuid").header("X-Correlation-ID", differentUuid),
            )
            .andExpect(status().isInternalServerError)
    }

    @Test
    fun `skal returnere correlationId i response`() {
        val mockMvc = MockMvcBuilders
            .standaloneSetup(TestController())
            .addFilter<StandaloneMockMvcBuilder>(MDCFilter())
            .build()

        val uuid = UUID.randomUUID().toString()

        mockMvc
            .perform(MockMvcRequestBuilders.get("/testCorrelationId?correlationIdToTestAgainst=$uuid").header("X-Correlation-ID", uuid))
            .andExpect(status().isOk)
            .andExpect(header().string("X-Correlation-ID", uuid))
    }
}
