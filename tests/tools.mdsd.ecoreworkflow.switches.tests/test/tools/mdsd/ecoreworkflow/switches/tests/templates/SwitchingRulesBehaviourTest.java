package tools.mdsd.ecoreworkflow.switches.tests.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.mdsd.ecoreworkflow.switches.ApplyableSwitch;
import tools.mdsd.ecoreworkflow.switches.MSwitch.SwitchingException;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.F;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioFactory;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioPackage.Literals;
import tools.mdsd.ecoreworkflow.switches.tests.builders.SwitchBuilder;

public abstract class SwitchingRulesBehaviourTest {
  protected abstract <T> SwitchBuilder<T, ? extends ApplyableSwitch<T>> createSwitchBuilder();
  protected boolean failsOnNoDefaultCase() {return true;}
  
  @Test
  void childCaseWins() {
    SwitchBuilder<String, ? extends ApplyableSwitch<String>> sw = this.<String>createSwitchBuilder();
    sw.addCase(Literals.G, (EObject g) -> "G");
    sw.addCase(Literals.H, (EObject h) -> "H");
    String result = sw.build().doSwitch(Utils.createE());

    assertEquals("G", result, "child cases are more specific");
  }

  @Test
  void firstParentWins() {
    SwitchBuilder<String, ? extends ApplyableSwitch<String>> sw = this.<String>createSwitchBuilder();
    sw.addCase(Literals.F, (EObject f) -> "F");
    sw.addCase(Literals.G, (EObject g) -> "G");
    String result = sw.build().doSwitch(Utils.createE());

    assertEquals("F", result, "first listed parent is treated more specific");
  }

  @Test
  void shorterInheritancePathsAreMoreSpecific() {
    StringBuffer callOrder = new StringBuffer();
    SwitchBuilder<Object, ? extends ApplyableSwitch<Object>> sw = createSwitchBuilder();
    sw.addCase(Literals.G, (EObject g) -> {
      callOrder.append("G");
      return null;
    });
    sw.addCase(Literals.I, (EObject i) -> {
      callOrder.append("I");
      return null;
    });
    sw.addCase(Literals.H, (EObject h) -> {
      callOrder.append("H");
      return "H";
    });
    sw.setDefaultCase(x -> "X");
    sw.build().doSwitch(Utils.createE());

    assertEquals("GIH", callOrder.toString(), "shorter pathes are more specific");
  }

  @Test
  public void exceptionOnNoMatch() {
    if (failsOnNoDefaultCase()) {
      SwitchBuilder<Object, ? extends ApplyableSwitch<Object>> sw = createSwitchBuilder();
      sw.addCase(Literals.G, (EObject g) -> null);
      F nonMatchingObject = TestscenarioFactory.eINSTANCE.createF();

      assertThrows(SwitchingException.class, () -> {
        sw.build().doSwitch(nonMatchingObject);
      }, "a SwitchingException must be thrown when none of the defined branches match and no default case is defined.");
    }
  }

  @Test
  void defaultCaseIsCalled() {
    SwitchBuilder<Object, ? extends ApplyableSwitch<Object>> sw = createSwitchBuilder();
    F nonMathingObject = TestscenarioFactory.eINSTANCE.createF();

    sw.addCase(Literals.G, (EObject g) -> null);
    sw.setDefaultCase((param) -> {
      assertTrue(nonMathingObject == param,
          "default case must be passed the EObject that was put into the switch");
      return "default";
    });

    assertEquals("default", sw.build().doSwitch(nonMathingObject),
        "default case must be used when the defined cases don't match");
  }

  @Test
  void subsequentCasesNotCalled() {
    SwitchBuilder<Object, ? extends ApplyableSwitch<Object>> sw = createSwitchBuilder();
    sw.addCase(Literals.A, (EObject a) -> {
      fail("The A case must not be called");
      return null;
    });
    sw.addCase(Literals.D, (EObject d) -> "d");
    sw.build().doSwitch(Utils.createE());
  }

  @Test
  void subsequentCasesCalledInCorrectOrder() {
    SwitchBuilder<Object, ? extends ApplyableSwitch<Object>> sw = createSwitchBuilder();
    StringBuffer callOrder = new StringBuffer();

    sw.addCase(Literals.A, (EObject x) -> {
      callOrder.append("A");
      return null;
    });
    sw.addCase(Literals.B, (EObject x) -> {
      callOrder.append("B");
      return null;
    });
    sw.addCase(Literals.C, (EObject x) -> {
      callOrder.append("C");
      return null;
    });
    sw.addCase(Literals.D, (EObject x) -> {
      callOrder.append("D");
      return null;
    });
    sw.addCase(Literals.E, (EObject x) -> {
      callOrder.append("E");
      return null;
    });
    sw.addCase(Literals.F, (EObject x) -> {
      callOrder.append("F");
      return null;
    });
    sw.addCase(Literals.G, (EObject x) -> {
      callOrder.append("G");
      return null;
    });
    sw.addCase(Literals.H, (EObject x) -> {
      callOrder.append("H");
      return null;
    });
    sw.addCase(Literals.I, (EObject x) -> {
      callOrder.append("I");
      return null;
    });
    sw.addCase(Literals.K, (EObject x) -> {
      callOrder.append("K");
      return null;
    });
    sw.addCase(Literals.L, (EObject x) -> {
      callOrder.append("L");
      return null;
    });
    sw.addCase(Literals.M, (EObject x) -> {
      callOrder.append("M");
      return null;
    });
    sw.setDefaultCase((x) -> {
      callOrder.append("X");
      return null;
    });

    sw.build().doSwitch(Utils.createE());

    assertEquals("EKDCFGILBHMAX", callOrder.toString());
  }
}
