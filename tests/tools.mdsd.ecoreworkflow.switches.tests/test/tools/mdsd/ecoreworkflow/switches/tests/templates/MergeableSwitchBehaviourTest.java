package tools.mdsd.ecoreworkflow.switches.tests.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.mdsd.ecoreworkflow.switches.ApplyableSwitch;
import tools.mdsd.ecoreworkflow.switches.InspectableSwitch;
import tools.mdsd.ecoreworkflow.switches.MergeableSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals;
import tools.mdsd.ecoreworkflow.switches.tests.builders.SwitchBuilder;

public abstract class MergeableSwitchBehaviourTest<S extends MergeableSwitch<? super S, ? extends S> & ApplyableSwitch<String> & InspectableSwitch<String>> {
  
  protected abstract SwitchBuilder<String, S> createSwitchBuilder();
  
  @Test
  void mergingLessSpecificBranchWithoutEffect() {
    SwitchBuilder<String, S> original = createSwitchBuilder();
    SwitchBuilder<String, S> merged = createSwitchBuilder();

    original.addCase(Literals.D, (EObject d) -> "D");
    merged.addCase(Literals.B, (EObject b) -> "B");

    assertEquals("D", original.build().merge(merged.build()).doSwitch(Utils.createE()));
  }



  @Test
  void mergingSameBranchOverrides() {
    SwitchBuilder<String, S> original = createSwitchBuilder();
    SwitchBuilder<String, S> merged = createSwitchBuilder();

    original.addCase(Literals.D, (EObject d) -> "1");
    merged.addCase(Literals.D, (EObject d) -> "2");

    assertEquals("2", original.build().merge(merged.build()).doSwitch(Utils.createE()));
  }

  @Test
  void mergingDefaultBranchOverrides() {
    SwitchBuilder<String, S> original = createSwitchBuilder();
    SwitchBuilder<String, S> merged = createSwitchBuilder();

    original.addCase(Literals.D, (EObject d) -> null);
    original.setDefaultCase(x -> "1");
    merged.setDefaultCase(x -> "2");

    assertEquals("2", original.build().merge(merged.build()).doSwitch(Utils.createE()));
  }

  @Test
  void mergingAffectsGetCases() {
    SwitchBuilder<String, S> original = createSwitchBuilder();
    SwitchBuilder<String, S> merged = createSwitchBuilder();

    merged.addCase(Literals.A, (EObject a) -> "a");

    S builtOriginal = original.build();
    builtOriginal.merge(merged.build());
    // TODO
    assertEquals("a", original.build().getCases().get(TestscenarioPackage.Literals.A).apply(Utils.createE()));
  }
}
