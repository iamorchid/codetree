package sh.will.reflect.test

import scala.reflect.runtime.universe._
import scala.reflect.ClassTag
import sh.will.reflect.data._

/**
 * Created by will on 8/29/15.
 */
object TypeTagDemo {

  def subclassesForAnimal = {
    // typeTag[Animal].tpe == typeOf[Animal]
    typeTag[Animal].tpe.typeSymbol.asClass.knownDirectSubclasses
  }

  /**
   * Would cause warning since type info would be erased
   */
  def outputList[A](xs: List[A]) = xs match {
    case _: List[String] => "list of strings"
    case _: List[Animal] => "list of amimals"
  }

  /**
   * typeOf needs an implicit TypeTag as its parameter. See the following definition:
   * def typeOf[T](implicit ttag: TypeTag[T]): Type = ttag.tpe
   */
  def outputList2[A: TypeTag](xs: List[A]) = typeOf[A] match {
    case tp if tp =:= typeOf[String] => "list of strings"
    case tp if tp <:< typeOf[Animal] => "list of amimals"
  }

  /**
   * Dynamically create an array
   */
  def createArray[A: ClassTag](seq: Seq[A]) = Array[A](seq: _*)

  def main(args: Array[String]): Unit = {
    println(outputList(List(new Fish, new Fish)))
    println("------------------------")
    println(outputList2(List(new Fish, new Fish)))
    println("------------------------")
    subclassesForAnimal.foreach(println(_))
  }

}
