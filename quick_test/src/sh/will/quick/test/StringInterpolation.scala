package sh.will.quick.test

/**
 * @author will
 */
object StringInterpolation {

  val registry = Map(1126 -> "Will", 1129 -> "Ali", 1130 -> "Yang", 1139 -> "Jerry", 1145 -> "Chris")

  // Note: We extends AnyVal to prevent runtime instantiation.  See 
  // value class guide for more info.
  implicit class RegistryHelper(val sc: StringContext) extends AnyVal {
    def name(args: Int*): String = {
      sc.s(args.map { id => registry.getOrElse(id, throw new RuntimeException(s"ID $id not registered")) }:_*)
    }
  }
  
  def main(args: Array[String]) {
    val id = 1129
    println(name"student: $id")
  }

}