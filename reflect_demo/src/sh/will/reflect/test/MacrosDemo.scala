package sh.will.reflect.test

import sh.will.reflect.MacroUtils
import sh.will.reflect.ModuleMacroUtils
import scala.reflect.runtime.universe._

object DVSNames {
  val WILL = "will"
  val NANCY = "nancy"
  val JERRY = "jerry"
  
  trait Module {
    def name: String
  }
  
  object ModuleOne extends Module {
    def name: String = "Module #1"
  }
  
  object ModuleTwo extends Module {
    def name: String = "Module #2"
  }
  
  def method1(): String = "method #1"
  
  def method2: String = "method #2"
}

/**
 * Created by will on 9/3/15.
 */
object MacrosDeom {

  val name = "Demo"

  def main(args: Array[String]): Unit = {
    println(ModuleMacroUtils.field[MacrosDeom.type, String](MacrosDeom, "name"))
    ModuleMacroUtils.modules[DVSNames.type, DVSNames.Module](DVSNames).foreach { mod => println(mod.name) }
    println(ModuleMacroUtils.field[DVSNames.type, String](DVSNames, "JERRY"))
    ModuleMacroUtils.method[DVSNames.type, String](DVSNames, "method1") // method
    println(ModuleMacroUtils.field[DVSNames.type, String](DVSNames, "method2")) // similar to field
    println(ModuleMacroUtils.fields[DVSNames.type, String](DVSNames)) 
    println(MacroUtils.echo(DVSNames.JERRY))
  }

}
