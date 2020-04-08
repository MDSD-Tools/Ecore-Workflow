package tools.mdsd.ecoreworkflow.switches.tests;

import org.junit.jupiter.api.Nested;
import tools.mdsd.ecoreworkflow.switches.ApplyableSwitch;
import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.HashDynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.InspectableSwitch;
import tools.mdsd.ecoreworkflow.switches.tests.builders.HashDynamicSwitchBuilder;
import tools.mdsd.ecoreworkflow.switches.tests.builders.SwitchBuilder;
import tools.mdsd.ecoreworkflow.switches.tests.templates.CrossPackageSwitchingRulesBehaviourTest;
import tools.mdsd.ecoreworkflow.switches.tests.templates.InspectableBehaviourTest;
import tools.mdsd.ecoreworkflow.switches.tests.templates.MergeableSwitchBehaviourTest;
import tools.mdsd.ecoreworkflow.switches.tests.templates.SwitchingRulesBehaviourTest;

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
  
  @Nested
  class ConformsToCrossPackageSwitchingRulesBehaviour extends CrossPackageSwitchingRulesBehaviourTest {
    @Override
    protected DynamicSwitch<String> getSubject() {
      return new HashDynamicSwitch<String>();
    }
  }
  
}
