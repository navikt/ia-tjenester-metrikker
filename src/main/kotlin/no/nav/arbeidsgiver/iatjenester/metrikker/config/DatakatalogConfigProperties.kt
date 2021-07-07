package no.nav.arbeidsgiver.iatjenester.metrikker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "datakatalog")
data class DatakatalogConfigProperties(
    var rootUrl: String,
    var datapakkeId: String,
    var erUtsendingAktivert: Boolean
)
