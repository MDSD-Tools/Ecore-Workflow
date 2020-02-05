package tools.mdsd.ecoreworkflow.switches;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import tools.mdsd.ecoreworkflow.switches.bytecodegen.ByteCodeSwitchCompiler;

public class BytecodeDynamicSwitch<T> extends AbstractInspectableDynamicSwitch<T> implements DynamicSwitch<T>, ApplyableSwitch<T>, InspectableSwitch<T> {
  
  private ApplyableSwitch<T> compiledSwitch;
  private Set<EPackage> explicitPackages = new HashSet<>();
  
  public BytecodeDynamicSwitch<T> precompile() {
    if (compiledSwitch == null) {
      compiledSwitch = new ByteCodeSwitchCompiler<T>(caseDefinitions, defaultCase, explicitPackages).compileSwitch();
    }
    return this;
  }
  
  public BytecodeDynamicSwitch<T> addPackage(EPackage ePackage) {
    explicitPackages.add(ePackage);
    return this;
  }
  
  @Override
  protected boolean canDafineCases() {
    return compiledSwitch == null;
  }

  @Override
  public T doSwitch(EObject object) {
    precompile();
    return compiledSwitch.doSwitch(object);
  }
}


