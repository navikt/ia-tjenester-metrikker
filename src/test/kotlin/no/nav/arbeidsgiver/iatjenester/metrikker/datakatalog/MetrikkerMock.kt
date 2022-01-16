package no.nav.arbeidsgiver.iatjenester.metrikker.datakatalog

import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattInnloggetIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.repository.IaTjenesterMetrikkerRepository.MottattUinnloggetIaTjenesteMetrikk
import no.nav.arbeidsgiver.iatjenester.metrikker.restdto.Kilde
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

fun dummyUinnloggetMetrikk(
    kilde: Kilde = Kilde.SAMTALESTØTTE,
    tidspunkt: LocalDateTime = _1_MAI.atStartOfDay()
) = MottattUinnloggetIaTjenesteMetrikk(kilde, tidspunkt)

fun dummyInnloggetMetrikk(
    orgnr: String = "99999999",
    kilde: Kilde = Kilde.SYKEFRAVÆRSSTATISTIKK,
    næring: Næring = barnehage,
    kommunenummer: String = "0301",
    kommune: String = "Oslo",
    fylke: String = "Oslo",
    tidspunkt: LocalDateTime = _1_MAI.atStartOfDay()
) = MottattInnloggetIaTjenesteMetrikk(
    orgnr,
    kilde,
    næring,
    kommunenummer,
    kommune,
    fylke,
    tidspunkt
)


val _1_JANUAR_2021 = LocalDate.of(2021, Month.JANUARY, 1)
val _21_JUNI_2021 = LocalDate.of(2021, Month.JUNE, 21)
val _1_MAI = LocalDate.of(2021, Month.MAY, 1)
val _5_JUNI = LocalDate.of(2021, Month.JUNE, 5)


val anleggsvirksomhet =
    Næring("42210", "Bygging av vann- og kloakkanlegg", "Anleggsvirksomhet")
val barnehage = Næring("88911", "Barnehager", "Helse- og sosialtjenester")
val detaljhandelMedBiler =
    Næring("45512", "Detaljhandel med biler", "Varehandel, reparasjon av motorvogner")


