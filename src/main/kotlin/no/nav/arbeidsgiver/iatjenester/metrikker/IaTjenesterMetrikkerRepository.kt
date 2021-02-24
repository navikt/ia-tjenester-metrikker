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
                INSERT INTO metrikker_ia_tjenester_innlogget(orgnr, naering_kode_5siffer, form_av_tjeneste, kilde_applikasjon,
                tjeneste_mottakkelsesdato,
                antall_ansatte,
                naering_kode5siffer_beskrivelse,
                naerring_2siffer_beskrivelse,
                ssb_sektor_kode,
                ssb_sektor_kode_beskrivelse,
                fylkesnummer,
                fylke,
                kommunenummer,
                kommune
                ) 
                   VALUES  (?, ?, ?, ?,?, ?, ?, ?,?, ?, ?, ?,?,?)
                   """
    ).use {
        it.setObject(1, iatjeneste.orgnr)
        it.setObject(2, iatjeneste.næringKode5Siffer)
        it.setObject(3, iatjeneste.type.name)
        it.setObject(4, iatjeneste.kilde.name)
        it.setObject(5, iatjeneste.tjenesteMottakkelsesdato)
        it.setObject(6, iatjeneste.antallAnsatte)
        it.setObject(7, iatjeneste.næringskode5SifferBeskrivelse)
        it.setObject(8, iatjeneste.næring2SifferBeskrivelse)
        it.setObject(9, iatjeneste.SSBSektorKode)
        it.setObject(10, iatjeneste.SSBSektorKodeBeskrivelse)
        it.setObject(11, iatjeneste.fylkesnummer)
        it.setObject(12, iatjeneste.fylke)
        it.setObject(13, iatjeneste.kommunenummer)
        it.setObject(14, iatjeneste.kommune)
        it.executeUpdate()
    }
}

interface DatabaseInterface {
    val connection: Connection
}
