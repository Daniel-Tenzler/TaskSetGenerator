package dt.tsg.task;

import dt.tsg.InputParams.InputParameters;
import dt.tsg.InputParams.TGFFInputParameters;
import dt.tsg.utils.GenerationResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import static java.lang.System.gc;


class TaskGenerationFrameworkTest {

    static final boolean DEBUG = false;

    private void printHints(GenerationResult generationResult) {

        if (generationResult == null) return;

        System.out.println("WCET HINTS");
        for (int i : generationResult.wcetHints) {
            System.out.print(i + " ");
        }
        System.out.println("\nWCET VALUES");
        for (int i : generationResult.wcetValues) {
            System.out.print(i + " ");
        }
        System.out.println("\nStart HINTS");
        for (int i : generationResult.startHints) {
            System.out.print(i + " ");
        }
        System.out.println("\nStart VALUES");
        for (int i : generationResult.startValues) {
            System.out.print(i + " ");
        }
        System.out.println("\nEnd HINTS");
        for (int i : generationResult.endHints) {
            System.out.print(i + " ");
        }
        System.out.println("\nEnd VALUES");
        for (int i : generationResult.endValues) {
            System.out.print(i + " ");
        }
        System.out.println("\nResource HINTS");
        for (int i : generationResult.resourceHints) {
            System.out.print(i + " ");
        }
        System.out.println("\nResource VALUES");
        for (int i : generationResult.resourceValues) {
            System.out.print(i + " ");
        }

        if (generationResult.dynamicHints.size() != 6 || generationResult.dynamicValues.isEmpty()) return;

        System.out.println("\nDynamicConstraints:");
        System.out.println("cp_min_releasetime Hint/Value: " + generationResult.dynamicHints.get(0) + "/" + generationResult.dynamicValues.get(0));
        System.out.println("cp_max_releasetime Hint/Value: " + generationResult.dynamicHints.get(1) + "/" + generationResult.dynamicValues.get(1));
        System.out.println("cp_min_deadline Hint/Value: " + generationResult.dynamicHints.get(2) + "/" + generationResult.dynamicValues.get(2));
        System.out.println("cp_max_deadline Hint/Value: " + generationResult.dynamicHints.get(3) + "/" + generationResult.dynamicValues.get(3));
        System.out.println("cp_min_wcet Hint/Value: " + generationResult.dynamicHints.get(4) + "/" + generationResult.dynamicValues.get(4));
        System.out.println("cp_max_wcet Hint/Value: " + generationResult.dynamicHints.get(5) + "/" + generationResult.dynamicValues.get(5));

    }

    @Test
    void generateBasicTaskSet() {

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        InputParameters inputs = new InputParameters();

        // Tasks
        inputs.setMin_number_of_tasks(6);
        inputs.setMax_number_of_tasks(6);
        // Resources
        inputs.setMin_number_of_resources(5);
        inputs.setMax_number_of_resources(5);
        // Residency
        inputs.setMin_residency_constraints(6);
        inputs.setMax_residency_constraints(6);
        // Precedence
        inputs.setMin_total_precedence_relations(2);
        inputs.setMax_total_precedence_relations(2);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(6);
        inputs.setMax_period_length(6);
        // Schedule Generation Limit == Hyperperiod
        inputs.setSchedulingLimit(-1);
        // set releasetime , deadline, wcet
        inputs.setMin_releaseTime(-1);
        inputs.setMax_releaseTime(-1);
        inputs.setMin_deadline(-1);
        inputs.setMax_deadline(-1);
        inputs.setMin_WCET(1);
        inputs.setMax_WCET(-1);
        inputs.setDeadlineEqualToPeriod(false);
        // set subgraph number
        inputs.setSubgraphNumber(0);

        // set distributions
        inputs.setResidency_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(InputParameters.Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(InputParameters.Distribution.UNIFORM);
        // set mean for poisson distributed variables
        inputs.setPeriodLengthPoissonMean(9.0);

        // set generation options
        inputs.setMultiResidency(true);
        inputs.setPhasedReleaseTimes(true);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);
        inputs.setSaveOutput(false);

        GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);

