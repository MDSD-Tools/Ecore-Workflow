package tools.mdsd.ecoreworkflow.switches.tests;

import org.eclipse.emf.ecore.EObject;
import org.junit.Test;
import org.junit.jupiter.api.Nested;
import tools.mdsd.ecoreworkflow.switches.ApplyableSwitch;
import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.HashDynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.InspectableSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioFactory;
import tools.mdsd.ecoreworkflow.switches.tests.builders.HashDynamicSwitchBuilder;
import tools.mdsd.ecoreworkflow.switches.tests.builders.SwitchBuilder;
import tools.mdsd.ecoreworkflow.switches.tests.templates.InspectableBehaviourTest;
import tools.mdsd.ecoreworkflow.switches.tests.templates.MergeableSwitchBehaviourTest;
import tools.mdsd.ecoreworkflow.switches.tests.templates.SwitchingRulesBehaviourTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals.*;

class HashDynamicSwitchTest {
  @Nested
  class ConformsToSwitchingRules extends SwitchingRulesBehaviourTest {

    @Override
    protected <T> SwitchBuilder<T, ? extends ApplyableSwitch<T>> createSwitchBuilder() {
      return new  HashDynamicSwitchBuilder<T>();
    }
    
  }
  
  @Nested
  class ConformsToMergeableSwitchBehaviour extends MergeableSwitchBehaviourTest<DynamicSwitch<String>> {

    @Override
    protected SwitchBuilder<String, DynamicSwitch<String>> createSwitchBuilder() {
      return new HashDynamicSwitchBuilder<String>();
    }
  }
  
  @Nested
  class ConformsToInspectableSwitchBehaviour extends InspectableBehaviourTest {

    @Override
    protected <T> SwitchBuilder<T, ? extends InspectableSwitch<T>> createSwitchBuilder() {
      return new  HashDynamicSwitchBuilder<T>();
    }
    
  }
  
  @Test
  void testChainedSyntax() {
    String result = new HashDynamicSwitch<String>()
        .dynamicCase(G, (EObject g) -> "G")
        .dynamicCase(H, (EObject h) -> "H")
        .doSwitch(TestscenarioFactory.eINSTANCE.create(E));

    assertEquals("G", result);
  }
}
