package tools.mdsd.ecoreworkflow.switches.tests.builders;

import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import tools.mdsd.ecoreworkflow.switches.BytecodeDynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;

public class BytecodeDynamicSwitchBuilder<T> implements SwitchBuilder<T, DynamicSwitch<T>> {
  private BytecodeDynamicSwitch<T> delegateTo = new BytecodeDynamicSwitch<>();
  
  @Override
  public void addCase(EClass clazz, Function<EObject, T> then) {
    delegateTo.dynamicCase(clazz, then);
  }

  @Override
  public void setDefaultCase(Function<EObject, T> then) {
    delegateTo.defaultCase(then);
  }

  @Override
  public BytecodeDynamicSwitch<T> build() {
    return delegateTo;
  }

}
