package tools.mdsd.ecoreworkflow.switches.tests;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import tools.mdsd.ecoreworkflow.switches.ApplyableSwitch;
import tools.mdsd.ecoreworkflow.switches.BytecodeDynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.InspectableSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioFactory;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario2.Testscenario2Factory;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario3.Testscenario3Factory;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario3.Testscenario3Package;
import tools.mdsd.ecoreworkflow.switches.tests.builders.BytecodeDynamicSwitchBuilder;
import tools.mdsd.ecoreworkflow.switches.tests.builders.SwitchBuilder;
import tools.mdsd.ecoreworkflow.switches.tests.templates.InspectableBehaviourTest;
import tools.mdsd.ecoreworkflow.switches.tests.templates.MergeableSwitchBehaviourTest;
import tools.mdsd.ecoreworkflow.switches.tests.templates.SwitchingRulesBehaviourTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals.*;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario2.Testscenario2Package.Literals.*;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario3.Testscenario3Package.Literals.*;


class BytecodeDynamicSwitchTest {
  @Nested
  class ConformsToSwitchingRules extends SwitchingRulesBehaviourTest {

    @Override
    protected <T> SwitchBuilder<T, ? extends ApplyableSwitch<T>> createSwitchBuilder() {
      return new BytecodeDynamicSwitchBuilder<T>();
    }
    
  }
  
  @Nested
  class ConformsToMergeableSwitchBehaviour extends MergeableSwitchBehaviourTest<DynamicSwitch<String>> {

    @Override
    protected SwitchBuilder<String, DynamicSwitch<String>> createSwitchBuilder() {
      return new BytecodeDynamicSwitchBuilder<String>();
    }
  }
  
  @Nested
  class ConformsToInspectableSwitchBehaviour extends InspectableBehaviourTest {

    @Override
    protected <T> SwitchBuilder<T, ? extends InspectableSwitch<T>> createSwitchBuilder() {
      return new  BytecodeDynamicSwitchBuilder<T>();
    }
    
  }
  
  @Test
  public void testChainedSyntax() {
    String result = new BytecodeDynamicSwitch<String>()
        .dynamicCase(G, (EObject g) -> "G")
        .dynamicCase(H, (EObject h) -> "H")
        .dynamicCase(Z, (EObject z) -> "Z")
        .doSwitch(TestscenarioFactory.eINSTANCE.create(E));
    
    assertEquals("G", result);
  }
  
  @Test
  public void registeredSubpackageEnablesMatching() {
    BytecodeDynamicSwitch<String> sw = new BytecodeDynamicSwitch<String>();
   
    sw.addPackage(Testscenario3Package.eINSTANCE);
    
    String result = sw
        .dynamicCase(A, (EObject z) -> "an A") // Q is a child of A, the switch considers that fact because Testscenario2Package has been registered.
        .defaultCase(o -> "<default>")
        .doSwitch(Testscenario3Factory.eINSTANCE.create(Q));

    assertEquals("an A", result);
  }
  
  @Test
  public void unregisteredSubpackageDoesntMatch() {
    BytecodeDynamicSwitch<String> sw = new BytecodeDynamicSwitch<String>();
   
    // NOTE THAT Q IS NOT RECOGNIZED AS A CHILD OF A BECAUSE WE FAIL TO REGISTER Testscenario3Package
    // NEGLECTED: sw.addPackage(Testscenario3Package.eINSTANCE);
    
    String result = sw
        .dynamicCase(A, a -> "an A") // Q is a child of A, the switch considers that fact because Testscenario2Package has been registered.
        .defaultCase(o -> "<default>")
        .doSwitch(Testscenario3Factory.eINSTANCE.create(Q));

    assertEquals("<default>", result);
  }
  
  @Test
  public void crossPackageDelegationWorks() {
    BytecodeDynamicSwitch<String> sw = new BytecodeDynamicSwitch<String>();
   
    // NOTE THAT REGISTERING THE PACKAGE IS NOT NECESSARY BECAUSE WE DEFINE A CASE in Tescscenario3
    // NOT NECARRARY: sw.addPackage(Testscenario3Package.eINSTANCE);
    
    boolean[] qWasCalled = {false};
    
    String result = sw
        .dynamicCase(Q, q -> {qWasCalled[0] = true; return null;}) // Q is more specific then A, but delegates by returning null
        .dynamicCase(A, a -> "an A") // Q is a child of A, the switch considers that fact because Testscenario2Package has been registered.
        .defaultCase(o -> "<default>")
        .doSwitch(Testscenario3Factory.eINSTANCE.create(Q));

    assertEquals("an A", result);
    assertTrue(qWasCalled[0], "case Q must be called before");
  }
}
