package dt.tsg.task;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import dt.tsg.InputParams.InputParameters;
import dt.tsg.InputParams.TGFFInputParameters;
import dt.tsg.cspModel.CSPModelFunctions;
import dt.tsg.cspModel.DynamicConstraints;
import dt.tsg.cspModel.ModelTask;
import dt.tsg.phasesAndSchedulingLimit.PhasesAndSchedulingLimit;
import dt.tsg.precedenceGraph.BasicPrecedenceConstraints;
import dt.tsg.precedenceGraph.TGFFGraphGeneration;
import dt.tsg.residencyConstraints.ResidencyConstraints;
import dt.tsg.taskSet.TGFFTaskSet;
import dt.tsg.taskSet.TaskSet;
import dt.tsg.utils.GenerationResult;
import dt.tsg.utils.RandomNumberGeneration;
import dt.tsg.utils.Utils;
import dt.tsg.utils.Utils.ScheduledTask;

import java.io.IOException;
import java.util.*;

import static dt.tsg.InputParams.InputParameters.Distribution;
import static java.lang.System.gc;


/**
 * This class serves as a Framework for the generation of Task Sets and Tasks.
 * It includes methods for generating (multiple) Task Sets with predefined constraints.
 */
public class TaskGenerationFramework {

    boolean DEBUG;
    Utils Utils;
    ArrayList<String> warnings;

    public TaskGenerationFramework(boolean DEBUG, Random random, ArrayList<String> warnings) {
        this.DEBUG = DEBUG;
        this.Utils = new Utils(DEBUG, random);
        this.warnings = warnings;
    }

    public TaskGenerationFramework(boolean DEBUG, Random random) {
        this.DEBUG = DEBUG;
        this.Utils = new Utils(DEBUG, random);
        this.warnings = new ArrayList<>();
    }

    public void setDEBUG(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }

    public dt.tsg.utils.Utils getUtils() {
        return Utils;
    }

