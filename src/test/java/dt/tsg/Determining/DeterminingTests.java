package dt.tsg.Determining;

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

public class DeterminingTests {

    final boolean DEBUG = false;

    @Test
    public void numberOfTasksTest1() {

        // number of iterations
        int numIterations = 10000;

        // Min 1, Max 10
        TaskGenerationFramework tgf = new TaskGenerationFramework(DEBUG, new Random());
        InputParameters inputs = new InputParameters();

        // Tasks
        int numTasks = 15;
        inputs.setMin_number_of_tasks(10);
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

    @Test
    public void numberOfResourcesTest1() {

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

        System.out.println("Number of Unique Resource Occurrences");
        numberOfUniqueResourceOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });
        System.out.println("Task Set Number of Resources");
        numberOfResourceOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void numberOfResidencyConstraintsTest1() {

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

        HashMap<Integer, Integer> numberOfResidencyOccurrences = new HashMap<>();
        for (int i=1;i<10;i++) numberOfResidencyOccurrences.put(i,0);

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
                if (!task.getResourceConstraint().isEmpty()) resourceIDList.add(task.getResourceConstraint().get(0));
            }));

            numberOfResidencyOccurrences.compute(resourceIDList.size(), (k, v) -> (v == null) ? 1 : v+1);

            numberOfResourceOccurrences.compute(generationResult.taskSet.getNum_resources(), (k, v) -> (v == null) ? 1 : v+1);

            numSuccessfullIterations++;

        }

        System.out.println("Number of Residency Constraint Occurrences");
        numberOfResidencyOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });
        System.out.println("Task Set Number of Resources");
        numberOfResourceOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void releaseTimeTest1() {

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
        inputs.setMin_releaseTime(5);
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
        for (int i=0;i<20;i++) releaseTimeOccurrences.put(i,0);

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

        System.out.println("Release Time \t Occurrences");
        releaseTimeOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void deadlineTest1() {

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
        inputs.setMin_deadline(10);
        inputs.setMax_deadline(20);
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

        System.out.println("Deadline \t Occurrences");
        deadlineOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void wcetTest1() {

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
        inputs.setMin_WCET(5);
        inputs.setMax_WCET(15);
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

        HashMap<Integer, Integer> wcetOccurrences = new HashMap<>();
        for (int i=1;i<=20;i++) wcetOccurrences.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            generationResult.taskSet.getTaskList().forEach((task -> {
                wcetOccurrences.compute(task.getWCET(), (k, v) -> (v == null) ? 1 : v+1);
            }));

            numSuccessfullIterations++;

        }

        System.out.println("WCET \t Occurrences");
        wcetOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void totalNumPrecTest1() {

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
        inputs.setMin_total_precedence_relations(5);
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

        HashMap<Integer, Integer> numPrecedenceRelations = new HashMap<>();
        for (int i=0;i<=25;i++) numPrecedenceRelations.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            ArrayList<Integer> precedenceList = new ArrayList<>();
            generationResult.taskSet.getTaskList().forEach((task -> {
                if (!task.getSuccessors().isEmpty()) precedenceList.addAll(task.getSuccessors());
            }));
            numPrecedenceRelations.compute(precedenceList.size(), (k, v) -> (v == null) ? 1 : v+1);
            numSuccessfullIterations++;

        }

        System.out.println("Total number of Precedence Relations \t Occurrences");
        numPrecedenceRelations.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void subgraphCountTest1() {

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
        int numSuccessfullIterations = 0;

        HashMap<Integer, Integer> numSubgraphs = new HashMap<>();
        for (int i=0;i<=20;i++) numSubgraphs.put(i,0);

        for (int i=0;i<numIterations;i++) {

            GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
            tgf.setUtils(new Utils(DEBUG, new Random()));
            tgf.setWarnings(new ArrayList<>());
            tgf.setDEBUG(DEBUG);

            if (generationResult.wcetValues.isEmpty()) continue; // the generated task set was infeasible

            numSubgraphs.compute(generationResult.taskSet.getNum_precedence_graphs(), (k, v) -> (v == null) ? 1 : v+1);
            numSuccessfullIterations++;

        }

        System.out.println("Number of Subgraphs \t Occurrences");
        numSubgraphs.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void periodLengthTest1() {

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

        System.out.println("Period Length \t Occurrences");
        periodLengthOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void periodicityTest1() {

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

        System.out.println("Periodic Tasks \t Occurrences");
        periodicityOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

    @Test
    public void periodicityTest2() {

        // number of iterations
        int numIterations = 1;

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
        inputs.setMin_period_percentage(15);
        inputs.setMax_period_percentage(15);
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

        System.out.println("Periodic Tasks \t Occurrences");
        periodicityOccurrences.forEach( (k,v) ->  {
            System.out.println(k + "\t" + v);
        });

        System.out.println("\nSuccessful iterations: " + numSuccessfullIterations);
    }

}
