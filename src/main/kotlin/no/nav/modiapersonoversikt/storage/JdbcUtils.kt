package no.nav.modiapersonoversikt.storage

import kotliquery.TransactionalSession
import kotliquery.sessionOf
import kotliquery.using
import javax.sql.DataSource

fun <A> transactional(dataSource: DataSource, operation: (TransactionalSession) -> A): A {
    return using(sessionOf(dataSource)) { session ->
        session.transaction(operation)
    }
}
