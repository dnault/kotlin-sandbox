package com.couchbase.client.kotlin.codec

public interface Transcoder {


}


public fun main() {

    val t  = typeRef<String>()
    println(t.type is Class<*>)


}
