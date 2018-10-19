package tools.mdsd.ecoreworkflow.builder;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.mwe2.language.Mwe2StandaloneSetup;
import org.eclipse.emf.mwe2.language.resource.MweResourceSetProvider;
import org.eclipse.emf.mwe2.launch.runtime.Mwe2Runner;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.service.GrammarProvider;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;

public class MWE2WorkflowBuilder extends IncrementalProjectBuilder {
	private static final String CONFIGURATION_PARAMETER_WORKFLOW_DEFINITION = "tools.mdsd.ecoreworkflow.builder.workflowdefinition";
	
	private static final Set<String> FILE_EXTENSIONS_TO_WATCH = Collections.unmodifiableSet(new HashSet<>(
			Arrays.asList(
					"ecore",
					"genmodel")));
	
	private boolean relevantResourceHasChanged = false;

	public MWE2WorkflowBuilder() {
	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		
		if (args == null || !args.containsKey(CONFIGURATION_PARAMETER_WORKFLOW_DEFINITION)) {
			Activator.getDefault().getLog().log(new Status(Status.WARNING, Activator.PLUGIN_ID, 
					String.format("The mwe2 worflow builder is registered for the project (%s) but no workflow description is provided.", getProject().getName())));
			return null;
		}
		this.relevantResourceHasChanged = kind == FULL_BUILD || kind == CLEAN_BUILD; 
		if (!this.relevantResourceHasChanged) checkIfBuildIsNecessary(monitor);
		
		if (this.relevantResourceHasChanged) {
			return doBuild(args, monitor);
		}
		
		return new IProject[] {getProject()};
	}
	
	protected void checkIfBuildIsNecessary(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Checking the deltas... ", IProgressMonitor.UNKNOWN);
		IResourceDelta delta = getDelta(getProject());
		
		if (delta != null) {
			delta.accept(new IResourceDeltaVisitor() {
				
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					if (FILE_EXTENSIONS_TO_WATCH.contains(delta.getFullPath().getFileExtension())) {
						MWE2WorkflowBuilder.this.relevantResourceHasChanged = true;
						return false;
					}
					return true;
				}
			});
		} else {
			this.relevantResourceHasChanged = true;
		}
		
		monitor.done();
	}

	private static class MweXtextResourceSetProviderHack extends MweResourceSetProvider {
		@Override
		public XtextResourceSet get() {
			XtextResourceSet result = super.get();
			result.setClasspathURIContext(MWE2WorkflowBuilder.class.getClassLoader());
			return result;
		}
	}
	
	protected IProject[] doBuild(Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		
		URI locationURI = getProject().getLocationURI().resolve(args.get(CONFIGURATION_PARAMETER_WORKFLOW_DEFINITION));
		Path path = Paths.get(locationURI);
		if (!path.toFile().exists()) {
			Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, 
					String.format("The specified mwe2 worflow description (%s) does not exists.", locationURI.toString())));
		}
		monitor.beginTask(String.format("Starting MWE2 Workflow %s", path.toString()), IProgressMonitor.UNKNOWN);
		
		OperationCanceledException.class.getName();
				
		Injector injector = (new Mwe2StandaloneSetup() {
			@Override
			public Injector createInjector() {
				return Guice.createInjector(new org.eclipse.emf.mwe2.language.Mwe2RuntimeModule() {
					@Override
					public Class<? extends Provider<XtextResourceSet>> provideXtextResourceSet() {
						return MweXtextResourceSetProviderHack.class;
					}
				});
			}
		}).createInjectorAndDoEMFRegistration();
		GrammarProvider provider = injector.getInstance(GrammarProvider.class);
		provider.setClassLoader(this.getClass().getClassLoader());
		
		Map<String, String> parameters = new HashMap<>();
		parameters.put("workspaceRoot", getProject().getLocation().append("..").toString());
		Mwe2Runner mweRunner = injector.getInstance(Mwe2Runner.class);
		mweRunner.run(org.eclipse.emf.common.util.URI.createURI(locationURI.toString()), parameters);
		
		for (IProject p : getProject().getWorkspace().getRoot().getProjects()) {
			p.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}
	
		monitor.done();
		return new IProject[] {getProject()};
	}

}
