package tools.mdsd.ecoreworkflow.switches.tests;

import org.junit.jupiter.api.Nested;
import tools.mdsd.ecoreworkflow.switches.ApplyableSwitch;
import tools.mdsd.ecoreworkflow.switches.tests.builders.SwitchBuilder;
import tools.mdsd.ecoreworkflow.switches.tests.builders.TestscenarioSwitchBuilder;
import tools.mdsd.ecoreworkflow.switches.tests.templates.SwitchingRulesBehaviourTest;

class TestscenarioSwitchTest {
  @Nested
  class ConformsToSwitchingRules extends SwitchingRulesBehaviourTest {

    @Override
    protected <T> SwitchBuilder<T, ? extends ApplyableSwitch<T>> createSwitchBuilder() {
      return new TestscenarioSwitchBuilder<T>();
    }   
    
    @Override
    protected boolean failsOnNoDefaultCase() {
      return false; // the old switches do not support throwing exceptions when no default case is defined.
      // This is a behavioural change. Therefore, by overriding this method, we disable the respective test.
    }
  }
}
