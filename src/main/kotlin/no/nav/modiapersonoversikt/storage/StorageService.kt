package no.nav.modiapersonoversikt.storage

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.CreateBucketRequest
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.features.BadRequestException
import no.nav.modiapersonoversikt.Metrics.Companion.timed
import no.nav.modiapersonoversikt.ObjectMapperProvider.Companion.objectMapper
import no.nav.modiapersonoversikt.XmlLoader
import no.nav.modiapersonoversikt.model.Tekst
import no.nav.modiapersonoversikt.model.Tekster
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.temporal.ChronoUnit.MINUTES
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

private const val SKRIVESTOTTE_BUCKET_NAME = "modiapersonoversikt-skrivestotte-bucket"
private const val SKRIVESTOTTE_KEY_NAME = "skrivestotte"
private val log = LoggerFactory.getLogger("modiapersonoversikt-skrivestotte.StorageService")

class StorageService(private val s3: AmazonS3) : StorageProvider {
    init {
        lagS3BucketsHvisNodvendig(SKRIVESTOTTE_BUCKET_NAME)

        // Kjører med update fra enonic (prod) i en periode før vi får formidlet til brukerne at endringer må gjøres i appen.
        // TODO Fjern da man slutter å bruke enonic
        val refreshRate = Duration.of(30, MINUTES).toMillis()
        Timer().scheduleAtFixedRate(0L, refreshRate) {
            refreshTekster()
        }
    }

    override fun hentTekster(): Tekster =
            timed("hent_tekster") {
                try {
                    val teksterContent = s3.getObject(SKRIVESTOTTE_BUCKET_NAME, SKRIVESTOTTE_KEY_NAME)
                    objectMapper.readValue(teksterContent.objectContent)
                } catch (e: Exception) {
                    emptyMap()
                }
            }

    override fun oppdaterTekst(tekst: Tekst): Tekst {
        val nyeTekster = tekst.id
                ?.let {
                    hentTekster().plus(it to tekst)
                }
                ?: throw BadRequestException("id må være definert")

        lagreTekster(nyeTekster)

        return nyeTekster[tekst.id] ?: error("Fant ikke tekst med id: ${tekst.id}")
    }

    override fun leggTilTekst(tekst: Tekst): Tekst {
        val id = UUID.randomUUID()
        val tekstTilLagring = tekst.copy(id = id)
        val tekster = hentTekster().plus(id to tekstTilLagring)

        lagreTekster(tekster)

        return tekstTilLagring
    }

    override fun slettTekst(id: UUID) {
        val nyeTekster = hentTekster().minus(id)
        lagreTekster(nyeTekster)
    }

    private fun lagreTekster(tekster: Tekster) {
        timed("lagre_tekster") {
            s3.putObject(SKRIVESTOTTE_BUCKET_NAME, SKRIVESTOTTE_KEY_NAME, objectMapper.writeValueAsString(tekster))
        }
    }

    private fun refreshTekster() {
        log.info("Refresher tekster fra enonic.")
        val tekster = XmlLoader.getFromUrl("https://appres.adeo.no/app/modiabrukerdialog/skrivestotte")
                .map { it.id!! to it }
                .toTypedArray()

        log.info("Fant ${tekster.size} tekster")
        lagreTekster(mapOf(*tekster))

        log.info("Tekster refreshed ok.")
    }

    private fun lagS3BucketsHvisNodvendig(vararg buckets: String) {
        timed("lag_buckets_hvis_nodvendig") {
            val s3BucketNames = s3.listBuckets().map { it.name }
            val missingBuckets = buckets.filter { !s3BucketNames.contains(it) }

            println("Wanted Buckets: ${buckets.joinToString(", ")}")
            println("Found Buckets: ${s3BucketNames.joinToString(", ")}")
            println("Missing Buckets: ${missingBuckets.joinToString(", ")}")

            missingBuckets
                    .forEach {
                        s3.createBucket(CreateBucketRequest(it).withCannedAcl(CannedAccessControlList.Private))
                    }
        }
    }
}