    public void setUtils(dt.tsg.utils.Utils utils) {
        Utils = utils;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(ArrayList<String> warnings) {
        this.warnings = warnings;
    }

    /**
     * This class is used for the onSolutionCallback method to return after the first x solutions have been found.
     */
    static class VarArraySolutionPrinter extends CpSolverSolutionCallback { //https://developers.google.com/optimization/cp/cp_tasks#solution-limit

        private int solutionCount;
        private final IntVar[] variableArray;
        private final int SolutionLimit;

        public VarArraySolutionPrinter(IntVar[] variables, int solutionLimit) {
            variableArray = variables;
            SolutionLimit = solutionLimit;
        }

        @Override
        public void onSolutionCallback() {
            System.out.printf("\nSolution #%d: time = %.02f s%n", solutionCount, wallTime());
            if (variableArray != null) {
                System.out.print("       ");
                for (int i = 0; i < (variableArray.length / 4); i++) {
                    System.out.printf("T_%-3d", i);
                }
                System.out.print("\nStart: ");
                for (int i = 0; i < variableArray.length; i++) {
                    if (i % 4 == 0) System.out.printf("%-5d", value(variableArray[i]));
                }
                System.out.print("\nEnd: ");
                for (int i = 0; i < variableArray.length; i++) {
                    if (i % 4 == 1) System.out.printf("%-5d", value(variableArray[i]));
                }
                System.out.print("\nWCET:  ");
                for (int i = 0; i < variableArray.length; i++) {
                    if (i % 4 == 2) System.out.printf("%-5d", value(variableArray[i]));
                }
                System.out.print("\nResou: ");
                for (int i = 0; i < variableArray.length; i++) {
                    if (i % 4 == 3) System.out.printf("%-5d", value(variableArray[i]));
                }
                System.out.println();
            }

            solutionCount++;
            if (SolutionLimit == solutionCount) {
                System.out.println("\nStopped Search after " + SolutionLimit + " solutions\n");
                stopSearch();
            }
        }

        public int getSolutionCount() {
            return solutionCount;
        }

    }

    /**
     * Builds the var array solution printer object for the CP solver.
     *
     * @param modelTaskMap        maps the task ids to the corresponding model tasks.
     * @param number_of_solutions the number of desired solutions.
     * @return the Var array solution printer object.
     */
    private static VarArraySolutionPrinter getVarArraySolutionPrinter(Map<Integer, ModelTask> modelTaskMap, int number_of_solutions) {

        // Designate variables to be printed on finding a solution
        List<IntVar> vars = new ArrayList<>();
        modelTaskMap.forEach((integer, modelTask) -> {
            vars.add(modelTask.getStart());
            vars.add(modelTask.getEnd());
            vars.add(modelTask.getWcet());
            vars.add(modelTask.getResource());
        });
        IntVar[] observeVar = vars.toArray(new IntVar[0]);
        return new VarArraySolutionPrinter(observeVar, number_of_solutions);
    }

    /**
     * This function generates basic periods for the Tasks within taskList. It directly manipulates the content of taskList!
     * The generated periods will either be harmonic, or
     *
     * @param inputs         Task Generation Input Parameters.
     * @param harmonic       Whether the periods of precedence-related tasks should be harmonic or equal.
     * @param taskList       The list of tasks.
     * @param precedence_map Mapping each taskID to all taskIDs of precedence-related tasks.
     */
    private static void GenerateBasicPeriods(InputParameters inputs, boolean harmonic, ArrayList<Task> taskList, Map<Integer, ArrayList<Integer>> precedence_map, ArrayList<String> warnings, Utils utils) {

        utils.DebugPrint("Start Method: GenerateBasicPeriods");

        int iterator = 0;
        double current_percentage = 0.0;

        ArrayList<Integer> taskIDList = new ArrayList<>();
        for (Task task : taskList) {
            taskIDList.add(task.getIdentifier());
        }

        while (current_percentage < (double) inputs.getMin_period_percentage() / 100.0) {
            // Select a random Task.


            if (taskIDList.isEmpty()) {
                System.out.println("WARNING: DID NOT HIT THE DESIRED PERIOD PERCENTAGE. Current period percentage = " + current_percentage + ". Minimum period percentage = " + (inputs.getMin_period_percentage() / 100.0));
                warnings.add("WARNING: DID NOT HIT THE DESIRED PERIOD PERCENTAGE. Current period percentage = " + current_percentage + ". Minimum period percentage = " + (inputs.getMin_period_percentage() / 100.0));
                break;
            }
            int field = RandomNumberGeneration.getNumWithDistribution(0, taskIDList.size() - 1, inputs.getPeriodicity_distribution(), utils, inputs.getPeriodicityPoissonMean(), inputs.getPeriodicityBinomialP(), inputs.getPeriodicityBinomialN(), warnings);
            int taskID = taskIDList.get(field);

            Task task = taskList.get(taskID);

            // check if the precedence map of the task throws us out of the desired period percentage
            int numberOfForcedPeriods = 0;
            if (!precedence_map.get(task.getIdentifier()).isEmpty()) {
                // precedence map of the task is not empty
                for (Integer id : precedence_map.get(task.getIdentifier())) { // the precedence map of the task also contains the task itself!
                    if (!taskList.get(id).isPeriodic()) {
                        // task has no period, but would get one if we continue
                        numberOfForcedPeriods++;
                    }
                }
            }
            double percentageShift = (double) numberOfForcedPeriods / (double) taskList.size();
            if (current_percentage + percentageShift > ((double) inputs.getMax_period_percentage() / 100.0)) {
                // remove the task and all its related tasks from the taskIDList
                for (Integer id : precedence_map.get(task.getIdentifier())) {
                    // remove the taskID from the taskIDList
                    int fieldOfRelatedTask = -1;
                    for (int o = 0; o < taskIDList.size(); o++) {
                        if (Objects.equals(taskIDList.get(o), id)) {
                            fieldOfRelatedTask = o;
                            break;
                        }
                    }
                    if (fieldOfRelatedTask != -1) {
                        taskIDList.remove(fieldOfRelatedTask);
                        utils.DebugPrint("removed task " + id + " from the taskIDList since we would overshoot our target periodicity percentage");
                    }
                }
                // skip the rest of this loop
                continue;
            }

            // Set period according to constraints
            int actual_period = RandomNumberGeneration.getNumWithDistribution(inputs.getMin_period_length(), inputs.getMax_period_length(), inputs.getPeriodLength_distribution(), utils, inputs.getPeriodLengthPoissonMean(), inputs.getPeriodLengthBinomialP(), inputs.getPeriodLengthBinomialN(), warnings);
            task.setPeriodic(true);
            task.setPeriod(actual_period);
            taskIDList.remove(field);
            iterator++;
            current_percentage = (double) iterator / taskList.size();
            utils.DebugPrint("added period to task: " + taskID);
            utils.DebugPrint("current period percentage: " + current_percentage);

            // Set the periods of all tasks with precedence relations to this task to the same
            // assemble all tasks with precedence relations to this task
            if (!precedence_map.get(task.getIdentifier()).isEmpty()) {

                utils.DebugPrint("task " + task.getIdentifier() + " has precedence map: " + precedence_map.get(task.getIdentifier()));

                for (int i : precedence_map.get(task.getIdentifier())) {
                    if (task.getIdentifier() != i) {
                        // set period of the related task to the same

                        if (harmonic) {
                            //calculate the harmonic period
                            int harmonicPeriod = actual_period;
                            int counter = 0;
                            while (utils.random.nextInt(2) == 0 && counter < 10) {
                                harmonicPeriod += actual_period;
                                counter++;
                            }

                            Task related_task = taskList.get(i);
                            related_task.setPeriod(harmonicPeriod);
                            related_task.setPeriodic(true);

                        } else {
                            //add a non-harmonic period
                            Task related_task = taskList.get(i);
                            related_task.setPeriod(actual_period);
                            related_task.setPeriodic(true);
                        }
                        // increase iterator, since we added a new period
                        iterator++;
                        current_percentage = (double) iterator / taskList.size();
                        utils.DebugPrint("added period to related task: " + i);
                        utils.DebugPrint("current period percentage: " + current_percentage);

                        // remove the taskID from the taskIDList since we added a period to it
                        int fieldOfRelatedTask = -1;
                        for (int o = 0; o < taskIDList.size(); o++) {
                            if (taskIDList.get(o) == i) {
                                fieldOfRelatedTask = o;
                                break;
                            }
                        }
                        if (fieldOfRelatedTask != -1) taskIDList.remove(fieldOfRelatedTask);
                    }
                }
            }
        }
    }

    /**
     * This function generates a list of Tasks with basic attributes. We assume all sanity checks have been performed before calling this function.
     * The function will generate precedence constraints, residency constraints, periodicity and period length for the tasks in taskList according to the constraints.
     * This function does not generate releaseTimes, Deadlines, and WCETs.
     *
     * @param numTasks Number of Tasks in the TaskSet
     * @param numResid Number of Residency Constraints in the Task Set
     * @param numResou Number of Resources in the Task Set
     * @param numPrec  Number of Precedence Constraints in the Task Set
     * @param inputs   The Task Generation Input Parameters
     * @return A list of tasks without the additional instances of periodic tasks
     */
    public ArrayList<Task> generateModelReadyTasks(int numTasks, int numResid, int numResou, int numPrec, InputParameters inputs, boolean harmonic, boolean multi_residency, boolean postHoc_precedence, boolean phased_releaseTimes, boolean postHoc_residency) {

        ArrayList<Task> taskList = new ArrayList<>();

        for (int i = 0; i < numTasks; i++) {
            Task newTask = new Task(i);

            // Put Task into TaskList
            taskList.add(newTask);
        }

        if (!postHoc_residency) {
            // Residency Constraints
            ResidencyConstraints.addResidencyConstraintsToTasks(numResid, numResou, inputs, multi_residency, taskList, getUtils(), warnings);
        }

        // add precedence constraints if the postHoc flag is not set
        Map<Integer, ArrayList<Integer>> precedence_map = new HashMap<>();
        //build map
        for (int i = 0; i < taskList.size(); i++) {
            precedence_map.put(i, new ArrayList<>());
        }
        if (!postHoc_precedence) {
            // Precedence Constraints
            precedence_map = BasicPrecedenceConstraints.GenerateBasicPrecedenceConstraints(numPrec, taskList, getUtils(), inputs, warnings);
        }

        // Calculate Periods
        if (inputs.getMax_period_percentage() > 0) {
            //desired task set contains periodic tasks
            GenerateBasicPeriods(inputs, harmonic, taskList, precedence_map, warnings, getUtils());

            int tasksWithPeriods = 0;
            for (Task task : taskList) {
                if (task.isPeriodic()) tasksWithPeriods++;
            }
            double actualPeriodicity = (double) tasksWithPeriods / (double) taskList.size();
            if (actualPeriodicity > (double) inputs.getMax_period_percentage() / 100 || actualPeriodicity < (double) inputs.getMin_period_percentage() / 100) {
                System.out.println("WARNING: periodicity of the task set is outside of specified bounds. Actual periodicity: " + actualPeriodicity);
                warnings.add("WARNING: periodicity of the task set is outside of specified bounds. Actual periodicity: " + actualPeriodicity);
            }
        }

        if (phased_releaseTimes) {
            PhasesAndSchedulingLimit.calculatePhasedReleaseTimes(taskList, Utils, precedence_map);
        }

        getUtils().DebugPrint("\n\nTaskList without periodic task instances\n");
        if (getUtils().debug) taskList.forEach(e -> System.out.println(e.toString()));

        if (taskList.isEmpty()) {
            System.out.println("=================CRITICAL ERROR, NO TASK LIST GENERATED=================");
            warnings.add("=================CRITICAL ERROR, NO TASK LIST GENERATED=================");
            System.out.println("---------- All generated Warnings during generation ----------");
            warnings.forEach(System.out::println);
            System.exit(-1);
        }

        return taskList;
    }

    /**
     * This function generates all *additional* instances of periodic tasks, and returns them
     * This doesn't work currently because release times and deadlines aren't set/used
     *
     * @param taskList List of tasks
     * @param limit    The Hyperperiod
     * @return A list of all task instances created for periodic tasks
     */
    public ArrayList<Task> generateModelReadyPeriodicTaskInstances(ArrayList<Task> taskList, int limit, boolean harmonic) {

        getUtils().DebugPrint("Start Method: generateModelReadyPeriodicTaskInstances");
        ArrayList<Task> additionalTasks = new ArrayList<>();

        // What about precedence constraints between periodic and non-periodic tasks? -> initial task will still have precedence constraint, the generated task instances will not have the precedence constraint
        // Reason: if a non periodic task is to be scheduled before a periodic task, all instances of the periodic task will be scheduled after the non-periodic task by default
        // if a non-periodic task is to be scheduled after a periodic task, not all instances of the periodic task can be scheduled before the non-periodic task, as we assume the periodic task is repeated ad infinitum. Thus, the non-periodic task would never be scheduled if all periodic task instances had the same precedence relationship with it.
        Map<Integer, ArrayList<TaskInstance>> taskInstances = new HashMap<>();       // maps the taskIds to all Ids of the periodic task instances

        int identifier = taskList.size();
        for (Task task : taskList) {
            if (task.isPeriodic()) {
                int periodOffset = task.getPeriod();
                int instanceNumber = 1; //start with 1, as the default task has instanceNumber == 0
                taskInstances.put(task.getIdentifier(), new ArrayList<>());

                while (task.getPhase() + periodOffset < limit) {
                    // create the new task
                    TaskInstance newTask = new TaskInstance(
                            identifier,
                            task.getPeriod(),
                            task.isPeriodic(),
                            task.isResidency_constrained());
                    // add resource constraints
                    newTask.addResourceConstraint(task.getResourceConstraint());
                    // set instanceNum of the task
                    newTask.setInstance_number(instanceNumber);
                    instanceNumber++;
                    // set original task ID and predecessor
                    newTask.setOriginal_task_ID(task.getIdentifier());
                    newTask.setOriginal_predecessor(task.getPredecessors());
                    newTask.setOriginal_successors(task.getSuccessors());

                    // add the new task to list
                    additionalTasks.add(newTask);

                    taskInstances.get(task.getIdentifier()).add(newTask); //adds the taskInstance identifier to the task instances list

                    //implementing precedence constraints between newly created tasks is done below
                    Utils.DebugPrint("Added task instance " + identifier + " originalID: " + task.getIdentifier());

                    identifier++;
                    periodOffset += task.getPeriod();
                }
            }
        }

        // adds precedence relationships between instances of periodic tasks
        for (Task task : taskList) {
            if (!task.isPeriodic()) {
                continue;
            }
            // the original task is periodic
            if (task.getPredecessors().isEmpty()) {
                continue;
            }
            // the original task has predecessors
            for (TaskInstance currentTaskInstance : taskInstances.get(task.getIdentifier())) {
                // iterate over all taskInstances of the original task
                // if the predecessor of the original task is periodic, then add precedence relation depending on the periodoffset
                for (Integer OriginalPredecessorID : task.getPredecessors()) {
                    if (taskList.get(OriginalPredecessorID).isPeriodic()) {
                        // the original predecessor is periodic, find the appropriate task instances to add relations to
                        int placeInMap = taskInstances.get(task.getIdentifier()).indexOf(currentTaskInstance);
                        ArrayList<TaskInstance> predecessorTaskInstances = taskInstances.get(OriginalPredecessorID);
                        if (harmonic) {
                            // TODO implement harmonic periods for tasks
                        } else {
                            // add the appropriate task instance id for the predecessor as pred
                            if (taskInstances.get(task.getIdentifier()).size() != predecessorTaskInstances.size()) {
                                throw new RuntimeException("ERROR: cannot assign predecessors since instance lists are not the same size.\nSuccessorInstances size=" + taskInstances.get(task.getIdentifier()).size() + " PredecessorInstances size=" + predecessorTaskInstances.size() + "\nOriginalTask: " + currentTaskInstance.getOriginal_task_ID() + ". OriginalPred: " + currentTaskInstance.getOriginal_predecessor());
                            }
                            currentTaskInstance.addPredecessor(predecessorTaskInstances.get(placeInMap).getIdentifier());
                            predecessorTaskInstances.get(placeInMap).addSuccessors(currentTaskInstance.getIdentifier());
                            Utils.DebugPrint("added predecessor to task " + currentTaskInstance.getIdentifier() + ", Pred: " + currentTaskInstance.getPredecessors().toString() + ". OriginalTask: " + currentTaskInstance.getOriginal_task_ID() + ". OriginalPred: " + currentTaskInstance.getOriginal_predecessor());
                        }
                    }
                }
            }
        }

        return additionalTasks;
    }

    /**
     * Generates a CSP Model using the CSPModelFunctions and solves it.
     *
     * @param limit    the Scheduling Limit
     * @param taskList the complete list of tasks that will be used in the model
     * @param numResou the total number of resources
     */
    private GenerationResult generateModelAndSolve(int limit, ArrayList<Task> taskList, int numResou, InputParameters inputs) {

        getUtils().DebugPrint("Start Method: generateModelAndSolve");

        // Call garbage collection to save memory
        gc();

        int min_residency_constraints = inputs.getMin_residency_constraints();
        int max_residency_constraints = inputs.getMax_residency_constraints();
        getUtils().DebugPrint("phased_releaseTimes: " + inputs.PhasedReleaseTimes());
        getUtils().DebugPrint("postHoc_precedence: " + inputs.PostHocPrecedence());
        getUtils().DebugPrint("postHoc_residency: " + inputs.PostHocResidency());
        getUtils().DebugPrint("multi_residency: " + inputs.MultiResidency());

        System.out.println("\nGenerate Model:\n");

        //Generate Model, see https://developers.google.com/optimization/scheduling/job_shop
        Loader.loadNativeLibraries();
        CpModel cspModel = new CpModel();

        // Build dynamic Constraints
        DynamicConstraints dynamicConstraints = CSPModelFunctions.buildDynamicConstraints(cspModel, limit, inputs);

        // Build Tasks for CSP Model and add Task Constraints
        Map<Integer, ModelTask> modelTaskMap = CSPModelFunctions.buildCSPModelTasksAndConstraints(cspModel, limit, taskList, numResou, dynamicConstraints, inputs.PhasedReleaseTimes(), Utils, inputs);

        // Add dynamic No Overlap between Tasks
        CSPModelFunctions.addNoOverlapConstraintsToModel(taskList, cspModel, modelTaskMap, getUtils());

        // Add actual Precedence Constraints to the Model
        CSPModelFunctions.addPrecedenceConstraintsToModel(taskList, cspModel, modelTaskMap, getUtils());

        // Add Hints
        GenerationResult generationResult = CSPModelFunctions.addHintsToModel(cspModel, modelTaskMap, taskList, limit, dynamicConstraints, numResou, inputs, getUtils(), warnings);

        // Add Decision Strategy
        CSPModelFunctions.addDecisionStrategies(cspModel, modelTaskMap);

        // Add objective for minimizing schedule length
        //CSPModelFunctions.addScheduleLengthObjective(limit, taskList, cspModel, modelTaskMap);

        if (inputs.isMaximizeWCET()) {
            // Add objective for maximizing WCET
            CSPModelFunctions.addWCETObjective(modelTaskMap, cspModel);
        }
        // Generate the Solver and Solution-parameters
        CpSolver solver = new CpSolver();

        // Set Solver Parameters
        CSPModelFunctions.setSolverParameters(solver, inputs.getNumberOfWorkerThreads(), inputs.getSolutionCount(), getUtils());

        // Solve the problem
        VarArraySolutionPrinter cb = getVarArraySolutionPrinter(modelTaskMap, inputs.getSolutionCount());
        System.out.println("\nRun CP-Solver\n");
        CpSolverStatus status = null;
        try {
            status = solver.solve(cspModel, cb);
        } catch (Exception e) {
            System.out.println("CP-Solver returned Exception: " + e.getMessage());
            System.exit(-1);
        }

        // Read result
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {

            List<CpSolverSolution> solutionList = solver.response().getAdditionalSolutionsList();       // TODO filter out solutions that are equal from our perspective
            System.out.println("The solver returned " + solutionList.size() + " solutions. We attempted to generate " + cb.getSolutionCount() + " solutions.\n");

            getUtils().DebugPrint("Solution from solver:\n");
            int solutionNumber = 0;

            getUtils().DebugPrint("\nSolution #" + solutionNumber);
            if (getUtils().debug) taskList.forEach(System.out::println);
            solutionNumber++;

            // make deep-copy of the taskList
            ArrayList<Task> firstCopyOfTaskList = new ArrayList<>();
            taskList.forEach(e -> {
                try {
                    firstCopyOfTaskList.add(e.clone());
                } catch (CloneNotSupportedException ex) {
                    throw new RuntimeException(ex);
                }
            });

            // build Scheduled Tasks from the Solver result
            ArrayList<ScheduledTask> scheduledTasks = new ArrayList<>();
            ScheduledTask.buildScheduledTaskList(firstCopyOfTaskList, solver, modelTaskMap, scheduledTasks);
            scheduledTasks.forEach(e -> getUtils().DebugPrint("added task from schedule: " + e));

            if (inputs.PostHocPrecedence()) {
                // add precedence constraints on top of the solved Schedule
                BasicPrecedenceConstraints.addPostHocPrecedenceConstraints(firstCopyOfTaskList, scheduledTasks, inputs.PhasedReleaseTimes(), inputs, warnings, Utils, 0);
            }

            int formattedTaskSetNumberOfResources = numResou;       // the number of resources in the formatted task set, this number is adjusted by postHoc residency constraint resource renaming
            if (inputs.PostHocResidency()) {
                // add residency constraints to tasks depending on the solver output
                formattedTaskSetNumberOfResources = ResidencyConstraints.addPostHocResidencyConstraints(firstCopyOfTaskList, numResou, scheduledTasks, inputs.MultiResidency(), min_residency_constraints, max_residency_constraints, inputs, warnings, Utils, 0);
            }

            // read releaseTimes & deadlines & wcet from the scheduled task set
            for (ScheduledTask st : scheduledTasks) {
                ScheduledTask.readScheduledTaskValues(firstCopyOfTaskList, inputs.PhasedReleaseTimes(), st);
            }

            System.out.println("\nFinal taskList");
            firstCopyOfTaskList.forEach(e -> {
                if (!(e instanceof TaskInstance)) System.out.println(e);
            });

            // Print solution
            if (Utils.debug)
                Utils.PrintSchedule(limit, status, firstCopyOfTaskList, solver, modelTaskMap, dynamicConstraints, numResou);
            generationResult.storeValues(solver, dynamicConstraints, modelTaskMap);

            // Format and Print TaskSet
            TaskSet taskSet = new TaskSet(firstCopyOfTaskList, formattedTaskSetNumberOfResources, modelTaskMap, solver);
            String formattedTaskSet;
            String dotFile;
            // Format Task Set
            try {
                formattedTaskSet = taskSet.toFormat("task_set", inputs.PhasedReleaseTimes(), inputs, limit, getUtils(), warnings);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // Print Task Set and Save to File
            try {
                getUtils().DebugPrint(formattedTaskSet);
                dotFile = taskSet.generateDot(firstCopyOfTaskList);
                getUtils().DebugPrint(dotFile);
                // Save file
                if (inputs.isSaveOutput()) {
                    boolean success = taskSet.saveToFile(formattedTaskSet, "taskSet", ".json");
                    if (success) {
                        System.out.println("Successfully saved task set to file.");
                    } else {
                        System.out.println("Failed to save task set to file.");
                    }
                    success = taskSet.saveToFile(dotFile, "taskSet", ".dot");
                    if (success) {
                        System.out.println("Successfully saved .dot file.");
                    } else {
                        System.out.println("Failed to save .dot file.");
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // extract additional solutions and parse them
            if (inputs.getSolutionCount() > 1) {
                for (CpSolverSolution cpSolverSolution : solutionList) {

                    if (solutionNumber == 1) {  // Fix to prevent the first solution from being printed twice
                        solutionNumber++;
                        continue;
                    }

                    System.out.println("\nSolution #" + (solutionNumber - 1));
                    extractAdditionalSolutionsFromSolver(limit, taskList, numResou, inputs, cpSolverSolution, modelTaskMap, min_residency_constraints, max_residency_constraints, status, solver, dynamicConstraints, generationResult, solutionNumber - 1);
                    solutionNumber++;
                }
            }

            generationResult.storeValues(solver, dynamicConstraints, modelTaskMap);
            generationResult.taskSet = taskSet;

        } else {
            System.out.println("No solution found.");
            System.out.println(solver.responseStats());
            modelTaskMap.forEach((taskID, modeltask) -> {
                System.out.print("TaskID: " + taskID + " - ");
                System.out.println("Start: " + modeltask.getStart().getIndex() + " End: " + modeltask.getEnd().getIndex() + " WCET: " + modeltask.getWcet().getIndex() + " resource: " + modeltask.getResource().getIndex() + " interval: " + modeltask.getInterval().getIndex());
                System.out.println(taskList.get(taskID));
            });
            if (inputs instanceof TGFFInputParameters) {
                System.out.println("\nTGFF Generation method was used. Infeasibility is most likely due to an insufficient number of resources.\nIncrease the number of resources, or change other TGFF parameters.\n\n");
                System.out.println("The current number of resources is " + numResou + ". Should we restart the Model with numberOfResources=" + (numResou + 1) + "?\n0=NO, 1=YES");
                int restart;
                try {
                    restart = System.in.read();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (restart == 1) {
                    // restart the method with a larger number of resources
                    return generateModelAndSolve(limit, taskList, numResou + 1, inputs);
                }
            }

        }

        if (!this.warnings.isEmpty()) {
            System.out.println("--------------------------------------------------\nAll warnings produced during generation:");
            for (String warning : this.warnings) {
                System.out.println(warning);
            }
            System.out.println("--------------------------------------------------");
        } else {
            System.out.println("----- No warnings were generated -----");
        }
        return generationResult;
    }

    /**
     * Extracts solutions from the solver, executes posthoc functions and formats the task sets.
     *
     * @param limit                     the scheduling limit.
     * @param taskList                  the list of tasks.
     * @param numResou                  the number of resources.
     * @param inputs                    the Input parameters object.
     * @param cpSolverSolution          the CP Solver solution object.
     * @param modelTaskMap              maps the task ids to the corresponding model tasks.
     * @param min_residency_constraints the minimum residency constraints.
     * @param max_residency_constraints the maximum residency constraints.
     * @param status                    the solver-status object for the solver.
     * @param solver                    the solver object.
     * @param dynamicConstraints        the dynamic constraints object.
     * @param generationResult          the Generation result object.
     * @param solutionNum               the current solution number.
     */
    private void extractAdditionalSolutionsFromSolver(int limit, ArrayList<Task> taskList, int numResou, InputParameters inputs, CpSolverSolution cpSolverSolution, Map<Integer, ModelTask> modelTaskMap, int min_residency_constraints, int max_residency_constraints, CpSolverStatus status, CpSolver solver, DynamicConstraints dynamicConstraints, GenerationResult generationResult, int solutionNum) {

        // make deep-copy of the taskList
        System.out.println(taskList);
        ArrayList<Task> copyOfTaskList = new ArrayList<>();
        taskList.forEach(e -> {
            try {
                copyOfTaskList.add(e.clone());
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
            }
        });

        // build scheduled tasks from the solver result
        ArrayList<ScheduledTask> scheduledTasks = new ArrayList<>();
        for (int id = 0; id < modelTaskMap.size(); id++) {
            ScheduledTask scheduledTask = new ScheduledTask(
                    id,
                    (int) cpSolverSolution.getValues(modelTaskMap.get(id).getStart().getIndex()),
                    (int) cpSolverSolution.getValues(modelTaskMap.get(id).getEnd().getIndex()),
                    (int) cpSolverSolution.getValues(modelTaskMap.get(id).getWcet().getIndex()),
                    (int) cpSolverSolution.getValues(modelTaskMap.get(id).getResource().getIndex()),
                    copyOfTaskList.get(id).getPredecessors().stream().mapToInt(i -> i).toArray(),
                    copyOfTaskList.get(id).getPeriod());
            scheduledTasks.add(scheduledTask);
        }

        if (inputs.PostHocPrecedence()) {
            // add precedence constraints on top of the solved Schedule
            BasicPrecedenceConstraints.addPostHocPrecedenceConstraints(copyOfTaskList, scheduledTasks, inputs.PhasedReleaseTimes(), inputs, warnings, Utils, solutionNum);
        }

        int formattedTaskSetNumberOfResources = numResou;       // the number of resources in the formatted task set, this number is adjusted by postHoc residency constraint resource renaming
        if (inputs.PostHocResidency()) {
            // add residency constraints to tasks depending on the solver output
            formattedTaskSetNumberOfResources = ResidencyConstraints.addPostHocResidencyConstraints(copyOfTaskList, numResou, scheduledTasks, inputs.MultiResidency(), min_residency_constraints, max_residency_constraints, inputs, warnings, Utils, solutionNum);
        }


        // read releaseTimes & deadlines & wcet from the scheduled task set
        for (ScheduledTask st : scheduledTasks) {
            ScheduledTask.readScheduledTaskValues(copyOfTaskList, inputs.PhasedReleaseTimes(), st);
        }

        System.out.println("\nFinal taskList");
        copyOfTaskList.forEach(e -> {
            if (!(e instanceof TaskInstance)) System.out.println(e);
        });

        // Print solution
        if (Utils.debug)
            Utils.PrintSchedule(limit, status, copyOfTaskList, solver, modelTaskMap, dynamicConstraints, numResou);
        generationResult.storeValues(solver, dynamicConstraints, modelTaskMap);

        // Format and Print TaskSet
        TaskSet taskSet = new TaskSet(copyOfTaskList, formattedTaskSetNumberOfResources, modelTaskMap, solver);
        String formattedTaskSet;
        String dotFile;
        // Format Task Set
        try {
            formattedTaskSet = taskSet.toFormat("task_set", inputs.PhasedReleaseTimes(), inputs, limit, getUtils(), warnings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Print Task Set and Save to File
        try {
            System.out.println(formattedTaskSet);
            dotFile = taskSet.generateDot(copyOfTaskList);
            System.out.println(dotFile);
            // Save file
            if (inputs.isSaveOutput()) {
                boolean success = taskSet.saveToFile(formattedTaskSet, "taskSet", ".json");
                if (success) {
                    System.out.println("Successfully saved task set to file.");
                } else {
                    System.out.println("Failed to save task set to file.");
                }
                success = taskSet.saveToFile(dotFile, "taskSet", ".dot");
                if (success) {
                    System.out.println("Successfully saved .dot file.");
                } else {
                    System.out.println("Failed to save .dot file.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates task sets according to our Basic Generation Approach.
     *
     * @param inputs   the InputParameters object.
     * @param harmonic boolean that controls the generation of harmonic periods.
     * @return the Generation Result object.
     */
    public GenerationResult GenerateBasicTaskSet(InputParameters inputs, boolean harmonic) {

        // Get specific numbers from min/max boundaries
        int numTasks = RandomNumberGeneration.getNumWithDistribution(inputs.getMin_number_of_tasks(), inputs.getMax_number_of_tasks(), inputs.getNumberOfTasks_distribution(), getUtils(), inputs.getNOTasksPoissonMean(), inputs.getNOTasksBinomialP(), inputs.getNOTasksBinomialN(), warnings);
        int numResou = RandomNumberGeneration.getNumWithDistribution(inputs.getMin_number_of_resources(), inputs.getMax_number_of_resources(), inputs.getNumberOfResources_distribution(), getUtils(), inputs.getNOResourcesPoissonMean(), inputs.getNOResourcesBinomialP(), inputs.getNOResourcesBinomialN(), warnings);
        int numResid = RandomNumberGeneration.getNumWithDistribution(inputs.getMin_residency_constraints(), inputs.getMax_residency_constraints(), inputs.getNumberOfResidencyConstraints_distribution(), getUtils(), inputs.getNOResidencyPoissonMean(), inputs.getNOResidencyBinomialP(), inputs.getNOResidencyBinomialN(), warnings);
        int numPrec = RandomNumberGeneration.getNumWithDistribution(inputs.getMin_total_precedence_relations(), inputs.getMax_total_precedence_relations(), inputs.getNumberOfPrecedenceConstraints_distribution(), getUtils(), inputs.getNOPrPoissonMean(), inputs.getNOPrBinomialP(), inputs.getNOPrBinomialN(), warnings);

        // Check values regarding subgraphs
        if (inputs.getSubgraphNumber() != 0) {
            if (numTasks < inputs.getSubgraphNumber()) {
                System.out.println("WARNING: Chosen value for subgraphs exceeds the number of tasks:\nSubgraphs: " + inputs.getSubgraphNumber() + " numTasks: " + numTasks);
                warnings.add("WARNING: Chosen value for subgraphs exceeds the number of tasks:\nSubgraphs: " + inputs.getSubgraphNumber() + " numTasks: " + numTasks);
                // Our chosen number of tasks is smaller than the number of subgraphs, try to repair it
                while (numTasks < inputs.getMax_number_of_tasks() && numTasks < inputs.getSubgraphNumber()) {
                    numTasks++;
                }
                if (numTasks >= inputs.getSubgraphNumber()) {
                    System.out.println("Successfully repaired the number of tasks regarding subgraph number:\nNumber of tasks: " + numTasks + " Number of subgraphs: " + inputs.getSubgraphNumber());
                } else {
                    System.out.println("ERROR: Failed to repair the number of tasks regarding subgraph number: Make sure the number of tasks is larger than the number of subgraphs!\nLast attempted number of Tasks: " + numTasks + " Subgraphs: " + inputs.getSubgraphNumber());
                    warnings.add("ERROR: Failed to repair the number of tasks regarding subgraph number: Make sure the number of tasks is larger than the number of subgraphs!\nLast attempted number of Tasks: " + numTasks + " Subgraphs: " + inputs.getSubgraphNumber());
                }
            }
        }

        // Check values regarding residency constraints
        if (numResid > numTasks) {
            System.out.println("WARNING: Chosen value for residency constraints exceeds the number of tasks:\nnumResid: " + numResid + " numTasks: " + numTasks);
            warnings.add("WARNING: Chosen value for residency constraints exceeds the number of tasks:\nnumResid: " + numResid + " numTasks: " + numTasks);
            if (numTasks <= inputs.getMax_residency_constraints() && numTasks >= inputs.getMin_residency_constraints()) {
                numResid = numTasks;
                System.out.println("Successfully repaired the chosen value for residency constraints:\nUser Input:\nmin-resid: " + inputs.getMin_residency_constraints() + " max-resid: " + inputs.getMax_residency_constraints() + "\nmin-tasks: " + inputs.getMin_number_of_tasks() + " max-tasks: " + inputs.getMax_number_of_tasks() + "\nChosen Values: Resid: " + numResid + " Tasks: " + numTasks);
            } else {
                // Our chosen number of tasks is smaller than the user specified minimum number of residency constraints.
                // Try to choose a different number of tasks if possible, or return a failure
                while (numTasks < inputs.getMax_number_of_tasks() && numTasks < inputs.getMin_residency_constraints()) {
                    numTasks++;
                }
                if (numTasks <= inputs.getMax_residency_constraints() && numTasks >= inputs.getMin_residency_constraints()) {
                    numResid = numTasks;
                    System.out.println("Successfully repaired the chosen value for residency constraints:\nUser Input:\nmin-resid: " + inputs.getMin_residency_constraints() + " max-resid: " + inputs.getMax_residency_constraints() + "\nmin-tasks: " + inputs.getMin_number_of_tasks() + " max-tasks: " + inputs.getMax_number_of_tasks() + "\nChosen Values: Resid: " + numResid + " Tasks: " + numTasks);
                } else {
                    System.out.println("ERROR: Failed to choose appropriate values from user input. Make suer the number of residency constraints does not exceed the number of tasks.");
                    warnings.add("ERROR: Failed to choose appropriate values from user input. Make suer the number of residency constraints does not exceed the number of tasks.");
                    System.exit(-1);
                }
            }
        }

        // Execute Sanity Checks
        inputs.executeSanityChecks();

        // Generate Tasks
        ArrayList<Task> taskList = generateModelReadyTasks(numTasks, numResid, numResou, numPrec, inputs, harmonic, inputs.MultiResidency(), inputs.PostHocPrecedence(), inputs.PhasedReleaseTimes(), inputs.PostHocResidency());

        // Calculate the appropriate Scheduling Limit considering periodicity, period length and phases
        int limit = PhasesAndSchedulingLimit.getPeriodicAperiodicSchedulingLimit(inputs, taskList, inputs.PhasedReleaseTimes());

        // Generate remaining Task instances for periodic tasks
        ArrayList<Task> additionalTasks = generateModelReadyPeriodicTaskInstances(taskList, limit, harmonic);
        if (!additionalTasks.isEmpty()) taskList.addAll(additionalTasks);

        System.out.println("\n\nComplete TaskList before solving the CP:\n");
        taskList.forEach(task -> System.out.println("    " + task.toString()));
        if (taskList.size() > numResou * limit)
            throw new RuntimeException("ERROR: Task List is not schedulable with minimum WCET, since the number of tasks is larger than the scheduling limit times the number of resources");

        for (Task task : taskList) {
            boolean[] visited = new boolean[taskList.size()];
            boolean[] indexed = new boolean[taskList.size()];
            boolean cyclic = BasicPrecedenceConstraints.dfsFindCycle(taskList, task, visited, indexed);
            if (cyclic)
                throw new RuntimeException("ERROR: generated TaskList contains a cycle and cannot be scheduled");
        }

        return generateModelAndSolve(limit, taskList, numResou, inputs);
    }

    /**
     * Generates task sets according to our TGFF-based Generation Approach.
     *
     * @param inputs the InputParameters object.
     * @return the Generation Result object.
     * @throws Exception if the TGFF generated task set is not schedulable with the number of resources.
     */
    public GenerationResult GenerateTaskSetFromTGFF(TGFFInputParameters inputs) throws Exception {

        String path = TGFFGraphGeneration.runTGFF(inputs, getUtils());

        String content = TGFFGraphGeneration.readTGFF(path, inputs.isCleanUpFiles(), warnings);

        TGFFTaskSet taskSet = TGFFGraphGeneration.readGraphs(content, inputs.isCalculatePeriods(), inputs.getPeriod_mul(), getUtils(), warnings);

        int limit;
        boolean periodic = true;
        boolean aperiodic = true;
        for (Task task : taskSet.getTaskList()) {
            if (!task.isPeriodic()) {
                periodic = false;
            } else {
                aperiodic = false;
            }
        }
        if (periodic) {
            System.out.println("TASK SET IS PERIODIC");
        } else if (aperiodic) {
            System.out.println("TASK SET IS APERIODIC");
        } else {
            System.out.println("TASK SET IS MIXED-PERIODIC");
        }
        limit = PhasesAndSchedulingLimit.calculateSchedulingLimit(taskSet.getTaskList(), periodic, aperiodic);

        getUtils().DebugPrint("Scheduling limit: " + limit);

        // Generate remaining Task instances for periodic tasks
        ArrayList<Task> additionalTasks = generateModelReadyPeriodicTaskInstances(taskSet.getTaskList(), limit, false);
        if (!additionalTasks.isEmpty()) taskSet.getTaskList().addAll(additionalTasks);

        // Calculate the necessary number of resources
        int numberOfResources = inputs.getNumberOfResources();
        if (inputs.getNumberOfResources() < 0) {
            System.out.println("WARNING: Specified number of resources is < 0, and the task set thus not schedulable.\nWe will instead use the sum of the widths of all subtrees as the number of resources.");
            warnings.add("WARNING: Specified number of resources is < 0, and the task set thus not schedulable.\nWe will instead use the sum of the widths of all subtrees as the number of resources.");
            int width = ResidencyConstraints.calculateWidth(taskSet.getTaskList(), taskSet.getSubgraphMap(), Utils);
            getUtils().DebugPrint("Calculated Tree width: " + width);
            numberOfResources = width;
        }

        Utils.DebugPrint("\n\nComplete TaskList:\n");
        if (DEBUG) taskSet.getTaskList().forEach(task -> System.out.println("Debug: " + task.toString()));
        if (taskSet.getTaskList().size() > numberOfResources * limit)
            throw new RuntimeException("ERROR: Task List is not schedulable with minimum WCET, since the number of tasks is larger than the scheduling limit times the number of resources");

        return generateModelAndSolve(limit, taskSet.getTaskList(), numberOfResources, inputs);
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.out.println("Usage: mode(0=Basic,1=TGFF)\nAlternative Usage: path/To/Input/File");
            System.exit(-1);

        } else if (args.length == 1) {
            if (args[0].equals("0") || args[0].equals("1")) {
                // ask the user to input constraints depending on the generation mode
                if (args[0].equals("0")) {
                    // ask for Basic Task Set appropriate constraints
                    Scanner scanner = new Scanner(System.in);
                    ArrayList<String> userInputs = new ArrayList<>();
                    do {
                        userInputs.clear();
                        System.out.println("Usage: \n" +
                                "min_number_tasks max_number_tasks \n" +
                                "min_number_resources max_number_resources \n" +
                                "min_residency_constraints max_residency_constraints \n" +
                                "min_precedence_constraints max_precedence_constraints \n" +
                                "min_period_percentage max_period_percentage \n" +
                                "min_period_length max_period_length \n" +
                                "multiresidency(0=OFF,1=ON) \n" +
                                "phasedreleasetimes(0=OFF,1=ON) \n" +
                                "posthocprecedence(0=OFF,1=ON) \n" +
                                "posthocresidency(0=OFF,1=ON) \n" +
                                "subgraphnumber \n" +
                                "numberofworkerthreads \n" +
                                "solutioncount \n" +
                                "debug_mode(0=OFF,1=ON)\n");
                        String userValue = scanner.nextLine();
                        String[] splitValues = userValue.split(" ");
                        userInputs.addAll(Arrays.asList(splitValues));

                        System.out.println("Read " + userInputs.size() + " values:\n");
                        userInputs.forEach(System.out::println);

                    } while (userInputs.size() != 20);

                    // Run GenerateBasicTaskSet
                    TaskGenerationFramework tgf = new TaskGenerationFramework(Integer.parseInt(userInputs.get(19)) == 1, new Random());
                    InputParameters inputs = getInputParametersFromUserInput(userInputs);
                    inputs.setSaveOutput(true);
                    System.out.println("input parameter object:\n" + inputs);

                    GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);
                    // TODO (user): use the generated output

                } else {
                    // ask for TGFF appropriate constraints
                    Scanner scanner = new Scanner(System.in);
                    ArrayList<String> userInputs = new ArrayList<>();
                    do {
                        userInputs.clear();
                        System.out.println("Usage: \n" +
                                "graphCount \n" +
                                "taskCountAverage taskCountMultiplier \n" +
                                "periodPercentage \n" +
                                "multiStartNodeProb \n" +
                                "startNodesAverage startNodesMultiplier \n" +
                                "taskDegreeIN taskDegreeOUT \n" +
                                "hardDeadlineProb \n" +
                                "numberOfResources \n" +
                                "taskTransitionTime \n" +
                                "period-multiplier(List of integers separated by a comma, no whitespaces. Example: \"1,2,3,4\" \n" +
                                "cleanUpFiles(0=OFF,1=ON)\n" +
                                "postHoc_residency(0=OFF,1=ON) \n" +
                                "CalculatePeriods(0=OFF,1=ON) \n" +
                                "multiResidency(0=OFF,1=ON) \n" +
                                "numberOfWorkerThreads \n" +
                                "solutionCount \n" +
                                "Debug-Mode(0=OFF,1=ON)\n");
                        String userValue = scanner.nextLine();
                        String[] splitValues = userValue.split(" ");
                        userInputs.addAll(Arrays.asList(splitValues));

                        System.out.println("Read " + userInputs.size() + " values:\n");
                        userInputs.forEach(System.out::println);

                    } while (userInputs.size() != 20);

                    // Run TGFF
                    TaskGenerationFramework tgf = new TaskGenerationFramework(Boolean.parseBoolean(userInputs.get(19)), new Random());
                    TGFFInputParameters inputs = new TGFFInputParameters();
                    inputs.setGraphCount(Integer.parseInt(userInputs.get(0)));
                    inputs.setTaskCountAverage(Integer.parseInt(userInputs.get(1)));
                    inputs.setTaskCountMultiplier(Integer.parseInt(userInputs.get(2)));
                    inputs.setPeriodPercentage(Double.parseDouble(userInputs.get(3)));
                    inputs.setMultiStartNodeProb(Double.parseDouble(userInputs.get(4)));
                    inputs.setStartNodesAverage(Integer.parseInt(userInputs.get(5)));
                    inputs.setStartNodesMultiplier(Integer.parseInt(userInputs.get(6)));
                    inputs.setTaskDegreeIN(Integer.parseInt(userInputs.get(7)));
                    inputs.setTaskDegreeOUT(Integer.parseInt(userInputs.get(8)));
                    inputs.setHardDeadlineProb(Double.parseDouble(userInputs.get(9)));
                    inputs.setNumberOfResources(Integer.parseInt(userInputs.get(10)));
                    inputs.setTaskTransitionTime(Double.parseDouble(userInputs.get(11)));
                    String[] periodMulStrings = String.valueOf(userInputs.get(12)).split(",");
                    ArrayList<String> period_mul_strings = new ArrayList<>(Arrays.asList(periodMulStrings));// TODO test period mul
                    ArrayList<Integer> period_mul = new ArrayList<>();
                    for (String str : period_mul_strings) {
                        period_mul.add(Integer.valueOf(str));
                    }
                    inputs.setPeriod_mul(period_mul);
                    inputs.setCleanUpFiles(Integer.parseInt(userInputs.get(13)) == 1);
                    inputs.setPostHocResidency(Integer.parseInt(userInputs.get(14)) == 1);
                    inputs.setCalculatePeriods(Integer.parseInt(userInputs.get(15)) == 1);
                    inputs.setMultiResidency(Integer.parseInt(userInputs.get(16)) == 1);
                    inputs.setNumberOfWorkerThreads(Integer.parseInt(userInputs.get(17)));
                    inputs.setSolutionCount(Integer.parseInt(userInputs.get(18)));
                    inputs.setDebug(Integer.parseInt(userInputs.get(15)) == 1);
                    inputs.setSaveOutput(true);
                    System.out.println("input parameter object:\n" + inputs);

                    try {
                        tgf.GenerateTaskSetFromTGFF(inputs);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            } else {
                // interpret argument 0 as an input file path
                ArrayList<String> warnings = new ArrayList<>();
                InputParameters inputs = InputParameters.readInputFile(args[0], warnings);
                if (inputs == null) {
                    System.out.println("ERROR: Failed to read input file. Read Function returned null.");
                    System.exit(-1);
                } else {
                    if (inputs instanceof TGFFInputParameters) {
                        TaskGenerationFramework tgf = new TaskGenerationFramework(inputs.isDebug(), new Random(), warnings);
                        tgf.GenerateTaskSetFromTGFF((TGFFInputParameters) inputs);
                        System.exit(0);
                    } else {
                        TaskGenerationFramework tgf = new TaskGenerationFramework(inputs.isDebug(), new Random(), warnings);
                        GenerationResult generationResult = tgf.GenerateBasicTaskSet(inputs, false);   // TODO change harmonic value once it is implemented

                        // TODO (user): use the generated output

                        System.exit(0);
                    }
                }
            }
        } else {
            System.out.println("Unexpected number of arguments. \nUsage: mode(0=Basic,1=TGFF)\nAlternative Usage: path/To/Input/File");
            System.exit(-1);
        }

    }

    /**
     * Interprets the user input for the basic generation input parameters.
     *
     * @param userInputs String list of user inputs
     * @return interpreted Input parameters object
     */
    private static InputParameters getInputParametersFromUserInput(ArrayList<String> userInputs) {
        InputParameters inputs = new InputParameters();
        // Tasks
        inputs.setMin_number_of_tasks(Integer.parseInt(userInputs.get(0)));
        inputs.setMax_number_of_tasks(Integer.parseInt(userInputs.get(1)));
        // Resources
        inputs.setMin_number_of_resources(Integer.parseInt(userInputs.get(2)));
        inputs.setMax_number_of_resources(Integer.parseInt(userInputs.get(3)));
        // Residency
        inputs.setMin_residency_constraints(Integer.parseInt(userInputs.get(4)));
        inputs.setMax_residency_constraints(Integer.parseInt(userInputs.get(5)));
        // Precedence
        inputs.setMin_total_precedence_relations(Integer.parseInt(userInputs.get(6)));
        inputs.setMax_total_precedence_relations(Integer.parseInt(userInputs.get(7)));
        // Periods
        inputs.setMin_period_percentage(Integer.parseInt(userInputs.get(8)));
        inputs.setMax_period_percentage(Integer.parseInt(userInputs.get(9)));
        inputs.setMin_period_length(Integer.parseInt(userInputs.get(10)));
        inputs.setMax_period_length(Integer.parseInt(userInputs.get(11)));

        // set distributions
        inputs.setResidency_distribution(Distribution.UNIFORM);
        inputs.setPeriodicity_distribution(Distribution.UNIFORM);
        inputs.setPeriodLength_distribution(Distribution.UNIFORM);

        inputs.setMultiResidency(Integer.parseInt(userInputs.get(12)) == 1);
        inputs.setPhasedReleaseTimes(Integer.parseInt(userInputs.get(13)) == 1);
        inputs.setPostHocPrecedence(Integer.parseInt(userInputs.get(14)) == 1);
        inputs.setPostHocResidency(Integer.parseInt(userInputs.get(15)) == 1);

        inputs.setSubgraphNumber(Integer.parseInt(userInputs.get(16)));
        inputs.setNumberOfWorkerThreads(Integer.parseInt(userInputs.get(17)));
        inputs.setSolutionCount(Integer.parseInt(userInputs.get(18)));
        inputs.setDebug(Integer.parseInt(userInputs.get(19)) == 1);
        return inputs;
    }

}
