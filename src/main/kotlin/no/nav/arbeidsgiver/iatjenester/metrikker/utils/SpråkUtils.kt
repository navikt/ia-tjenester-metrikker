package no.nav.arbeidsgiver.iatjenester.metrikker.utils

import java.time.Month
import java.time.format.TextStyle
import java.util.*

fun Month.tilNorskTekstformat(kortform: Boolean = true): String {
    return getDisplayName(
        if (kortform) TextStyle.SHORT else TextStyle.FULL,
        Locale("no", "NO", "NB")
    )
}