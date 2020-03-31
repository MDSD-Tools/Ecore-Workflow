package tools.mdsd.ecoreworkflow.switches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * A dynamic switch implementation that is optimized for being quick when the objects the graph is
 * applied to are hierarchically close to the types the cases are defined on.
 * 
 * <p>
 * Switching works by exploring an objects type hierarchy upwards until a match is found in the
 * table of registered cases.
 * </p>
 * 
 * <p>
 * Note that matching an object to the default cases therefore involves scanning its whole, possibly
 * complex, type hierarchy. On the other hand the lookup complexity is sublinear in the number of
 * registered cases.
 * </p>
 * 
 * @author Christian van Rensen
 *
 * @param <T> the return type of the switch's clauses
 */
public class HashDynamicSwitch<T> extends AbstractInspectableDynamicSwitch<T>
    implements DynamicSwitch<T>, ApplyableSwitch<T>, InspectableSwitch<T> {

  private Map<EClass, Function<EObject, T>[]> cachedInvokationSequences =
      new HashMap<>();
  
  @Override
  public T doSwitch(EObject object) {
    EClass eClass = object.eClass();

    // determine in which order to call which cases
    Function<EObject, T>[] targets = cachedInvokationSequences
        .computeIfAbsent(eClass, this::calculateInvocationSequence);
    
    // and then call those cases until one does not delegate.
    for (Function<EObject, T> target : targets) {
      T evaluation = target.apply(object);
      if (evaluation != null) {
        return evaluation;
      }

    }

    if (defaultCase != null) {
      return defaultCase.apply(object); // the default case will not fall through!
    }

    throw new SwitchingException("no default case defined");
  }
  
  @Override
  protected boolean canDafineCases() {
    return cachedInvokationSequences.isEmpty(); // adding cases to a used switch might cause synchronization issues
  }

  @SuppressWarnings("unchecked") /*
                                  * not problematic as the function we return comes from our
                                  * correctly typed map.
                                  */
  private Function<EObject, T>[] calculateInvocationSequence(EClass eClass) {

    // In a tree that only contains the longest possibly path to each ancestor, do a
    // breadth-first-search and add all results to a list of fall-through-targets.
    // This algorithm is compatible to EMF's semantics which were reverse-engineered.
    
    List<Function<EObject, T>> invocations = new ArrayList<>();
    new BreadthFirstSearch<EClass>().scan(eClass, (c, r) -> {
      // the second part of the condition ensures that we are exploring a longest path.
      if (caseDefinitions.containsKey(c) && r.stream().noneMatch(c::isSuperTypeOf)) {
        invocations.add(caseDefinitions.get(c));
      }
    }, EClass::getESuperTypes);

    return invocations.toArray(new Function[0]);
  }

}
