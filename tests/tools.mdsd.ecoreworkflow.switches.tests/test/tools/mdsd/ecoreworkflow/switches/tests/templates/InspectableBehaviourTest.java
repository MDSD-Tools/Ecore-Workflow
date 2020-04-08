package tools.mdsd.ecoreworkflow.switches.tests.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import tools.mdsd.ecoreworkflow.switches.InspectableSwitch;
import tools.mdsd.ecoreworkflow.switches.tests.builders.SwitchBuilder;

public abstract class InspectableBehaviourTest {
  protected abstract <T> SwitchBuilder<T, ? extends InspectableSwitch<T>> createSwitchBuilder();
  
  @Test
  void testGetCases() {
    SwitchBuilder<String, ? extends InspectableSwitch<String>> s = createSwitchBuilder();
    s.setDefaultCase(o -> "default");
    InspectableSwitch<String> builtSwitch = s.build();
    assertEquals(0, builtSwitch.getCases().size(), "Only the default case must be defined");
    assertNotNull(builtSwitch.getDefaultCase(), "The default case must be defined");
  }
}
