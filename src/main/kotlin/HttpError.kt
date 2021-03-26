import okhttp3.Response
import java.lang.RuntimeException

class HttpError(
    val response: Response
) : RuntimeException(
    response.let {
        val request = response.request
        "Request: ( url=${request.url}, method=${request.method} ) has failed with response code ${response.code}"
    }
)