package tools.mdsd.ecoreworkflow.mwe2lib.util;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;

/**
 * utility class for converting platform: and other URIs to absolute file paths 
 * (method copied from {@link tools.mdsd.ecoreworkflow.mwe2lib.component.GapPatternPostProcessor})
 * 
 *
 */

public class URIToPath {
	public String convertUri(URI uri) {
		if (uri.isPlatform()) {
			if (Platform.isRunning()) {
				return ResourcesPlugin.getWorkspace().getRoot()
						.getFile(new org.eclipse.core.runtime.Path(uri.toPlatformString(true))).getLocation()
						.toString();
			} else {
				return EcorePlugin.resolvePlatformResourcePath(uri.toPlatformString(true)).toFileString();
			}
		} else {
			URIConverter uriConverter = new ExtensibleURIConverterImpl();
			return uriConverter.normalize(uri).toFileString();
		}
	}
}
