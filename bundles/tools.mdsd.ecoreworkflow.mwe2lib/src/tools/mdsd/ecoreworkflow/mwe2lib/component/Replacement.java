package tools.mdsd.ecoreworkflow.mwe2lib.component;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.mwe.utils.Mapping;

public class Replacement {

	private String directory = null;
	private String wildcard = null;
	
	private Collection<String> filenames = new ArrayList<>();
	private Collection<Mapping> mappings = new ArrayList<>();
	

	public void addFilename(String filename) {
		filenames.add(filename);
	}
	
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	/**
	 * @param wildcard the wildcard to set
	 */
	public void setWildcard(String wildcard) {
		this.wildcard = wildcard;
	}
	
	/**
	 * @return the directory
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * @return the wildcard
	 */
	public String getWildcard() {
		return wildcard;
	}

	public Collection<String> getFilenames() {
		return filenames;
	}
	public Collection<Mapping> getMappings() {
		return mappings;
	}
	public void addMapping(Mapping mapping) {
		mappings.add(mapping);
	}
	
}
