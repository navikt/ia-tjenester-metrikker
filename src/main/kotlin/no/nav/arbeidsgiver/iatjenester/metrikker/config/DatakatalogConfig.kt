package no.nav.arbeidsgiver.iatjenester.metrikker.config

import no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog.DatakatalogKlient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class DatakatalogConfig(private val datakatalogConfigProperties: DatakatalogConfigProperties) {

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()

    @Bean fun datakatalogKlient(): DatakatalogKlient {
        return DatakatalogKlient(
            restTemplate(),
            datakatalogConfigProperties.rootUrl,
            datakatalogConfigProperties.datapakkeId,
            datakatalogConfigProperties.erUtsendingAktivert
        )
    }
}

