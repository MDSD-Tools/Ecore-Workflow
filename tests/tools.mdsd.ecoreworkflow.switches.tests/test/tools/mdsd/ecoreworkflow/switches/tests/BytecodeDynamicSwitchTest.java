package tools.mdsd.ecoreworkflow.switches.tests;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import tools.mdsd.ecoreworkflow.switches.ApplyableSwitch;
import tools.mdsd.ecoreworkflow.switches.BytecodeDynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.InspectableSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioFactory;
import tools.mdsd.ecoreworkflow.switches.tests.builders.BytecodeDynamicSwitchBuilder;
import tools.mdsd.ecoreworkflow.switches.tests.builders.SwitchBuilder;
import tools.mdsd.ecoreworkflow.switches.tests.templates.InspectableBehaviourTest;
import tools.mdsd.ecoreworkflow.switches.tests.templates.MergeableSwitchBehaviourTest;
import tools.mdsd.ecoreworkflow.switches.tests.templates.SwitchingRulesBehaviourTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals.*;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario2.Testscenario2Package.Literals.*;

@Disabled
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
}
