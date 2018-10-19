package tools.mdsd.ecoreworkflow.mwe2lib.component;

import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.mwe.utils.Mapping;

public class ContextDependentMapping extends Mapping {
	public void setOnRunningPlatform(String value) {
		if (Platform.isRunning()) {
			this.setTo(value);
		} else {
			this.setFrom(value);
		}
	}
	
	public void setOnStandalone(String value) {
		if (Platform.isRunning()) {
			this.setFrom(value);
		} else {
			this.setTo(value);
		}
	}
}
