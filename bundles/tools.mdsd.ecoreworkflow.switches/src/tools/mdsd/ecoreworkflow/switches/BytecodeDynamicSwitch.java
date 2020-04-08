package tools.mdsd.ecoreworkflow.switches;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import tools.mdsd.ecoreworkflow.switches.bytecodegen.ByteCodeSwitchCompiler;

/**
 * A dynamic switch that is accelerated using bytecode generation.
 * Note that for this to work, knowledge of the complete package hierarchy that might
 * be fed into the doSwitch method is required. Use the addPackage-method to add the
 * possible packages.
 *
 * @param <T> the return type of the case methods
 */
public class BytecodeDynamicSwitch<T> extends AbstractInspectableDynamicSwitch<T> implements DynamicSwitch<T>, ApplyableSwitch<T>, InspectableSwitch<T> {
  
  /**
   * a pre-compiled switch to which we delegate all doSwitch calls
   */
  private ApplyableSwitch<T> compiledSwitch;
  private Set<EPackage> explicitPackages = new HashSet<>();
  
  public BytecodeDynamicSwitch<T> precompile() {
    if (compiledSwitch == null) {
      compiledSwitch = new ByteCodeSwitchCompiler<T>(caseDefinitions, defaultCase, explicitPackages).compileSwitch();
    }
    return this;
  }
  
  /**
   * Tell the switch that objects fed into the switch might have
   * a dynamic type of a class in the given package.
   * 
   * If a package's class is part of a case definition, that package is
   * already implicitly added and it is not necessary to call addPackage
   * 
   * @param ePackage
   * @return itself (builder pattern)
   */
  public BytecodeDynamicSwitch<T> addPackage(EPackage ePackage) {
    explicitPackages.add(ePackage);
    return this;
  }
  
  @Override
  protected boolean canDafineCases() {
    /** don't allow to define more cases once the switch has been compiled.
        Clients who need this can merge the switch into a new one and add their
       cases there. **/
    return compiledSwitch == null;
  }

  @Override
  public T doSwitch(EObject object) {
    precompile();
    return compiledSwitch.doSwitch(object);
  }
}


