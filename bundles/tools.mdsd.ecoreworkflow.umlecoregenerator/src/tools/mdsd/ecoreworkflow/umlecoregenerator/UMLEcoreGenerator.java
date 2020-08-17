package tools.mdsd.ecoreworkflow.umlecoregenerator;

import org.eclipse.emf.codegen.ecore.generator.GeneratorAdapterFactory;
import org.eclipse.emf.codegen.ecore.generator.GeneratorAdapterFactory.Descriptor.Registry;
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

    @Override
    public void invoke(IWorkflowContext ctx) {
        /**
         * We do this registration globally because we cannot access the generator of the base
         * implementation to inject the adapter directly.
         */
        Registry registry = GeneratorAdapterFactory.Descriptor.Registry.INSTANCE;
        if (registry.getDescriptors(GenModelPackage.eNS_URI).isEmpty()) {
            registry.addDescriptor(GenModelPackage.eNS_URI,
                    new GeneratorAdapterDescriptor(getTypeMapper(), getLineDelimiter()));
        }
        super.invoke(ctx);
    }

}
