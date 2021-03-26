import okhttp3.ResponseBody

interface Parser {

    fun <OUTPUT> parse(input: ResponseBody): OUTPUT?
}