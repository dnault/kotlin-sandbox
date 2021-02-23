package kt.sandbox

import com.couchbase.client.kotlin.codec.JacksonJsonSerializer
import com.couchbase.client.kotlin.codec.JsonSerializer
import com.couchbase.client.kotlin.codec.typeRef
import com.couchbase.client.kotlin.internal.LookupInMacro
import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AppTest {

    private val time = System.currentTimeMillis();

    @BeforeAll
    fun setup() {
        println("BEFORE ALL")
    }



    @BeforeEach
    fun setupEach() {
        println("BEFORE each")
    }

    @AfterAll
    fun teardown() {
        println("AFTER ALL")
    }

    @Test
    fun testAppHasAGreeting() {
        val classUnderTest = App()
        assertNotNull(classUnderTest.greeting, "app should have a greeting")
        println(time)
        Thread.sleep(1000)
    }
    @Test
    fun `typeref knows nullability`() {
        assertTrue(typeRef<String?>().nullable)
        assertFalse(typeRef<String>().nullable)
    }

    @Test
    fun `jackson can serialize null`() {
        println(LookupInMacro.DOCUMENT)
        val serializer : JsonSerializer = JacksonJsonSerializer(jsonMapper())
        val s : String? = null
        assertEquals("null", serializer.serialize(s, typeRef()).toStringUtf8())

        val roundTrip : String? = serializer.deserialize("null".toByteArray(), typeRef());
        assertNull(roundTrip)


        val roundTripNotNull : String = serializer.deserialize("null".toByteArray(), typeRef());
        assertNull(roundTrip)
    }

}
