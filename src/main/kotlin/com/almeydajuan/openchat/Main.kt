package com.almeydajuan.openchat

import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters.PrintRequestAndResponse
import org.http4k.filter.ServerFilters.CatchAll
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.ApacheServer
import org.http4k.server.asServer

fun main() {
    newBackend().asServer(ApacheServer(port = 8080)).start()
}

fun newBackend() = routes(
    "/status" bind GET to {
        Response(OK).body("OpenChat: OK!")
    }
).withFilter(PrintRequestAndResponse().then(CatchAll()))