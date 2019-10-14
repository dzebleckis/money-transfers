package lt.dzebleckis.api.rest.v1

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OpenApiServlet : HttpServlet() {

    override fun doGet(req: HttpServletRequest?, resp: HttpServletResponse?) {
        val result = javaClass.classLoader?.getResourceAsStream("openapi.v1.yml")?.readBytes()
        resp?.contentType = "text/yml"
        resp?.addHeader("Access-Control-Allow-Origin", "*")

        if (result != null) {
            resp?.status = HttpServletResponse.SC_OK
            resp?.writer?.write(String(result))
        } else {
            resp?.status = HttpServletResponse.SC_NOT_FOUND
        }
    }
}