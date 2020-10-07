package tools.mdsd.ecoreworkflow.mwe2lib.component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
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
                replace(determineFilesToReplace(replacement), replacement.getMappings());
                arg1.worked(1);
            } catch (IOException e) {
                arg2.addError("Replacement failed.", e);
                return;
            }
        }
        arg1.done();
    }

    private void replace(List<URI> uris, Collection<Mapping> replacements) throws IOException {
        for (URI uri : uris) {
            var content = readFile(uri);
            for (Mapping replacement : replacements) {
                content = content.replaceAll(replacement.getFrom(), replacement.getTo());
            }
            writeFile(uri, content);
            LOG.info("Regex Replacement for:" + uri.toString());
        }
    }

    protected String readFile(URI fileUri) throws IOException {
        try (var stream = uriConverter.createInputStream(fileUri)) {
            return new String(stream.readAllBytes(), charset);
        }
    }

    protected void writeFile(URI fileUri, String content) throws IOException {
        try (var stream = uriConverter.createOutputStream(fileUri)) {
            stream.write(content.getBytes(charset));
        }
    }

    protected List<URI> determineFilesToReplace(Replacement replacement) throws IOException {
        Optional<URI> directory = Optional.ofNullable(replacement.getDirectory())
            .map(this::fromFilename);

        List<URI> filesToProcess = replacement.getFilenames()
            .stream()
            .map(this::fromFilename)
            .map(uri -> directory.map(this::ensureAbsoluteURI)
                .map(uri::resolve)
                .orElse(uri))
            .collect(Collectors.toList());

        if (replacement.getWildcard() != null && directory.isPresent()) {
            final PathMatcher matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + replacement.getWildcard());

            var dirPath = getAbsolutePathOfDirectory(directory.get());

            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    var relativeMatch = dirPath.relativize(file);
                    if (matcher.matches(relativeMatch)) {
                        filesToProcess.add(URI.createURI(file.toUri()
                            .toString()));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        return filesToProcess;
    }

    protected URI fromFilename(String filename) {
        var uri = parseURI(filename);
        return uri.orElseGet(() -> URI.createURI(Path.of(filename)
            .toUri()
            .toString()));
    }

    protected Optional<URI> parseURI(String uri) {
        try {
            var jUri = new java.net.URI(uri);
            return Optional.of(URI.createURI(jUri.toString()));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }

    }

    protected Path getAbsolutePathOfDirectory(URI uri) {
        Path result;
        if (uri.isPlatform()) {
            var platformString = uri.toPlatformString(true);
            if (Platform.isRunning()) {
                var path = new org.eclipse.core.runtime.Path(platformString);
                result = ResourcesPlugin.getWorkspace()
                    .getRoot()
                    .getFile(path)
                    .getLocation()
                    .toFile()
                    .toPath();
            } else {
                var resolvedUri = EcorePlugin.resolvePlatformResourcePath(platformString);
                result = getPathOfFileURI(resolvedUri);
            }
        } else if (uri.isFile() || uri.isRelative()) {
            result = getPathOfFileURI(uri);
        } else {
            throw new IllegalArgumentException(
                    "Could not resolve the URI of the base directory to a file path: " + uri.toString());
        }
        return result;
    }

    protected Path getPathOfFileURI(URI uri) {
        var jUri = java.net.URI.create(ensureAbsoluteURI(uri).toString());
        return Paths.get(jUri);
    }

    protected URI ensureAbsoluteURI(URI uri) {
        var jUri = java.net.URI.create(uri.toString());
        if (!jUri.isAbsolute()) {
            try {
                jUri = new java.net.URI("file", jUri.getHost(), jUri.getPath(), jUri.getFragment());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return URI.createURI(jUri.toString());
    }
}
