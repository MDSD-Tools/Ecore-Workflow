package tools.mdsd.ecoreworkflow.switches.tests.builders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import tools.mdsd.ecoreworkflow.switches.ApplyableSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.A;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.B;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.C;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.D;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.E;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.F;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.G;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.H;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.I;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.K;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.L;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.M;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.util.TestscenarioSwitch;

/**
 * adapter implementation of TestScenarioSwitch that implements all methods and allows for filling them at runtime
 * @author Christian van Rensen
 *
 * @param <T> return type of each case
 */
class TestscenarioSwitchAdapter<T> extends TestscenarioSwitch<T> implements ApplyableSwitch<T> {
  private final Map<String, Function<EObject, T>> functionMap;
  private Function<EObject, T> defaultValue = x -> null;
  
  /**
   * keep in mind that this method ONLY works for the classes defined in the switch.
   * @param clazz
   * @param then
   */
  public void setCase(EClass clazz, Function<EObject, T> then) {
    functionMap.put(clazz.getInstanceClassName(), then);
  }
  
  public void setDefaultCase(Function<EObject, T> then) {
    functionMap.put("<default>", then);
  }
  
  TestscenarioSwitchAdapter() {
    functionMap = new HashMap<>();
  }

  @Override
  public T caseA(A object) {
    return functionMap.getOrDefault(A.class.getCanonicalName(), defaultValue).apply(object);
  }

  @Override
  public T caseB(B object) {
    return functionMap.getOrDefault(B.class.getCanonicalName(), defaultValue).apply(object);
  }

  @Override
  public T caseC(C object) {
    return functionMap.getOrDefault(C.class.getCanonicalName(), defaultValue).apply(object);
  }

  @Override
  public T caseD(D object) {
    return functionMap.getOrDefault(D.class.getCanonicalName(), defaultValue).apply(object);
  }

  @Override
  public T caseE(E object) {
    return functionMap.getOrDefault(E.class.getCanonicalName(), defaultValue).apply(object);
  }

  @Override
  public T caseF(F object) {
    return functionMap.getOrDefault(F.class.getCanonicalName(), defaultValue).apply(object);
  }

  @Override
  public T caseG(G object) {
    return functionMap.getOrDefault(G.class.getCanonicalName(), defaultValue).apply(object);
  }

  @Override
  public T caseH(H object) {
    return functionMap.getOrDefault(H.class.getCanonicalName(), defaultValue).apply(object);
  }

  @Override
  public T caseI(I object) {
    return functionMap.getOrDefault(I.class.getCanonicalName(), defaultValue).apply(object);
  }

  @Override
  public T caseK(K object) {
    return functionMap.getOrDefault(K.class.getCanonicalName(), defaultValue).apply(object);
  }

  @Override
  public T caseL(L object) {
    return functionMap.getOrDefault(L.class.getCanonicalName(), defaultValue).apply(object);
  }

  @Override
  public T caseM(M object) {
    return functionMap.getOrDefault(M.class.getCanonicalName(), defaultValue).apply(object);
  }
  
  @Override
  public T defaultCase(EObject object) {
    return functionMap.getOrDefault("<default>", defaultValue).apply(object);
  }
}