        this.printHints(generationResult);
    }

    @Test
    void generateBasicTaskSetN1000() {
        int iterations = 10;

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        InputParameters inputs = new InputParameters();

        // Tasks
        int tasknum = 10;
        inputs.setMin_number_of_tasks(tasknum);
        inputs.setMax_number_of_tasks(tasknum);
        // Resources
        inputs.setMin_number_of_resources(6);
        inputs.setMax_number_of_resources(6);
        // Residency
        inputs.setMin_residency_constraints(0);
        inputs.setMax_residency_constraints(0);
        // Precedence
        inputs.setMin_total_precedence_relations(0);
        inputs.setMax_total_precedence_relations(0);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(16);
        inputs.setMax_period_length(16);
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
        inputs.setPeriodLengthPoissonMean(9.0);

        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(false);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);


        ArrayList<Double> averageWCET = new ArrayList<>();
        ArrayList<Double> averageDistanceWCET = new ArrayList<>();
        int numSuccessfulIterations = 0;
        int totalWCETHints = 0;
        int WCETHintHits = 0;
        int totalWCETDistance = 0;
        int totalStartHints = 0;
        int startHintHits = 0;
        int totalStartDistance = 0;
        int totalEndHints = 0;
        int endHintHits = 0;
        int totalEndDistance = 0;
        int totalResourceHints = 0;
        int resourceHintHits = 0;
        int totalResourceDistance = 0;

        HashMap<Integer, Integer> totalActualWCETsForAllTasks = new HashMap<>();

        for (int i = 0; i < iterations; i++) {
            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            this.printHints(generationResult);

            if (averageWCET.isEmpty()) {
                // first iteration
                if (!generationResult.wcetValues.isEmpty()) {
                    // iteration was successful
                    if (!generationResult.wcetHints.isEmpty()) {
                        for (int k = 0; k < tasknum; k++) {
                            averageWCET.add((double) generationResult.wcetValues.get(k));
                        }
                        for (int k = 0; k < tasknum; k++) {
                            averageDistanceWCET.add((double) Math.abs(generationResult.wcetHints.get(k) - generationResult.wcetValues.get(k)));
                        }
                    } else {
                        // we didnt set any hints during generation -> just calculate average WCET
                        for (int k = 0; k < tasknum; k++) {
                            averageWCET.add((double) generationResult.wcetValues.get(k));
                        }
                    }

                    // parse Hints
                    for (int k = 0; k < generationResult.wcetHints.size(); k++) {
                        totalWCETHints++;
                        totalStartHints++;
                        totalResourceHints++;
                        totalEndHints++;
                        //WCET
                        if (Objects.equals(generationResult.wcetHints.get(k), generationResult.wcetValues.get(k))) {
                            WCETHintHits++;
                        } else {
                            totalWCETDistance += Math.abs(generationResult.wcetValues.get(k) - generationResult.wcetHints.get(k));
                        }
                        //START
                        /*if (Objects.equals(generationResult.startHints.get(k), generationResult.startValues.get(k))) {
                            startHintHits++;
                        } else {
                            totalStartDistance += Math.abs(generationResult.startValues.get(k) - generationResult.startHints.get(k));
                        }*/
                        //END
                        /*if (Objects.equals(generationResult.endHints.get(k), generationResult.endValues.get(k))) {
                            endHintHits++;
                        } else {
                            totalEndDistance += Math.abs(generationResult.endValues.get(k) - generationResult.endHints.get(k));
                        }*/
                        //RESOURCE
                        if (Objects.equals(generationResult.resourceHints.get(k), generationResult.resourceValues.get(k))) {
                            resourceHintHits++;
                        } else {
                            totalResourceDistance += Math.abs(generationResult.resourceValues.get(k) - generationResult.resourceHints.get(k));
                        }

                        int wcet = generationResult.wcetValues.get(k);

                        if (totalActualWCETsForAllTasks.containsKey(wcet)) {
                            totalActualWCETsForAllTasks.replace(wcet, totalActualWCETsForAllTasks.get(wcet), totalActualWCETsForAllTasks.get(wcet) + 1);
                        } else {
                            totalActualWCETsForAllTasks.put(wcet, 1);
                        }
                    }

                }

            } else {
                // all other iterations
                if (!generationResult.wcetValues.isEmpty()) {
                    // iteration was successfull
                    if (!generationResult.wcetHints.isEmpty()) {
                        for (int k = 0; k < tasknum; k++) {
                            averageWCET.set(k, (averageWCET.get(k) + generationResult.wcetValues.get(k)));
                        }

                        for (int k = 0; k < tasknum; k++) {
                            int newWCETDistance = Math.abs(generationResult.wcetHints.get(k) - generationResult.wcetValues.get(k));
                            averageDistanceWCET.set(k, (averageDistanceWCET.get(k) + newWCETDistance));
                        }

                        // parse Hints
                        for (int k = 0; k < generationResult.wcetHints.size(); k++) {
                            totalWCETHints++;
                            totalStartHints++;
                            totalResourceHints++;
                            totalEndHints++;
                            //WCET
                            if (Objects.equals(generationResult.wcetHints.get(k), generationResult.wcetValues.get(k))) {
                                WCETHintHits++;
                            } else {
                                totalWCETDistance += Math.abs(generationResult.wcetValues.get(k) - generationResult.wcetHints.get(k));
                            }
                            //START
                            /*if (Objects.equals(generationResult.startHints.get(k), generationResult.startValues.get(k))) {
                                startHintHits++;
                            } else {
                                totalStartDistance += Math.abs(generationResult.startValues.get(k) - generationResult.startHints.get(k));
                            }
                            //END
                            if (Objects.equals(generationResult.endHints.get(k), generationResult.endValues.get(k))) {
                                endHintHits++;
                            } else {
                                totalEndDistance += Math.abs(generationResult.endValues.get(k) - generationResult.endHints.get(k));
                            }*/
                            //RESOURCE
                            if (Objects.equals(generationResult.resourceHints.get(k), generationResult.resourceValues.get(k))) {
                                resourceHintHits++;
                            } else {
                                totalResourceDistance += Math.abs(generationResult.resourceValues.get(k) - generationResult.resourceHints.get(k));
                            }
                            int wcet = generationResult.wcetValues.get(k);
                            if (totalActualWCETsForAllTasks.containsKey(wcet)) {
                                totalActualWCETsForAllTasks.replace(wcet, totalActualWCETsForAllTasks.get(wcet), totalActualWCETsForAllTasks.get(wcet) + 1);
                            } else {
                                totalActualWCETsForAllTasks.put(wcet, 1);
                            }
                        }

                    } else {
                        for (int k = 0; k < tasknum; k++) {
                            averageWCET.set(k, (averageWCET.get(k) + generationResult.wcetValues.get(k)));
                        }

                    }
                }
            }

            if (!generationResult.wcetValues.isEmpty()) numSuccessfulIterations++;

            System.out.println("\n\nIteration " + (i + 1) + ":\n");
            System.out.println("Average WCET");
            for (double k : averageWCET) {
                System.out.print(k / numSuccessfulIterations + " ");
            }
            System.out.println("\nAverage distance between WCET Hints and actual Values");
            for (double k : averageDistanceWCET) {
                System.out.print(k / numSuccessfulIterations + " ");
            }
            gc();

        }

        System.out.println("\n\nNumber of successful iterations: " + numSuccessfulIterations);
        System.out.println("Number of wcet hints: " + totalWCETHints);
        System.out.println("Number of wcet hits: " + WCETHintHits);
        System.out.println("Total distance of wcet hints to hits: " + totalWCETDistance);
        System.out.println("Average distance of wcet hints to hits per iteration: " + (double) totalWCETDistance / numSuccessfulIterations);
        System.out.println("Number of start hints: " + totalStartHints);
        System.out.println("Number of start hits: " + startHintHits);
        System.out.println("Total distance of start hints to hits: " + totalStartDistance);
        System.out.println("Number of end hints: " + totalEndHints);
        System.out.println("Number of end hits: " + endHintHits);
        System.out.println("Total distance of end hints to hits: " + totalEndDistance);
        System.out.println("Number of resource hints: " + totalResourceHints);
        System.out.println("Number of resource hits: " + resourceHintHits);
        System.out.println("Total distance of resource hints to hits: " + totalResourceDistance);

        System.out.println("\n\nWCET - Occurrences");
        for (int wcet : totalActualWCETsForAllTasks.keySet()) {
            System.out.println("   " + wcet + " - " + totalActualWCETsForAllTasks.get(wcet));
        }


    }

    @Test
    void generateTGFFTaskSet() throws Exception {

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        TGFFInputParameters inputs = new TGFFInputParameters();
        ArrayList<Integer> period_mul = new ArrayList<>() {
        };
        period_mul.add(1);
        period_mul.add(1);
        //period_mul.add(3);
        //period_mul.add(4);
        inputs.setPeriod_mul(period_mul);
        inputs.setMin_residency_constraints(4);
        inputs.setMax_residency_constraints(12);
        inputs.setGraphCount(3);
        inputs.setTaskCountAverage(10);
        inputs.setTaskCountMultiplier(1);
        inputs.setPeriodPercentage(1.0);
        inputs.setMultiStartNodeProb(1.0);
        inputs.setStartNodesAverage(3);
        inputs.setStartNodesMultiplier(1);
        inputs.setTaskDegreeIN(3);
        inputs.setTaskDegreeOUT(6);
        inputs.setHardDeadlineProb(1.0);
        inputs.setNumberOfResources(-1);
        inputs.setTaskTransitionTime(1.0);
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

        // set subgraph number
        inputs.setSubgraphNumber(6);

        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);


        GenerationResult generationResult = tgf.GenerateTaskSetFromTGFF(inputs);

        this.printHints(generationResult);
    }

    @Test
    void generateTGFFTaskSetN20() throws Exception {

        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());


        int totalNumberOfTasks = 0;
        ArrayList<Double> averageDistanceWCET = new ArrayList<>();
        int WCETHintHits = 0;
        int totalWCETHints = 0;
        int numSuccessfulIterations = 0;
        int totalWCETDistance = 0;

        TGFFInputParameters inputs = new TGFFInputParameters();
        ArrayList<Integer> period_mul = new ArrayList<>() {
        };
        period_mul.add(2);
        period_mul.add(3);
        //period_mul.add(3);
        period_mul.add(4);
        inputs.setPeriod_mul(period_mul);
        inputs.setGraphCount(2);
        inputs.setTaskCountAverage(4);
        inputs.setTaskCountMultiplier(1);
        inputs.setPeriodPercentage(1.0);
        inputs.setMultiStartNodeProb(1.0);
        inputs.setStartNodesAverage(2);
        inputs.setStartNodesMultiplier(1);
        inputs.setTaskDegreeIN(2);
        inputs.setTaskDegreeOUT(2);
        inputs.setHardDeadlineProb(1.0);
        inputs.setNumberOfResources(15);
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


        HashMap<Integer, Integer> totalActualWCETsForAllTasks = new HashMap<>();

        for (int i = 0; i < 20; i++) {
            GenerationResult generationResult = tgf.GenerateTaskSetFromTGFF(inputs);
            this.printHints(generationResult);

            System.out.println("\n\nIteration " + (i + 1) + ":\n");

            if (!generationResult.wcetValues.isEmpty()) {
                numSuccessfulIterations++;
                System.out.println("WCET Hints");
                for (int k : generationResult.wcetHints) {
                    totalNumberOfTasks++;
                    System.out.print(k + " ");
                }
                System.out.println("\nWCET Values");
                for (int k : generationResult.wcetValues) {
                    System.out.print(k + " ");
                }

                for (int k = 0; k < generationResult.wcetHints.size(); k++) {
                    totalWCETHints++;
                    if (Objects.equals(generationResult.wcetHints.get(k), generationResult.wcetValues.get(k))) {
                        WCETHintHits++;
                    } else {
                        totalWCETDistance += Math.abs(generationResult.wcetValues.get(k) - generationResult.wcetHints.get(k));
                    }
                    int wcet = generationResult.wcetValues.get(k);
                    if (totalActualWCETsForAllTasks.containsKey(wcet)) {
                        totalActualWCETsForAllTasks.replace(wcet, totalActualWCETsForAllTasks.get(wcet), totalActualWCETsForAllTasks.get(wcet) + 1);
                    } else {
                        totalActualWCETsForAllTasks.put(wcet, 1);
                    }
                }

            }

            gc();

        }

        System.out.println("\n\nNumber of successful iterations: " + numSuccessfulIterations);
        System.out.println("Number of wcet hints: " + totalWCETHints);
        System.out.println("Number of wcet hits: " + WCETHintHits);
        System.out.println("Total distance of wcet hints to hits: " + totalWCETDistance);
        System.out.println("Average distance of wcet hints to hits per iteration: " + (double) totalWCETDistance / numSuccessfulIterations);
        System.out.println("Average number of tasks: " + (double) totalNumberOfTasks / numSuccessfulIterations);

        System.out.println("\n\nWCET - Occurrences");
        for (int wcet : totalActualWCETsForAllTasks.keySet()) {
            System.out.println("   " + wcet + " - " + totalActualWCETsForAllTasks.get(wcet));
        }


    }

    @Test
    void testPostHocResidencyConstraints() {
        int numIterations = 10;
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());

        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 8;
        inputs.setMin_number_of_tasks(numTasks);
        inputs.setMax_number_of_tasks(numTasks);
        // Resources
        int numResources = 6;
        inputs.setMin_number_of_resources(numResources);
        inputs.setMax_number_of_resources(numResources);
        // Residency
        inputs.setMin_residency_constraints(3);
        inputs.setMax_residency_constraints(7);
        // Precedence
        inputs.setMin_total_precedence_relations(3);
        inputs.setMax_total_precedence_relations(17);
        // Periods
        inputs.setMin_period_percentage(100);
        inputs.setMax_period_percentage(100);
        inputs.setMin_period_length(7);
        inputs.setMax_period_length(8);
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
        inputs.setPeriodLengthPoissonMean(9.0);
        // set generation options
        inputs.setMultiResidency(false);
        inputs.setPhasedReleaseTimes(false);
        inputs.setPostHocPrecedence(false);
        inputs.setPostHocResidency(true);
        inputs.setNumberOfWorkerThreads(1);
        inputs.setSolutionCount(1);

        int totalNumberOfFormattedResources = 0;

        for (int i = 0; i < numIterations; i++) {

            System.out.println("Iteraiton: " + i);

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            ArrayList<Integer> formattedResources = new ArrayList<>();
            for (int k : generationResult.resourceValues) {
                if (!(formattedResources.contains(k))) formattedResources.add(k);
            }
            System.out.println("Initial number of resources " + numResources + " formatted Number of Resources " + formattedResources.size());
            totalNumberOfFormattedResources += formattedResources.size();
        }

        System.out.println("Total number of formatted resources: " + totalNumberOfFormattedResources);
        System.out.println("Total initial number of resources: " + numIterations * numResources);

    }

}