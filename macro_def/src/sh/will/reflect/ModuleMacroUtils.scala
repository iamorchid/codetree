package sh.will.reflect

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context
import scala.reflect.macros.Universe

/**
 * If we use Ident or internal.gen.mkAttributedIdent for non-module symbols, we have 
 * to make sure the macro is invoked under its owner scope. For module symbols, it seems 
 * we don't have such limit.
 */
object ModuleMacroUtils {

  def modules[O, T](obj: O): Seq[T] = macro modules_impl[O, T]

  def fields[O, T](obj: O): Seq[T] = macro fields_impl[O, T]

  def field[O, T](obj: O, name: String): T = macro field_impl[O, T]

  def method[O, T](obj: O, name: String): T = macro method_impl[O, T]

  def modules_impl[O: c.WeakTypeTag, T: c.WeakTypeTag](c: Context)(obj: c.Expr[O]): c.Expr[Seq[T]] = {
    import c.universe._

    val modules = obj.tree.tpe.decls.filter { _.isModule }.map {
      // Ident(_) 
      internal.gen.mkAttributedIdent(_) // works for object
    }
    
    c.Expr[Seq[T]](q"Seq(..$modules)")
  }

  def fields_impl[O: c.WeakTypeTag, T: c.WeakTypeTag](c: Context)(obj: c.Expr[O]): c.Expr[Seq[T]] = {
    import c.universe._

    def isField(symbol: Symbol) = {
      symbol.isTerm && !symbol.isMethod && !symbol.isModule && !symbol.isSynthetic
    }

    val typeSymbol = obj.tree.tpe.typeSymbol.asClass // class symbol
    if (!typeSymbol.isModuleClass) {
      c.error(c.macroApplication.pos, "this macro must be invoked for an object")
    }
    // val moduleSymbol = typeSymbol.module
    val moduleSymbol = c.mirror.staticModule(typeSymbol.fullName) // module symbol

    val fields = obj.tree.tpe.decls.filter { isField(_) }.map { symbol =>
/*     
      // Doesn't work for non-module symbol
      internal.gen.mkAttributedIdent(symbol) 
*/
      // TODO Need to understand why symbol.name.toTermName doesn't work here
      val parts = symbol.fullName.split("[.]")
 //   Select(Ident(moduleSymbol), TermName(parts.last))
      Select(obj.tree, TermName(parts.last))
    }
    c.Expr[Seq[T]](q"Seq(..$fields)")
  }

  def field_impl[O: c.WeakTypeTag, T: c.WeakTypeTag](c: Context)(obj: c.Expr[O], name: c.Expr[String]): c.Expr[T] = {
    import c.universe._
    /*
    val typeSymbol = obj.tree.tpe.typeSymbol.asClass // class symbol
    val moduleSymbol = c.mirror.staticModule(typeSymbol.fullName) // module symbol
*/
    val typeSymbol = obj.tree.tpe.typeSymbol.asClass
    if (!typeSymbol.isModuleClass) {
      c.error(c.macroApplication.pos, "this macro must be invoked for an object")
    }
    val moduleSymbol = typeSymbol.module
    val Literal(Constant(field: String)) = name.tree
    val fieldTree = Select(Ident(moduleSymbol), TermName(field))

    c.Expr[T](fieldTree)
  }

  def method_impl[O: c.WeakTypeTag, T: c.WeakTypeTag](c: Context)(obj: c.Expr[O], name: c.Expr[String]): c.Expr[T] = {
    import c.universe._

    val typeSymbol = obj.tree.tpe.typeSymbol.asClass // class symbol
    val moduleSymbol = c.mirror.staticModule(typeSymbol.fullName) // module symbol

    val Literal(Constant(method: String)) = name.tree
    val methodTree = Apply(Select(Ident(moduleSymbol), TermName(method)), List())

    c.Expr[T](methodTree)
  }
}