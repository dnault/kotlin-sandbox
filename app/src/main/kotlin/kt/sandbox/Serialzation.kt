package kt.sandbox

import com.couchbase.client.kotlin.codec.typeRef
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.squareup.moshi.JsonAdapter

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


@Serializable
public class Project(
    public var name: String,
    public var language: String,
    public val oops : List<String> = listOf("a", "b", "c"),
) {


    override fun toString(): String {
        return "I am a real project(name='$name', language='$language')"
    }
}


public fun main() {

    val data = Project("kotlinx.serialization", "Kotlin")

    val json = Json.encodeToString(data)
    println(json)

    val obj = Json.decodeFromString<Project>(json)
   println(obj.name + ":" + obj.language)

    val f: Project = json.parseJson()
    println(f)
    println(json.parseJson<Project>())

    val jsonList = """
        [{"name":"kotlinx.serialization","language":"Kotlin"}]
    """.trimIndent()

    //val x = typeRef<List<String>>()
  //  println(x)
  //  val radList : List<Project> = mapper.readValue(jsonList, typeRef())
    //val firstProj = radList.first()
    //println("THE NAME: " + firstProj.name)


//    val projects: List<Project> = jsonList.parseJson()
//
//    println(projects)
//
//    println(projects.toJsonString())
//
//    val asMap: List<Map<String, Any?>> = jsonList.parseJson()
//
//        println(asMap)


    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    val jsonAdapter: JsonAdapter<List<Project>> = moshi.adapter(typeRef<List<Project>>().type)

    val moshiProj: List<Project>? = jsonAdapter.fromJson(jsonList)
    println("moshi $moshiProj")
}

internal val mapper = jsonMapper {
    addModule(kotlinModule())
    //  addModule(EncryptionModule(FakeCryptoManager()))
}


internal inline fun <reified T : Any> String.parseJson(): T {
    return mapper.readValue(this, T::class.java)
}

//
//public inline fun <reified T : Any> typeRef(): TypeReference<T> {
//    return object : TypeReference<T>() {}
//}

internal inline fun <reified T : Any> ByteArray.parseJson(): T {
    return mapper.readValue(this, T::class.java)
}
//
internal fun Any?.toJsonBytes(): ByteArray {
    return mapper.writeValueAsBytes(this)
}

internal fun Any?.toJsonString(): String {
    return mapper.writeValueAsString(this)
}

