package sh.will.quick.test

import scala._

object ImplicitConversion {

  trait ValueFunction[I, O] {
    def apply(input: I): O
  }

  /*
  implicit def SubtractiveOperation[I, O: Subtractive](func: ValueFunction[I, O]) = new {
    def -(other: ValueFunction[I, O]): ValueFunction[I, O] = SubtractFunction(func, other)
  }
  */

  implicit class SubtractiveOperation[I, O: Subtractive](func: ValueFunction[I, O]) {
    def -(other: ValueFunction[I, O]): ValueFunction[I, O] = SubtractFunction(func, other)
  }

  case class SubtractFunction[I, O: Subtractive](func1: ValueFunction[I, O], func2: ValueFunction[I, O]) extends ValueFunction[I, O] {
    def apply(input: I): O = implicitly[Subtractive[O]].subtract(func1(input), func2(input))
  }

  trait Subtractive[I] {
    def subtract(input1: I, input2: I): I
  }

  case class Factor(value: Int) {
    def apply(input: Int) = value * input
  }

  object MinusValueFunction extends ValueFunction[Int, Int] {
    def apply(input: Int) = -1 * input
  }

  implicit class FactorValueFunction(factor: Factor) extends ValueFunction[Int, Int] {
    def apply(input: Int) = factor(input)
  }

  /*
  implicit def IntSubtractive = new Subtractive[Int] {
    def subtract(input1: Int, input2: Int) = input1 - input2
  }
  */
  
  implicit object IntSubtractive extends Subtractive[Int] {
    def subtract(input1: Int, input2: Int) = input1 - input2
  }

  implicit def int2Factor(value: Int) = Factor(value)

  def main(args: Array[String]) {
    val res = (FactorValueFunction(9) - MinusValueFunction)(10)
    println(s"res: $res")
  }
}
