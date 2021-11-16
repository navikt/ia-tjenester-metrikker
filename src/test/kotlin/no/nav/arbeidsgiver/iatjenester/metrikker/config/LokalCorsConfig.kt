package no.nav.arbeidsgiver.iatjenester.metrikker.config

import no.nav.arbeidsgiver.iatjenester.metrikker.utils.log
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.CorsRegistry

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Profile("local")
@Configuration
class LokalCorsConfig {
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                log("main()").warn("***OBS*** Åpner CORS til http://localhost:3000, denne meldingen skal bare vises lokalt")
                registry.addMapping("/uinnlogget/mottatt-iatjeneste").allowedOrigins("http://localhost:3000")
                registry.addMapping("/innlogget/mottatt-iatjeneste").allowedOrigins("http://localhost:3000")
            }
        }
    }
}
