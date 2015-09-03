package sh.will.reflect.data

sealed trait Animal

class Dog(val name: String) extends Animal {
  override def toString = s"Dog: $name"
}

class Tiger(val legs: Integer) extends Animal {
  override def toString = s"Tiger: $legs"
}

class Fish extends Animal {
  override def toString = "Fish"
}