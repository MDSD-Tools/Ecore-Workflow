package tools.mdsd.ecoreworkflow.switches;

import java.util.List;
import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * Base class for static (=package specific) switches generated with the MSwitchClassGenerator.
 *
 * @param <T> return type of the case methods
 */
public abstract class MSwitch<T> implements ApplyableSwitch<T>, InspectableSwitch<T> {
  protected Function<EObject, T> defaultCase;

  public MSwitch() {
    super();
  }

  public T doSwitch(EObject s) {
    return doSwitch(s.eClass(), s);
  }
  
  protected T doSwitch(EClass eClass, EObject eObject) {
    if (isSwitchFor(eClass.getEPackage())) {
      return doSwitch(eClass.getClassifierID(), eObject);
    } else { // logic as in Ecore: when a type comes from an unknown child package, climb up to its first supertype.
      // This is NOT 100% compliant to the semantics of DynamicSwitch in a cross package setting.
      // However, use cases where this matters should be very rare, because static switches are used for intra-package switching.
      // It would be even uglier if the behaviour of dynamic switching depended on package boundaries.
      List<EClass> eSuperTypes = eClass.getESuperTypes();
      return eSuperTypes.isEmpty() ? applyDefaultCase(eObject) : doSwitch(eSuperTypes.get(0), eObject);
    }
  }
  
  protected abstract T doSwitch(int classifierID, EObject eObject) throws SwitchingException;
  
  @Override
  public Function<EObject, T> getDefaultCase() {
    return defaultCase;
  }
  
  protected T applyDefaultCase(EObject eObject) {
    if (defaultCase != null) {
      return defaultCase.apply(eObject); // the default case will not fall through
    } else {
      throw new SwitchingException("no default case defined");
    }
  }

  public abstract boolean isSwitchFor(EPackage ePackage);

}
