package sh.will.reflect.test

import sh.will.reflect._
import scala.reflect.runtime.universe._

trait Module { def name: String }

case class CreditCurveType(val name: String)

object CreditCurveType {
  val FCS = CreditCurveType("FixedCouponSpread")
  val Arrears = CreditCurveType("Arrears")
  val FCP = CreditCurveType("FixedCouponPrice")
  val Upfront = CreditCurveType("Upfront")
  val UpfrontPlus = CreditCurveType("Upfront+")

  object FCSModule extends Module { def name = "FixedCouponSpread" }

  object ArrearsModule extends Module { def name = "Arrears" }

  object UpfrontModule extends Module { def name = "FixedCouponPrice" }

  def values = DynamicValueSetMacro.findPredefinedValues

  def modules = DynamicValueSetMacro.findPredefinedModules
}

object DVSNames {
  val WILL = "will"
  val NANCY = "nancy"
  val JERRY = "jerry"

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

  val name = "MacrosDeom"

  def main(args: Array[String]): Unit = {
    println(ModuleMacroUtils.field[MacrosDeom.type, String](MacrosDeom, "name"))
    
    ModuleMacroUtils.modules[DVSNames.type, Module](DVSNames).foreach { mod => println(mod.name) }
    
    println(ModuleMacroUtils.field[DVSNames.type, String](DVSNames, "JERRY"))
    println(ModuleMacroUtils.field[DVSNames.type, String](DVSNames, "method2")) // similar to field
    println(ModuleMacroUtils.fields[DVSNames.type, String](DVSNames))
    
    ModuleMacroUtils.method[DVSNames.type, String](DVSNames, "method1") // method
    
    println(MacroUtils.echo(DVSNames.JERRY))
    println(MacroUtils.echo(DVSNames.ModuleOne))
    
    CreditCurveType.values.foreach { println(_) }
    CreditCurveType.modules.foreach { println(_) }
  }

}
