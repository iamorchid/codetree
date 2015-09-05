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

    def printlnSymbol(symbol: Symbol) {
      print(s"symbol: $symbol, isType: ${symbol.isType}, isSynthetic: ${symbol.isSynthetic}")
      if (symbol.isType) {
        print(s", isModuleClass: ${symbol.isModuleClass}")
      }
      if (symbol.isMethod) {
        print(s", isMethod: true, isAccessor: ${symbol.asMethod.isAccessor}, ${symbol.asMethod.isGetter}, ${symbol.asMethod.isSetter}")
      }
      println
      if (symbol.isModuleClass) {
        print("module -> ")
        printlnSymbol(symbol.asClass.module)
      }
    }

    def printlnOwners(symbol: Symbol) {
      if (symbol.owner != NoSymbol) {
        print("owner  -> ")
        printlnSymbol(symbol.owner)
        printlnOwners(symbol.owner)
      }
    }

    typeOf[Targets.type].decls.foreach(symbol => {
     println("*********************************************************")
     printlnSymbol(symbol)
     printlnOwners(symbol)
      if (symbol.isClass && symbol.info =:= typeOf[Action]) {
        val clazzMirror = ruMirror.reflectClass(symbol.asClass)
        val ctorSymbol = symbol.info.decl(termNames.CONSTRUCTOR).asMethod
        val ctorMirror = clazzMirror.reflectConstructor(ctorSymbol)
        val stud = ctorMirror("Will Zhang").asInstanceOf[Action]
        println(s"<class>student: ${stud.name}")
      } else if (symbol.isModule && symbol.info <:< typeOf[Targetable]) {
        val target = ruMirror.reflectModule(symbol.asModule).instance.asInstanceOf[Targetable]
        println(s"<module>target name: ${target.name}")
      } else if (symbol.isMethod && !symbol.isConstructor && !symbol.asMethod.isAccessor && !symbol.isSynthetic && symbol.isImplicit) {
        val methodMirror = instanceMirror.reflectMethod(symbol.asMethod)
        println(s"<method>return: ${methodMirror(CreditCurve)}")
      } else if (symbol.isTerm && !symbol.isModule && !symbol.isMethod && !symbol.isSynthetic) {
        val fieldMirror = instanceMirror.reflectField(symbol.asTerm)
        println(s"<field>value: ${fieldMirror.get}")
      }
    })
  }
}