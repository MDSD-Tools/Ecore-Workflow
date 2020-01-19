package tools.mdsd.ecoreworkflow.switches.tests.builders;

import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public interface SwitchBuilder<T, S> {
  void addCase(EClass clazz, Function<EObject, T> then);
  void setDefaultCase(Function<EObject, T> then);
  S build();
}
