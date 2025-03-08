package dt.tsg.Versatility;

import dt.tsg.InputParams.InputParameters;
import dt.tsg.task.Task;
import dt.tsg.task.TaskGenerationFramework;
import dt.tsg.utils.GenerationResult;
import dt.tsg.utils.Utils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class VersatilityTests {

    final boolean DEBUG = false;

    @Test
    public void postHocPrecedenceVersatilityTest1() {

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

        HashMap<Integer, HashMap<Integer, Integer>> WCETValuesForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETValuesForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintsForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintHitsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintHitsForEachTask.putIfAbsent(i, new HashMap<>());
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
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

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

            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }
        }

        HashMap<Integer, String> printStrings = new HashMap<>();
        printStrings.put(0,"WCET\t");
        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                printStrings.put(wcetValue,wcetValue + "\t");
            });
        });
        System.out.println("\nWCET Value Occurrences per Task:");

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

        System.out.println("\nTotal WCET Occurrences");
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

        System.out.println("\nTotal WCET Hints / Hits overall");
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

        System.out.println("\nTotal WCET Hints / Hits overall");
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

        System.out.println("\nEdge List");

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

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void postHocPrecedenceVersatilityTest2() {

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
        inputs.setPostHocPrecedence(true);
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
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

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

            for (Task task : generationResult.taskSet.getTaskList()) {
                System.out.println(task);
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }
        }

        HashMap<Integer, String> printStrings = new HashMap<>();
        printStrings.put(0,"WCET\t");
        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                printStrings.put(wcetValue,wcetValue + "\t");
            });
        });
        System.out.println("\nWCET Value Occurrences per Task:");

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

        System.out.println("\nTotal WCET Occurrences");
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

        System.out.println("\nTotal WCET Hints / Hits overall");
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

        System.out.println("\nTotal WCET Hints / Hits overall");
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

        System.out.println("\nEdge List");

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

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void postHocResidencyVersatilityTest1() {

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
        int numResources = 10;
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

        HashMap<Integer, HashMap<Integer, Integer>> WCETValuesForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETValuesForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintsForEachTask.putIfAbsent(i, new HashMap<>());
        HashMap<Integer, HashMap<Integer, Integer>> WCETHintHitsForEachTask = new HashMap<>();
        for (int i=0;i<numTasks;i++) WCETHintHitsForEachTask.putIfAbsent(i, new HashMap<>());
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
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

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

            for (Task task : generationResult.taskSet.getTaskList()) {
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

        }

        HashMap<Integer, String> printStrings = new HashMap<>();
        printStrings.put(0,"WCET\t");
        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                printStrings.put(wcetValue,wcetValue + "\t");
            });
        });
        System.out.println("\nWCET Value Occurrences per Task:");

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

        System.out.println("\nTotal WCET Occurrences");
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

        System.out.println("\nTotal WCET Hints / Hits overall");
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

        System.out.println("\nTotal WCET Hints / Hits overall");
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

        System.out.println("\nResource Occurrences per TaskID");
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


        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void postHocResidencyVersatilityTest2() {

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
        int numResources = 10;
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
        inputs.setPostHocResidency(true);
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

        HashMap<Integer,HashMap<Integer,Integer>> resourceIdentifierOccurrencesPerTaskID = new HashMap<>();
        for (int i = 0;i< numTasks;i++) {
            resourceIdentifierOccurrencesPerTaskID.put(i,new HashMap<>());
            for (int j=0;j<numResources;j++) {
                resourceIdentifierOccurrencesPerTaskID.get(i).put(j, 0);
            }
        }

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

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

            for (Task task : generationResult.taskSet.getTaskList()) {
                if (!task.getResourceConstraint().isEmpty()) {
                    for (int res : task.getResourceConstraint()) {
                        resourceIdentifierOccurrencesPerTaskID.get(task.getIdentifier()).compute(res, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }

        }

        HashMap<Integer, String> printStrings = new HashMap<>();
        printStrings.put(0,"WCET\t");
        WCETValuesForEachTask.forEach( (taskID, WCETMap) -> {
            WCETMap.forEach( (wcetValue, numberOfOccurrences) -> {
                printStrings.put(wcetValue,wcetValue + "\t");
            });
        });
        System.out.println("\nWCET Value Occurrences per Task:");

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

        System.out.println("\nTotal WCET Occurrences");
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

        System.out.println("\nTotal WCET Hints / Hits overall");
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

        System.out.println("\nTotal WCET Hints / Hits overall");
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

        System.out.println("\nResource Occurrences per TaskID");
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


        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

}
