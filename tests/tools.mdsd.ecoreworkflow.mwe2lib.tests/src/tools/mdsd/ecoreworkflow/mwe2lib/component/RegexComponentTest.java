package tools.mdsd.ecoreworkflow.mwe2lib.component;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tools.mdsd.library.standalone.initialization.InitializationTask;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

class RegexComponentTest {
    static final String PROJECT_NAME = "some.test.project";
    static Path TEMP_FOLDER;
    static Path TEMP_FILE;

    @BeforeAll
    static void setUp() throws StandaloneInitializationException, IOException {
        TEMP_FOLDER = Files.createTempDirectory("test_").toAbsolutePath();
        TEMP_FILE = Files.createTempFile(TEMP_FOLDER, "some", "file");
        Files.writeString(TEMP_FILE, RegexComponentTest.class.toString());

        StandaloneInitializerBuilder.builder()
            .useEcoreClasspathDetection(true)
            .addCustomTask(new InitializationTask() {
                @Override
                public void initilizationWithoutPlatform() throws StandaloneInitializationException {
                    EcorePlugin.getPlatformResourceMap()
                        .put(PROJECT_NAME, URI.createURI(TEMP_FOLDER.toUri()
                            .toString()));
                }

                @Override
                public void initializationWithPlatform() throws StandaloneInitializationException {
                    var progMon = new NullProgressMonitor();
                    var project = ResourcesPlugin.getWorkspace()
                        .getRoot()
                        .getProject(PROJECT_NAME);
                    try {
                        if (project.exists())
                            project.delete(true, progMon);
                        project.create(progMon);
                        project.open(progMon);
                        var testFile = project.getFile(TEMP_FILE.getFileName()
                            .toString());
                        testFile.createLink(TEMP_FILE.toUri(), IResource.REPLACE, progMon);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            })
            .build()
            .init();
    }

    @Test
    void testFromFilename() throws Exception {
        var cut = new RegexComponent();
        var compareUri = URI.createURI(TEMP_FILE.toUri().toString());
               
        var replacement = new Replacement();
        replacement.setDirectory(TEMP_FOLDER.toString());
        replacement.addFilename(TEMP_FILE.getFileName().toString());
        var converted = cut.determineFilesToReplace(replacement);
        assertNotNull(converted);
        assertEquals(1, converted.size());
        assertEquals(compareUri, converted.get(0));
        
        replacement = new Replacement();
        replacement.addFilename(TEMP_FILE.toUri().toString());
        converted = cut.determineFilesToReplace(replacement);
        assertNotNull(converted);
        assertEquals(1, converted.size());
        assertEquals(compareUri, converted.get(0));
        
        replacement = new Replacement();
        replacement.setDirectory(TEMP_FOLDER.toString());
        replacement.setWildcard("some*file");
        converted = cut.determineFilesToReplace(replacement);
        assertNotNull(converted);
        assertEquals(1, converted.size());
        assertEquals(compareUri, converted.get(0));
        
        
        replacement = new Replacement();
        replacement.addFilename("platform:/resource/" + PROJECT_NAME + "/" + TEMP_FILE.getFileName()
            .toString());
        converted = cut.determineFilesToReplace(replacement);
        assertNotNull(converted);
        assertEquals(1, converted.size());
        var phrase = UUID.randomUUID().toString();
        cut.writeFile(compareUri, phrase);
        var content = cut.readFile(converted.get(0));
        assertEquals(phrase, content);
    }

}
