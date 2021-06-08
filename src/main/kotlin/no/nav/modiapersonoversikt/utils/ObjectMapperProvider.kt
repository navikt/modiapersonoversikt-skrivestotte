package no.nav.modiapersonoversikt.utils

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class ObjectMapperProvider {
    companion object {
        val objectMapper = jacksonObjectMapper()
            .apply {
                setDefaultPrettyPrinter(
                    DefaultPrettyPrinter().apply {
                        indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                        indentObjectsWith(DefaultIndenter("  ", "\n"))
                    }
                )
                disableDefaultTyping()
                enable(SerializationFeature.INDENT_OUTPUT)
            }
    }
}
