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
import java.util.*

private const val SKRIVESTOTTE_BUCKET_NAME = "modiapersonoversikt-skrivestotte-bucket"
private const val SKRIVESTOTTE_KEY_NAME = "skrivestotte"
private val log = LoggerFactory.getLogger("modiapersonoversikt-skrivestotte.StorageService")

class StorageService(private val s3: AmazonS3) : StorageProvider {
    init {
        lagS3BucketsHvisNodvendig(SKRIVESTOTTE_BUCKET_NAME)
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

    private fun lagS3BucketsHvisNodvendig(vararg buckets: String) {
        timed("lag_buckets_hvis_nodvendig") {
            val s3BucketNames = s3.listBuckets().map { it.name }
            val missingBuckets = buckets.filter { !s3BucketNames.contains(it) }

            missingBuckets
                    .forEach {
                        s3.createBucket(CreateBucketRequest(it).withCannedAcl(CannedAccessControlList.Private))
                    }

            if (missingBuckets.isNotEmpty()) {
                log.info("Buckets måtte opprettes, populerer disse med data fra xml-fil...")
                val xmlTekster = XmlLoader.get("/data.xml")
                        .map { it.id!! to it }

                lagreTekster(hentTekster().plus(xmlTekster))

                log.info("Lagret ${xmlTekster.size} predefinerte tekster")
            }
        }
    }
}
