package tools.mdsd.ecoreworkflow;

import org.openjdk.jmh.runner.options.OptionsBuilder;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.xutil.TestscenarioMSwitch;
import org.eclipse.emf.ecore.EObject;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import tools.mdsd.ecoreworkflow.switches.DynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.HashDynamicSwitch;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.TestscenarioFactory;
import tools.mdsd.ecoreworkflow.switches.testmodel.testscenario.util.TestscenarioSwitch;


public class SwitchingSpeedBenchmark {

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    SwitchConfigurator conf = new SwitchConfigurator();
    
    TestscenarioSwitch<String> classicSwitch = conf.buildClassicSwitch();
    TestscenarioMSwitch<String> mswitch = conf.buildMSwitch();
    DynamicSwitch<String> dynamicSwitch = conf.buildDynamicSwitch();
    DynamicSwitch<String> dynamicBytecodeSwitch = conf.buildDynamicBytecodeSwitch();
  }
  
  @State(Scope.Thread)
  public static class ThreadState {
    TestscenarioFactory factory = TestscenarioFactory.eINSTANCE;
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
    };
  }
  
  @Benchmark
  public void classicSwitch(BenchmarkState benchmarkState, ThreadState threadState, Blackhole blackHole) {
    for (EObject obj: threadState.testObjects) {
      blackHole.consume(benchmarkState.classicSwitch.doSwitch(obj));
    }
  }
  
  @Benchmark
  public void mSwitch(BenchmarkState benchmarkState, ThreadState threadState, Blackhole blackHole) {
    for (EObject obj: threadState.testObjects) {
      blackHole.consume(benchmarkState.mswitch.doSwitch(obj));
    }
  }
  
  @Benchmark
  public void dynamicSwitch(BenchmarkState benchmarkState, ThreadState threadState, Blackhole blackHole) {
    for (EObject obj: threadState.testObjects) {
      blackHole.consume(benchmarkState.dynamicSwitch.doSwitch(obj));
    }
  }
  
  @Benchmark
  public void dynamicBytecodeSwitch(BenchmarkState benchmarkState, ThreadState threadState, Blackhole blackHole) {
    for (EObject obj: threadState.testObjects) {
      blackHole.consume(benchmarkState.dynamicBytecodeSwitch.doSwitch(obj));
    }
  }
  
  @Benchmark
  public void justBlackhole(BenchmarkState benchmarkState, ThreadState threadState, Blackhole blackHole) {
    for (EObject obj: threadState.testObjects) {
      blackHole.consume(obj);
    }
  }

}
