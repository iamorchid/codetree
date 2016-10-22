package sh.will.quick.test

trait Printer[-T] {
  def print(item: T): Unit
}

class DefaultPrinter[T] extends Printer[T] {
  def print(item: T) = println(s"Unknown Type: $item")
}

class Employee(val name: String)

class ProjectManager(name: String, val project: String) extends Employee(name)

object ImplicitMethod {
  
  // generic printer (#method)
  implicit def printer[A]: Printer[A] = new DefaultPrinter[A]
  
  // Employee printer (#val)
  implicit val printerEmployee = new Printer[Employee] {
    def print(item: Employee) = println(s"Employee: $item")
  }
  
  def print[T](item: T)(implicit printer: Printer[T]) = {
    printer.print(item)
  }
  
  def main(args: Array[String]) = {
    print(100.6)
    println(new ProjectManager("Will", "Credit"))
  }
}