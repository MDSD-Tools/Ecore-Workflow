package tools.mdsd.ecoreworkflow.switches;

import java.util.List;
import java.util.function.Function;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

public abstract class MSwitch<T> {
	protected Function<EObject, T> defaultCase;
	
	public static class SwitchingException extends RuntimeException {
	
			private static final long serialVersionUID = 1L;
	
			public SwitchingException(String message) {
				super(message);
			}
		}

	public MSwitch() {
		super();
	}

	public T doSwitch(EObject s) {
		return doSwitch(s.eClass(), s);
	}
	
	protected T doSwitch(EClass eClass, EObject eObject) {
		if (isSwitchFor(eClass.getEPackage())) {
			return doSwitch(eClass.getClassifierID(), eObject);
		} else {
			List<EClass> eSuperTypes = eClass.getESuperTypes();
			return eSuperTypes.isEmpty() ? applyDefaultCase(eObject) : doSwitch(eSuperTypes.get(0), eObject);
		}
	}
	
	protected T applyDefaultCase(EObject eObject) {
		if (defaultCase != null) {
			return defaultCase.apply(eObject); // the default case will not fall through
		} else {
			throw new SwitchingException("no default case defined");
		}
	}
	
	protected abstract T doSwitch(int classifierID, EObject eObject) throws SwitchingException;
	public abstract boolean isSwitchFor(EPackage ePackage);
}
