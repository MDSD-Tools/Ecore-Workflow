package tools.mdsd.ecoreworkflow;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.ComposedSwitch;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.BytecodeDynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioFactory;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario2.Testscenario2Factory;


public class MultiPackageSwitchingSpeedBenchmark {

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    SwitchConfigurator conf = new SwitchConfigurator();
    ComposedSwitch<String> composedSwitch = conf.buildComposedSwitch();
    DynamicSwitch<String> dynamicSwitch = conf.buildComposedDynamicSwitch();
    DynamicSwitch<String> bytecodeSwitch = conf.buildDynamicBytecodeSwitch();
  }
  
  @State(Scope.Thread)
  public static class ThreadState {
    TestscenarioFactory factory = TestscenarioFactory.eINSTANCE;
    Testscenario2Factory factory2 = Testscenario2Factory.eINSTANCE;
    EObject[] testObjects = new EObject[] {
        factory.createA(),
        factory.createB(),
        factory.createC(),
        factory.createD(),
        factory.createE(),
        factory.createF(),
        factory.createG(),
        factory.createH(),
        factory.createI(),
        factory.createK(),
        factory.createL(),
        factory.createM(),
        factory2.createY(),
        factory2.createZ(),
    };
  }
  
  @Benchmark
  public void composedSwitch(BenchmarkState benchmarkState, ThreadState threadState, Blackhole blackHole) {
    for (EObject obj: threadState.testObjects) {
      blackHole.consume(benchmarkState.composedSwitch.doSwitch(obj));
    }
  }
  
  @Benchmark
  public void dynamicSwitch(BenchmarkState benchmarkState, ThreadState threadState, Blackhole blackHole) {
    for (EObject obj: threadState.testObjects) {
      blackHole.consume(benchmarkState.dynamicSwitch.doSwitch(obj));
    }
  }
  
  @Benchmark
  public void bytecodeSwitch(BenchmarkState benchmarkState, ThreadState threadState, Blackhole blackHole) {
    for (EObject obj: threadState.testObjects) {
      blackHole.consume(benchmarkState.bytecodeSwitch.doSwitch(obj));
    }
  }
  
}
