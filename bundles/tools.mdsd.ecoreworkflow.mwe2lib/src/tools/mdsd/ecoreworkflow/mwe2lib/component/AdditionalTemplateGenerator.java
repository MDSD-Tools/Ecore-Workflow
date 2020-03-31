package tools.mdsd.ecoreworkflow.mwe2lib.component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.codegen.ecore.genmodel.impl.GenModelImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.lib.AbstractWorkflowComponent2;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.emf.mwe2.runtime.Mandatory;

import tools.mdsd.ecoreworkflow.mwe2lib.util.URIToPath;

/**
 * MWE2 component used to generate an additional extra file for an ecore package
 * based on a template.
 *
 */
public class AdditionalTemplateGenerator extends AbstractWorkflowComponent2 {
	
    private String destPath;
	private String genModel;
	private List<PackageLevelCodeFileGenerator> packageLevelGenerators;
	
	
	public AdditionalTemplateGenerator() {
		packageLevelGenerators = new ArrayList<>();
	}
	
	/**
	 * Set the path to the base folder.
     * (This is usually a platform:-URL pointing at the target project's src-gen folder).
     * The template's relative path will be resolved against this URL.
     * 
	 * @param destPath path to the base folder
	 */
	@Mandatory
	public void setDestPath(String destPath) {
		this.destPath = destPath;
	}
	
	/**
     * Set the path to the .genmodel-file.
     * The template will have acces to the genmodel.
     * 
	 * @param genModel path to the .genmodel file
	 */
	@Mandatory
	public void setGenModel(String genModel) {
		this.genModel = genModel;
	}
	
	/**
	 * Add a template (=generator) to be executed.
	 * @param gen classname of the generator to be added. Must be on the classpath and must be a subclass of tools.mdsd.ecoreworkflow.mwe2lib.component.PackageLevelCodeFileGenerator.
	 */
	public void addPackageLevelGenerator(String gen) {
		try {
			packageLevelGenerators.add((PackageLevelCodeFileGenerator) Class.forName(gen).getConstructor().newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	// This method is the workflow component's entry point.
	@Override
	protected void invokeInternal(WorkflowContext workflowContext, ProgressMonitor progressMonitor, Issues issues) {
		// ProgressMonitor is a (useless) NullProgressMonitor in the mwe2 context :(
		progressMonitor.beginTask("Creating additional templates", 20);
		
		GenModel loadedGenModel = loadGenModel(this.genModel);
		runGenerator(loadedGenModel);

		progressMonitor.done();
		
	}
	
	/**
	 * Load the genmodel as an ecore resource
	 * @param pathToGenmodelFile TODO
	 * @return
	 */
    private GenModel loadGenModel(String pathToGenmodelFile) {
        ResourceSet resSet = new ResourceSetImpl();
  		Resource resource = resSet.getResource(URI.createURI(pathToGenmodelFile), true);
  		GenModel genModel = (GenModelImpl) resource.getContents().get(0);
        return genModel;
    }
    
	private void runGenerator(GenModel genModel) {
		genModel.getGenPackages().forEach(this::generatePackageLevelCode);
	}
	
	private void generatePackageLevelCode(GenPackage genPackage) {
		Path path = Paths.get(new URIToPath().convertUri(URI.createURI(destPath)));
		for (PackageLevelCodeFileGenerator gen : packageLevelGenerators) {
			gen.setGenPackage(genPackage);
			Path outfilePath = path.resolve(gen.getRelativePath());
			
			try {
				Files.createDirectories(outfilePath.getParent());
			} catch (IOException e) {
				throw new RuntimeException("Parent directory could not be created", e);
			}
			
			try (FileOutputStream out = new FileOutputStream(outfilePath.toFile())) {
				gen.generateCode(out);
			} catch (IOException e) {
				throw new RuntimeException("Error writing the generated code", e);
			}
		}
	}
	
	
	
}
