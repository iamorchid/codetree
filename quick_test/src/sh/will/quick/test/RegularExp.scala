package sh.will.quick.test

object RegularExp {

  def main(args: Array[String]) {
    var regex = """([0-9]+) ([a-z]+)""".r
    for (m <- regex.findAllIn("Will is 27 in 2015. And he'd be 28 next year")) {
      println(m)
    }
    for (regex(age, text) <- regex.findAllIn("Will is 27 in 2015. And he'd be 28 next year")) {
      println(s"$age, $text")
    }

    regex = """([^"]+)"([^"]+)""".r

    "Hello\"World" match {
      case regex(_*) => println(s"match without specifying groups")
      case _         => println("not match")
    }

    "Hello\"World" match {
      case regex(grp1, grp2) => println(s"match with specifying groups ($grp1, $grp2)")
      case _                 => println("not match")
    }

    regex = """(-)?(\d+)(\.\d+)?""".r
    val regex(sign, integer, decimal) = "3.24"
    println(s"$sign, $integer, $decimal")

    val students = List(
      Student("Will", 28, "SJTU"),
      Student("Kevin", 27, "SJTU"),
      Student("Joy", 17, "LTMS"),
      Student("Lei", 17, "PU"))

    val School = "SJTU" // use School rather than school as the constant
    for (Student(name, age, School) <- students) {
      println(s"SJTU students -> name: $name, age: $age")
    }

    val someone: AnyRef = Person("Will")
    val name = someone match {
      case Student(name, _, _) => Some(s"Student: $name")
      case Person(name)        => Some(s"Person: $name")
      case _                   => None
    }
    println(name.getOrElse("N/A"))
  }

  /**
   * For case class, we don't need to provide extractor for match/case
   */
  case class Student(name: String, age: Int, college: String)

  /**
   * For non-case class, we have to provide extractor for match/case
   */
  class Person(val name: String)

  object Person {
    def apply(name: String) = new Person(name)
    def unapply(person: Person) = Option(person.name)
  }
}