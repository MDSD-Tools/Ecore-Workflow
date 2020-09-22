package tools.mdsd.ecoreworkflow.umlecoregenerator;

import org.apache.log4j.Logger;
import org.eclipse.emf.codegen.ecore.generator.Generator;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
import org.eclipse.emf.codegen.merge.java.JControlModel;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.mwe2.ecore.EcoreGenerator;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;
import org.eclipse.uml2.codegen.ecore.genmodel.GenModelPackage;

/**
 * Implementation of {@link EcoreGenerator} that is capable of generating code for ecore meta models
 * that do use the UML2 genmodel rather than the Ecore genmodel.
 */
public class UMLEcoreGenerator extends EcoreGenerator {

    static {
        GenModelPackage.eINSTANCE.getEFactoryInstance();
    }
    
    private static Logger log = Logger.getLogger(UMLEcoreGenerator.class);
    private final ResourceSet resSet = new ResourceSetImpl();
    private String genModel;
    private boolean generateModel = true;
    private boolean generateEdit;
    private boolean generateEditor;
    
    public UMLEcoreGenerator() {
        setResourceSet(resSet);
    }
    
    @Override
    public void setGenModel(String genModel) {
        this.genModel = genModel;
        super.setGenModel(genModel);
    }
    
    public void setGenerateModel(boolean generateModel) {
        this.generateModel = generateModel;
    }

    @Override
    public void setGenerateEdit(boolean generateEdit) {
        this.generateEdit = generateEdit;
        super.setGenerateEdit(generateEdit);
    }

    @Override
    public void setGenerateEditor(boolean generateEditor) {
        this.generateEditor = generateEditor;
        super.setGenerateEditor(generateEditor);
    }

    @Override
    public void invoke(IWorkflowContext ctx) {
        Resource resource = resSet.getResource(URI.createURI(genModel), true);
        final GenModel genModel = (GenModel) resource.getContents().get(0);
        genModel.setCanGenerate(true);
        genModel.reconcile();
        createGenModelSetup().registerGenModel(genModel);

        Generator generator = new Generator() {
            @Override
            public JControlModel getJControlModel() {
                return new JControlModel(){
                    @Override
                    public boolean canMerge() {
                        return false;
                    }
                };
            }
        };
        // registration of genmodel adapter for UML genmodels
        generator.getAdapterFactoryDescriptorRegistry().addDescriptor(GenModelPackage.eNS_URI,
                    new GeneratorAdapterDescriptor(getTypeMapper(), getLineDelimiter()));
        
        log.info("generating EMF code for "+this.genModel);
        generator.getAdapterFactoryDescriptorRegistry().addDescriptor(GenModelPackage.eNS_URI,
                new GeneratorAdapterDescriptor(getTypeMapper(), getLineDelimiter()));
        generator.setInput(genModel);

        // added condition for model code generation
        if (generateModel) {
            Diagnostic diagnostic = generator.generate(genModel, GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE,
                    new BasicMonitor());
            if (diagnostic.getSeverity() != Diagnostic.OK)
                log.info(diagnostic);            
        }

        if (generateEdit) {
            Diagnostic editDiag = generator.generate(genModel, GenBaseGeneratorAdapter.EDIT_PROJECT_TYPE,
                    new BasicMonitor());
            if (editDiag.getSeverity() != Diagnostic.OK)
                log.info(editDiag);
        }

        if (generateEditor) {
            Diagnostic editorDiag = generator.generate(genModel, GenBaseGeneratorAdapter.EDITOR_PROJECT_TYPE,
                    new BasicMonitor());
            if (editorDiag.getSeverity() != Diagnostic.OK)
                log.info(editorDiag);
        }
        
    }

}
