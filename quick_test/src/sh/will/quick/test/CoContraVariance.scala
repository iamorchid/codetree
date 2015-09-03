package sh.will.quick.test

import java.util.Date

object CoContraVariance {

  val person = new Person("Tom", "male")

  def info(conv: Converter[Person, AnimalInfo]): AnimalInfo = {
    conv(person)
  }

  def main(args: Array[String]) {
    val func1 = new Converter[Animal, AnimalInfo] {
      def apply(animal: Animal) = {
        new AnimalInfo
      }
    }
    info(func1)

    val func2 = new Converter[Person, PersonInfo] {
      def apply(person: Person) = {
        new PersonInfo
      }
    }
    info(func2)

    /*
    // Not working
    val func3 = new Converter[Student, StudentInfo] {
      def apply(student: Student) = {
        new StudentInfo
      }
    }
    info(func3) 
    */
  }

}

class Animal(val name: String)

class AnimalInfo

class Person(name: String, val gender: String) extends Animal(name)

class PersonInfo extends AnimalInfo

class Student(name: String, gender: String, val school: String) extends Person(name, gender)

class StudentInfo extends PersonInfo

trait Converter[-T, +R] extends Function[T, R]

