package no.nav.modiapersonoversikt.skrivestotte.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotliquery.TransactionalSession
import kotliquery.sessionOf
import kotliquery.using
import javax.sql.DataSource

suspend fun <A> transactional(dataSource: DataSource, operation: (TransactionalSession) -> A): A {
    return withContext(Dispatchers.IO) {
        using(sessionOf(dataSource)) { session ->
            session.transaction(operation)
        }
    }
}
