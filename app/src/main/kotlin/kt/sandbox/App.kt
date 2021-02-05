/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package kt.sandbox

import com.couchbase.client.core.msg.kv.MutationToken
import com.couchbase.client.kotlin.Cluster
import com.couchbase.client.kotlin.codec.*
import com.couchbase.client.kotlin.kv.Durability
import com.couchbase.client.kotlin.kv.Expiry
import com.couchbase.client.kotlin.kv.ReplicateTo
import com.couchbase.client.kotlin.query.formatForQuery
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import kotlinx.coroutines.runBlocking
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Duration
import java.time.Instant
import java.util.*

internal class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}


public fun main() {
    System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
//    foo()


    println(jacksonObjectMapper().writeValueAsString(listOf(
        MutationToken(1, 2, 3, "foo"),
        MutationToken(1, 2, 4, "foo"),
        MutationToken(1, 2, 1, "foo"),
        MutationToken(1, 2, -11, "bar")
    ).formatForQuery()))

}




internal fun zot(foo: String = UUID.randomUUID().toString().also { println("calculated!") }): String = foo


internal fun foo() = runBlocking {


    println(Expiry.Absolute(Instant.now()))
    println(Durability.majority())
    println(Durability.polling(ReplicateTo.ONE))
    println(Durability.inMemoryOnActive())

    val cluster = Cluster.connect("localhost", "Administrator", "password")
        .waitUntilReady(Duration.ofSeconds(10))

    println("cluster ready!")
    // cluster.query("SELECT * from default")

    //   val cluster = Cluster.connect("localhost", "Administrator", "password")
//    println(
//        collection.get(
//            "foo", GetOptions(
//                timeout = null
//            )
//        ).content.toString(UTF_8)
//    )
    val collection = cluster.bucket("default")
        .waitUntilReady(Duration.ofSeconds(10), setOf()).also { println("bucket ready!") }
        .defaultCollection()


    // delay(1000)

    println(collection)

//    println("loading foo 3 times")

    // val serializer = JacksonJsonSerializer(mapper)
//    val serializer: JsonSerializer = KotlinxSerializer()
    val serializer: JsonSerializer = MoshiSerializer(Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build())

    val transcoder = JsonTranscoder(serializer)

    println(serializer.deserialize("null".toByteArray(UTF_8), typeRef<String>()))

    val x = serializer.deserialize("[\"nool\"]".toByteArray(UTF_8), typeRef<List<String>>())

    println(x)
    println(x?.javaClass)

    val t = typeRef<List<String>>()

    println(t)
    val obj = Project("Hank", "English")

    collection.upsert("bar", listOf(obj, obj))

    collection.upsert("raw", Content.binary("boogers".toByteArray()))
    println("got raw: " + collection.get("raw").content.toStringUtf8())

    val out = collection.get("bar").contentAs<List<Project>>(transcoder)!!
    println("*** $out")

    collection.get("bar").contentAs<List<Project>>(transcoder)


    val proj = out.first()
    println(proj.name)


    collection.upsert("zoinks2", mapOf("foo" to mapOf("x" to "y")))

    println("class: ${out.first().javaClass}")


//    collection.get("foo")
//    collection.get("foo")
//    println("done loading foo 3 times")
//

    println("$$$" + collection.upsert("magicWord", listOf(Project("xyzzy", "ick", listOf("1")))))

    val w = runCatching { collection.get("magicWord") }
        .mapCatching { it.contentAs<List<Project>>(JsonTranscoder(KotlinxJsonSerializer())) }
        .getOrThrow()

    println("$$$" + w)


//        "xyzzy".toByteArray(UTF_8),
//        "\"xyzzy\"".toByteArray(UTF_8),


    //expiry = Expiry.Relative(Duration.ofSeconds(3)),
    //durability = Durability.polling(PersistTo.TWO, ReplicateTo.NONE)
//        durability = Durability.polling(PersistTo.NONE, ReplicateTo.NONE),
//
//        expiry = Exp
    //  expiry = Expiry.absolute(Instant.now()),
//        durability = Durability.persistToMajority(),
//        options = RequestOptions()
//    ))


//    println("result! " +
//            collection.get(
//                "foo",
//                withExpiry = true,
//                projections = listOf("__crypt_one.alg")
//            ).content.toString(UTF_8)
////    ).content
//    )

//
//    collection.get("foo") { timeout = Duration.ofMillis(0) }
//
//    println(
//        collection.get("foo").content.toString(UTF_8)
//    )


}

internal fun dumpFlags(flags: Int) {
// 50333696
    // CodecFlags.extractCommonFlags()


}

internal fun ByteArray.toStringUtf8() = toString(UTF_8)
