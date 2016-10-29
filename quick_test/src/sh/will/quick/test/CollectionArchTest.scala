package sh.will.quick.test

import scala.collection.IndexedSeqLike
import scala.collection.mutable.Builder
import scala.collection.mutable.ArrayBuffer
import scala.collection.generic.CanBuildFrom

object CollectionArchTest {
  def main(args: Array[String]) {
    val change = (x: Base) => x match { case A => G case x => x }

    val rna1 = RNA.fromSeq(Seq(A, A, U, G, C, C))
    println(rna1)
    
    println(rna1.filterNot(_ == G))

    println(rna1.map { change })

    val rna2: Iterable[Base] = RNA.fromSeq(Seq(A, A, U, G, C, C))
    println(rna2.map { change })
  }
}

abstract class Base
case object A extends Base
case object U extends Base
case object G extends Base
case object C extends Base

object Base {
  val fromInt: Int => Base = Array(A, U, G, C)
  val toInt: Base => Int = Map(A -> 0, U -> 1, G -> 2, C -> 3)
}

final class RNA private (val groups: Array[Int], val length: Int)
    extends IndexedSeq[Base] with IndexedSeqLike[Base, RNA] {

  import RNA._

  override def genericBuilder[B]: Builder[B, IndexedSeq[B]] = {
    println("call genericBuilder")
    RNA.newBuilder.asInstanceOf[Builder[B, IndexedSeq[B]]]
  }


  // Mandatory re-implementation of `newBuilder` in `IndexedSeq`
  override protected[this] def newBuilder: Builder[Base, RNA] = {
    println("call newBuilder")
    RNA.newBuilder
  }

  // Mandatory implementation of `apply` in `IndexedSeq`
  def apply(idx: Int): Base = {
    if (idx < 0 || length <= idx)
      throw new IndexOutOfBoundsException
    Base.fromInt(groups(idx / N) >> (idx % N * S) & M)
  }

  // Optional re-implementation of foreach, 
  // to make it more efficient.
  override def foreach[U](f: Base => U): Unit = {
    var i = 0
    var b = 0
    while (i < length) {
      b = if (i % N == 0) groups(i / N) else b >>> S
      f(Base.fromInt(b & M))
      i += 1
    }
  }
}

object RNA {
  private val S = 2 // number of bits in group
  private val M = (1 << S) - 1 // bitmask to isolate a group
  private val N = 32 / S // number of groups in an Int

  def fromSeq(buf: Seq[Base]): RNA = {
    val groups = new Array[Int]((buf.length + N - 1) / N)
    for (i <- 0 until buf.length)
      groups(i / N) |= Base.toInt(buf(i)) << (i % N * S)
    new RNA(groups, buf.length)
  }

  def apply(bases: Base*) = fromSeq(bases)

  def newBuilder: Builder[Base, RNA] = new ArrayBuffer mapResult fromSeq

  implicit def canBuildFrom: CanBuildFrom[RNA, Base, RNA] = {
    println("RNA::canBuildFrom")
    new CanBuildFrom[RNA, Base, RNA] {
      def apply(): Builder[Base, RNA] = newBuilder
      def apply(from: RNA): Builder[Base, RNA] = newBuilder
    }
  }
}