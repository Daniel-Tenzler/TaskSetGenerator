package dt.tsg.Variety;

import dt.tsg.InputParams.InputParameters;
import dt.tsg.task.Task;
import dt.tsg.task.TaskGenerationFramework;
import dt.tsg.task.TaskInstance;
import dt.tsg.utils.GenerationResult;
import dt.tsg.utils.Utils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class VarietyTests {

    static final boolean DEBUG = false;

    /**
     * Test for examining the variety in the generated wcet depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 0 precedence relations,
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void wcetVarietyTestPERTASK() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test

        HashMap<Integer, HashMap<Integer, Integer>> WCETValuesForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETValuesForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintsForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintHitsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintHitsForEachTask.putIfAbsent(i, new HashMap<>());
        int numSuccessfullIterations = 0;
        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            // save wcet values
            for (int k=0;k<numTasks;k++) {
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETValuesForEachTask.get(k).containsKey(wcetValueOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, WCETValuesForEachTask.get(k).get(wcetValueOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, 1);
                }
            }
            // save hint values
            numSuccessfullIterations++;
            for (int k=0;k<numTasks;k++) {
                int wcetHintOfTask_K = generationResult.wcetHints.get(k);
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETHintsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintsForEachTask.get(k).get(wcetHintOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                }
                if (wcetHintOfTask_K == wcetValueOfTask_K) {
                    if (WCETHintHitsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintHitsForEachTask.get(k).get(wcetHintOfTask_K) + 1);
                    } else {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                    }
                }
            }
        }

        HashMap<Integer, String> printStrings = new HashMap<>();
        printStrings.put(0,"Tasks:\t");
        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                printStrings.put(wcetValue,wcetValue + "\t");
            });
        });
        System.out.println("WCET Value Occurrences per Task:");

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Occurrences");
        HashMap<Integer, Integer> wcetValues = new HashMap<>();

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetValues.containsKey(wcetValue)) {
                    wcetValues.replace(wcetValue, wcetValues.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetValues.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetvalue : wcetValues.keySet()) {
            System.out.println(wcetvalue + "\t" + wcetValues.get(wcetvalue));
        }

        System.out.println("\nTOTAL WCET HINTS PER TASK");

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("\nTOTAL WCET HINT HITS PER TASK");

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHints = new HashMap<>();

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHints.containsKey(wcetValue)) {
                    wcetHints.replace(wcetValue, wcetHints.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHints.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHint : wcetHints.keySet()) {
            System.out.println(wcetHint + "\t" + wcetHints.get(wcetHint));
        }

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHintHits = new HashMap<>();

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHintHits.containsKey(wcetValue)) {
                    wcetHintHits.replace(wcetValue, wcetHintHits.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHintHits.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHintHit : wcetHintHits.keySet()) {
            System.out.println(wcetHintHit + "\t" + wcetHintHits.get(wcetHintHit));
        }
    }

    /**
     * Test for examining the variety in the generated wcet depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 5 resources, 0 residency constraints, 0 precedence relations,
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void wcetVarietyTestPERTASK1() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(5);
        inputs.setMax_number_of_resources(5);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test

        HashMap<Integer, HashMap<Integer, Integer>> WCETValuesForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETValuesForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintsForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintHitsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintHitsForEachTask.putIfAbsent(i, new HashMap<>());
        int numSuccessfullIterations = 0;
        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            // save wcet values
            for (int k=0;k<numTasks;k++) {
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETValuesForEachTask.get(k).containsKey(wcetValueOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, WCETValuesForEachTask.get(k).get(wcetValueOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, 1);
                }
            }
            // save hint values
            numSuccessfullIterations++;
            for (int k=0;k<numTasks;k++) {
                int wcetHintOfTask_K = generationResult.wcetHints.get(k);
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETHintsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintsForEachTask.get(k).get(wcetHintOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                }
                if (wcetHintOfTask_K == wcetValueOfTask_K) {
                    if (WCETHintHitsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintHitsForEachTask.get(k).get(wcetHintOfTask_K) + 1);
                    } else {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                    }
                }
            }
        }

        HashMap<Integer, String> printStrings = new HashMap<>();
        printStrings.put(0,"Tasks:\t");
        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                printStrings.put(wcetValue,wcetValue + "\t");
            });
        });
        System.out.println("WCET Value Occurrences per Task:");

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Occurrences");
        HashMap<Integer, Integer> wcetValues = new HashMap<>();

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetValues.containsKey(wcetValue)) {
                    wcetValues.replace(wcetValue, wcetValues.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetValues.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetvalue : wcetValues.keySet()) {
            System.out.println(wcetvalue + "\t" + wcetValues.get(wcetvalue));
        }

        System.out.println("\nTOTAL WCET HINTS PER TASK");

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("\nTOTAL WCET HINT HITS PER TASK");

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHints = new HashMap<>();

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHints.containsKey(wcetValue)) {
                    wcetHints.replace(wcetValue, wcetHints.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHints.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHint : wcetHints.keySet()) {
            System.out.println(wcetHint + "\t" + wcetHints.get(wcetHint));
        }

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHintHits = new HashMap<>();

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHintHits.containsKey(wcetValue)) {
                    wcetHintHits.replace(wcetValue, wcetHintHits.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHintHits.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHintHit : wcetHintHits.keySet()) {
            System.out.println(wcetHintHit + "\t" + wcetHintHits.get(wcetHintHit));
        }
    }
    /**
     * Test for examining the variety in the generated wcet depending on several different min and max values per task.
     * The default values for this test are: 20 tasks, 10 resources, 0 residency constraints, 0 precedence relations,
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void wcetVarietyTestPERTASK2() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 20;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test

        HashMap<Integer, HashMap<Integer, Integer>> WCETValuesForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETValuesForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintsForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintHitsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintHitsForEachTask.putIfAbsent(i, new HashMap<>());
        int numSuccessfullIterations = 0;
        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            // save wcet values
            for (int k=0;k<numTasks;k++) {
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETValuesForEachTask.get(k).containsKey(wcetValueOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, WCETValuesForEachTask.get(k).get(wcetValueOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, 1);
                }
            }
            // save hint values
            numSuccessfullIterations++;
            for (int k=0;k<numTasks;k++) {
                int wcetHintOfTask_K = generationResult.wcetHints.get(k);
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETHintsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintsForEachTask.get(k).get(wcetHintOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                }
                if (wcetHintOfTask_K == wcetValueOfTask_K) {
                    if (WCETHintHitsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintHitsForEachTask.get(k).get(wcetHintOfTask_K) + 1);
                    } else {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                    }
                }
            }
        }

        HashMap<Integer, String> printStrings = new HashMap<>();
        printStrings.put(0,"Tasks:\t");
        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                printStrings.put(wcetValue,wcetValue + "\t");
            });
        });
        System.out.println("WCET Value Occurrences per Task:");

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Occurrences");
        HashMap<Integer, Integer> wcetValues = new HashMap<>();

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetValues.containsKey(wcetValue)) {
                    wcetValues.replace(wcetValue, wcetValues.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetValues.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetvalue : wcetValues.keySet()) {
            System.out.println(wcetvalue + "\t" + wcetValues.get(wcetvalue));
        }

        System.out.println("\nTOTAL WCET HINTS PER TASK");

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("\nTOTAL WCET HINT HITS PER TASK");

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHints = new HashMap<>();

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHints.containsKey(wcetValue)) {
                    wcetHints.replace(wcetValue, wcetHints.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHints.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHint : wcetHints.keySet()) {
            System.out.println(wcetHint + "\t" + wcetHints.get(wcetHint));
        }

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHintHits = new HashMap<>();

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHintHits.containsKey(wcetValue)) {
                    wcetHintHits.replace(wcetValue, wcetHintHits.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHintHits.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHintHit : wcetHintHits.keySet()) {
            System.out.println(wcetHintHit + "\t" + wcetHintHits.get(wcetHintHit));
        }
    }


    /**
     * Test for examining the variety in the generated wcet depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 10 residency constraints, 0 precedence relations,
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void wcetVarietyTestPERTASK3() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test

        HashMap<Integer, HashMap<Integer, Integer>> WCETValuesForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETValuesForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintsForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintHitsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintHitsForEachTask.putIfAbsent(i, new HashMap<>());
        int numSuccessfullIterations = 0;
        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            // save wcet values
            for (int k=0;k<numTasks;k++) {
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETValuesForEachTask.get(k).containsKey(wcetValueOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, WCETValuesForEachTask.get(k).get(wcetValueOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, 1);
                }
            }
            // save hint values
            numSuccessfullIterations++;
            for (int k=0;k<numTasks;k++) {
                int wcetHintOfTask_K = generationResult.wcetHints.get(k);
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETHintsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintsForEachTask.get(k).get(wcetHintOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                }
                if (wcetHintOfTask_K == wcetValueOfTask_K) {
                    if (WCETHintHitsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintHitsForEachTask.get(k).get(wcetHintOfTask_K) + 1);
                    } else {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                    }
                }
            }
        }

        HashMap<Integer, String> printStrings = new HashMap<>();
        printStrings.put(0,"Tasks:\t");
        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                printStrings.put(wcetValue,wcetValue + "\t");
            });
        });
        System.out.println("WCET Value Occurrences per Task:");

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Occurrences");
        HashMap<Integer, Integer> wcetValues = new HashMap<>();

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetValues.containsKey(wcetValue)) {
                    wcetValues.replace(wcetValue, wcetValues.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetValues.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetvalue : wcetValues.keySet()) {
            System.out.println(wcetvalue + "\t" + wcetValues.get(wcetvalue));
        }

        System.out.println("\nTOTAL WCET HINTS PER TASK");

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("\nTOTAL WCET HINT HITS PER TASK");

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHints = new HashMap<>();

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHints.containsKey(wcetValue)) {
                    wcetHints.replace(wcetValue, wcetHints.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHints.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHint : wcetHints.keySet()) {
            System.out.println(wcetHint + "\t" + wcetHints.get(wcetHint));
        }

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHintHits = new HashMap<>();

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHintHits.containsKey(wcetValue)) {
                    wcetHintHits.replace(wcetValue, wcetHintHits.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHintHits.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHintHit : wcetHintHits.keySet()) {
            System.out.println(wcetHintHit + "\t" + wcetHintHits.get(wcetHintHit));
        }
    }

    /**
     * Test for examining the variety in the generated wcet depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 10 precedence relations,
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void wcetVarietyTestPERTASK4() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(10);
        inputs.setMax_total_precedence_relations(10);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test

        HashMap<Integer, HashMap<Integer, Integer>> WCETValuesForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETValuesForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintsForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintHitsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintHitsForEachTask.putIfAbsent(i, new HashMap<>());
        int numSuccessfullIterations = 0;
        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            // save wcet values
            for (int k=0;k<numTasks;k++) {
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETValuesForEachTask.get(k).containsKey(wcetValueOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, WCETValuesForEachTask.get(k).get(wcetValueOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, 1);
                }
            }
            // save hint values
            numSuccessfullIterations++;
            for (int k=0;k<numTasks;k++) {
                int wcetHintOfTask_K = generationResult.wcetHints.get(k);
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETHintsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintsForEachTask.get(k).get(wcetHintOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                }
                if (wcetHintOfTask_K == wcetValueOfTask_K) {
                    if (WCETHintHitsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintHitsForEachTask.get(k).get(wcetHintOfTask_K) + 1);
                    } else {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                    }
                }
            }
        }

        HashMap<Integer, String> printStrings = new HashMap<>();
        printStrings.put(0,"Tasks:\t");
        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                printStrings.put(wcetValue,wcetValue + "\t");
            });
        });
        System.out.println("WCET Value Occurrences per Task:");

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Occurrences");
        HashMap<Integer, Integer> wcetValues = new HashMap<>();

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetValues.containsKey(wcetValue)) {
                    wcetValues.replace(wcetValue, wcetValues.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetValues.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetvalue : wcetValues.keySet()) {
            System.out.println(wcetvalue + "\t" + wcetValues.get(wcetvalue));
        }

        System.out.println("\nTOTAL WCET HINTS PER TASK");

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("\nTOTAL WCET HINT HITS PER TASK");

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHints = new HashMap<>();

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHints.containsKey(wcetValue)) {
                    wcetHints.replace(wcetValue, wcetHints.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHints.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHint : wcetHints.keySet()) {
            System.out.println(wcetHint + "\t" + wcetHints.get(wcetHint));
        }

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHintHits = new HashMap<>();

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHintHits.containsKey(wcetValue)) {
                    wcetHintHits.replace(wcetValue, wcetHintHits.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHintHits.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHintHit : wcetHintHits.keySet()) {
            System.out.println(wcetHintHit + "\t" + wcetHintHits.get(wcetHintHit));
        }

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    /**
     * Test for examining the variety in the generated wcet depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 0 precedence relations, 5 subgraphs
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void wcetVarietyTestPERTASK5() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(5);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test

        HashMap<Integer, HashMap<Integer, Integer>> WCETValuesForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETValuesForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintsForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintHitsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintHitsForEachTask.putIfAbsent(i, new HashMap<>());
        int numSuccessfullIterations = 0;
        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            // save wcet values
            for (int k=0;k<numTasks;k++) {
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETValuesForEachTask.get(k).containsKey(wcetValueOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, WCETValuesForEachTask.get(k).get(wcetValueOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, 1);
                }
            }
            // save hint values
            numSuccessfullIterations++;
            for (int k=0;k<numTasks;k++) {
                int wcetHintOfTask_K = generationResult.wcetHints.get(k);
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETHintsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintsForEachTask.get(k).get(wcetHintOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                }
                if (wcetHintOfTask_K == wcetValueOfTask_K) {
                    if (WCETHintHitsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintHitsForEachTask.get(k).get(wcetHintOfTask_K) + 1);
                    } else {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                    }
                }
            }
        }

        HashMap<Integer, String> printStrings = new HashMap<>();
        printStrings.put(0,"Tasks:\t");
        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                printStrings.put(wcetValue,wcetValue + "\t");
            });
        });
        System.out.println("WCET Value Occurrences per Task:");

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Occurrences");
        HashMap<Integer, Integer> wcetValues = new HashMap<>();

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetValues.containsKey(wcetValue)) {
                    wcetValues.replace(wcetValue, wcetValues.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetValues.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetvalue : wcetValues.keySet()) {
            System.out.println(wcetvalue + "\t" + wcetValues.get(wcetvalue));
        }

        System.out.println("\nTOTAL WCET HINTS PER TASK");

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("\nTOTAL WCET HINT HITS PER TASK");

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHints = new HashMap<>();

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHints.containsKey(wcetValue)) {
                    wcetHints.replace(wcetValue, wcetHints.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHints.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHint : wcetHints.keySet()) {
            System.out.println(wcetHint + "\t" + wcetHints.get(wcetHint));
        }

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHintHits = new HashMap<>();

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHintHits.containsKey(wcetValue)) {
                    wcetHintHits.replace(wcetValue, wcetHintHits.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHintHits.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHintHit : wcetHintHits.keySet()) {
            System.out.println(wcetHintHit + "\t" + wcetHintHits.get(wcetHintHit));
        }

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    /**
     * Test for examining the variety in the generated wcet depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 0 precedence relations, 0 subgraphs
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, postHocprecedence = false,
     * postHocresidency = false, multi residency = false. We limit the wcet to a maximum of 10.
     */
    @Test
    public void wcetVarietyTestPERTASK6() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(10);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test

        HashMap<Integer, HashMap<Integer, Integer>> WCETValuesForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETValuesForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintsForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintHitsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintHitsForEachTask.putIfAbsent(i, new HashMap<>());
        int numSuccessfullIterations = 0;
        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            // save wcet values
            for (int k=0;k<numTasks;k++) {
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETValuesForEachTask.get(k).containsKey(wcetValueOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, WCETValuesForEachTask.get(k).get(wcetValueOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETValuesForEachTask.get(k).put(wcetValueOfTask_K, 1);
                }
            }
            // save hint values
            numSuccessfullIterations++;
            for (int k=0;k<numTasks;k++) {
                int wcetHintOfTask_K = generationResult.wcetHints.get(k);
                int wcetValueOfTask_K = generationResult.wcetValues.get(k);
                if (WCETHintsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                    // wcet for task k was already computed before and exists in the map
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintsForEachTask.get(k).get(wcetHintOfTask_K) +1 );
                } else {
                    // wcet for task k was not in the Hashmap
                    WCETHintsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                }
                if (wcetHintOfTask_K == wcetValueOfTask_K) {
                    if (WCETHintHitsForEachTask.get(k).containsKey(wcetHintOfTask_K)) {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, WCETHintHitsForEachTask.get(k).get(wcetHintOfTask_K) + 1);
                    } else {
                        WCETHintHitsForEachTask.get(k).put(wcetHintOfTask_K, 1);
                    }
                }
            }
        }

        HashMap<Integer, String> printStrings = new HashMap<>();
        printStrings.put(0,"Tasks:\t");
        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                printStrings.put(wcetValue,wcetValue + "\t");
            });
        });
        System.out.println("WCET Value Occurrences per Task:");

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Occurrences");
        HashMap<Integer, Integer> wcetValues = new HashMap<>();

        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetValues.containsKey(wcetValue)) {
                    wcetValues.replace(wcetValue, wcetValues.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetValues.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetvalue : wcetValues.keySet()) {
            System.out.println(wcetvalue + "\t" + wcetValues.get(wcetvalue));
        }

        System.out.println("\nTOTAL WCET HINTS PER TASK");

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("\nTOTAL WCET HINT HITS PER TASK");

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            printStrings.replace(0,printStrings.get(0) + "Task " + taskID + "\t");
            for (int i = 1; i< WCETMap.size()+1; i++) {
                printStrings.replace(i,printStrings.get(i) + WCETMap.get(i) + "\t");
            }
        });

        printStrings.forEach( (key, value) -> {
            System.out.println(value);
            printStrings.replace(key, key + "\t");
        });

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHints = new HashMap<>();

        WCETHintsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHints.containsKey(wcetValue)) {
                    wcetHints.replace(wcetValue, wcetHints.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHints.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHint : wcetHints.keySet()) {
            System.out.println(wcetHint + "\t" + wcetHints.get(wcetHint));
        }

        System.out.println("Total WCET Hints / Hits overall");
        HashMap<Integer, Integer> wcetHintHits = new HashMap<>();

        WCETHintHitsForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                if (wcetHintHits.containsKey(wcetValue)) {
                    wcetHintHits.replace(wcetValue, wcetHintHits.get(wcetValue) + numberOfOccurrences);
                } else {
                    wcetHintHits.put(wcetValue, numberOfOccurrences);
                }
            });
        });

        for (int wcetHintHit : wcetHintHits.keySet()) {
            System.out.println(wcetHintHit + "\t" + wcetHintHits.get(wcetHintHit));
        }

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 1 precedence relations,
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     *
     * This test is for determining bias in the selection of the first edge.
     */
    @Test
    public void precedenceVarietyTest0() {

        // number of iterations
        int numIterations = 100000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(1);
        inputs.setMax_total_precedence_relations(1);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 10 precedence relations,
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void precedenceVarietyTest1() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(10);
        inputs.setMax_total_precedence_relations(10);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                         //edgeList.add(new int[]{task.getIdentifier(),suc});
                         predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 20 precedence relations,
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void precedenceVarietyTest2() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(20);
        inputs.setMax_total_precedence_relations(20);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }


    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 30 precedence relations,
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void precedenceVarietyTest3() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(30);
        inputs.setMax_total_precedence_relations(30);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 40 precedence relations,
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void precedenceVarietyTest4() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(40);
        inputs.setMax_total_precedence_relations(40);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 40 precedence relations,
     * period of 10, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void precedenceVarietyTest4_2() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(40);
        inputs.setMax_total_precedence_relations(40);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(10);
        inputs.setMax_period_length(10);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }


    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 50 precedence relations,
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void precedenceVarietyTest5() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(50);
        inputs.setMax_total_precedence_relations(50);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }


    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 0 precedence relations, 9 subgraphs
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void precedenceVarietyTest6() {

        // number of iterations
        int numIterations = 100000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(9);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }


    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 0 precedence relations, 5 subgraphs
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void precedenceVarietyTest7() {

        // number of iterations
        int numIterations = 10;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(5);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }


    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 0 precedence relations, 1 subgraphs
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void precedenceVarietyTest8() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(1);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 5 residency constraints, 20 precedence relations, 0 subgraphs
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = false,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void precedenceVarietyTest9() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(5);
        inputs.setMax_residency_constraints(5);
        // Precedence
        inputs.setMin_total_precedence_relations(20);
        inputs.setMax_total_precedence_relations(20);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }


    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 20 precedence relations, 0 subgraphs
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = true,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void precedenceVarietyTest10() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(20);
        inputs.setMax_total_precedence_relations(20);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }



    /**
     * Test for examining the variety in the generated precedence relations depending on several different min and max values per task.
     * The default values for this test are: 10 tasks, 10 resources, 0 residency constraints, 1 precedence relations, 0 subgraphs
     * period of 20, periodicity of 100%, no set releasetimes, deadlines, wcet, postHocprecedence = true,
     * postHocresidency = false, multi residency = false
     */
    @Test
    public void precedenceVarietyTest11() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(1);
        inputs.setMax_total_precedence_relations(1);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            predToSucTotalEdges.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                if (i==j) continue;
                predToSucTotalEdges.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                System.out.println(predID + "\t" + sucID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> {
            value.forEach((sucID,numOccurrences) -> {
                taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> {
            System.out.println(predID + "\t" + numOcc);
        });

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> {
            System.out.println(sucID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 1 residency constraint
    @Test
    public void residencyVarietyTest0() {

        // number of iterations
        int numIterations = 100000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(1);
        inputs.setMax_residency_constraints(1);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 10 residency constraints
    @Test
    public void residencyVarietyTest1() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 5 residency constraints
    @Test
    public void residencyVarietyTest2() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(5);
        inputs.setMax_residency_constraints(5);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 10 - GEOMETRIC
    @Test
    public void residencyVarietyTest3() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.GEOMETRIC);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 5 - GEOMETRIC
    @Test
    public void residencyVarietyTest4() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(5);
        inputs.setMax_residency_constraints(5);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.GEOMETRIC);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 10 - POISSON 4.5
    @Test
    public void residencyVarietyTest5() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.POISSON);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);
        inputs.setResidencyPoissonMean(4.5);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 10 - POISSON 9.0
    @Test
    public void residencyVarietyTest6() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.POISSON);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);
        inputs.setResidencyPoissonMean(9.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 10 - BINOMIAL - N=10, P=0,5
    @Test
    public void residencyVarietyTest6_5() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.BINOMIAL);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);
        inputs.setResidencyPoissonMean(9.0);
        inputs.setResidencyBinomialN(10);
        inputs.setResidencyBinomialP(0.5);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 10 - BINOMIAL - N=10, P=0,5
    @Test
    public void residencyVarietyTest6_7() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.BINOMIAL);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);
        inputs.setResidencyPoissonMean(9.0);
        inputs.setResidencyBinomialN(9);
        inputs.setResidencyBinomialP(0.75);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 10 - UNIFORM - multi residency tests
    @Test
    public void residencyVarietyTest7() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(true);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        double totalAverageNumResidencyConstraints = 0.0;

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            int numResidencyConstraints = 0;
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                    numResidencyConstraints += task.getResourceConstraint().size();
                }
            }
            totalAverageNumResidencyConstraints += (double) numResidencyConstraints / (double) generationResult.taskSet.getTaskList().size();

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("TotalAverageNumResidencyConstraints: " + totalAverageNumResidencyConstraints + "\nAverage Residency Constraints per Task Set: " + totalAverageNumResidencyConstraints / (double) numIterations);

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 5 - UNIFORM - multi residency tests
    @Test
    public void residencyVarietyTest8() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(5);
        inputs.setMax_residency_constraints(5);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(true);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        double totalAverageNumResidencyConstraints = 0.0;

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            int numResidencyConstraints = 0;
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                    numResidencyConstraints += task.getResourceConstraint().size();
                }
            }
            totalAverageNumResidencyConstraints += (double) numResidencyConstraints / (double) generationResult.taskSet.getTaskList().size();

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("TotalAverageNumResidencyConstraints: " + totalAverageNumResidencyConstraints + "\nAverage Residency Constraints per Task Set: " + totalAverageNumResidencyConstraints / (double) numIterations);

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 10 - UNIFORM - posthoc residency
    @Test
    public void residencyVarietyTest9() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(true);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        double totalAverageNumResidencyConstraints = 0.0;

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            int numResidencyConstraints = 0;
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                    numResidencyConstraints += task.getResourceConstraint().size();
                }
            }
            totalAverageNumResidencyConstraints += (double) numResidencyConstraints / (double) generationResult.taskSet.getTaskList().size();

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("TotalAverageNumResidencyConstraints: " + totalAverageNumResidencyConstraints + "\nAverage Residency Constraints per Task Set: " + totalAverageNumResidencyConstraints / (double) numIterations);

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 5 - UNIFORM - posthoc residency
    @Test
    public void residencyVarietyTest10() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(5);
        inputs.setMax_residency_constraints(5);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(true);
        inputs.setPostHocResidency(true);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            //ArrayList<int[]> edgeList = new ArrayList<>();
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        //edgeList.add(new int[]{task.getIdentifier(),suc});
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 10 - UNIFORM - multi residency and posthoc tests
    @Test
    public void residencyVarietyTest11() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(true);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(true);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        double totalAverageNumResidencyConstraints = 0.0;

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            int numResidencyConstraints = 0;
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                    numResidencyConstraints += task.getResourceConstraint().size();
                }
            }
            totalAverageNumResidencyConstraints += (double) numResidencyConstraints / (double) generationResult.taskSet.getTaskList().size();

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("TotalAverageNumResidencyConstraints: " + totalAverageNumResidencyConstraints + "\nAverage Residency Constraints per Task Set: " + totalAverageNumResidencyConstraints / (double) numIterations);

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 5 - UNIFORM - multi_residency and posthoc
    @Test
    public void residencyVarietyTest12() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(5);
        inputs.setMax_residency_constraints(5);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(true);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(true);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numTasks;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        double totalAverageNumResidencyConstraints = 0.0;

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            int numResidencyConstraints = 0;
            for (Task task : generationResult.taskSet.getTaskList()) {
                //System.out.println(task);
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                    numResidencyConstraints += task.getResourceConstraint().size();
                }
            }
            totalAverageNumResidencyConstraints += (double) numResidencyConstraints / (double) generationResult.taskSet.getTaskList().size();

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("TotalAverageNumResidencyConstraints: " + totalAverageNumResidencyConstraints + "\nAverage Residency Constraints per Task Set: " + totalAverageNumResidencyConstraints / (double) numIterations);

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 10 residency constraints, 5 resources
    @Test
    public void residencyVarietyTest13() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        int numResources = 5;
        inputs.setMin_number_of_resources(numResources);
        inputs.setMax_number_of_resources(numResources);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numResources;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible
            tgf.setWarnings(new ArrayList<>()); // clear the warnings
            tgf.setUtils(new Utils(DEBUG, new Random()));

            for (Task task : generationResult.taskSet.getTaskList()) {
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            numSuccessfullIterations++;
        }

        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                System.out.println(taskID + "\t" + resourceID + "\t" + numOccurrences);
            });
        });

        HashMap<Integer, Integer> taskIDOccurrences = new HashMap<>();
        HashMap<Integer, Integer> resourceIDOccurrences = new HashMap<>();
        resourceIdentifierOccurrencesPerTaskID.forEach((taskID,value) -> {
            value.forEach((resourceID,numOccurrences) -> {
                taskIDOccurrences.compute(taskID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
                resourceIDOccurrences.compute(resourceID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            });
        });

        System.out.println("\nTaskID Occurrences:");
        taskIDOccurrences.forEach((taskID, numOcc) -> {
            System.out.println(taskID + "\t" + numOcc);
        });

        System.out.println("\nResource Occurrences:");
        resourceIDOccurrences.forEach((resourceID, numOcc) -> {
            System.out.println(resourceID + "\t" + numOcc);
        });

        System.out.println("Successfull iterations: " + numSuccessfullIterations);
    }

    // 5 to 15 tasks - UNIFORM
    @Test
    public void numberOfTasksVarietyTest1() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 15;
        inputs.setMin_number_of_tasks(5);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);

        inputs.setNumberOfTasks_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> numberOfTasksOccurrences = new HashMap<>();
        for (int i=0;i<numTasks;i++) numberOfTasksOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            numberOfTasksOccurrences.compute(generationResult.wcetHints.size(), (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }

        System.out.println("Number of Task Occurrences");
        numberOfTasksOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    // 5 to 15 tasks - GEOMETRIC
    @Test
    public void numberOfTasksVarietyTest2() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 15;
        inputs.setMin_number_of_tasks(5);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);

        inputs.setNumberOfTasks_distribution(InputParameters.Distribution.GEOMETRIC);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> numberOfTasksOccurrences = new HashMap<>();
        for (int i=5;i<numTasks;i++) numberOfTasksOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            numberOfTasksOccurrences.compute(generationResult.wcetHints.size(), (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }

        System.out.println("Number of Task \t Occurrences");
        numberOfTasksOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    // 5 to 15 tasks - POISSON - 10.0
    @Test
    public void numberOfTasksVarietyTest3() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 15;
        inputs.setMin_number_of_tasks(5);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);

        inputs.setNumberOfTasks_distribution(InputParameters.Distribution.POISSON);
        inputs.setNOTasksPoissonMean(10.0);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> numberOfTasksOccurrences = new HashMap<>();
        for (int i=5;i<numTasks;i++) numberOfTasksOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            numberOfTasksOccurrences.compute(generationResult.wcetHints.size(), (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }

        System.out.println("Number of Task \t Occurrences");
        numberOfTasksOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    // 5 to 15 tasks - BINOMIAL - 15 , 0.75
    @Test
    public void numberOfTasksVarietyTest4() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 15;
        inputs.setMin_number_of_tasks(5);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);

        inputs.setNumberOfTasks_distribution(InputParameters.Distribution.BINOMIAL);
        inputs.setNOTasksBinomialN(15);
        inputs.setNOTasksBinomialP(0.75);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> numberOfTasksOccurrences = new HashMap<>();
        for (int i=5;i<numTasks;i++) numberOfTasksOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            numberOfTasksOccurrences.compute(generationResult.wcetHints.size(), (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }

        System.out.println("Number of Task \t Occurrences");
        numberOfTasksOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }
    // 5 to 15 tasks - UNIFORM

    @Test
    public void numberOfResourcesVarietyTest1() {
        String testName = "Test 1 - 5 to 10 resources - UNIFORM";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(5);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> numberOfUniqueResourceOccurrences = new HashMap<>();
        for (int i=1;i<10;i++) numberOfUniqueResourceOccurrences.put(i,0);

        HashMap<Integer, Integer> numberOfResourceOccurrences = new HashMap<>();
        for (int i=1;i<10;i++) numberOfResourceOccurrences.put(i,0);


        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            ArrayList<Integer> resourceIDList = new ArrayList<>();
            generationResult.taskSet.getTaskList().forEach((task -> {
                if (!resourceIDList.contains(task.getResourceConstraint().get(0))) resourceIDList.add(task.getResourceConstraint().get(0));
            }));

            numberOfUniqueResourceOccurrences.compute(resourceIDList.size(), (k, v) -> (v == null) ? 1 : v+1);

            numberOfResourceOccurrences.compute(generationResult.taskSet.getNum_resources(), (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }

        System.out.println(testName);
        System.out.println("Number of Unique Resources in Residency Constraints \t Occurrences");
        numberOfUniqueResourceOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });
        System.out.println("Task Set Number of Resources \t Occurrences");
        numberOfResourceOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void numberOfResourcesVarietyTest2() {
        String testName = "Test 2 - 5 to 10 resources - GEOMETRIC";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(5);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setNumberOfResources_distribution(InputParameters.Distribution.GEOMETRIC);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> numberOfUniqueResourceOccurrences = new HashMap<>();
        for (int i=1;i<10;i++) numberOfUniqueResourceOccurrences.put(i,0);

        HashMap<Integer, Integer> numberOfResourceOccurrences = new HashMap<>();
        for (int i=1;i<10;i++) numberOfResourceOccurrences.put(i,0);


        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            ArrayList<Integer> resourceIDList = new ArrayList<>();
            generationResult.taskSet.getTaskList().forEach((task -> {
                if (!resourceIDList.contains(task.getResourceConstraint().get(0))) resourceIDList.add(task.getResourceConstraint().get(0));
            }));

            numberOfUniqueResourceOccurrences.compute(resourceIDList.size(), (k, v) -> (v == null) ? 1 : v+1);

            numberOfResourceOccurrences.compute(generationResult.taskSet.getNum_resources(), (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }

        System.out.println(testName);
        System.out.println("Number of Unique Resources in Residency Constraints \t Occurrences");
        numberOfUniqueResourceOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });
        System.out.println("Task Set Number of Resources \t Occurrences");
        numberOfResourceOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void numberOfResourcesVarietyTest3() {
        String testName = "Test 3 - 5 to 10 resources - POISSON - 7.5";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(5);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setNumberOfResources_distribution(InputParameters.Distribution.POISSON);
        inputs.setNOResourcesPoissonMean(7.5);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> numberOfUniqueResourceOccurrences = new HashMap<>();
        for (int i=1;i<10;i++) numberOfUniqueResourceOccurrences.put(i,0);

        HashMap<Integer, Integer> numberOfResourceOccurrences = new HashMap<>();
        for (int i=1;i<10;i++) numberOfResourceOccurrences.put(i,0);


        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            ArrayList<Integer> resourceIDList = new ArrayList<>();
            generationResult.taskSet.getTaskList().forEach((task -> {
                if (!resourceIDList.contains(task.getResourceConstraint().get(0))) resourceIDList.add(task.getResourceConstraint().get(0));
            }));

            numberOfUniqueResourceOccurrences.compute(resourceIDList.size(), (k, v) -> (v == null) ? 1 : v+1);

            numberOfResourceOccurrences.compute(generationResult.taskSet.getNum_resources(), (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }

        System.out.println(testName);
        System.out.println("Number of Unique Resources in Residency Constraints \t Occurrences");
        numberOfUniqueResourceOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });
        System.out.println("Task Set Number of Resources \t Occurrences");
        numberOfResourceOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void numberOfResourcesVarietyTest4() {
        String testName = "Test 4 - 5 to 10 resources - BINOMIAL - 10 - 0.9";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(5);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(10);
        inputs.setMax_residency_constraints(10);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setNumberOfResources_distribution(InputParameters.Distribution.BINOMIAL);
        inputs.setNOResourcesBinomialN(10);
        inputs.setNOResourcesBinomialP(0.9);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> numberOfUniqueResourceOccurrences = new HashMap<>();
        for (int i=1;i<10;i++) numberOfUniqueResourceOccurrences.put(i,0);

        HashMap<Integer, Integer> numberOfResourceOccurrences = new HashMap<>();
        for (int i=1;i<10;i++) numberOfResourceOccurrences.put(i,0);


        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            ArrayList<Integer> resourceIDList = new ArrayList<>();
            generationResult.taskSet.getTaskList().forEach((task -> {
                if (!resourceIDList.contains(task.getResourceConstraint().get(0))) resourceIDList.add(task.getResourceConstraint().get(0));
            }));

            numberOfUniqueResourceOccurrences.compute(resourceIDList.size(), (k, v) -> (v == null) ? 1 : v+1);

            numberOfResourceOccurrences.compute(generationResult.taskSet.getNum_resources(), (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }

        System.out.println(testName);
        System.out.println("Number of Unique Resources in Residency Constraints \t Occurrences");
        numberOfUniqueResourceOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });
        System.out.println("Task Set Number of Resources \t Occurrences");
        numberOfResourceOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void periodLengthVarietyTest1() {
        String testName = "Test 1 - 5 to 8 period - UNIFORM";

        // number of iterations
        int numIterations = 100;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 5;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(5);
        inputs.setMax_period_length(8);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(0);
        inputs.setMax_releaseTime(0);
        inputs.setMin_deadline(1);
        inputs.setMax_deadline(1);
        inputs.setMin_WCET(1);
        inputs.setMax_WCET(1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> periodLengthOccurrences = new HashMap<>();
        for (int i=0;i<=15;i++) periodLengthOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            generationResult.taskSet.getTaskList().forEach((task -> {
                if (!(task instanceof TaskInstance)) {
                    periodLengthOccurrences.compute(task.getPeriod(), (k, v) -> (v == null) ? 1 : v + 1);
                }
            }));

            numSuccessfullIterations++;

        }
        System.out.println(testName);
        System.out.println("Period Length \t Occurrences");
        periodLengthOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void periodicityVarietyTest1() {
        String testName = "Test 1 - 0 to 100 periodicity - UNIFORM";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(0);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> periodicityOccurrences = new HashMap<>();
        for (int i=0;i<=15;i++) periodicityOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            int tasksWithPeriod = 0;
            for (Task t : generationResult.taskSet.getTaskList()) {
                if (!(t instanceof TaskInstance)) {
                    if (t.isPeriodic()) tasksWithPeriod++;
                }
            }

            periodicityOccurrences.compute(tasksWithPeriod, (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }
        System.out.println(testName);
        System.out.println("Periodic Tasks \t Occurrences");
        periodicityOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void periodicityVarietyTest2() {
        String testName = "Test 2 - 50 to 100 periodicity - UNIFORM";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(50);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> periodicityOccurrences = new HashMap<>();
        for (int i=0;i<=15;i++) periodicityOccurrences.put(i,0);
        HashMap<Integer, Integer> periodicityPerTaskOccurrences = new HashMap<>();
        for (int i=0;i<numTasks;i++) periodicityPerTaskOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            int tasksWithPeriod = 0;
            for (Task t : generationResult.taskSet.getTaskList()) {
                if (!(t instanceof TaskInstance)) {
                    if (t.isPeriodic()) {
                        tasksWithPeriod++;
                        periodicityPerTaskOccurrences.compute(t.getIdentifier(), (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            periodicityOccurrences.compute(tasksWithPeriod, (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }
        System.out.println(testName);
        /*System.out.println("Periodic Tasks \t Occurrences");
        periodicityOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });*/
        System.out.println("TaskID \t Occurrences of periodicity");
        periodicityPerTaskOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void periodicityVarietyTest3() {
        String testName = "Test 3 - 50 to 100 periodicity - GEOMETRIC";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(50);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.GEOMETRIC);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> periodicityOccurrences = new HashMap<>();
        for (int i=0;i<=15;i++) periodicityOccurrences.put(i,0);
        HashMap<Integer, Integer> periodicityPerTaskOccurrences = new HashMap<>();
        for (int i=0;i<numTasks;i++) periodicityPerTaskOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            int tasksWithPeriod = 0;
            for (Task t : generationResult.taskSet.getTaskList()) {
                if (!(t instanceof TaskInstance)) {
                    if (t.isPeriodic()) {
                        tasksWithPeriod++;
                        periodicityPerTaskOccurrences.compute(t.getIdentifier(), (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            periodicityOccurrences.compute(tasksWithPeriod, (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }
        System.out.println(testName);
        /*System.out.println("Periodic Tasks \t Occurrences");
        periodicityOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });*/
        System.out.println("TaskID \t Occurrences of periodicity");
        periodicityPerTaskOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void periodicityVarietyTest4() {
        String testName = "Test 4 - 50 to 100 periodicity - POISSON - 3.0";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(50);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.POISSON);
        inputs.setPeriodicityPoissonMean(3.0);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> periodicityOccurrences = new HashMap<>();
        for (int i=0;i<=15;i++) periodicityOccurrences.put(i,0);
        HashMap<Integer, Integer> periodicityPerTaskOccurrences = new HashMap<>();
        for (int i=0;i<numTasks;i++) periodicityPerTaskOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            int tasksWithPeriod = 0;
            for (Task t : generationResult.taskSet.getTaskList()) {
                if (!(t instanceof TaskInstance)) {
                    if (t.isPeriodic()) {
                        tasksWithPeriod++;
                        periodicityPerTaskOccurrences.compute(t.getIdentifier(), (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            periodicityOccurrences.compute(tasksWithPeriod, (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }
        System.out.println(testName);
        /*System.out.println("Periodic Tasks \t Occurrences");
        periodicityOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });*/
        System.out.println("TaskID \t Occurrences of periodicity");
        periodicityPerTaskOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void periodicityVarietyTest5() {
        String testName = "Test 5 - 50 to 100 periodicity - BINOMIAL - 10 - 0.5";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(50);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.BINOMIAL);
        inputs.setPeriodicityBinomialN(10);
        inputs.setPeriodicityBinomialP(0.5);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> periodicityOccurrences = new HashMap<>();
        for (int i=0;i<=15;i++) periodicityOccurrences.put(i,0);
        HashMap<Integer, Integer> periodicityPerTaskOccurrences = new HashMap<>();
        for (int i=0;i<numTasks;i++) periodicityPerTaskOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            int tasksWithPeriod = 0;
            for (Task t : generationResult.taskSet.getTaskList()) {
                if (!(t instanceof TaskInstance)) {
                    if (t.isPeriodic()) {
                        tasksWithPeriod++;
                        periodicityPerTaskOccurrences.compute(t.getIdentifier(), (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            periodicityOccurrences.compute(tasksWithPeriod, (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }
        System.out.println(testName);
        /*System.out.println("Periodic Tasks \t Occurrences");
        periodicityOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });*/
        System.out.println("TaskID \t Occurrences of periodicity");
        periodicityPerTaskOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void periodicityVarietyTest6() {
        String testName = "Test 6 - 10 to 100 periodicity - GEOMETRIC";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(10);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(20);
        inputs.setMax_period_length(20);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.GEOMETRIC);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> periodicityOccurrences = new HashMap<>();
        for (int i=0;i<=15;i++) periodicityOccurrences.put(i,0);
        HashMap<Integer, Integer> periodicityPerTaskOccurrences = new HashMap<>();
        for (int i=0;i<numTasks;i++) periodicityPerTaskOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            int tasksWithPeriod = 0;
            for (Task t : generationResult.taskSet.getTaskList()) {
                if (!(t instanceof TaskInstance)) {
                    if (t.isPeriodic()) {
                        tasksWithPeriod++;
                        periodicityPerTaskOccurrences.compute(t.getIdentifier(), (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

            periodicityOccurrences.compute(tasksWithPeriod, (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }
        System.out.println(testName);
        /*System.out.println("Periodic Tasks \t Occurrences");
        periodicityOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });*/
        System.out.println("TaskID \t Occurrences of periodicity");
        periodicityPerTaskOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void releaseTimeVarietyTest1() {
        String testName = "Test 1 - 0 to 10 releasetime";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(10);
        inputs.setMax_period_length(10);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(0);
        inputs.setMax_releaseTime(10);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> releaseTimeOccurrences = new HashMap<>();
        for (int i=0;i<10;i++) releaseTimeOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            generationResult.taskSet.getTaskList().forEach((task -> {
                releaseTimeOccurrences.compute(task.getReleaseTime(), (k, v) -> (v == null) ? 1 : v+1);
            }));

            numSuccessfullIterations++;

        }

        System.out.println(testName);
        System.out.println("Release Time \t Occurrences");
        releaseTimeOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void releaseTimeVarietyTest2() {
        String testName = "Test 1 - 0 to 5 releasetime";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(10);
        inputs.setMax_period_length(10);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(0);
        inputs.setMax_releaseTime(5);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> releaseTimeOccurrences = new HashMap<>();
        for (int i=0;i<10;i++) releaseTimeOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            generationResult.taskSet.getTaskList().forEach((task -> {
                releaseTimeOccurrences.compute(task.getReleaseTime(), (k, v) -> (v == null) ? 1 : v+1);
            }));

            numSuccessfullIterations++;

        }

        System.out.println(testName);
        System.out.println("Release Time \t Occurrences");
        releaseTimeOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void deadlineVarietyTest1() {
        String testName = "Test 1 - 1 to 10 deadline";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(10);
        inputs.setMax_period_length(10);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(1);
        inputs.setMax_deadline(10);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> deadlineOccurrences = new HashMap<>();
        for (int i=1;i<=20;i++) deadlineOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            generationResult.taskSet.getTaskList().forEach((task -> {
                deadlineOccurrences.compute(task.getDeadline(), (k, v) -> (v == null) ? 1 : v+1);
            }));

            numSuccessfullIterations++;

        }
        System.out.println(testName);
        System.out.println("Deadline \t Occurrences");
        deadlineOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void deadlineVarietyTest2() {
        String testName = "Test 1 - 1 to 10 deadline";

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 10;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        inputs.setMin_number_of_resources(10);
        inputs.setMax_number_of_resources(10);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(10);
        inputs.setMax_period_length(10);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(0.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);


        // execute the test
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> deadlineOccurrences = new HashMap<>();
        for (int i=1;i<=20;i++) deadlineOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            generationResult.taskSet.getTaskList().forEach((task -> {
                deadlineOccurrences.compute(task.getDeadline(), (k, v) -> (v == null) ? 1 : v+1);
            }));

            numSuccessfullIterations++;

        }
        System.out.println(testName);
        System.out.println("Deadline \t Occurrences");
        deadlineOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

}
