package lt.dzebleckis.api.rest

import java.net.URI
import java.net.URLEncoder
import java.util.concurrent.CompletableFuture
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.core.Response

internal fun String.urlEncode(): String {
    return URLEncoder.encode(this, Charsets.UTF_8)
}

internal fun <T> CompletableFuture<T>.toAccepted(response: AsyncResponse) {
    this.whenComplete(fun(_, ex) {
        if (ex !== null) {
            response.resume(ex)
        } else {
            response.resume(Response.accepted().build())
        }
    })
}

internal fun <T> CompletableFuture<T?>.toCreated(response: AsyncResponse, builder: (T?) -> String) {
    this.whenComplete(fun(id, ex) {
        if (ex !== null) {
            response.resume(ex)
        } else {
            response.resume(Response.created(URI.create(builder(id))).build())
        }
    })
}