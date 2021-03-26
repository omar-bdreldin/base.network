import Method.*
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST

abstract class BaseApi<DATA>(
    val okHttpClient: OkHttpClient,
    val parser: (ResponseBody?) -> DATA?
) : Api<DATA> {

    companion object {
        @JvmStatic
        private val gson = Gson()
        @JvmStatic
        private val jsonMediaType: MediaType = "application/json; charset=utf-8".toMediaType()
    }

    var throwError: Boolean = true

    override fun call(
        url: String,
        method: Method,
        queryParams: Set<Param<*>>?,
        bodyParams: Any?,
        pathParams: Set<Param<*>>?,
        headers: Set<Param<*>>?
    ): Observable<out Res<DATA>> {
        return Single.create<Res<DATA>> { emitter ->
            val request = Request.Builder()
                /**
                 * Setting url
                 */
                .let { builder ->
                    builder.url(
                        url
                            /**
                             * Setting path params
                             */
                            .let {
                                if (pathParams.isNullOrEmpty()) it
                                else {
                                    var urlWithPathParamsSet = it
                                    pathParams.forEach { param ->
                                        urlWithPathParamsSet =
                                            urlWithPathParamsSet.replace("{${param.key}}", param.valueString)
                                    }
                                    urlWithPathParamsSet
                                }
                            }
                            /**
                             * Setting query params
                             */
                            .let {
                                if (queryParams.isNullOrEmpty()) it
                                else queryParams
                                    .joinToString("&") { param -> param.toString() }
                                    .let { joinedParams -> "$it?$joinedParams" }
                            }
                    )
                }
                /**
                 * Setting method
                 */
                .let { builder ->
                    val body: RequestBody? = if (method == GET) null
                    else {
                        bodyParams?.let { gson.toJson(it).toRequestBody(jsonMediaType) }
                    }
                    when (method) {
                        GET -> builder.get()
                        POST -> builder.post(body ?: EMPTY_REQUEST)
                        PUT -> builder.put(body ?: EMPTY_REQUEST)
                        DELETE -> builder.delete(body ?: EMPTY_REQUEST)
                    }
                }
                /**
                 * Setting headers
                 */
                .let { builder ->
                    if (headers == null || headers.isEmpty()) builder
                    else builder.headers(
                        Headers.Builder()
                            .also {
                                headers.forEach { param ->
                                    it.add(param.key, param.valueString)
                                }
                            }.build()
                    )
                }.build()
            val call = okHttpClient.newCall(request)
            emitter.setCancellable { call.cancel() }
            val response = call.execute()
            val res = if (response.isSuccessful) {
                response.runCatching {
                    val data: DATA? = parser.invoke(body)
                    data
                }.let {
                    if (it.isSuccess)
                        Res<DATA>(
                            data = it.getOrNull(),
                            rawResponse = response
                        )
                    else
                        Res<DATA>(
                            rawResponse = response,
                            error = it.exceptionOrNull()
                        )
                }
            } else Res<DATA>(
                rawResponse = response,
                error = HttpError(response)
            )
            emitter.onSuccess(res)
        }.flatMap {
            if (throwError && it.error != null) Single.error<Res<DATA>>(it.error)
            else Single.just(it)
        }.toObservable()
    }
}