package no.nav.modiapersonoversikt

import no.nav.modiapersonoversikt.model.Locale
import no.nav.modiapersonoversikt.model.Tekst
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class XmlLoader {
    companion object {
        fun get(path: String): List<Tekst> {
            return DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(this::class.java.getResourceAsStream(path))
                    .getElementsByTagName("contentdata")
                    .asList()
                    .map { createTekst(it) }
        }

        private fun createTekst(node: Node): Tekst {
            val overskrift = node.getElementsByTagName("overskrift").first().textContent
            val tags = node.getElementsByTagName("tags").first().textContent.split(" ")
            val innhold = node.getElementsByTagName("innhold")
                    .map { createInnhold(it) }
                    .toTypedArray()

            return Tekst(
                    UUID.randomUUID(),
                    overskrift,
                    tags,
                    mapOf(*innhold)
            )
        }

        private fun createInnhold(node: Node): Pair<Locale, String> {
            val locale = Locale.valueOf(node.getElementsByTagName("locale").first().textContent)
            val tekst = node
                    .getElementsByTagName("fritekst")
                    .first()
                    .textContent
                    .split("\r\n")
                    .joinToString("\r\n") { it.trim() }
            return Pair(locale, tekst)
        }

        private fun NodeList.asList(): List<Node> {
            val list = mutableListOf<Node>()

            for (i in 0 until this.length) {
                list.add(this.item(i))
            }

            return list
        }

        private fun Node.getElementsByTagName(name: String): List<Node> {
            return when (this) {
                is Element -> this.getElementsByTagName(name).asList()
                else -> emptyList()
            }
        }
    }
}

fun main() {
    val tekster = XmlLoader.get("/data.xml")
    println(tekster.size)
}