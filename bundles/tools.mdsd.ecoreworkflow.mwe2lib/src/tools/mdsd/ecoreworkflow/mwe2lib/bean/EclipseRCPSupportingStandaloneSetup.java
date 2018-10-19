package tools.mdsd.ecoreworkflow.mwe2lib.bean;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.mwe.utils.StandaloneSetup;

public class EclipseRCPSupportingStandaloneSetup extends StandaloneSetup {
	
	@Override
	protected boolean scanFolder(File f) {
		// Only scan for projects if running standalone
		if (!Platform.isRunning()) return super.scanFolder(f);
		else return true;
	}
	
	@Override
	protected void registerMapping(String name, URI uri) {
		// Do not modify the Ecore Platform mapping if the platform is running
		if (!Platform.isRunning()) super.registerMapping(name, uri);
	}

}
