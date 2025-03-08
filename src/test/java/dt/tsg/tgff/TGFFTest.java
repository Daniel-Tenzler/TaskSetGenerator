package dt.tsg.tgff;

import dt.tsg.InputParams.TGFFInputParameters;
import dt.tsg.task.Task;
import dt.tsg.task.TaskGenerationFramework;
import dt.tsg.utils.GenerationResult;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import static java.lang.System.gc;

public class TGFFTest {

    final boolean DEBUG = false;

    private void printHints(GenerationResult generationResult) {

        if (generationResult == null) return;

        System.out.println("WCET HINTS");
        for (int i : generationResult.wcetHints) {
            System.out.print(i + "\t");
        }
        System.out.println("\nWCET VALUES");
        for (int i : generationResult.wcetValues) {
            System.out.print(i + "\t");
        }

        if (generationResult.dynamicHints.size() != 6 || generationResult.dynamicValues.isEmpty()) return;

        System.out.println("\nDynamicConstraints:");
        System.out.println("cp_min_releasetime Hint/Value:\t" + generationResult.dynamicHints.get(0) + "/" + generationResult.dynamicValues.get(0));
        System.out.println("cp_max_releasetime Hint/Value:\t" + generationResult.dynamicHints.get(1) + "/" + generationResult.dynamicValues.get(1));
        System.out.println("cp_min_deadline Hint/Value:\t"+ generationResult.dynamicHints.get(2) + "/" + generationResult.dynamicValues.get(2));
        System.out.println("cp_max_deadline Hint/Value:\t" + generationResult.dynamicHints.get(3) + "/" + generationResult.dynamicValues.get(3));
        System.out.println("cp_min_wcet Hint/Value:\t" + generationResult.dynamicHints.get(4) + "/" + generationResult.dynamicValues.get(4));
        System.out.println("cp_max_wcet Hint/Value:\t" + generationResult.dynamicHints.get(5) + "/" + generationResult.dynamicValues.get(5));

    }

