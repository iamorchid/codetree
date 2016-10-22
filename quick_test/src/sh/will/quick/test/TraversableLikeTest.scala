package sh.will.quick.test

import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom

/**
 * @author will
 */
object TraversableLikeTest {

  trait Nameable {
    def name: String
  }

  case class Dog(name: String) extends Nameable

  case class Student(name: String)

  implicit def student2Nameable(stud: Student) = new Nameable {
    def name = stud.name
  }

  def name[A](animal: A)(implicit ev: A => Nameable) = {
    println(ev + ", " + ev.getClass)
    animal.name
  }

  def each[C, A <: Nameable](coll: C)(implicit ev: C <:< Iterable[A]) = {
    coll.map { arg => arg.name }
  }

  def each2[C[_], A <: Nameable, B](coll: C[A], func: A => B)(implicit cbf: CanBuildFrom[C[A], B, C[B]], ev: C[A] => TraversableLike[A, C[A]]): C[B] = {
    coll.map { func(_) }
  }

  def main(args: Array[String]) {
    var dogs = List(Dog("AHuang"), Dog("SaiHu"), Dog("HeiBao"))
    println("each: " + each(dogs))

    // don't need to define implict conversion for Dog as it extends Nameable
    println(name(Dog("AHuang")))

    // have to define implict conversion for Student
    println(name(Student("Will")))

    println("each2: " + each2(dogs, (animal: Nameable) => animal.name.toUpperCase))
  }
}