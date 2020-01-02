package tools.mdsd.ecoreworkflow.switches;

import java.util.Map;
import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/** 
 * a switch that can list the cases upon which it is defined.
 * @param <T> the return type of the switch's clauses
 */
public interface InspectableSwitch<T> {
  Map<EClass, Function<EObject, T>> getCases();

  Function<EObject, T> getDefaultCase();
}
