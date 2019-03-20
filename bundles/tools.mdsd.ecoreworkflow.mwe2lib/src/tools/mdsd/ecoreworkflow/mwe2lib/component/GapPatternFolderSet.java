package tools.mdsd.ecoreworkflow.mwe2lib.component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class GapPatternFolderSet {
	
	final Collection<String> manualSourceFolders = new LinkedList<>();
	final Collection<String> generatedSourceFolders = new LinkedList<>();
	
	public void addSrc(String manualSourceFolder) {
		if (!manualSourceFolders.contains(manualSourceFolder))
			manualSourceFolders.add(manualSourceFolder);
	}
	
	public void addGen(String generatedSourceFolder) {
		if (!generatedSourceFolders.contains(generatedSourceFolder))
			generatedSourceFolders.add(generatedSourceFolder);
	}

	/**
	 * @return the manualSourceFolders
	 */
	public Collection<String> getManualSourceFolders() {
		return Collections.unmodifiableCollection(manualSourceFolders);
	}

	/**
	 * @return the generatedSourceFolders
	 */
	public Collection<String> getGeneratedSourceFolders() {
		return Collections.unmodifiableCollection(generatedSourceFolders);
	}
	
	
}
