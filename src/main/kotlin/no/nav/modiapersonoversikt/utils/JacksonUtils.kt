package no.nav.modiapersonoversikt.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class JacksonUtils {
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

inline fun <reified T> T.toJson(): String {
    return JacksonUtils.objectMapper.writeValueAsString(this)
}

inline fun <reified T> String.fromJson(): T {
    return JacksonUtils.objectMapper.readValue(this, object : TypeReference<T>() {})
}
