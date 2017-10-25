import com.github.gtache.{LanguageServerDefinition, ArtifactLanguageServerDefinition}


object DottyLanguageExtension extends ArtifactLanguageServerDefinition("scala", "ch.epfl.lamp:dotty-language-server_0.3:0.3.0-RC2", "dotty.tools.languageserver.Main", Array[String]("-stdio")) {
  def register(): Unit = LanguageServerDefinition.register(this)
}