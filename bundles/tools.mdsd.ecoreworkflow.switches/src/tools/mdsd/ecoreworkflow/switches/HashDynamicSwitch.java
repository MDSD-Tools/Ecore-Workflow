package tools.mdsd.ecoreworkflow.switches;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import tools.mdsd.ecoreworkflow.switches.MSwitch.SwitchingException;

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
public class HashDynamicSwitch<T>
    implements DynamicSwitch<T>, ApplyableSwitch<T>, InspectableSwitch<T> {

  private Map<EClass, Function<EObject, T>> caseDefinitions = new LinkedHashMap<>();
  private ConcurrentMap<EClass, Function<EObject, T>[]> cachedInvokationSequences =
      new ConcurrentHashMap<>();
  private Function<EObject, T> defaultCase;
  private static final EClass E_OBJECT_CLASS = EcorePackage.Literals.EOBJECT;


  @Override
  public DynamicSwitch<T> dynamicCase(EClass clazz, Function<EObject, T> then) {
    if (!cachedInvokationSequences.isEmpty()) { // adding cases might cause synchronization issues
      throw new IllegalStateException("The switch was modified after already being used");
    }
    if (E_OBJECT_CLASS.equals(clazz)) {
      // special treatment necessary, because EObject might not be caught otherwise.
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
  public T doSwitch(EObject object) {
    EClass eClass = object.eClass();

    Function<EObject, T>[] targets = cachedInvokationSequences
        .computeIfAbsent(eClass, this::calculateInvocationSequence);

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

  @SuppressWarnings("unchecked") /*
                                  * not problematic as the function we return comes from our
                                  * correctly typed map.
                                  */
  private Function<EObject, T>[] calculateInvocationSequence(EClass eClass) {

    // In a tree that only contains the longest possibly path to each ancestor, do a
    // breadth-first-search and add all results to a list of fall-through-targets.
    List<Function<EObject, T>> invocations = new ArrayList<>();
    new BreadthFirstSearch<EClass>().scan(eClass, (c, r) -> {
      if (caseDefinitions.containsKey(c) && r.stream().noneMatch(c::isSuperTypeOf)) {
        invocations.add(caseDefinitions.get(c));
      }
    }, EClass::getESuperTypes);

    return invocations.toArray(new Function[0]);
  }

  @Override
  public Map<EClass, Function<EObject, T>> getCases() {
    return Collections.unmodifiableMap(caseDefinitions);
  }

  @Override
  public Function<EObject, T> getDefaultCase() {
    return defaultCase;
  }

}