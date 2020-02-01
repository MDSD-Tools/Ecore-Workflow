package tools.mdsd.ecoreworkflow.switches;
// TODO uncomment the following. It is only commented out, because xtend classes don't make it into the maven build up to now

import java.io.OutputStream
import java.io.PrintWriter
import java.nio.file.Paths
import org.eclipse.emf.codegen.ecore.genmodel.GenClass
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage
import tools.mdsd.ecoreworkflow.mwe2lib.component.PackageLevelCodeFileGenerator

class MSwitchClassGenerator implements PackageLevelCodeFileGenerator {
	
	GenPackage genPackage
	
	override setGenPackage(GenPackage genPackage) {
		this.genPackage = genPackage;
	}
	
	override getRelativePath() {
		if (genPackage === null) throw new IllegalStateException("genPackage was not initialized");
		Paths
			.get("", packageName.split("\\."))
			.resolve(className + ".java")
	}
	
	override generateCode(OutputStream out) {
		if (genPackage === null) throw new IllegalStateException("genPackage was not initialized");
		val printWriter = new PrintWriter(out)
		printWriter.print(makeContent())
		printWriter.flush()		
	}
	
	private def String getClassName() {
		genPackage.switchClassName.replaceFirst("Switch$", "MSwitch")
	}
	
	private def String getPackageName() {
		genPackage.packageName + ".xutil"
	}
	
	private def String makeContent() {
		'''
		package «packageName»;
		
		import java.util.function.Function;
		import java.util.HashMap;
		import java.util.Map;
		
		import org.eclipse.emf.ecore.EClass;
		import org.eclipse.emf.ecore.EPackage;
		import org.eclipse.emf.ecore.EObject;
		
		import tools.mdsd.ecoreworkflow.switches.MSwitch;
		import tools.mdsd.ecoreworkflow.switches.MergeableSwitch;
		
		// auto-generated class, do not edit
		
		public class «className»<T> extends MSwitch<T> implements MergeableSwitch<«className»<T>, «className»<T>> {
			private static «genPackage.importedPackageInterfaceName» MODEL_PACKAGE = «genPackage.importedPackageInterfaceName».eINSTANCE;
			«FOR c:genPackage.genClasses»
			private Function<«c.importedInterfaceName»,T> «getCaseName(c)»;
			«ENDFOR»
		
			public boolean isSwitchFor(EPackage ePackage) {
				return ePackage == MODEL_PACKAGE;
			}
			
			protected T doSwitch(int classifierID, EObject eObject) throws MSwitch.SwitchingException {
				T result;
				switch(classifierID) {
					«FOR c : genPackage.genClasses»
					case «genPackage.importedPackageInterfaceName».«genPackage.getClassifierID(c)»: {
						«c.importedInterfaceName» casted = («c.importedInterfaceName») eObject;
						if («getCaseName(c)» != null) {
							result = «getCaseName(c)».apply(casted);
							if (result != null) return result;
						}
						«FOR alternative:c.switchGenClasses»
						if («getCaseName(alternative)» != null) {
							result = «getCaseName(alternative)».apply(casted);
							if (result != null) return result;
						}
						«ENDFOR»
						break;
					}
					«ENDFOR»
					default:
						throw new Error("type " + eObject.eClass() + " was not considered by the mswitch code generator");
				}
				return applyDefaultCase(eObject);
			}
			
			public «className»<T> merge(«className»<T> other) {
				«FOR field: genPackage.genClasses.map[caseName]»
				if (other.«field» != null) this.«field» = other.«field»;
				«ENDFOR»
				if (other.defaultCase != null) this.defaultCase = other.defaultCase;
				return this;
			} 
			
			«FOR c : genPackage.genClasses»
			public interface «getInterfaceName(c)»<T> extends Function<«c.importedInterfaceName»,T> {}
			«ENDFOR»
			
			«FOR c : genPackage.genClasses»
			public «className»<T> when(«getInterfaceName(c)»<T> then) {
				this.«getCaseName(c)» = then;
				return this;
			}
			«ENDFOR»
			public «className»<T> orElse(Function<EObject, T> defaultCase) {
				this.defaultCase = defaultCase;
				return this;
			}
			
			@Override
			public Map<EClass, Function<EObject, T>> getCases() {
			  Map<EClass, Function<EObject, T>> definedCases = new HashMap<>();
			  
			  «FOR c:genPackage.genClasses»
			  if (this.«getCaseName(c)» != null) {
			  	definedCases.put(«genPackage.importedPackageInterfaceName».Literals.«genPackage.getClassifierID(c)», this.«getCaseName(c)».compose(o -> («c.importedInterfaceName») o));
			  }
			  «ENDFOR»
			  
			  return definedCases;
			}
		}
		'''
	}
	
	private def getCaseName(GenClass c) {
		"case" + genPackage.getClassUniqueName(c)
	}
	
	private def getInterfaceName(GenClass c) {
		"When" + genPackage.getClassUniqueName(c)
	}
	
}