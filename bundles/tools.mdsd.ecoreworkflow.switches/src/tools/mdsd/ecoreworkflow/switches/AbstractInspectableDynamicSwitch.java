package tools.mdsd.ecoreworkflow.switches;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;

/**
 * an implementation of DynamicSwitch and InspectableSwitch that serves as a base class for dynamic switch implementations
 * to which it leaves the actual implementation of the doSwitch method
 *
 * @param <T> the result type of the case methods
 */
public abstract class AbstractInspectableDynamicSwitch<T> implements DynamicSwitch<T>, InspectableSwitch<T> {

  protected Map<EClass, Function<EObject, T>> caseDefinitions = new LinkedHashMap<>();
  protected Function<EObject, T> defaultCase;
  private static final EClass E_OBJECT_CLASS = EcorePackage.Literals.EOBJECT;

  @Override
  public DynamicSwitch<T> dynamicCase(EClass clazz, Function<EObject, T> then) {
    if (!canDafineCases()) {
      throw new IllegalStateException("The switch was modified after already being used");
    }
    if (E_OBJECT_CLASS.equals(clazz)) {
      // special treatment necessary, because EObject might not be caught otherwise (EObject is not always returned by eGetSupertypes()).
      defaultCase(then);
    } else {
      caseDefinitions.put(clazz, then);
    }
    return this;
  }

  @Override
  public DynamicSwitch<T> defaultCase(Function<EObject, T> then) {
    this.defaultCase = then;
    return this;
  }

  @Override
  public Map<EClass, Function<EObject, T>> getCases() {
    return Collections.unmodifiableMap(caseDefinitions);
  }

  @Override
  public Function<EObject, T> getDefaultCase() {
    return defaultCase;
  }
  
  protected abstract boolean canDafineCases();

}
