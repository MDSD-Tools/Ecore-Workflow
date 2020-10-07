package tools.mdsd.ecoreworkflow.mwe2lib.component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
import org.eclipse.emf.mwe.utils.Mapping;

public class RegexComponent extends AbstractWorkflowComponent2 {

    private static final Log LOG = LogFactory.getLog(RegexComponent.class);
    private final Collection<Replacement> replacements = new ArrayList<>();
    protected URIConverter uriConverter = new ExtensibleURIConverterImpl();

    private Charset charset = StandardCharsets.UTF_8;

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public void addReplacement(Replacement replacement) {
        replacements.add(replacement);
    }

    @Override
    protected void invokeInternal(WorkflowContext arg0, ProgressMonitor arg1, Issues arg2) {
        arg1.beginTask("Replacing patterns for files", replacements.size());
        for (Replacement replacement : replacements) {
            try {
                Optional<Path> directory = Optional.ofNullable(replacement.getDirectory())
                    .map(this::fromFilename);

                List<Path> filesToProcess = replacement.getFilenames()
                    .stream()
                    .map(this::fromFilename)
                    .map(path -> {
                        if ((!path.isAbsolute()) && directory.isPresent()) {
                            return directory.get()
                                .resolve(path);
                        }
                        return path;
                    })
                    .collect(Collectors.toList());

                if (replacement.getWildcard() != null && directory.isPresent()) {
                    final PathMatcher matcher = FileSystems.getDefault()
                        .getPathMatcher("glob:" + replacement.getWildcard());

                    Files.walkFileTree(directory.get(), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (matcher.matches(file)) {
                                filesToProcess.add(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
                replace(filesToProcess, replacement.getMappings());
                arg1.worked(1);
            } catch (IOException e) {
                arg2.addError("Replacement failed.", e);
                return;
            }
        }
        arg1.done();
    }

    private void replace(List<Path> paths, Collection<Mapping> replacements) throws IOException {
        for (Path filePath : paths) {
            String content = new String(Files.readAllBytes(filePath), charset);
            for (Mapping replacement : replacements) {
                content = content.replaceAll(replacement.getFrom(), replacement.getTo());
            }
            Files.write(filePath, content.getBytes(charset));
            LOG.info("Regex Replacement for:" + filePath.toString());
        }
    }

    protected Path fromFilename(String filename) {
        Path result = null;
        try {
            // Try resolve the path string directly as path
            result = Path.of(filename);
        } catch (InvalidPathException e1) {
            try {
                // Otherwise, try resolving the backing folder pointed to by the URI directly
                result = Path.of(convertUri(URI.createURI(filename)));
            } catch (InvalidPathException e2) {
                try {
                    // Otherwise, try resolving the backing folder pointed to by the URI through
                    // java.net.URI.
                    result = Path.of(java.net.URI.create(convertUri(URI.createURI(filename))));
                } catch (InvalidPathException e3) {
                    LOG.error("Could not resolve filename: " + filename);
                    throw e3;
                }
            }
        }
        return result;
    }

    protected String convertUri(URI uri) {
        if (uri.isPlatform()) {
            if (Platform.isRunning()) {
                return ResourcesPlugin.getWorkspace()
                    .getRoot()
                    .getFile(new org.eclipse.core.runtime.Path(uri.toPlatformString(true)))
                    .getLocation()
                    .toString();
            } else {
                return EcorePlugin.resolvePlatformResourcePath(uri.toPlatformString(true))
                    .toString();
            }
        } else {
            return uriConverter.normalize(uri)
                .toString();
        }
    }

}
