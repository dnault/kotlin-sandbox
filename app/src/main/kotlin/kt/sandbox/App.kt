/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package kt.sandbox

import com.couchbase.client.kotlin.Cluster
import com.couchbase.client.kotlin.RequestOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Duration

class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

data class Foo(var x: String)

fun main() = runBlocking {

    val cluster = Cluster.connect("localhost", "Administrator", "password")


    // cluster.query("SELECT * from default")

    //   val cluster = Cluster.connect("localhost", "Administrator", "password")
//    println(
//        collection.get(
//            "foo", GetOptions(
//                timeout = null
//            )
//        ).content.toString(UTF_8)
//    )
    val collection = cluster.bucket("default").defaultCollection()

    println("loading foo 3 times")
    collection.get("foo")
    collection.get("foo")
    collection.get("foo")
    println("done loading foo 3 times")


    println(
        collection.get(
            "foo",
            options = RequestOptions(timeout = Duration.ofMillis(1))
        ).content.toString(UTF_8)
    )

    runBlocking {
        println("helloooooo async " + async { collection.get("foo") }.await().content.toString(UTF_8))
    }

//
//    collection.get("foo") { timeout = Duration.ofMillis(0) }
//
//    println(
//        collection.get("foo").content.toString(UTF_8)
//    )

    println(CommonOptions({
        clientContext = mapOf("1" to 3)
    }).copy { timeout = Duration.ofSeconds(3) })


}
