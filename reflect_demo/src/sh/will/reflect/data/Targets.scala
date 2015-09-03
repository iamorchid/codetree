package sh.will.reflect.data

/**
 * @author will
 */
object Targets {
  val dummyName = "TargetName"

  /**
   * Action that could be applied to targetable object
   */
  class Action(val name: String)
  
  trait Targetable {
    def name: String
  }

  implicit def targetName(target: Targetable) = {
    target.name
  }

  object CreditCurve extends Targetable {
    def name: String = "CreditCurve"
  }

  object YieldCurve extends Targetable {
    def name: String = "YieldCurve"
  }

  object CollateralCurve extends Targetable {
    def name: String = "CollateralCurve"
  }
}