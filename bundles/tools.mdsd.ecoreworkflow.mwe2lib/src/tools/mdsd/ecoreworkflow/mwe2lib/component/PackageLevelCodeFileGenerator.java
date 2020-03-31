package tools.mdsd.ecoreworkflow.mwe2lib.component;

import java.io.OutputStream;
import java.nio.file.Path;

import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;

/**
 * Generator that can generate an additional single code file given a GenPackage.
 * The kind and contents of the generated is completely up to the implementation.
 */
public interface PackageLevelCodeFileGenerator {
    /**
     * Generate the code now.
     * Must only be invoked after setGenPackage
     * @param out the output stream to write the code to.
     */
	public void generateCode(OutputStream out);
	
	/**
	 * Specify where the generated code is to be written to, 
	 * relative to the src-gen folder of the ecore code generation
	 * @return a relative Path
	 */
	public Path getRelativePath();
	
	/**
	 * Set the GenPackage that the code is to be based upon / created for.
	 * @param genPackage
	 */
	public void setGenPackage(GenPackage genPackage);
}
