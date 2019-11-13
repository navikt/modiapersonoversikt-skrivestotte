package no.nav.modiapersonoversikt

import org.h2.tools.Server
import java.util.*

fun main() {
    val tcpServer: Server = Server.createTcpServer("-tcpAllowOthers", "-tcpPort", "8090")
    tcpServer.start()

    val webServer: Server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8091")
    webServer.start()

    println("TcpPort: ${tcpServer.url}")
    println("WebPort: ${webServer.url}")

    val scanner = Scanner(System.`in`)
    scanner.nextLine()

    Server.shutdownTcpServer(tcpServer.url, null, true, true)
    webServer.shutdown()
}