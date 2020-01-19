package tools.mdsd.ecoreworkflow.switches;

/**
 * a switch that supports merging other switches behaviour into itself.
 * 
 * @author Christian van Rensen
 *
 * @param <F> the type of switches that can be merged
 * @param <I> the type of switch that is result of the merge,
 *            should be equal to the implementing class
 */
public interface MergeableSwitch<F, I extends MergeableSwitch<F, I>> {
  I merge(F other);
}
