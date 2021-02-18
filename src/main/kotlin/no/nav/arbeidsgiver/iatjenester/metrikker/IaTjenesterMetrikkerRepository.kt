package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import java.sql.Connection
import javax.sql.DataSource

class IaTjenesterMetrikkerRepository(private val datasource: DataSource) : DatabaseInterface {

    override val connection: Connection
        get() = datasource.connection
}


fun Connection.opprett(iatjeneste: IaTjeneste) {
    use { connection ->
        insertIaTjeneste(connection, iatjeneste)
        connection.commit()
    }
}

private fun insertIaTjeneste(connection: Connection, iatjeneste: IaTjeneste) {
    connection.prepareStatement(
        """
                INSERT INTO metrikker_ia_tjenester_innlogget(orgnr, naering_kode_5siffer, form_av_tjeneste, kilde_applikasjon) 
                   VALUES  (?, ?, ?, ?)
                   """
    ).use {
        it.setObject(1, iatjeneste.orgnr)
        it.setObject(2, iatjeneste.næringKode5Siffer)
        it.setObject(3, iatjeneste.type.name)
        it.setObject(4, iatjeneste.kilde.name)
        it.executeUpdate()
    }
}

interface DatabaseInterface {
    val connection: Connection
}