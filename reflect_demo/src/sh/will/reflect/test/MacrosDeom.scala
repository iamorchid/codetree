package sh.will.reflect.test

import sh.will.reflect.MacroUtils

/**
 * Created by will on 9/3/15.
 */
object MacrosDeom {

  def main(args: Array[String]): Unit = {
    MacroUtils.echo("Hello World")
    MacroUtils.printf("test, test %s", "good")
  }

}
