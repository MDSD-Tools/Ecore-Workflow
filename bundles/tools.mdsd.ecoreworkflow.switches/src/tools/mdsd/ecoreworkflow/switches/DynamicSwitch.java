package tools.mdsd.ecoreworkflow.switches;

import java.util.Map;
import java.util.function.Function;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public interface DynamicSwitch<T> {
	public DynamicSwitch<T> dynamicCase(EClass clazz, Function<EObject,T> then);
	public DynamicSwitch<T> defaultCase(Function<EObject, T> then);
	public T doSwitch(EObject object);
	
	public default DynamicSwitch<T> merge(DynamicSwitch<? extends T> dyn) {
		dyn.getCases().forEach((clazz, then) -> {
			this.dynamicCase(clazz, then::apply); // up-casting if necessary
		});
		Function<EObject, ? extends T> defaultCase = dyn.getDefaultCase();
		if (defaultCase != null) {
			defaultCase(defaultCase::apply); // up-casting if necessary
		}
		return this;
	}
	public Map<EClass, Function<EObject, T>> getCases();
	public Function<EObject, T> getDefaultCase();
}
