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

public class AdditionalTemplateGenerator extends AbstractWorkflowComponent2 {
	
	private String destPath;
	private String genModel;
	private List<PackageLevelCodeFileGenerator> packageLevelGenerators;
	
	
	public AdditionalTemplateGenerator() {
		packageLevelGenerators = new ArrayList<>();
	}
	
	@Mandatory
	public void setDestPath(String destPath) {
		this.destPath = destPath;
	}
	
	@Mandatory
	public void setGenModel(String genModel) {
		this.genModel = genModel;
	}
	
	public void addPackageLevelGenerator(String gen) {
		try {
			packageLevelGenerators.add((PackageLevelCodeFileGenerator) Class.forName(gen).getConstructor().newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void invokeInternal(WorkflowContext workflowContext, ProgressMonitor progressMonitor, Issues issues) {
		// ProgressMonitor is a (useless) NullProgressMonitor in the mwe2 context :(
		progressMonitor.beginTask("Creating additional templates", 20);
		
		ResourceSet resSet = new ResourceSetImpl();
		Resource resource = resSet.getResource(URI.createURI(genModel), true);
		GenModel genModel = (GenModelImpl) resource.getContents().get(0);
		
		runGenerator(genModel);

		progressMonitor.done();
		
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
