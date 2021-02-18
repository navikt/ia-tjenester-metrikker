package no.nav.arbeidsgiver.iatjenester.metrikker

import no.nav.arbeidsgiver.iatjenester.metrikker.domene.IaTjeneste
import java.sql.Connection
import javax.sql.DataSource

class IaTjenesterMetrikkerRepository(val datasource: DataSource): DatabaseInterfacePostgres {

    override val connection: Connection
        get() = datasource.connection
}


fun Connection.opprett(iatjeneste: IaTjeneste) {
    use { connection ->
        insertIaTjeneste(connection, iatjeneste)
        connection.commit()
    }
}

fun insertIaTjeneste(connection : Connection, iatjeneste: IaTjeneste) {
    connection.prepareStatement(
        """
                INSERT INTO metrikker_ia_tjenester_innlogget(id, sykmelding) 
                   VALUES  (?, ?)
                   """
    ).use {
        // it.setString(1, iatjeneste.id)  --> TODO: trenger vi ID?
        it.setObject(2, iatjeneste.orgnr)
        it.setObject(3, iatjeneste.næringKode5Siffer)
        it.setObject(4, iatjeneste.type.name)
        it.setObject(5, iatjeneste.kilde)
        it.executeUpdate()
    }
}

interface DatabaseInterfacePostgres {
    val connection: Connection
}