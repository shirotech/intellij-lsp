package org.jetbrains.plugins.scala.lang.psi.stubs.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.{IStubElementType, StubElement}
import com.intellij.util.io.StringRef
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScReferencePattern
import org.jetbrains.plugins.scala.lang.psi.stubs.ScReferencePatternStub

/**
  * User: Alexander Podkhalyuzin
  * Date: 17.07.2009
  */
class ScReferencePatternStubImpl(parent: StubElement[_ <: PsiElement],
                                 elementType: IStubElementType[_ <: StubElement[_ <: PsiElement], _ <: PsiElement],
                                 nameRef: StringRef)
  extends ScNamedStubBase[ScReferencePattern](parent, elementType, nameRef) with ScReferencePatternStub