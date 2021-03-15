package no.nav.arbeidsgiver.iatjenester.metrikker.config

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc", "springfox.documentation.swagger.web.ApiResourceController", "org.springframework"])
@ConfigurationProperties(prefix = "no.nav.security.jwt")
@Configuration
class SecurityConfig