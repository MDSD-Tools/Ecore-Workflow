package tools.mdsd.ecoreworkflow.switches.tests.builders;

import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.HashDynamicSwitch;

public class HashDynamicSwitchBuilder<T> implements SwitchBuilder<T, DynamicSwitch<T>> {
  private HashDynamicSwitch<T> delegateTo = new HashDynamicSwitch<>();
  
  @Override
  public void addCase(EClass clazz, Function<EObject, T> then) {
    delegateTo.dynamicCase(clazz, then);
  }

  @Override
  public void setDefaultCase(Function<EObject, T> then) {
    delegateTo.defaultCase(then);
  }

  @Override
  public HashDynamicSwitch<T> build() {
    return delegateTo;
  }

}
