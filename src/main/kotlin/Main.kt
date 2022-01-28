import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import rand.Rand
import rand.RandInt
import rand.RandNum

//@Serializable
//@SerialName("SB")
//sealed class SuperBase
//@Serializable
//@SerialName("B")
//sealed class Base:SuperBase()
//@Serializable
//@SerialName("I")
//class Inherited:Base()


fun main(args: Array<String>) {
    val rnd:Rand = RandNum(5.0,7.0,2.0)
    val str = Json.encodeToString(rnd)
    println(str)
    val obj = Json.decodeFromString<Rand>(str)
    println(obj)
    println(obj.evaluate())
}