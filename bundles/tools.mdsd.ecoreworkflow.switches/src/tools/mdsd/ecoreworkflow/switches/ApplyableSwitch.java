package tools.mdsd.ecoreworkflow.switches;

import org.eclipse.emf.ecore.EObject;

/**
 * can take an EObject and perform switching
 * @param <T>
 */
public interface ApplyableSwitch<T> {
  /**
   * 
   * @param object the input for the switch
   * @return the switches output
   */
  T doSwitch(EObject object);
}
