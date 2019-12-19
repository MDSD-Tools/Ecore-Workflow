package tools.mdsd.ecoreworkflow.mwe2lib.component;

import java.io.OutputStream;
import java.nio.file.Path;

import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;

public interface PackageLevelCodeFileGenerator {
	public void generateCode(OutputStream out);
	public Path getRelativePath();
	public void setGenPackage(GenPackage genPackage);
}
