package tools.mdsd.ecoreworkflow.switches;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

class BreadthFirstSearch<T> {

  /**
   * perform a BreathFirstSearch in an acyclic graph represented by a root node and an exploration.
   * relationship and return the first matching node
   * 
   * @param rootNode the starting node
   * @param criterion for a node and a set of unexplored nodes determines if the node matches
   * @param getParents exploration relationship
   * @return the first matching node, null when no node is found
   */
  public T find(T rootNode, BiPredicate<T, Collection<T>> criterion,
      Function<T, Collection<T>> getParents) {
    Queue<T> queue = new ArrayDeque<>();
    queue.add(rootNode);
    while (!queue.isEmpty()) {
      T current = queue.remove();
      if (criterion.test(current, Collections.unmodifiableCollection(queue))) {
        return current;
      } else {
        queue.addAll(getParents.apply(current));
      }
    }

    return null;
  }

  public void scan(T rootNode, BiConsumer<T, Collection<T>> consumer,
      Function<T, Collection<T>> getParents) {
    Queue<T> queue = new ArrayDeque<>();
    queue.add(rootNode);
    while (!queue.isEmpty()) {
      T current = queue.remove();
      consumer.accept(current, Collections.unmodifiableCollection(queue));
      queue.addAll(getParents.apply(current));
    }
  }
}
