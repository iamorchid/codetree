object PartialFunctionTest {

  def main(args: Array[String]) {

    val seq = Seq(Some("Will"), None, Some("Jian"), None, Some("Nancy"))
    println(seq.collect({ case Some(name) => name }))
    //  println(seq.map({ case Some(name) => name })) // throw exception

    val func: PartialFunction[Option[String], String] = { case Some(name) => name };
    println(seq.collect(func.andThen { _.toUpperCase() }))

    println(seq.map(func compose { x => x match { case None => Some("None"); case x => x } }))
  }

}