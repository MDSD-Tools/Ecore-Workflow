package tools.mdsd.ecoreworkflow.switches.tests.builders;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import testscenario.xutil.TestscenarioMSwitch;

public class TestscenarioMSwitchBuilder<T> implements SwitchBuilder<T, TestscenarioMSwitch<T>> {

  private TestscenarioMSwitch<T> delegateTo = new TestscenarioMSwitch<>();

  @Override
  public void addCase(EClass clazz, Function<EObject, T> then) {
    Class<?> functionalInterfaceClass; // the WhenX interface that is used to identify the overloaded when method
    
    try {
      functionalInterfaceClass = Class.forName("testscenario.xutil.TestscenarioMSwitch$When" + clazz.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("The corresponding When...-interface could not be found in the mswitch class", e);
    }
    
    Method overloadedWhenMethod;
    
    try {
      overloadedWhenMethod = TestscenarioMSwitch.class.getMethod("when", functionalInterfaceClass);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new RuntimeException("The corresponding when(...) method could not be found on the mswitch class", e);
    }
    
    // we have a Function<EObject,T>-object but need a When...-object
    InvocationHandler relayToThen = (proxy, method, args) -> method.invoke(then, args); // relay all method calls to the then-object
    Object castedThen = Proxy.newProxyInstance(functionalInterfaceClass.getClassLoader(), new Class[]{functionalInterfaceClass}, relayToThen); // returns a When...-object
    
    try {
      overloadedWhenMethod.invoke(delegateTo, castedThen);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(
          "The corresponding when(...) method could not be invoked on the switch", e);
    }
    
  }

  @Override
  public void setDefaultCase(Function<EObject, T> then) {
    delegateTo.orElse(then);
  }

  @Override
  public TestscenarioMSwitch<T> build() {
    return delegateTo;
  }

}
