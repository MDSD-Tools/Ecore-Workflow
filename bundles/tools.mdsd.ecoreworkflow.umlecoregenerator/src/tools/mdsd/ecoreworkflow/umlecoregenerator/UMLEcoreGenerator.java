package tools.mdsd.ecoreworkflow.umlecoregenerator;

import org.eclipse.emf.codegen.ecore.generator.GeneratorAdapterFactory;
import org.eclipse.emf.mwe2.ecore.EcoreGenerator;
import org.eclipse.uml2.codegen.ecore.genmodel.GenModelPackage;

/**
 * Implementation of {@link EcoreGenerator} that is capable of generating code for ecore meta models
 * that do use the UML2 genmodel rather than the Ecore genmodel.
 */
public class UMLEcoreGenerator extends EcoreGenerator {

    static {
        GenModelPackage.eINSTANCE.getEFactoryInstance();
    }

    public UMLEcoreGenerator() {
        super();
        registerUMLGeneratorAdapterFactory();
    }

    protected void registerUMLGeneratorAdapterFactory() {
        GeneratorAdapterFactory.Descriptor.Registry.INSTANCE.addDescriptor(GenModelPackage.eNS_URI,
                new GeneratorAdapterDescriptor(getTypeMapper(), getLineDelimiter()));
    }

}
