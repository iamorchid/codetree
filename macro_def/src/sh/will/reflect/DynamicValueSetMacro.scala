package sh.will.reflect

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

/**
 * @author will
 */
object DynamicValueSetMacro {

  def enclosingSymbol(c: Context)(predict: c.universe.Symbol => Boolean) = {
    var currentSym = c.internal.enclosingOwner
    while (currentSym != c.universe.NoSymbol && !predict(currentSym)) {
      currentSym = currentSym.owner
    }
    currentSym
  }

  def findPredefinedValues: Any = macro findPredefinedValues_impl

  def findPredefinedModules: Any = macro findPredefinedModules_impl

  def findPredefinedValues_impl(c: Context): c.Expr[Any] = {
    import c.universe._

    val module = enclosingSymbol(c) { _.isModuleClass }.asClass.module
    if (!module.isModule) {
      c.error(c.macroApplication.pos, "this macro must be invoked from an object")
    }

    val clazz = module.companion // the class of companion object
    if (clazz == NoSymbol) {
      c.error(c.macroApplication.pos, "find no companion class for module " + module.fullName)
    }

    val matchingDecls = module.typeSignature.decls.filter { sym =>
      sym.isMethod && sym.asMethod.isAccessor && !sym.isSynthetic && sym.asMethod.returnType.baseClasses.contains(clazz)
    }

    // we could use the symbol to create a tree since it's under the scope of its closing object
    val ids = matchingDecls.map { sym => internal.gen.mkAttributedIdent(sym) }

    val tree = q"Seq(..$ids)"

    c.Expr[Any](tree)
  }

  def findPredefinedModules_impl(c: Context): c.Expr[Any] = {
    import c.universe._

    val module = enclosingSymbol(c) { _.isModuleClass }.asClass.module
    if (!module.isModule) {
      c.error(c.macroApplication.pos, "this macro must be invoked from an object")
    }

    val matchingDecls = module.typeSignature.decls.filter { sym => sym.isModule && !sym.isSynthetic }

    // we could use the symbol to create a tree since it's under the scope of its closing object
    val ids = matchingDecls.map { sym => internal.gen.mkAttributedIdent(sym) }

    val tree = Apply(reify(Seq).tree, ids.toList)

    c.Expr[Any](tree)
  }
}