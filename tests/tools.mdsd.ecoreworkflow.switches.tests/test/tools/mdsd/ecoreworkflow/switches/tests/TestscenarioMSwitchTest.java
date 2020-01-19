package tools.mdsd.ecoreworkflow.switches.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals.E;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import testscenario.xutil.TestscenarioMSwitch;
import tools.mdsd.ecoreworkflow.switches.ApplyableSwitch;
import tools.mdsd.ecoreworkflow.switches.InspectableSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.F;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.G;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioFactory;
import tools.mdsd.ecoreworkflow.switches.tests.builders.SwitchBuilder;
import tools.mdsd.ecoreworkflow.switches.tests.builders.TestscenarioMSwitchBuilder;
import tools.mdsd.ecoreworkflow.switches.tests.templates.InspectableBehaviourTest;
import tools.mdsd.ecoreworkflow.switches.tests.templates.MergeableSwitchBehaviourTest;
import tools.mdsd.ecoreworkflow.switches.tests.templates.SwitchingRulesBehaviourTest;

// Note: Redundancy with StaticSwitchTest was deliberately chosen in order to have explicit and stable test cases.
class TestscenarioMSwitchTest {
  @Nested
  class ConformsToSwitchingRules extends SwitchingRulesBehaviourTest {

    @Override
    protected <T> SwitchBuilder<T, ? extends ApplyableSwitch<T>> createSwitchBuilder() {
      return new TestscenarioMSwitchBuilder<T>();
    }   
    
  }
  
  @Nested
  class ConformsToInspectableMergeSwitch extends InspectableBehaviourTest {
    @Override
    protected <T> SwitchBuilder<T, ? extends InspectableSwitch<T>> createSwitchBuilder() {
      return new TestscenarioMSwitchBuilder<T>();
    }
  }
  
  @Nested
  class ConformsToMergeableSwitch extends MergeableSwitchBehaviourTest<TestscenarioMSwitch<String>> {

    @Override
    protected SwitchBuilder<String, TestscenarioMSwitch<String>> createSwitchBuilder() {
      return new TestscenarioMSwitchBuilder<String>();
    }
    
  }
  
  @Test()
  void testChainedSyntax() {
    String result = new TestscenarioMSwitch<String>()
        .when((F f) -> "F")
        .when((G g) -> "G")
        .doSwitch(TestscenarioFactory.eINSTANCE.create(E));
    
    assertEquals("F", result);
  }
}
