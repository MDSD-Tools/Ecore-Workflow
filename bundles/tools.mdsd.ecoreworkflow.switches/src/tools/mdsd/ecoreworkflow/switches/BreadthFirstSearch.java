package tools.mdsd.ecoreworkflow.switches;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
 
/***
  * Utility class for performing breadth-first search on a graph that is only implicitly
  * given by its root node and a function that returns each node's neighbours.
  * @author Christian
  *
  * @param <T>
  */
public class BreadthFirstSearch<T> {

  /**
   * perform a breadth-first search in an acyclic graph represented by a root node and a
   * given exploration relationship and return the first matching node.
   * 
   * @param rootNode the starting node
   * @param criterion used to check if a node matches. Is also given the set of currently unexplored nodes.
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
  
  
  /**
   * perform a breadth-first search in an acyclic graph represented by a root node and a
   * given exploration relationship and feed all nodes into the given consumer in the order
   * that the breadth-first search finds them.
   * The consumer also has access to a list of unexplored nodes in the queue.
   * @param rootNode
   * @param consumer
   * @param getParents
   */
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
