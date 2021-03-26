import io.reactivex.rxjava3.core.Observable

interface Api<DATA> {

    val url: String

    val method: Method

    fun call(
        url: String = this.url,
        method: Method = this.method,
        queryParams: Set<Param<*>>? = null,
        bodyParams: Any? = null,
        pathParams: Set<Param<*>>? = null,
        headers: Set<Param<*>>?= null
    ): Observable<out Res<DATA>>

    fun call(
        url: String = this.url,
        method: Method = this.method,
        queryParams: Set<Param<*>>? = null,
        bodyParams: Set<Param<*>>? = null,
        pathParams: Set<Param<*>>? = null,
        headers: Set<Param<*>>?= null
    ): Observable<out Res<DATA>> = call(
        url = url,
        method = method,
        queryParams = queryParams,
        bodyParams = bodyParams?.associate { it.key to it.valueString },
        pathParams = pathParams,
        headers = headers
    )
}