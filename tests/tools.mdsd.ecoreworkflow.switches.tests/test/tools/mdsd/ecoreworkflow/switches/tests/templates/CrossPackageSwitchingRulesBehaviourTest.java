package tools.mdsd.ecoreworkflow.switches.tests.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals.A;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals.E;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals.G;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals.H;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario3.Testscenario3Package.Literals.Q;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioFactory;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario3.Testscenario3Factory;

public abstract class CrossPackageSwitchingRulesBehaviourTest {
  protected abstract DynamicSwitch<String> getSubject();
  
  @Test
  public void crossPackageDelegationWorks() {
    DynamicSwitch<String> sw = getSubject();
    
    boolean[] qWasCalled = {false};
    
    String result = sw
        .dynamicCase(Q, q -> {qWasCalled[0] = true; return null;}) // Q is more specific then A, but delegates by returning null
        .dynamicCase(A, a -> "an A") // Q is a child of A, the switch considers that fact because Testscenario2Package has been registered.
        .defaultCase(o -> "<default>")
        .doSwitch(Testscenario3Factory.eINSTANCE.create(Q));

    assertEquals("an A", result);
    assertTrue(qWasCalled[0], "case Q must be called");
  }
  
  @Test
  void testChainedSyntax() {
    String result = getSubject()
        .dynamicCase(G, (EObject g) -> "G")
        .dynamicCase(H, (EObject h) -> "H")
        .doSwitch(TestscenarioFactory.eINSTANCE.create(E));

    assertEquals("G", result);
  }
}
