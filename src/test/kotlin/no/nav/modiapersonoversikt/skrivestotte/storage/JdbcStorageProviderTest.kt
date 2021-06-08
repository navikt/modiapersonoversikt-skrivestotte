package no.nav.modiapersonoversikt.skrivestotte.storage

import kotlinx.coroutines.runBlocking
import no.nav.modiapersonoversikt.WithDatabase
import no.nav.modiapersonoversikt.skrivestotte.model.Locale
import no.nav.modiapersonoversikt.skrivestotte.model.Tekst
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class JdbcStorageProviderTest : WithDatabase {
    val dao: StorageProvider = JdbcStorageProvider(dataSource(), configuration())

    @Test
    fun `skal hente alle tekster`() = runBlocking {
        dao.leggTilTekst(lagTekst(overskrift = "Overskrift 1"))
        dao.leggTilTekst(lagTekst(overskrift = "Overskrift 2"))
        assertEquals(2, dao.hentTekster(null, true).size)
    }

    @Test
    fun `skal oppdatere tekst og innhold`() = runBlocking {
        val originalTekst = dao.leggTilTekst(lagTekst(overskrift = "Original"))

        dao.oppdaterTekst(
            originalTekst.copy(
                overskrift = "Oppdatert",
                tags = listOf("SUT"),
                innhold = mapOf(
                    Locale.nb_NO to "Oppdatert innhold",
                    Locale.nn_NO to "Nytt innhold"
                )
            )
        )

        val oppdatertTekst = dao.hentTekster(listOf("SUT"), true)[originalTekst.id]
        assertNotEquals(originalTekst.overskrift, oppdatertTekst?.overskrift)
        assertNotEquals(originalTekst.tags, oppdatertTekst?.tags)
        assertNotEquals(originalTekst.innhold.size, oppdatertTekst?.innhold?.size)
        assertNotEquals(originalTekst.innhold[Locale.nb_NO], oppdatertTekst?.innhold?.get(Locale.nb_NO))
        assertEquals("Oppdatert", oppdatertTekst?.overskrift)
        assertEquals("Oppdatert innhold", oppdatertTekst?.innhold?.get(Locale.nb_NO))
        assertEquals("Nytt innhold", oppdatertTekst?.innhold?.get(Locale.nn_NO))
    }

    @Test
    fun `skal slette tekster`() = runBlocking {
        dao.leggTilTekst(lagTekst(overskrift = "Overskrift 1"))
        val tekstSomSkalSlettes = dao.leggTilTekst(lagTekst(overskrift = "Overskrift 2"))
        dao.leggTilTekst(lagTekst(overskrift = "Overskrift 3"))

        dao.slettTekst(requireNotNull(tekstSomSkalSlettes.id))

        assertEquals(2, dao.hentTekster(null, true).size)
    }
}

fun lagTekst(overskrift: String, tags: List<String> = emptyList(), vekttall: Int = 0) = Tekst(
    id = null,
    overskrift = overskrift,
    tags = tags,
    innhold = mapOf(
        Locale.nb_NO to "Innhold"
    ),
    vekttall = vekttall
)
