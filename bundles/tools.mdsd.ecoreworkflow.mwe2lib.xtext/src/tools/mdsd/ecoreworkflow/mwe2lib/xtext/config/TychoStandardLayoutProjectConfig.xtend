package tools.mdsd.ecoreworkflow.mwe2lib.xtext.config

import org.eclipse.xtext.xtext.generator.model.project.StandardProjectConfig
import org.eclipse.xtext.xtext.generator.model.project.SubProjectConfig

import org.eclipse.xtend.lib.annotations.Accessors

@Accessors
class TychoStandardLayoutProjectConfig extends StandardProjectConfig {
	
	String bundlesFolder = "bundles"
	String featuresFolder = "features"
	String testsFolder = "tests"
	String relengFolder = "releng" 
	
	override protected computeRoot(SubProjectConfig project) {
		val folderPrefix = switch project {
			case runtime: bundlesFolder
			case runtimeTest: testsFolder
			case genericIde: bundlesFolder
			case eclipsePlugin: bundlesFolder
			case eclipsePluginTest: testsFolder
			case ideaPlugin: bundlesFolder
			case web: bundlesFolder
		}
		rootPath + '/' + folderPrefix + '/' + project.name
	}
	
}