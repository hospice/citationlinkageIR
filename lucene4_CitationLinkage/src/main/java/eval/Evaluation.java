package eval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A functional interface for evaluation methods that compare two sense
 * inventories.  
 */
public interface Evaluation {

    /**
     * Returns a mapping from each instance to its score according to this
     * evaluation
     *
     * @param testInstances the set of instances shared by both keys that should
     *        be evaluated
     * @param termToNumSenses a mapping from each lemma form to the number of
     *        senses that it has
     *
     * @see KeyUtil for a description of how keys are represented
     */
    public Map<String,Double> test(
        Map<String,Map<String,Map<String,Double>>> test,
        Map<String,Map<String,Map<String,Double>>> gold,
        Set<String> testInstances,
        Map<String,Integer> termToNumSenses);
}
