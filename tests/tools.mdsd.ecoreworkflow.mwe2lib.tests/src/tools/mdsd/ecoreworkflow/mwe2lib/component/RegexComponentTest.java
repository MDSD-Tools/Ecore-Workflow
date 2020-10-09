package tools.mdsd.ecoreworkflow.mwe2lib.component;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.mdsd.library.standalone.initialization.InitializationTask;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

class RegexComponentTest {
    static final String PROJECT_NAME = "test.project." + UUID.randomUUID();
    static Path TEMP_FOLDER;
    static Path PARENT_TEMP_FOLDER;
    static Path TEMP_FILE;

    @BeforeAll
    static void setUp() throws StandaloneInitializationException, IOException {
        PARENT_TEMP_FOLDER = Files.createTempDirectory("test_");
        TEMP_FOLDER = Files.createTempDirectory(PARENT_TEMP_FOLDER, "");
        TEMP_FILE = Files.createTempFile(TEMP_FOLDER, "some", "file");

        StandaloneInitializerBuilder.builder()
            .useEcoreClasspathDetection(true)
            .addCustomTask(new InitializationTask() {
                @Override
                public void initilizationWithoutPlatform() throws StandaloneInitializationException {
                    EcorePlugin.getPlatformResourceMap()
                        .put(PROJECT_NAME, URI.createURI(PARENT_TEMP_FOLDER.toUri()
                            .toString()));
                }

                @Override
                public void initializationWithPlatform() throws StandaloneInitializationException {
                    var progMon = new NullProgressMonitor();
                    var project = ResourcesPlugin.getWorkspace()
                        .getRoot()
                        .getProject(PROJECT_NAME);
                                        
                    try {
                        var projectDesc = ResourcesPlugin.getWorkspace().newProjectDescription(PROJECT_NAME);
                        projectDesc.setLocationURI(PARENT_TEMP_FOLDER.toUri());
                        project.create(projectDesc, progMon);
                        project.open(progMon);
                        var tmpFolder = project.getFolder(TEMP_FOLDER.getFileName().toString());
                        tmpFolder.createLink(TEMP_FOLDER.toUri(), IResource.REPLACE, progMon);
                    } catch (CoreException e) {
                        throw new StandaloneInitializationException("Error occured during initialization", e);
                    }
                }
            })
            .build()
            .init();
    }
    
    @AfterAll
    static void tearDown() throws IOException, CoreException, StandaloneInitializationException {
        StandaloneInitializerBuilder.builder()
            .addCustomTask(new InitializationTask() {
                @Override
                public void initilizationWithoutPlatform() throws StandaloneInitializationException {
                    try {
                        Files.delete(TEMP_FILE);
                        Files.delete(TEMP_FOLDER);
                        Files.delete(PARENT_TEMP_FOLDER);
                    } catch (IOException e) {
                        throw new StandaloneInitializationException("Error occured during tear down", e);
                    }
                    
                }
                
                @Override
                public void initializationWithPlatform() throws StandaloneInitializationException {
                    var progMon = new NullProgressMonitor();
                    try {
                        ResourcesPlugin.getWorkspace()
                            .getRoot()
                            .getProject(PROJECT_NAME)
                            .delete(true, progMon);
                    } catch (CoreException e) {
                        throw new StandaloneInitializationException("Error occured during tear down", e);
                    }
                }
            }).build().init();
    }
    
    protected RegexComponent cut;
    
    @BeforeEach
    void setUpComponentUnderTest() {
        this.cut = new RegexComponent();
    }

    protected void runTest(Optional<String> directory, Optional<String> filename, Optional<String> wildcard, Consumer<URI> resultValidator) throws IOException {
        var replacement = new Replacement();
        directory.ifPresent(replacement::setDirectory);
        filename.ifPresent(replacement::addFilename);
        wildcard.ifPresent(replacement::setWildcard);
        
        var converted = cut.determineFilesToReplace(replacement);
        assertNotNull(converted);
        assertEquals(1, converted.size());
        
        resultValidator.accept(converted.get(0));
    }
    
    @Test
    void testDirectoryAndFilename() throws Exception {
        runTest(Optional.of(TEMP_FOLDER.toString()), 
                Optional.of(TEMP_FILE.getFileName().toString()), 
                Optional.empty(), 
                uri -> assertEquals(URI.createURI(TEMP_FILE.toUri().toString()), uri));
    }
    
    @Test
    void testAbsoluteFilenameAsPath() throws Exception {
        runTest(Optional.empty(), 
                Optional.of(TEMP_FILE.toString()), 
                Optional.empty(), 
                uri -> assertEquals(URI.createURI(TEMP_FILE.toUri().toString()), uri));
    }
    
    @Test
    void testAbsoluteFilenameAsFileURI() throws Exception {
        runTest(Optional.empty(), 
                Optional.of(TEMP_FILE.toUri().toString()), 
                Optional.empty(), 
                uri -> assertEquals(URI.createURI(TEMP_FILE.toUri().toString()), uri));
    }
    
    @Test
    void testDirectoryAndAbsoluteFilename() throws Exception {
        runTest(Optional.of(TEMP_FOLDER.toString()), 
                Optional.of(TEMP_FILE.toString()), 
                Optional.empty(), 
                uri -> assertEquals(URI.createURI(TEMP_FILE.toUri().toString()), uri));
    }
    
    @Test
    void testWildcard() throws Exception {
        runTest(Optional.of(TEMP_FOLDER.toString()), 
                Optional.empty(), 
                Optional.of("some*file"), 
                uri -> assertEquals(URI.createURI(TEMP_FILE.toUri().toString()), uri));
    }
    
    @Test
    void testWildcardWithPlatformURI() throws Exception {
        runTest(Optional.of(String.format("platform:/resource/%s/%s", PROJECT_NAME, TEMP_FOLDER.getFileName())), 
                Optional.empty(), 
                Optional.of("some*file"), 
                uri -> assertEquals(URI.createURI(TEMP_FILE.toUri().toString()), uri));
    }
    
    @Test
    void testReadPlatformURI() throws Exception {
        var phrase = UUID.randomUUID().toString();
        Files.writeString(TEMP_FILE, phrase);
        runTest(Optional.empty(), 
                Optional.of(String.format("platform:/resource/%s/%s/%s", PROJECT_NAME, TEMP_FOLDER.getFileName(), TEMP_FILE.getFileName())), 
                Optional.empty(), 
                uri -> {
                    try {
                        assertEquals(phrase, cut.readFile(uri));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
