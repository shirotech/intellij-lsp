package org.jetbrains.plugins.scala.lang.parser.parsing.top.params

import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes
import org.jetbrains.plugins.scala.lang.parser.parsing.builder.ScalaPsiBuilder

/**
* @author Alexander Podkhalyuzin
* Date: 08.02.2008
*/

/*
 * ClassParamClauses ::= {ClassParamClause}
 *                       [[nl] '(' 'implicit' ClassParams ')']
 */
object ClassParamClauses extends ClassParamClauses {
  override protected def classParamClause = ClassParamClause
  override protected def implicitClassParamClause = ImplicitClassParamClause
}

trait ClassParamClauses {
  protected def classParamClause: ClassParamClause
  protected def implicitClassParamClause: ImplicitClassParamClause

  def parse(builder: ScalaPsiBuilder): Boolean = {
    val classParamClausesMarker = builder.mark
    while (classParamClause parse builder) {}
    implicitClassParamClause parse builder
    classParamClausesMarker.done(ScalaElementTypes.PARAM_CLAUSES)
    true
  }
}