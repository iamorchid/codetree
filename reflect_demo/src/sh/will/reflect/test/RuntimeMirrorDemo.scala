package sh.will.reflect.test

import scala.reflect.runtime.universe._

/**
 * Created by will on 8/30/15.
 */
object RuntimeMirrorDemo {

  val ruMirror = runtimeMirror(getClass.getClassLoader)

  def main(args: Array[String]): Unit = {
    import sh.will.reflect.data.Targets
    import sh.will.reflect.data.Targets._

    val instanceMirror = ruMirror.reflect(Targets)

    typeOf[Targets.type].decls.foreach(symbol => {
      println(s"symbol: $symbol(${symbol.getClass})")
      if (symbol.isClass && symbol.info =:= typeOf[Action]) {
        val clazzMirror = ruMirror.reflectClass(symbol.asClass)
        val ctorSymbol = symbol.info.decl(termNames.CONSTRUCTOR).asMethod
        val ctorMirror = clazzMirror.reflectConstructor(ctorSymbol)
        val stud = ctorMirror("Will Zhang").asInstanceOf[Action]
        println(s"<class>student: ${stud.name}")
      } else if (symbol.isModule && symbol.info <:< typeOf[Targetable]) {
        val target = ruMirror.reflectModule(symbol.asModule).instance.asInstanceOf[Targetable]
        println(s"<module>target name: ${target.name}")
      } else if (symbol.isMethod && symbol.isImplicit) {
        val methodMirror = instanceMirror.reflectMethod(symbol.asMethod)
        println(s"<method>return: ${methodMirror(CreditCurve)}")
      }
    })
    val fieldSymbol = typeOf[Targets.type].decl(TermName("dummyName")).asTerm
    println(s"symbol: $fieldSymbol(${fieldSymbol.getClass})")
    val fieldMirror = instanceMirror.reflectField(fieldSymbol)
    println(s"<field>value: ${fieldMirror.get}")
  }

}