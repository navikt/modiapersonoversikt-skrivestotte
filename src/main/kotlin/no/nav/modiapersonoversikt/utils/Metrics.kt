package no.nav.modiapersonoversikt.utils

import io.prometheus.client.Histogram

class Metrics {
    companion object {
        const val METRICS_NS = "modiapersonoversikt_skrivestotte"
        private val histograms: MutableMap<String, Histogram> = mutableMapOf()

        fun <T> timed(name: String, fn: () -> T): T {
            return getHistogram(name).time(fn)
        }

        private fun getHistogram(name: String): Histogram {
            return histograms.getOrPut(name) {
                Histogram.Builder()
                    .namespace(METRICS_NS)
                    .name(name)
                    .help("Histogram for $name")
                    .register()
            }
        }
    }
}
