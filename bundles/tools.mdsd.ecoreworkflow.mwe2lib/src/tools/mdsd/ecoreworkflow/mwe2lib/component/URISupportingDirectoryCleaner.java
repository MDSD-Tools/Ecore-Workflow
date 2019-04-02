package tools.mdsd.ecoreworkflow.mwe2lib.component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.emf.mwe.utils.DirectoryCleaner;

public class URISupportingDirectoryCleaner extends DirectoryCleaner {
	URIConverter uriConverter = new ExtensibleURIConverterImpl();
	
	protected List<URI> uris = new ArrayList<>();
	
	public void addDirectory(String string) {
		uris.add(URI.createURI(string));
	}
	
	@Override
	protected void invokeInternal(WorkflowContext model, ProgressMonitor monitor, Issues issues) {
		this.setDirectory(uris.stream().map(this::convertUri).collect(Collectors.joining(",")));
		
		super.invokeInternal(model, monitor, issues);
	}
	
	protected String convertUri(URI uri) {
		if (uri.isPlatform()) {
			if (Platform.isRunning()) {
				return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(uri.toPlatformString(true))).getLocation().toString();
			} else {
				return EcorePlugin.resolvePlatformResourcePath(uri.toPlatformString(true)).toFileString();
			}
		}            	
		else {
			return uriConverter.normalize(uri).toFileString();
		}
	}
	
	
	
	

}