    // period mul 2 - graphcount 1 - tasks 4/1 - periodpercentage 1.0 - multistartnodeprob 1.0 - startnodes 1/1 - degree 3/3 - resources 10 - tasktranstime 4.0 - calcperiods true - posthoc residency true
    @Test
    void tgffTest0() throws Exception {

        int iterations = 1000;

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        TGFFInputParameters inputs = new TGFFInputParameters();
        ArrayList<Integer> period_mul = new ArrayList<>() {
        };
        period_mul.add(2);
        //period_mul.add(2);
        //period_mul.add(3);
        //period_mul.add(3);
        //period_mul.add(4);
        inputs.setPeriod_mul(period_mul);
        inputs.setGraphCount(1);
        inputs.setTaskCountAverage(4);
        inputs.setTaskCountMultiplier(1);
        inputs.setPeriodPercentage(1.0);
        inputs.setMultiStartNodeProb(1.0);
        inputs.setStartNodesAverage(1);
        inputs.setStartNodesMultiplier(1);
        inputs.setTaskDegreeIN(3);
        inputs.setTaskDegreeOUT(3);
        inputs.setHardDeadlineProb(1.0);
        inputs.setNumberOfResources(10);
        inputs.setTaskTransitionTime(4.0);
        inputs.setCleanUpFiles(true);
        inputs.setPostHocResidency(true);
        inputs.setCalculatePeriods(true);

        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);

        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);

        executeTest(iterations, tgf, inputs);

    }

    // graphcount 1 -> 2
    // period mul 2 - graphcount 2 - tasks 4/1 - periodpercentage 1.0 - multistartnodeprob 1.0 - startnodes 1/1 - degree 3/3 - resources 10 - tasktranstime 4.0 - calcperiods true - posthoc residency true
    @Test
    void tgffTest1() throws Exception {

        int iterations = 1000;

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        TGFFInputParameters inputs = new TGFFInputParameters();
        ArrayList<Integer> period_mul = new ArrayList<>() {
        };
        //period_mul.add(2);
        //period_mul.add(3);
        //period_mul.add(3);
        period_mul.add(2);
        inputs.setPeriod_mul(period_mul);
        inputs.setGraphCount(2);
        inputs.setTaskCountAverage(4);
        inputs.setTaskCountMultiplier(1);
        inputs.setPeriodPercentage(1.0);
        inputs.setMultiStartNodeProb(1.0);
        inputs.setStartNodesAverage(1);
        inputs.setStartNodesMultiplier(1);
        inputs.setTaskDegreeIN(3);
        inputs.setTaskDegreeOUT(3);
        inputs.setHardDeadlineProb(1.0);
        inputs.setNumberOfResources(10);
        inputs.setTaskTransitionTime(4.0);
        inputs.setCleanUpFiles(true);
        inputs.setPostHocResidency(true);
        inputs.setCalculatePeriods(true);
        inputs.setSaveOutput(false);

        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);

        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);

        executeTest(iterations,tgf,inputs);
    }

    // periodmul 2 -> 4
    // period mul 4 - graphcount 2 - tasks 4/1 - periodpercentage 1.0 - multistartnodeprob 1.0 - startnodes 1/1 - degree 3/3 - resources 10 - tasktranstime 4.0 - calcperiods true - posthoc residency true
    @Test
    void tgffTest2() throws Exception {

        int iterations = 1000;

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        TGFFInputParameters inputs = new TGFFInputParameters();
        ArrayList<Integer> period_mul = new ArrayList<>() {
        };
        //period_mul.add(2);
        //period_mul.add(3);
        //period_mul.add(3);
        period_mul.add(4);
        inputs.setPeriod_mul(period_mul);
        inputs.setGraphCount(2);
        inputs.setTaskCountAverage(4);
        inputs.setTaskCountMultiplier(1);
        inputs.setPeriodPercentage(1.0);
        inputs.setMultiStartNodeProb(1.0);
        inputs.setStartNodesAverage(1);
        inputs.setStartNodesMultiplier(1);
        inputs.setTaskDegreeIN(3);
        inputs.setTaskDegreeOUT(3);
        inputs.setHardDeadlineProb(1.0);
        inputs.setNumberOfResources(10);
        inputs.setTaskTransitionTime(4.0);
        inputs.setCleanUpFiles(true);
        inputs.setPostHocResidency(true);
        inputs.setCalculatePeriods(true);
        inputs.setSaveOutput(false);

        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);

        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);

        executeTest(iterations,tgf,inputs);
    }

    // resources 10 -> 20
    // period mul 4 - graphcount 2 - tasks 4/1 - periodpercentage 1.0 - multistartnodeprob 1.0 - startnodes 1/1 - degree 3/3 - resources 20 - tasktranstime 4.0 - calcperiods true - posthoc residency true
    @Test
    void tgffTest3() throws Exception {

        int iterations = 1000;

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        TGFFInputParameters inputs = new TGFFInputParameters();
        ArrayList<Integer> period_mul = new ArrayList<>() {
        };
        //period_mul.add(2);
        //period_mul.add(3);
        //period_mul.add(3);
        period_mul.add(4);
        inputs.setPeriod_mul(period_mul);
        inputs.setGraphCount(2);
        inputs.setTaskCountAverage(4);
        inputs.setTaskCountMultiplier(1);
        inputs.setPeriodPercentage(1.0);
        inputs.setMultiStartNodeProb(1.0);
        inputs.setStartNodesAverage(1);
        inputs.setStartNodesMultiplier(1);
        inputs.setTaskDegreeIN(3);
        inputs.setTaskDegreeOUT(3);
        inputs.setHardDeadlineProb(1.0);
        inputs.setNumberOfResources(20);
        inputs.setTaskTransitionTime(4.0);
        inputs.setCleanUpFiles(true);
        inputs.setPostHocResidency(true);
        inputs.setCalculatePeriods(true);
        inputs.setSaveOutput(false);

        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);

        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);

        executeTest(iterations, tgf, inputs);
    }

    /*
    // period mul 4 - graphcount 2 - tasks 4/1 - periodpercentage 1.0 - multistartnodeprob 1.0 - startnodes 1/1 - degree 3/3 - resources 20 - tasktranstime 4.0 - calcperiods true - posthoc residency true
    @Test
    void tgffTest5() throws Exception {

        int iterations = 1000;

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG);

        TGFFInputParameters inputs = new TGFFInputParameters();
        ArrayList<Integer> period_mul = new ArrayList<>() {
        };
        period_mul.add(4);
        //period_mul.add(2);
        //period_mul.add(3);
        //period_mul.add(3);
        //period_mul.add(4);
        inputs.setPeriod_mul(period_mul);
        inputs.setGraphCount(2);
        inputs.setTaskCountAverage(4);
        inputs.setTaskCountMultiplier(1);
        inputs.setPeriodPercentage(1.0);
        inputs.setMultiStartNodeProb(1.0);
        inputs.setStartNodesAverage(1);
        inputs.setStartNodesMultiplier(1);
        inputs.setTaskDegreeIN(3);
        inputs.setTaskDegreeOUT(3);
        inputs.setHardDeadlineProb(1.0);
        inputs.setNumberOfResources(20);
        inputs.setTaskTransitionTime(4.0);
        inputs.setCleanUpFiles(true);
        inputs.setPostHocResidency(true);
        inputs.setCalculatePeriods(true);

        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);

        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        executeTest(iterations, tgf, inputs);
    }
    */

    // Nicht wirklich spannend - periodmul 4 -> 8
    // period mul 8 - graphcount 2 - tasks 4/1 - periodpercentage 1.0 - multistartnodeprob 1.0 - startnodes 1/1 - degree 3/3 - resources 20 - tasktranstime 4.0 - calcperiods true - posthoc residency true
    @Test
    void tgffTest6() throws Exception {

        int iterations = 1000;

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        TGFFInputParameters inputs = new TGFFInputParameters();
        ArrayList<Integer> period_mul = new ArrayList<>() {
        };
        period_mul.add(8);
        //period_mul.add(2);
        //period_mul.add(3);
        //period_mul.add(3);
        //period_mul.add(4);
        inputs.setPeriod_mul(period_mul);
        inputs.setGraphCount(2);
        inputs.setTaskCountAverage(4);
        inputs.setTaskCountMultiplier(1);
        inputs.setPeriodPercentage(1.0);
        inputs.setMultiStartNodeProb(1.0);
        inputs.setStartNodesAverage(1);
        inputs.setStartNodesMultiplier(1);
        inputs.setTaskDegreeIN(3);
        inputs.setTaskDegreeOUT(3);
        inputs.setHardDeadlineProb(1.0);
        inputs.setNumberOfResources(20);
        inputs.setTaskTransitionTime(4.0);
        inputs.setCleanUpFiles(true);
        inputs.setPostHocResidency(true);
        inputs.setCalculatePeriods(true);
        inputs.setSaveOutput(false);

        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);

        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);

        executeTest(iterations, tgf, inputs);

    }

    // Nicht wirklich interessant - graphcount 2 -> 4
    // period mul 4 - graphcount 4 - tasks 4/1 - periodpercentage 1.0 - multistartnodeprob 1.0 - startnodes 1/1 - degree 3/3 - resources 20 - tasktranstime 4.0 - calcperiods true - posthoc residency true
    @Test
    void tgffTest7() throws Exception {

        int iterations = 1000;

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        TGFFInputParameters inputs = new TGFFInputParameters();
        ArrayList<Integer> period_mul = new ArrayList<>() {
        };
        period_mul.add(4);
        //period_mul.add(2);
        //period_mul.add(3);
        //period_mul.add(3);
        //period_mul.add(4);
        inputs.setPeriod_mul(period_mul);
        inputs.setGraphCount(4);
        inputs.setTaskCountAverage(4);
        inputs.setTaskCountMultiplier(1);
        inputs.setPeriodPercentage(1.0);
        inputs.setMultiStartNodeProb(1.0);
        inputs.setStartNodesAverage(1);
        inputs.setStartNodesMultiplier(1);
        inputs.setTaskDegreeIN(3);
        inputs.setTaskDegreeOUT(3);
        inputs.setHardDeadlineProb(1.0);
        inputs.setNumberOfResources(20);
        inputs.setTaskTransitionTime(4.0);
        inputs.setCleanUpFiles(true);
        inputs.setPostHocResidency(true);
        inputs.setCalculatePeriods(true);
        inputs.setSaveOutput(false);

        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);

        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);

        executeTest(iterations, tgf, inputs);

    }

    // degree 3/3 -> 6/6
    // period mul 4 - graphcount 2 - tasks 4/1 - periodpercentage 1.0 - multistartnodeprob 1.0 - startnodes 1/1 - degree 6/6 - resources 20 - tasktranstime 4.0 - calcperiods true - posthoc residency true
    @Test
    void tgffTest8() throws Exception {

        int iterations = 1000;

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        TGFFInputParameters inputs = new TGFFInputParameters();
        ArrayList<Integer> period_mul = new ArrayList<>() {
        };
        period_mul.add(4);
        //period_mul.add(2);
        //period_mul.add(3);
        //period_mul.add(3);
        //period_mul.add(4);
        inputs.setPeriod_mul(period_mul);
        inputs.setGraphCount(2);
        inputs.setTaskCountAverage(4);
        inputs.setTaskCountMultiplier(1);
        inputs.setPeriodPercentage(1.0);
        inputs.setMultiStartNodeProb(1.0);
        inputs.setStartNodesAverage(1);
        inputs.setStartNodesMultiplier(1);
        inputs.setTaskDegreeIN(6);
        inputs.setTaskDegreeOUT(6);
        inputs.setHardDeadlineProb(1.0);
        inputs.setNumberOfResources(20);
        inputs.setTaskTransitionTime(4.0);
        inputs.setCleanUpFiles(true);
        inputs.setPostHocResidency(true);
        inputs.setCalculatePeriods(true);
        inputs.setSaveOutput(false);

        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);

        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);

        executeTest(iterations, tgf, inputs);

    }

    // task node multi 1 -> 2
    // period mul 4 - graphcount 2 - tasks 4/2 - periodpercentage 1.0 - multistartnodeprob 1.0 - startnodes 1/1 - degree 3/3 - resources 20 - tasktranstime 4.0 - calcperiods true - posthoc residency true
    @Test
    void tgffTest9() throws Exception {

        int iterations = 1000;

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        TGFFInputParameters inputs = new TGFFInputParameters();
        ArrayList<Integer> period_mul = new ArrayList<>() {
        };
        period_mul.add(4);
        //period_mul.add(2);
        //period_mul.add(3);
        //period_mul.add(3);
        //period_mul.add(4);
        inputs.setPeriod_mul(period_mul);
        inputs.setGraphCount(2);
        inputs.setTaskCountAverage(4);
        inputs.setTaskCountMultiplier(2);
        inputs.setPeriodPercentage(1.0);
        inputs.setMultiStartNodeProb(1.0);
        inputs.setStartNodesAverage(1);
        inputs.setStartNodesMultiplier(1);
        inputs.setTaskDegreeIN(3);
        inputs.setTaskDegreeOUT(3);
        inputs.setHardDeadlineProb(1.0);
        inputs.setNumberOfResources(20);
        inputs.setTaskTransitionTime(4.0);
        inputs.setCleanUpFiles(true);
        inputs.setPostHocResidency(true);
        inputs.setCalculatePeriods(true);
        inputs.setSaveOutput(false);

        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);

        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);

        executeTest(iterations, tgf, inputs);

    }

    // start node average 1 -> 3
    // period mul 4 - graphcount 2 - tasks 4/1 - periodpercentage 1.0 - multistartnodeprob 1.0 - startnodes 3/1 - degree 3/3 - resources 20 - tasktranstime 4.0 - calcperiods true - posthoc residency true
    @Test
    void tgffTest10() throws Exception {

        int iterations = 1000;

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        TGFFInputParameters inputs = new TGFFInputParameters();
        ArrayList<Integer> period_mul = new ArrayList<>() {
        };
        period_mul.add(4);
        //period_mul.add(2);
        //period_mul.add(3);
        //period_mul.add(3);
        //period_mul.add(4);
        inputs.setPeriod_mul(period_mul);
        inputs.setGraphCount(2);
        inputs.setTaskCountAverage(4);
        inputs.setTaskCountMultiplier(1);
        inputs.setPeriodPercentage(1.0);
        inputs.setMultiStartNodeProb(1.0);
        inputs.setStartNodesAverage(3);
        inputs.setStartNodesMultiplier(1);
        inputs.setTaskDegreeIN(3);
        inputs.setTaskDegreeOUT(3);
        inputs.setHardDeadlineProb(1.0);
        inputs.setNumberOfResources(20);
        inputs.setTaskTransitionTime(4.0);
        inputs.setCleanUpFiles(true);
        inputs.setPostHocResidency(true);
        inputs.setCalculatePeriods(true);
        inputs.setSaveOutput(false);

        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(-1);
        inputs.setMax_WCET(-1);

        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);

        executeTest(iterations, tgf, inputs);

    }

    private void executeTest(int iterations, TaskGenerationFramework tgf, TGFFInputParameters inputs) throws Exception {
        int totalNumberOfTasks = 0;
        int WCETHintHits = 0;
        int totalWCETHints = 0;
        int numSuccessfulIterations = 0;
        int totalWCETDistance = 0;
        HashMap<Integer, Integer> totalActualWCETsForAllTasks = new HashMap<>();
        HashMap<Integer, Integer> totalHintsForEachWCET = new HashMap<>();
        HashMap<Integer, Integer> totalHintHitsForEachWCET = new HashMap<>();
        HashMap<Integer, Integer> NumberOfTaskOccurrences = new HashMap<>();
        HashMap<Integer, Integer> NumberOfSubgraphsOccurrences = new HashMap<>();
        HashMap<Integer,HashMap<Integer,Integer>> predToSucTotalEdges = new HashMap<>();

        for (int i = 0; i < iterations; i++) {
            System.out.println("\n\nIteration " + (i + 1) + ":\n");
            GenerationResult generationResult = tgf.GenerateTaskSetFromTGFF(inputs);
            this.printHints(generationResult);

            if (!generationResult.wcetValues.isEmpty()) {
                numSuccessfulIterations++;
                for (int k = 0; k < generationResult.wcetHints.size(); k++) {

                    totalWCETHints++;
                    // increment hint hits and hint distance
                    if (Objects.equals(generationResult.wcetHints.get(k), generationResult.wcetValues.get(k))) {
                        WCETHintHits++;
                        int wcetHint = generationResult.wcetHints.get(k);
                        incrementHashMapField(totalHintHitsForEachWCET, wcetHint);
                    } else {
                        totalWCETDistance += Math.abs(generationResult.wcetValues.get(k) - generationResult.wcetHints.get(k));
                    }
                    // save hint
                    int wcetHint = generationResult.wcetHints.get(k);
                    incrementHashMapField(totalHintsForEachWCET, wcetHint);
                    // save wcet
                    int wcet = generationResult.wcetValues.get(k);
                    incrementHashMapField(totalActualWCETsForAllTasks, wcet);
                }
                // save numberOfTasks
                int numberOfTasks = generationResult.wcetHints.size();
                incrementHashMapField(NumberOfTaskOccurrences, numberOfTasks);
                // save number of Subgraphs
                int numberOfSubgraphs = generationResult.taskSet.getNum_precedence_graphs();
                incrementHashMapField(NumberOfSubgraphsOccurrences, numberOfSubgraphs);
            }
            for (Task task : generationResult.taskSet.getTaskList()) {
                predToSucTotalEdges.putIfAbsent(task.getIdentifier(), new HashMap<>());
                if (!task.getSuccessors().isEmpty()) {
                    for (int suc : task.getSuccessors()) {
                        predToSucTotalEdges.get(task.getIdentifier()).compute(suc, (k, v) -> (v == null) ? 1 : v+1);
                    }
                }
            }
            gc();
            Thread.sleep(100);
        }

        System.out.println("\n\nNumber of successful iterations: " + numSuccessfulIterations);
        System.out.println("Number of wcet hints: " + totalWCETHints);
        System.out.println("Number of wcet hits: " + WCETHintHits);
        System.out.println("Total distance of wcet hints to hits: " + totalWCETDistance);
        System.out.println("Average distance of wcet hints to hits per iteration: " + (double) totalWCETDistance / numSuccessfulIterations);
        System.out.println("Average number of tasks: " + (double) totalNumberOfTasks / numSuccessfulIterations);

        System.out.println("\n\nWCET\tACTUAL Occurrences");
        for (int wcet : totalActualWCETsForAllTasks.keySet()) {
            System.out.println(wcet + " \t " + totalActualWCETsForAllTasks.get(wcet));
        }
        System.out.println("\n\nWCET\tHINT Occurrences");
        for (int wcet : totalHintsForEachWCET.keySet()) {
            System.out.println(wcet + " \t " + totalHintsForEachWCET.get(wcet));
        }
        System.out.println("\n\nWCET\tHINT HIT Occurrences");
        for (int wcet : totalHintHitsForEachWCET.keySet()) {
            System.out.println(wcet + " \t " + totalHintHitsForEachWCET.get(wcet));
        }
        System.out.println("\n\nNumber of Tasks\tOccurrences");
        for (int numberOfTasks : NumberOfTaskOccurrences.keySet()) {
            System.out.println(numberOfTasks + " \t " + NumberOfTaskOccurrences.get(numberOfTasks));
        }
        System.out.println("\n\nNumber of Subgraphs\tOccurrences");
        for (int subgraphcount : NumberOfSubgraphsOccurrences.keySet()) {
            System.out.println(subgraphcount + " \t " + NumberOfSubgraphsOccurrences.get(subgraphcount));
        }

        System.out.println("\nEdge List");

        predToSucTotalEdges.forEach((predID,value) -> value.forEach((sucID, numOccurrences) -> System.out.println(predID + "\t" + sucID + "\t" + numOccurrences)));

        HashMap<Integer, Integer> taskAsPredecessor = new HashMap<>();
        HashMap<Integer, Integer> taskAsSuccessor = new HashMap<>();
        predToSucTotalEdges.forEach((predID,value) -> value.forEach((sucID, numOccurrences) -> {
            taskAsPredecessor.compute(predID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
            taskAsSuccessor.compute(sucID, (k, v) -> (v == null) ? numOccurrences : v+numOccurrences);
        }));

        System.out.println("\nTask As Predecessor:");
        taskAsPredecessor.forEach((predID, numOcc) -> System.out.println(predID + "\t" + numOcc));

        System.out.println("\nTask As Successor:");
        taskAsSuccessor.forEach((sucID, numOcc) -> System.out.println(sucID + "\t" + numOcc));
    }


    private static void incrementHashMapField(HashMap<Integer, Integer> HashMap, int key) {
        if (HashMap.containsKey(key)) {
            HashMap.replace(key, HashMap.get(key), HashMap.get(key) + 1);
        } else {
            HashMap.put(key, 1);
        }
    }

}

