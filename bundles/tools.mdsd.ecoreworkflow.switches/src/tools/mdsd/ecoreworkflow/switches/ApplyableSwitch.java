package tools.mdsd.ecoreworkflow.switches;

import org.eclipse.emf.ecore.EObject;

/**
 * can take an EObject and perform switching.
 * 
 * @param <T> the return type of the switch's clauses
 */
public interface ApplyableSwitch<T> {
  
  /**
   * takes an eObject and applies the best-matching case.
   * @param object the input for the switch
   * @return the switches output
   */
  T doSwitch(EObject object);
}
