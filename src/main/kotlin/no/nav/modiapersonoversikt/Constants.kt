package no.nav.modiapersonoversikt

import no.nav.personoversikt.utils.EnvUtils

const val appName = "modiapersonoversikt-skrivestotte"
const val appContextpath = "modiapersonoversikt-skrivestotte"

val appImage = EnvUtils.getConfig("NAIS_APP_IMAGE") ?: "N/A"
