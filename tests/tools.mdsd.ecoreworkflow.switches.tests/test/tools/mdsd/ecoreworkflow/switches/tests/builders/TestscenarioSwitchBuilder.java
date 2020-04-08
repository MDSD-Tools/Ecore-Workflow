package tools.mdsd.ecoreworkflow.switches.tests.builders;

import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public class TestscenarioSwitchBuilder<T> implements SwitchBuilder<T, TestscenarioSwitchAdapter<T>> {

  private TestscenarioSwitchAdapter<T> adapter = new TestscenarioSwitchAdapter<>();

  @Override
  public void addCase(EClass clazz, Function<EObject, T> then) {
    adapter.setCase(clazz, then);
  }
  
  @Override
  public void setDefaultCase(Function<EObject, T> then) {
    adapter.setDefaultCase(then);
  }

  @Override
  public TestscenarioSwitchAdapter<T> build() {
    return adapter;
  }

}
