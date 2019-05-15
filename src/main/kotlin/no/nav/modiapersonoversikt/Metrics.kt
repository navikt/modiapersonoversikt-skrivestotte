package no.nav.modiapersonoversikt

import io.prometheus.client.Histogram

const val METRICS_NS = "modiapersonoversikt_skrivestotte"
private val histograms: MutableMap<String, Histogram> = mutableMapOf()

private fun getHistogram(name: String): Histogram {
    return histograms.getOrPut(name) {
        Histogram.Builder()
                .namespace(METRICS_NS)
                .name(name)
                .help("Histogram for $name")
                .register()
    }
}

fun <T> timed(name: String, fn: () -> T): T {
    return getHistogram(name).time(fn)
}