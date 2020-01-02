package tools.mdsd.ecoreworkflow.switches;

import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * a switch upon which cases may be defined dynamically.
 *
 * @param <T> the return type of the switch's clauses
 */
/* inheriting from ApplyableSwitch, InspectableSwitch is against single-concern principles,
   but is necessary for the builder pattern */
public interface DynamicSwitch<T> extends ApplyableSwitch<T>, InspectableSwitch<T> {
  /**
   * add a dynamically specified case clause to the switch.
   * 
   * @param clazz the class for which to define the case
   * @param then the functional body of the case clause
   * @return {@code this}
   */
  DynamicSwitch<T> dynamicCase(EClass clazz, Function<EObject, T> then);

  /**
   * define the default case clause for the switch.
   * 
   * @param then the functional body of the case clause
   * @return {@code this}
   */
  DynamicSwitch<T> defaultCase(Function<EObject, T> then);

  /**
   * merge another switch into this switch. equivalent to defining all of the other switches cases
   * and default case on this switch
   * 
   * @param other the other switch
   * @return {@code this}
   */
  default DynamicSwitch<T> merge(InspectableSwitch<? extends T> other) {
    other.getCases().forEach((clazz, then) -> {
      this.dynamicCase(clazz, then::apply); // up-casting if necessary
    });
    Function<EObject, ? extends T> defaultCase = other.getDefaultCase();
    if (defaultCase != null) {
      defaultCase(defaultCase::apply); // up-casting if necessary
    }
    return this;
  }


}
