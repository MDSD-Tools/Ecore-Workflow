package tools.mdsd.ecoreworkflow.mwe2lib.component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.lib.AbstractWorkflowComponent2;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;

public class GapPatternPostProcessor extends AbstractWorkflowComponent2 {
	private static final String JAVAFILE_MATCHER_PATTERN = 
			"([^\\n\\s]*).java$";

	private static final Log LOG = LogFactory.getLog(GapPatternPostProcessor.class);
	private final Collection<GapPatternFolderSet> folders = new LinkedList<>();
	private String searchPattern = "(?<!\\bnew\\W)\\b(%s)(?=[^a-zA-Z\\d_$])(?!\\W+eINSTANCE\\b)";
	private String replacementPattern = "$1Gen";
	protected URIConverter uriConverter = new ExtensibleURIConverterImpl();

	private Charset charset = StandardCharsets.UTF_8;
	
	public void setCharset(Charset charset) {
		this.charset = charset;
	}
	
	public void addFolders(GapPatternFolderSet folders) {
		this.folders.add(folders);
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	public void setReplacementPattern(String replacementPattern) {
		this.replacementPattern = replacementPattern;
	}

	@Override
	protected void invokeInternal(WorkflowContext arg0, ProgressMonitor arg1, Issues arg2) {
		arg1.beginTask("Starting Gap-Pattern post processing", folders.size());
		for (GapPatternFolderSet set : folders) {
			try {
				List<Path> manualFolders = set.getManualSourceFolders().stream()
						.map(URI::createURI)
						.map(this::convertUri)
						.map(Paths::get).collect(Collectors.toList());
				List<Path> generatedFolders = set.getGeneratedSourceFolders().stream()
						.map(URI::createURI)
						.map(this::convertUri)
						.map(Paths::get)
						.collect(Collectors.toList());
				
				List<Path> relativePathsToCheck = new LinkedList<>();
				for (Path manFolder: manualFolders) {
					Files.walkFileTree(manFolder, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path arg0, BasicFileAttributes arg1) throws IOException {
							relativePathsToCheck.add(manFolder.relativize(arg0));
							return FileVisitResult.CONTINUE;
						}
					});
				}
				
				List<Path> filesToPostProcess = new LinkedList<>();
				for (Path genFolder: generatedFolders) {
					Files.walkFileTree(genFolder, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path arg0, BasicFileAttributes arg1) throws IOException {
							Path relPath = genFolder.relativize(arg0);
							if (relativePathsToCheck.contains(relPath)) {
								filesToPostProcess.add(genFolder.resolve(relPath));
							}
							return FileVisitResult.CONTINUE;
						}
					});
				}
				
				for (Path processFile: filesToPostProcess) {
					String oldClassName = processFile.getFileName().toString().replaceAll(".java", "");
					String className = processFile.getFileName().toString().replaceAll(JAVAFILE_MATCHER_PATTERN,
							replacementPattern);
					Path targetPath = processFile.getParent().resolve(Paths.get(className + ".java"));
					LOG.info(String.format("Renaming %s to %s", processFile.toString(), targetPath.toString()));
					Files.move(processFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
					
					String content = new String(Files.readAllBytes(targetPath), charset);
					content = content.replaceAll(String.format(searchPattern, oldClassName), 
							replacementPattern);
					
					Files.write(targetPath, content.getBytes(charset));
					
				}
				arg1.worked(1);
			} catch (IOException e) {
				arg2.addError("Replacement failed.", e);
				return;
			}
		}
		arg1.done();
	}

	protected String convertUri(URI uri) {
		if (uri.isPlatform()) {
			if (Platform.isRunning()) {
				return ResourcesPlugin.getWorkspace().getRoot()
						.getFile(new org.eclipse.core.runtime.Path(uri.toPlatformString(true))).getLocation()
						.toString();
			} else {
				return EcorePlugin.resolvePlatformResourcePath(uri.toPlatformString(true)).toFileString();
			}
		} else {
			return uriConverter.normalize(uri).toFileString();
		}
	}

}
