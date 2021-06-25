package statistikkapi.datakatalog.alder

import statistikkapi.datakatalog.til
import java.time.LocalDate

class AntallMottattIATjenesteDatagrunnlag(
    antallMottattIATjeneste: Int,
    antallVirksomheterMottattIATjenester: Int,
    dagensDato: () -> LocalDate
) {
    private val fraDatoMottattIATjeneste = LocalDate.of(2021, 3, 22)
    private val gjeldendeDatoer = fraDatoMottattIATjeneste til dagensDato()
    val antallMottattIATjeneste= antallMottattIATjeneste
    val antallVirksomheterMottattIATjenester =antallVirksomheterMottattIATjenester
    fun gjeldendeDatoer() = gjeldendeDatoer
}



