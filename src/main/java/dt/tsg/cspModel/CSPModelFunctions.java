package dt.tsg.cspModel;

import com.google.ortools.sat.*;
import dt.tsg.InputParams.InputParameters;
import dt.tsg.task.Task;
import dt.tsg.task.TaskInstance;
import dt.tsg.utils.GenerationResult;
import dt.tsg.utils.RandomNumberGeneration;
import dt.tsg.utils.Utils;

import java.util.*;

public class CSPModelFunctions {

    /**
     * Adds NoOverlap Constraints between all tasks to the CP Model.
     *
     * @param taskList     List of all tasks.
     * @param cspModel     The CP Model object.
     * @param modelTaskMap Map of all CP Model tasks.
     */
    public static void addNoOverlapConstraintsToModel(ArrayList<Task> taskList, CpModel cspModel, Map<Integer, ModelTask> modelTaskMap, Utils utils) {
        // Add No Overlap Constraints
        int numberOfSkips = 0;
        for (Task firstTask : taskList) {
            for (Task secondTask : taskList) {
                if (firstTask.getIdentifier() < secondTask.getIdentifier()) {
                    BoolVar t2t = cspModel.newBoolVar("t2t_" + firstTask.getIdentifier() + "_" + secondTask.getIdentifier());
                    //System.out.println("t2t_" + firstTask.getIdentifier() + "_" + secondTask.getIdentifier());
                    ModelTask firstModelTask = modelTaskMap.get(firstTask.getIdentifier());
                    ModelTask secondModelTask = modelTaskMap.get(secondTask.getIdentifier());
                    // skip tasks with non-intersecting domains
                    // first task domain ends before the second task domain begins
                    if (firstModelTask.getEnd().getDomain().max() < secondModelTask.getStart().getDomain().min()) {
                        numberOfSkips++;
                        continue;
                    }
                    // first task domain begins after second task domain has ended
                    if (firstModelTask.getStart().getDomain().min() > secondModelTask.getEnd().getDomain().max()) {
                        numberOfSkips++;
                        continue;
                    }

                    cspModel.addEquality(firstModelTask.getResource(), secondModelTask.getResource()).onlyEnforceIf(t2t);
                    cspModel.addDifferent(firstModelTask.getResource(), secondModelTask.getResource()).onlyEnforceIf(t2t.not());
                    //cspModel.addNoOverlap(list).onlyEnforceIf(t2t.not()); breaks the model

                    // Use OptionalIntervalVars to add conditional NoOverlap Constraints to the Model
                    firstModelTask.setInterval(cspModel.newOptionalIntervalVar(
                            firstModelTask.getStart(),
                            firstModelTask.getWcet(),
                            firstModelTask.getEnd(),
                            t2t,
                            "interval" + firstTask.getIdentifier()));

                    secondModelTask.setInterval(cspModel.newOptionalIntervalVar(
                            secondModelTask.getStart(),
                            secondModelTask.getWcet(),
                            secondModelTask.getEnd(),
                            t2t,
                            "interval" + secondTask.getIdentifier()));

                    ArrayList<IntervalVar> list = new ArrayList<>();
                    list.add(firstModelTask.getInterval());
                    list.add(secondModelTask.getInterval());

                    cspModel.addNoOverlap(list);

                }
            }
        }
        utils.DebugPrint("NumberOfNoOverlapSkips: " + numberOfSkips);
    }

    /**
     * Generates the following dynamic Constraints and adds them to the model:
     * Minimum ReleaseTime, Maximum ReleaseTime, Minimum Deadline, Maximum Deadline, Minimum WCET, Maximum WCET.
     * ReleaseTime and Deadline Variables have the domain [0,limit], while WCET have the domain [1,limit].
     *
     * @param cspModel The CP Model object.
     * @param limit    The limit up to which the Task Set is evaluated.
     * @return IntVar[], which contains the dynamic constraints.
     */
    public static DynamicConstraints buildDynamicConstraints(CpModel cspModel, int limit, InputParameters inputs) {
        // Generate Dynamic Constraints - Tasks will use these dynamic constraints as bounds
        //ReleaseTime
        IntVar cp_releasetime_min;
        if (inputs.getMin_releaseTime() == -1) {
            cp_releasetime_min = cspModel.newIntVar(0, limit, "CP_RELEASETIME_MIN");
        } else {
            cp_releasetime_min = cspModel.newIntVar(inputs.getMin_releaseTime(), inputs.getMin_releaseTime(), "CP_RELEASETIME_MIN");
        }
        IntVar cp_releasetime_max;
        if (inputs.getMax_releaseTime() == -1) {
            cp_releasetime_max = cspModel.newIntVar(0, limit, "CP_RELEASETIME_MAX");
        } else {
            cp_releasetime_max = cspModel.newIntVar(inputs.getMax_releaseTime(), inputs.getMax_releaseTime(), "CP_RELEASETIME_MAX");
        }
        cspModel.addGreaterOrEqual(cp_releasetime_max, cp_releasetime_min);

        //Deadline
        IntVar cp_deadline_min;
        if (inputs.getMin_deadline() == -1) {
            cp_deadline_min = cspModel.newIntVar(0, limit, "CP_DEADLINE_MIN");
        } else {
            cp_deadline_min = cspModel.newIntVar(inputs.getMin_deadline(), inputs.getMin_deadline(), "CP_DEADLINE_MIN");
        }

        IntVar cp_deadline_max;
        if (inputs.getMax_deadline() == -1 || inputs.isDeadlineEqualToPeriod()) {
            cp_deadline_max = cspModel.newIntVar(0, limit, "CP_DEADLINE_MAX");
        } else {
            cp_deadline_max = cspModel.newIntVar(inputs.getMax_deadline(), inputs.getMax_deadline(), "CP_DEADLINE_MAX");
        }
        cspModel.addGreaterOrEqual(cp_deadline_max, cp_deadline_min);

        //WCET
        IntVar cp_wcet_min;
        if (inputs.getMin_WCET() == -1) {
            cp_wcet_min = cspModel.newIntVar(1, limit, "CP_WCET_MIN");
        } else {
            cp_wcet_min = cspModel.newIntVar(inputs.getMin_WCET(), inputs.getMin_WCET(), "CP_WCET_MIN");
        }
        IntVar cp_wcet_max;
        if (inputs.getMax_WCET() == -1) {
            cp_wcet_max = cspModel.newIntVar(1, limit, "CP_WCET_MAX");
        } else {
            cp_wcet_max = cspModel.newIntVar(inputs.getMax_WCET(), inputs.getMax_WCET(), "CP_WCET_MAX");
        }

        cspModel.addGreaterOrEqual(cp_wcet_max, cp_wcet_min);

        return new DynamicConstraints(cp_releasetime_min, cp_releasetime_max, cp_deadline_min, cp_deadline_max, cp_wcet_min, cp_wcet_max);
    }

    /**
     * Generates the CSP Model equivalent of Tasks, while respecting the dynamicConstraints previously generated.
     * The generated ModelTasks are a set of variables used in the CSP Model.
     * It returns a Map which maps the taskIDs to the equivalent CSP Model representation object.
     *
     * @param cspModel           The CSP Model object.
     * @param limit              The limit up to which the Task Set is evaluated.
     * @param taskList           The List of all Tasks.
     * @param numResou           The number of resources.
     * @param dynamicConstraints Dynamic Constraints. See "buildDynamicConstraints"
     * @return Map<Integer, ModelTask> which maps the taskIds to the equivalent ModelTask object that is used in the CP Model
     */
    public static Map<Integer, ModelTask> buildCSPModelTasksAndConstraints(CpModel cspModel, int limit, ArrayList<Task> taskList, int numResou, DynamicConstraints dynamicConstraints, boolean phased_releaseTimes, Utils utils, InputParameters inputs) {

        IntVar helper = cspModel.newIntVar(1, 1, "helper=1");

        // Generate ModelTask instances for the CP Model
        Map<Integer, ModelTask> modelTaskMap = new HashMap<>();    // maps TaskIds to their respective ModelTasks (ModelTasks being the representation of a task in the CP model)

        for (int taskID = 0; taskID < taskList.size(); ++taskID) {
            Task task = taskList.get(taskID);
            String suffix = "_" + taskID;

            ModelTask modelTask = new ModelTask();

            generateIntegerVariableDomains(cspModel, limit, taskList, phased_releaseTimes, task, modelTask, suffix, inputs);

            utils.DebugPrint("DOMAIN for task " + task.getIdentifier() + ": START:" + modelTask.getStart().getDomain() + " - END:" + modelTask.getEnd().getDomain() + " - WCET:" + modelTask.getWcet().getDomain());

            // Set lower and upper limit for WCET
            cspModel.addGreaterOrEqual(modelTask.getWcet(), dynamicConstraints.getCp_wcet_min());
            cspModel.addGreaterOrEqual(dynamicConstraints.getCp_wcet_max(), modelTask.getWcet());

            // Set resource constraints
            if (task.getResourceConstraint().isEmpty()) {
                // task is "free", set domain to [0,numResources-1]
                modelTask.setResource(cspModel.newIntVar(0, numResou - 1, "resource" + suffix));
            } else {
                // task is residency constrained
                if (task.getResourceConstraint().size() == 1) {
                    // task can only run on one resource
                    modelTask.setResource(cspModel.newIntVar(task.getResourceConstraint().get(0), task.getResourceConstraint().get(0), "resource" + suffix));
                } else {
                    // multi-resource-constraint - add NotEquality Constraints to every impossible resource value
                    modelTask.setResource(cspModel.newIntVar(0, numResou - 1, "resource" + suffix));
                    for (int l = 0; l < numResou; l++) {
                        if (!task.getResourceConstraint().contains(l)) {
                            // task cannot be scheduled on resource "l"
                            cspModel.addDifferent(modelTask.getResource(), l);
                        }
                    }
                }
            }

            //Make sure the end variable is always start+WCET
            LinearExpr offset = LinearExpr.weightedSum(new IntVar[]{modelTask.getStart(), modelTask.getWcet()}, new long[]{1, 1});   //this is equal to "start*1+1*WCET"
            cspModel.addEquality(offset, modelTask.getEnd());

            shiftTaskVariablesAndDynamicConstraints(cspModel, dynamicConstraints, task, modelTask, helper);

            // enforce equal parameters for instances of periodic tasks
            enforceEqualParametersForPeriodicTaskInstances(cspModel, taskList, phased_releaseTimes, task, modelTaskMap, modelTask, helper);

            // put the new modelTask into the modelTaskMap
            modelTaskMap.put(taskID, modelTask);
        }

        return modelTaskMap;

    }

    /**
     * Generates the Integer Variable Domains Start, End, WCET, for the specific task.
     * Domains are tailored to the period of the task, while also respecting user inputs regarding min and max values for the releaseTime, WCET, Deadline.
     * If one of these inputs is -1, we assume the user did not want to set a hard limit, thus we do not further constrain the domains beyond the period.
     *
     * @param cspModel            The CP-Model object
     * @param limit               The scheduling limit / hyperperiod.
     * @param taskList            The complete list of tasks
     * @param phased_releaseTimes Boolean that determines if phased releaseTimes are used
     * @param task                The current Task
     * @param modelTask           The CP-Model Task corresponding to the actual Task
     * @param suffix              The task suffix, used for naming CP-Variables
     * @param inputs              The input constraints
     */
    private static void generateIntegerVariableDomains(CpModel cspModel, int limit, ArrayList<Task> taskList, boolean phased_releaseTimes, Task task, ModelTask modelTask, String suffix, InputParameters inputs) {
        //generate Integer Variables
        if (task.isPeriodic()) {
            // task is periodic

            // set wcet domain
            int wcet_lowerbound = 1;            // wcet must be within [1,period_length]
            int wcet_upperbound = task.getPeriod();
            if (inputs.getMin_WCET() > -1) {
                // use the input min wcet for the lowerbound
                wcet_lowerbound = inputs.getMin_WCET();
            }
            if (inputs.getMax_WCET() > -1) {
                // use the input max wcet for the upperbound
                wcet_upperbound = Math.min(task.getPeriod(), inputs.getMax_WCET());
            }
            if (wcet_upperbound > inputs.getMax_deadline() - inputs.getMin_releaseTime() && inputs.getMax_deadline() != -1 && inputs.getMin_releaseTime() != -1)
                wcet_upperbound = inputs.getMax_deadline() - inputs.getMin_releaseTime();
            modelTask.setWcet(cspModel.newIntVar(wcet_lowerbound, wcet_upperbound, "wcet" + suffix));


            // set start and end domain according to the period and user inputs
            if (task instanceof TaskInstance) {
                // task is an instance of a periodic task, change variable domains depending on the original task
                if (phased_releaseTimes) {
                    // adjust the period to the phase of the original task
                    int originalTaskPhase = taskList.get(((TaskInstance) task).getOriginal_task_ID()).getPhase();
                    // the task must start and end within the period
                    int periodStart = ((TaskInstance) task).getInstance_number() * task.getPeriod() + originalTaskPhase;
                    int periodEnd = (((TaskInstance) task).getInstance_number() + 1) * task.getPeriod() + originalTaskPhase;
                    // take the user inputs into consideration
                    int start_lowerbound = periodStart;
                    int start_upperbound = periodEnd - 1;
                    if (inputs.getMin_releaseTime() > -1) {
                        // the start variable cannot be smaller than the period + minimum releasetime, otherwise we would miss the minimum releasetime input constraint
                        start_lowerbound += inputs.getMin_releaseTime();
                    }
                    int end_lowerbound = periodStart + 1;
                    int end_upperbound = getEndUpperbound(task, inputs, periodEnd);

                    // the end lowerbound must always be at minimum the start lowerbound + 1.
                    if (end_lowerbound < start_lowerbound + 1) end_lowerbound = start_lowerbound + 1;
                    // the start upperbound must always be at most the end upperbound -1.
                    if (start_upperbound > end_upperbound - 1) start_upperbound = end_upperbound - 1;

                    modelTask.setStart(cspModel.newIntVar(start_lowerbound, start_upperbound, "start" + suffix));
                    modelTask.setEnd(cspModel.newIntVar(end_lowerbound, end_upperbound, "end" + suffix));   //add +1 because the minimum WCET is 1
                } else {
                    // the original task has an implicit phase of 0
                    int periodStart = ((TaskInstance) task).getInstance_number() * task.getPeriod();
                    int periodEnd = (((TaskInstance) task).getInstance_number() + 1) * task.getPeriod();
                    // take the user inputs into consideration
                    int start_lowerbound = periodStart;
                    int start_upperbound = periodEnd - 1;
                    if (inputs.getMin_releaseTime() > -1) {
                        // the start variable cannot be smaller than the period + minimum releasetime, otherwise we would miss the minimum releasetime input constraint
                        start_lowerbound += inputs.getMin_releaseTime();
                    }
                    int end_lowerbound = periodStart + 1;
                    int end_upperbound = getEndUpperbound(task, inputs, periodEnd);

                    // the end lowerbound must always be at minimum the start lowerbound + 1.
                    if (end_lowerbound < start_lowerbound + 1) end_lowerbound = start_lowerbound + 1;
                    // the start upperbound must always be at most the end upperbound -1.
                    if (start_upperbound > end_upperbound - 1) start_upperbound = end_upperbound - 1;

                    modelTask.setStart(cspModel.newIntVar(start_lowerbound, start_upperbound, "start" + suffix));
                    modelTask.setEnd(cspModel.newIntVar(end_lowerbound, end_upperbound, "end" + suffix));   //add +1 because the minimum WCET is 1
                }
            } else {
                // task is an original periodic task
                int periodStart;
                int periodEnd;
                if (phased_releaseTimes) {
                    // domain starts at the releasetime and ends at releasetime + period
                    periodStart = task.getPhase();
                    periodEnd = task.getPhase() + task.getPeriod();
                } else {
                    // domain is only restricted by period and starts at 0
                    periodStart = 0;
                    periodEnd = task.getPeriod();
                }
                // take the user inputs into consideration
                int start_lowerbound = periodStart;
                int start_upperbound = periodEnd - 1;
                if (inputs.getMin_releaseTime() > -1) {
                    // the start variable cannot be smaller than the period + minimum releasetime, otherwise we would miss the minimum releasetime input constraint
                    start_lowerbound += inputs.getMin_releaseTime();
                }
                int end_lowerbound = periodStart + 1;
                int end_upperbound = getEndUpperbound(task, inputs, periodEnd);

                // the end lowerbound must always be at minimum the start lowerbound + 1.
                if (end_lowerbound < start_lowerbound + 1) end_lowerbound = start_lowerbound + 1;
                // the start upperbound must always be at most the end upperbound -1.
                if (start_upperbound > end_upperbound - 1) start_upperbound = end_upperbound - 1;

                modelTask.setStart(cspModel.newIntVar(start_lowerbound, start_upperbound, "start" + suffix));
                modelTask.setEnd(cspModel.newIntVar(end_lowerbound, end_upperbound, "end" + suffix));   //add +1 because the minimum WCET is 1
            }
        } else {
            // task is not periodic, we cannot further constrain the domain
            // we constrain aperiodic tasks to a min/max RT/Deadline/WCET
            // take user inputs into consideration
            int wcet_lowerbound = 1;
            int wcet_upperbound = limit;
            if (inputs.getMin_WCET() > -1) {
                // use the input min wcet for the lowerbound
                wcet_lowerbound = inputs.getMin_WCET();
            }
            if (inputs.getMax_WCET() > -1) {
                // use the input max wcet for the upperbound
                wcet_upperbound = Math.min(inputs.getMax_WCET(), limit);
            }
            modelTask.setWcet(cspModel.newIntVar(wcet_lowerbound, wcet_upperbound, "wcet" + suffix));
            int start_lowerbound = 0;
            int start_upperbound = limit - 1;
            if (inputs.getMin_releaseTime() > -1) {
                start_lowerbound += inputs.getMin_releaseTime();
            }

            int end_lowerbound = 1;     // end starts at 1 because the minimum WCET is 1
            int end_upperbound = limit;
            if (inputs.getMax_deadline() > -1) {
                end_upperbound -= limit - inputs.getMax_deadline();
                end_upperbound = Math.min(end_upperbound, limit);
            }

            // the end lowerbound must always be at minimum the start lowerbound + 1.
            if (end_lowerbound < start_lowerbound + 1) end_lowerbound = start_lowerbound + 1;
            // the start upperbound must always be at most the end upperbound -1.
            if (start_upperbound > end_upperbound - 1) start_upperbound = end_upperbound - 1;

            modelTask.setStart(cspModel.newIntVar(start_lowerbound, start_upperbound, "start" + suffix));
            modelTask.setEnd(cspModel.newIntVar(end_lowerbound, end_upperbound, "end" + suffix));   //add +1 because the minimum WCET is 1
        }
    }

    /**
     * Returns the upper bound of the end IntVar for a Model Task
     *
     * @param task      the task on which the model task is based.
     * @param inputs    the InpuParameters object.
     * @param periodEnd the end of the period of the task.
     * @return the upper bound of the end IntVar.
     */
    private static int getEndUpperbound(Task task, InputParameters inputs, int periodEnd) {
        int end_upperbound = periodEnd;
        if (inputs.getMax_deadline() > -1) {
            // set the upperbound of the end variable to: period_upperbound - (period - max deadline)
            int shiftedValue = Math.min(inputs.getMax_deadline(), task.getPeriod());    // if the maximum deadline is larger than our period, we must not extend the domain beyond our period
            end_upperbound -= task.getPeriod() - shiftedValue;
        }
        // set end_upperbound to the periodEnd if DeadlineEqualToPeriod is true
        if (inputs.isDeadlineEqualToPeriod()) {
            end_upperbound = periodEnd;
        }
        return end_upperbound;
    }

    /**
     * This function enforces equal relative release times and deadlines between original periodic tasks and periodic task instances.
     *
     * @param cspModel            the CPModel object.
     * @param taskList            the list of tasks.
     * @param phased_releaseTimes boolean variable that controls the generation of phases for tasks.
     * @param task                the task currently worked on.
     * @param modelTaskMap        the hashmap that maps tasks to Model Tasks.
     * @param modelTask           the corresponding Model Task.
     * @param helper              a helper object that is equal to 1 for linear expressions.
     */
    private static void enforceEqualParametersForPeriodicTaskInstances(CpModel cspModel, ArrayList<Task> taskList, boolean phased_releaseTimes, Task task, Map<Integer, ModelTask> modelTaskMap, ModelTask modelTask, IntVar helper) {

        if (task.isPeriodic()) {
            // task is periodic
            if (task instanceof TaskInstance) {
                // task is a task instance
                // enforce that the periodic task instance has the same WCET/resource relative to the original task
                int originalTaskID = ((TaskInstance) task).getOriginal_task_ID();
                // WCET
                cspModel.addEquality(modelTaskMap.get(originalTaskID).getWcet(), modelTask.getWcet());
                // Resource
                cspModel.addEquality(modelTaskMap.get(originalTaskID).getResource(), modelTask.getResource());

                int periodStart = ((TaskInstance) task).getInstance_number() * task.getPeriod();
                int periodEnd = (((TaskInstance) task).getInstance_number() + 1) * task.getPeriod();

                // ReleaseTimes == the relative offset from periodStart, set start IntVar of this task to be equal to this task's periodStart+releaseTime of the original task
                // calculate the relative offset (releasetime) of the original task
                if (phased_releaseTimes) {
                    // periodStart of the original task is its phase, releaseTime of the original task is equal to the start IntVar - phase
                    int originalTaskPhase = taskList.get(originalTaskID).getPhase();
                    LinearExpr releaseTimeOfOriginalTask = LinearExpr.weightedSum(new IntVar[]{modelTaskMap.get(originalTaskID).getStart(), helper}, new long[]{1, -originalTaskPhase});
                    // calculate the relative offset of this task
                    int phasedPeriodStart = periodStart + originalTaskPhase;
                    LinearExpr relativeReleasetime = LinearExpr.weightedSum(new LinearArgument[]{releaseTimeOfOriginalTask, helper}, new long[]{1, phasedPeriodStart});
                    cspModel.addEquality(modelTask.getStart(), relativeReleasetime);
                    //System.out.println("RT for Task " + task.getIdentifier() + ": originalTask-Phase=" + originalTaskPhase + " originalTask-StartDomain=" + modelTaskMap.get(originalTaskID).start.getDomain() + " periodStart=" + periodStart + " taskStartDomain=" + modelTask.start.getDomain() );
                } else {
                    // periodStart of the original task is 0, releaseTime of the original task is equal to the start IntVar
                    // calculate the relative offset of this task
                    LinearExpr relativeReleasetime = LinearExpr.weightedSum(new IntVar[]{modelTaskMap.get(originalTaskID).getStart(), helper}, new long[]{1, periodStart});
                    cspModel.addEquality(modelTask.getStart(), relativeReleasetime);
                }
                //utils.DebugPrint("added relative releasetime to task " + task.getIdentifier());
                // Deadlines - probably not necessary since deadlines are dependent on start + wcet
                if (phased_releaseTimes) {
                    // periodEnd of the original task is its phase + period, deadline of the original task is equal to the period + phase - end IntVar
                    int originalTaskPeriodEnd = taskList.get(originalTaskID).getPhase() + taskList.get(originalTaskID).getPeriod();
                    LinearExpr deadlineOfOriginalTask = LinearExpr.weightedSum(new IntVar[]{modelTaskMap.get(originalTaskID).getEnd(), helper}, new long[]{-1, originalTaskPeriodEnd});
                    // calculate the relative offset of this task
                    int phasedPeriodEnd = periodEnd + taskList.get(originalTaskID).getPhase();
                    LinearExpr relativeDeadline = LinearExpr.weightedSum(new LinearArgument[]{deadlineOfOriginalTask, helper}, new long[]{-1, phasedPeriodEnd});
                    cspModel.addEquality(modelTask.getEnd(), relativeDeadline);
                    //System.out.println("RT for Task " + task.getIdentifier() + ": originalTask-PeriodEnd=" + originalTaskPeriodEnd + " originalTaskEndDomain=" + modelTaskMap.get(originalTaskID).end.getDomain() + " phasedPeriodEnd=" + phasedPeriodEnd + " taskEndDomain=" + modelTask.end.getDomain() );
                } else {
                    // periodEnd of the original task is its period, deadline of the original task is equal to the end IntVar - period
                    int originalTaskPeriodEnd = taskList.get(originalTaskID).getPeriod();
                    LinearExpr deadlineOfOriginalTask = LinearExpr.weightedSum(new IntVar[]{modelTaskMap.get(originalTaskID).getEnd(), helper}, new long[]{-1, originalTaskPeriodEnd});
                    // calculate the relative offset of this task
                    LinearExpr relativeDeadline = LinearExpr.weightedSum(new LinearArgument[]{deadlineOfOriginalTask, helper}, new long[]{-1, periodEnd});
                    cspModel.addEquality(modelTask.getEnd(), relativeDeadline);
                }
                //utils.DebugPrint("added relative deadline to task " + task.getIdentifier());
            }
        }
    }

    /**
     * Shift values to account for dynamic constraints.
     *
     * @param cspModel           the CPModel object.
     * @param dynamicConstraints the DynamicConstraints object.
     * @param task               the task that is a basis for the Model Task.
     * @param modelTask          the Model Task.
     * @param helper             a helper object that is equal to 1 for linear expressions.
     */
    private static void shiftTaskVariablesAndDynamicConstraints(CpModel cspModel, DynamicConstraints dynamicConstraints, Task task, ModelTask modelTask, IntVar helper) {

        if (!(task instanceof TaskInstance)) {
            // skip task instances, as their start and end values are directly related to the original periodic task
            // Add relationships between Task start and end to CP_RELEASETIME_MIN,
            // CP_RELEASETIME_MAX, CP_DEADLINE_MIN, CP_DEADLINE_MAX

            if (task.getPhase() == 0) {
                cspModel.addGreaterOrEqual(modelTask.getStart(), dynamicConstraints.getCp_releasetime_min());
                cspModel.addGreaterOrEqual(dynamicConstraints.getCp_releasetime_max(), modelTask.getStart());
                cspModel.addGreaterOrEqual(modelTask.getEnd(), dynamicConstraints.getCp_deadline_min());
                cspModel.addGreaterOrEqual(dynamicConstraints.getCp_deadline_max(), modelTask.getEnd());
            } else {
                // add the phase to the min/max releasetime
                LinearExpr startMinOffset = LinearExpr.weightedSum(new IntVar[]{helper, dynamicConstraints.getCp_releasetime_min()}, new long[]{task.getPhase(), 1});
                cspModel.addGreaterOrEqual(modelTask.getStart(), startMinOffset);
                LinearExpr startMaxOffset = LinearExpr.weightedSum(new IntVar[]{helper, dynamicConstraints.getCp_releasetime_max()}, new long[]{task.getPhase(), 1});
                cspModel.addGreaterOrEqual(startMaxOffset, modelTask.getStart());
                // add the phase to the min/max deadline
                LinearExpr endMinOffset = LinearExpr.weightedSum(new IntVar[]{helper, dynamicConstraints.getCp_deadline_min()}, new long[]{task.getPhase(), 1});
                cspModel.addGreaterOrEqual(modelTask.getEnd(), endMinOffset);
                LinearExpr endMaxOffset = LinearExpr.weightedSum(new IntVar[]{helper, dynamicConstraints.getCp_deadline_max()}, new long[]{task.getPhase(), 1});
                cspModel.addGreaterOrEqual(endMaxOffset, modelTask.getEnd());
            }
        }
    }

    /**
     * This function adds Precedence constraints to the model according to the predecessor attribute of each task.
     *
     * @param taskList     List of all tasks
     * @param cspModel     The CP Model Object
     * @param modelTaskMap Map of all CP Model tasks
     */
    public static void addPrecedenceConstraintsToModel(ArrayList<Task> taskList, CpModel cspModel, Map<Integer, ModelTask> modelTaskMap, Utils utils) {

        for (Task task : taskList) {
            if (!task.getPredecessors().isEmpty()) {
                // The Predecessor task must have ended before the successor task can start
                for (Integer predID : task.getPredecessors()) {
                    ModelTask firstModelTask = modelTaskMap.get(task.getIdentifier());
                    ModelTask secondModelTask = modelTaskMap.get(predID);
                    // skip tasks with non-intersecting domains
                    // first task domain ends before the second task domain begins
                    if (firstModelTask.getEnd().getDomain().max() < secondModelTask.getStart().getDomain().min()) {
                        continue;
                    }
                    // first task domain begins after second task domain has ended
                    if (firstModelTask.getStart().getDomain().min() > secondModelTask.getEnd().getDomain().max()) {
                        continue;
                    }
                    cspModel.addGreaterOrEqual(firstModelTask.getStart(), secondModelTask.getEnd());
                    utils.DebugPrint("Debug: added precedence constraint to model. Task: " + task.getIdentifier() + " Pred:" + predID);
                }
            }
        }
    }

    /**
     * Adds hints to the integer variables of all Model Tasks
     *
     * @param cspModel           the CPModel object.
     * @param modelTaskMap       the Hashmap that maps task ids to the Model Tasks
     * @param taskList           the list of tasks.
     * @param limit              the scheduling limit.
     * @param dynamicConstraints the DynamicConstraints object.
     * @param numResources       the number of resources.
     * @param inputs             the InputParameters object.
     * @param utils              the Utils object.
     * @param warnings           the Warnings object.
     * @return the Hints saved in a GenerationResult.
     */
    public static GenerationResult addHintsToModel(CpModel cspModel, Map<Integer, ModelTask> modelTaskMap, ArrayList<Task> taskList, int limit, DynamicConstraints dynamicConstraints, int numResources, InputParameters inputs, Utils utils, ArrayList<String> warnings) {

        GenerationResult generationResult = new GenerationResult();

        // Hints should be as random as possible, while still falling inside the IntVar domains
        for (Task task : taskList) {
            if (task instanceof TaskInstance) continue; // skip periodic task instances
            // Add a random hint to a variable. Hints can contradict each other!
            // WCET
            addHintToTaskWCET(cspModel, modelTaskMap, limit, task, numResources, generationResult, inputs, utils);
            // Start
            //addHintToTaskStart(cspModel, modelTaskMap, Debug, limit, task, random, generationResult, utils, warnings);
            // End
            //addHintToTaskEnd(cspModel, modelTaskMap, Debug, limit, task, random, generationResult, utils, warnings);
            // Resource
            addHintToTaskResource(cspModel, modelTaskMap, task, numResources, generationResult, utils, warnings);
        }

        addHintToDynamicConstraints(cspModel, dynamicConstraints, limit, generationResult, inputs, utils);

        return generationResult;

    }

    /**
     * Adds DecisionStrategies to the CPModel
     *
     * @param cspModel     the CPModel object.
     * @param modelTaskMap the Hashmap that maps task ids to the Model Tasks
     */
    public static void addDecisionStrategies(CpModel cspModel, Map<Integer, ModelTask> modelTaskMap) {

        ArrayList<IntVar> WCETs = new ArrayList<>();
        ArrayList<IntVar> ends = new ArrayList<>();
        modelTaskMap.forEach((Integer i, ModelTask m) -> {
            WCETs.add(m.getWcet());
            ends.add(m.getEnd());
        });
        IntVar[] allWCETs = WCETs.toArray(new IntVar[0]);
        cspModel.addDecisionStrategy(allWCETs, DecisionStrategyProto.VariableSelectionStrategy.CHOOSE_FIRST, DecisionStrategyProto.DomainReductionStrategy.SELECT_MAX_VALUE);
        IntVar[] allEnds = ends.toArray(new IntVar[0]);
        cspModel.addDecisionStrategy(allEnds, DecisionStrategyProto.VariableSelectionStrategy.CHOOSE_FIRST, DecisionStrategyProto.DomainReductionStrategy.SELECT_MAX_VALUE);

    }

    /**
     * Add Dynamic Constraint hints.
     *
     * @param cspModel           The CPModel object.
     * @param dynamicConstraints the dynamic constraints object
     * @param limit              the scheduling limit.
     * @param generationResult   the Generation Result object for saving hints.
     * @param inputs             the InpuParameter object.
     * @param utils              the Utils object.
     */
    private static void addHintToDynamicConstraints(CpModel cspModel, DynamicConstraints dynamicConstraints, int limit, GenerationResult generationResult, InputParameters inputs, Utils utils) {
        // add Hints to dynamic Constraints
        int min_releasetime_hint;
        if (inputs.getMin_releaseTime() != -1) {
            min_releasetime_hint = inputs.getMin_releaseTime();
        } else {
            min_releasetime_hint = utils.random.nextInt(0, limit);
        }
        int max_releasetime_hint;
        if (inputs.getMax_releaseTime() != -1) {
            max_releasetime_hint = inputs.getMax_releaseTime();
        } else {
            max_releasetime_hint = utils.random.nextInt(min_releasetime_hint, limit);
        }
        int min_deadline_hint;
        if (inputs.getMin_deadline() != -1) {
            min_deadline_hint = inputs.getMin_deadline();
        } else {
            min_deadline_hint = utils.random.nextInt(0, limit);
        }
        int max_deadline_hint;
        if (inputs.getMax_deadline() != -1) {
            max_deadline_hint = inputs.getMax_deadline();
        } else {
            max_deadline_hint = utils.random.nextInt(min_deadline_hint, limit);
        }
        int min_wcet_hint = 1;
        if (inputs.getMin_WCET() != -1) {
            min_wcet_hint = inputs.getMin_WCET();
        } else {
            while (utils.random.nextInt(2) == 0 && min_wcet_hint < limit - 1) {     // WCET geometric distributed around 1
                min_wcet_hint++;
            }
        }
        int max_wcet_hint;
        if (inputs.getMax_WCET() != -1) {
            max_wcet_hint = inputs.getMax_WCET();
        } else {
            if (min_wcet_hint < limit) {
                max_wcet_hint = utils.random.nextInt(min_wcet_hint, limit);
            } else {
                max_wcet_hint = limit;
            }
        }

        utils.DebugPrint("\nDynamic Constraint Hints:");
        utils.DebugPrint("min_releasetime_hint: " + min_releasetime_hint);
        utils.DebugPrint("max_releasetime_hint: " + max_releasetime_hint);
        utils.DebugPrint("min_deadline_hint: " + min_deadline_hint);
        utils.DebugPrint("max_deadline_hint: " + max_deadline_hint);
        utils.DebugPrint("min_wcet_hint: " + min_wcet_hint);
        utils.DebugPrint("max_wcet_hint: " + max_wcet_hint);

        cspModel.addHint(dynamicConstraints.getCp_releasetime_min(), min_releasetime_hint);
        cspModel.addHint(dynamicConstraints.getCp_releasetime_max(), max_releasetime_hint);
        cspModel.addHint(dynamicConstraints.getCp_deadline_min(), min_deadline_hint);
        cspModel.addHint(dynamicConstraints.getCp_deadline_max(), max_deadline_hint);
        cspModel.addHint(dynamicConstraints.getCp_wcet_min(), min_wcet_hint);
        cspModel.addHint(dynamicConstraints.getCp_wcet_max(), max_wcet_hint);

        ArrayList<Integer> hints = new ArrayList<>();
        hints.add(min_releasetime_hint);
        hints.add(max_releasetime_hint);
        hints.add(min_deadline_hint);
        hints.add(max_deadline_hint);
        hints.add(min_wcet_hint);
        hints.add(max_wcet_hint);

        generationResult.dynamicHints = hints;
    }

    /**
     * Add Hints to WCETs of Model Tasks
     *
     * @param cspModel         the CPModel object.
     * @param modelTaskMap     the Model Task map.
     * @param limit            the scheduling limit.
     * @param task             the task to which a hint will be assigned.
     * @param numResources     the number of resources.
     * @param generationResult the Generation Result object.
     * @param inputs           the Input Parameter object.
     * @param utils            the Utils object.
     */
    private static void addHintToTaskWCET(CpModel cspModel, Map<Integer, ModelTask> modelTaskMap, int limit, Task task, int numResources, GenerationResult generationResult, InputParameters inputs, Utils utils) {
        // add random hint for WCET
        int hint;
        if (task.isPeriodic()) {
            // WCET hint has to fall within [1,period]

            int lowerbound = 1;
            if (task.getSubGraphLength() == 1) {
                int upperbound = task.getPeriod();
                if (inputs.getMax_WCET() != -1 && inputs.getMax_WCET() < task.getPeriod()) {
                    upperbound = inputs.getMax_WCET();
                }
                hint = utils.random.nextInt(lowerbound, upperbound + 1);  //+1 is necessary since bound is exclusive
            } else {
                // the task is in a subgraph, choose a hint within [1, period_length / subgraph_length]
                int upperbound = task.getPeriod() / task.getSubGraphLength();
                if (inputs.getMax_WCET() != -1 && inputs.getMax_WCET() < upperbound) {
                    upperbound = inputs.getMax_WCET();
                }
                if (upperbound < lowerbound) upperbound = lowerbound;
                hint = utils.random.nextInt(lowerbound, upperbound + 1);
                //System.out.println("Added Hint " + hint + " with upperbound " + upperbound);
            }

        } else {
            // Task is not periodic, add a hint for WCET with domain [1,limit*#Resources/#tasks]
            int upperBound = Math.max(1, (limit * numResources) / modelTaskMap.values().size());
            if (inputs.getMax_WCET() != -1 && inputs.getMax_WCET() < upperBound) {
                upperBound = inputs.getMax_WCET();
            }
            hint = utils.random.nextInt(1, upperBound + 1);      //+1 is necessary since bound is exclusive
            utils.DebugPrint("Added Hint to task " + task.getIdentifier() + ": wcet=" + hint + " upperbound:" + upperBound);
        }

        cspModel.addHint(modelTaskMap.get(task.getIdentifier()).getWcet(), hint);
        generationResult.wcetHints.add(hint);
    }

    @Deprecated
    private static void addHintToTaskStart(CpModel cspModel, Map<Integer, ModelTask> modelTaskMap, int limit, Task task, GenerationResult generationResult, Utils utils, ArrayList<String> warnings) {

        int startHint;
        if (task.isPeriodic()) {
            // for periodic tasks, hint at a start time within their respective period

            if (task.getPhase() > 0) {
                // task has a phased releasetime, hint at a value within the tasks' period, after it is released
                utils.DebugPrint("phased releasetime: " + task.getPhase() + " period: " + task.getPeriod());
                // the domain of the task's start variable is [phase, phase+period-1]
                startHint = utils.random.nextInt(task.getPhase(), task.getPeriod() + task.getPhase());
            } else {
                // the domain of the tasks start variable is [0, period-1]
                startHint = utils.random.nextInt(0, task.getPeriod());
            }

        } else {
            // for aperiodic tasks, hint at a start time within the domain
            startHint = RandomNumberGeneration.getNumWithDistribution(0, limit - 1, InputParameters.Distribution.GEOMETRIC, utils, 0.0, -1, -1, warnings);
        }
        // add the hint
        cspModel.addHint(modelTaskMap.get(task.getIdentifier()).getStart(), startHint);
        generationResult.startHints.add(startHint);
        //utils.DebugPrint("Added Hint to task " + task.getIdentifier() + ": start=" + startHint);
    }

    @Deprecated
    private static void addHintToTaskEnd(CpModel cspModel, Map<Integer, ModelTask> modelTaskMap, int limit, Task task, GenerationResult generationResult, Utils utils, ArrayList<String> warnings) {

        int endHint;
        if (task.isPeriodic()) {
            // for periodic tasks, hint at an end time within their respective period

            if (task.getPhase() > 0) {
                // task has a phased releasetime, hint at a value within the tasks' period, after it is released
                utils.DebugPrint("phased releasetime: " + task.getPhase() + " period: " + task.getPeriod());
                // the domain of the task end variable is [phase+1, phase+period]
                endHint = utils.random.nextInt(task.getPhase() + 1, task.getPeriod() + task.getPhase() + 1);
            } else {
                // the domain of the task end variable is [1, period]
                endHint = utils.random.nextInt(1, task.getPeriod() + 1);
            }

        } else {
            // for aperiodic tasks, hint at an end time within the domain
            endHint = RandomNumberGeneration.getNumWithDistribution(1, limit, InputParameters.Distribution.GEOMETRIC, utils, 0.0, -1, -1, warnings);
        }
        // add the hint
        cspModel.addHint(modelTaskMap.get(task.getIdentifier()).getEnd(), endHint);
        generationResult.endHints.add(endHint);
        //utils.DebugPrint("Added Hint to task " + task.getIdentifier() + ": end=" + endHint);
    }

    /**
     * Adds Hints to the resource integer variable of Model Tasks
     *
     * @param cspModel         the CPModel object.
     * @param modelTaskMap     the Model Task map.
     * @param task             the task to which a hint will be assigned.
     * @param numResources     the number of resources.
     * @param generationResult the GenerationResult object.
     * @param utils            the Utils object.
     * @param warnings         the Warnings object.
     */
    private static void addHintToTaskResource(CpModel cspModel, Map<Integer, ModelTask> modelTaskMap, Task task, int numResources, GenerationResult generationResult, Utils utils, ArrayList<String> warnings) {
        // add random hint for Resource
        int resourceHint;

        if (task.getResourceConstraint().isEmpty()) {
            // task is not residency constrained
            resourceHint = RandomNumberGeneration.getNumWithDistribution(0, numResources - 1, InputParameters.Distribution.UNIFORM, utils, 0.0, -1, -1, warnings);
        } else {
            if (task.getResourceConstraint().size() == 1) {
                // task can only be scheduled on 1 resource
                resourceHint = task.getResourceConstraint().get(0);
            } else {
                // task can be scheduled on multiple resources, hint at one randomly
                int field = RandomNumberGeneration.getNumWithDistribution(0, task.getResourceConstraint().size() - 1, InputParameters.Distribution.UNIFORM, utils, 0.0, -1, -1, warnings);
                resourceHint = task.getResourceConstraint().get(field);
            }
        }
        cspModel.addHint(modelTaskMap.get(task.getIdentifier()).getResource(), resourceHint);
        generationResult.resourceHints.add(resourceHint);
        //utils.DebugPrint("added Hint to task " + task.getIdentifier() + ": resource=" + resourceHint );
    }

    /***
     * Sets solver parameters
     * @param solver the Solver object.
     * @param numberOfWorkerThreads the number of worker threads specified by the user.
     * @param solutionCount the number of sultions that should be found by the solver.
     * @param utils the Utils object.
     */
    public static void setSolverParameters(CpSolver solver, int numberOfWorkerThreads, int solutionCount, Utils utils) {

        // Set the Search Strategy
        solver.getParameters().setRandomizeSearch(true);
        solver.getParameters().setSearchBranching(SatParameters.SearchBranching.HINT_SEARCH);

        // repair Hints
        solver.getParameters().setRepairHint(true);
        solver.getParameters().setHintConflictLimit(10000);
        // solver.getParameters().setFixVariablesToTheirHintedValue(true);      // forces variables to be their hint value

        // Asks the Solver not to pre-solve the Model
        solver.getParameters().setCpModelPresolve(false);
        solver.getParameters().setKeepAllFeasibleSolutionsInPresolve(true);
        solver.getParameters().setUseSatInprocessing(false);

        // Use multiple workers or enumerate all solutions
        if (numberOfWorkerThreads != 1) {
            solver.getParameters().setNumWorkers(numberOfWorkerThreads);
        } else {
            solver.getParameters().setEnumerateAllSolutions(true);
        }

        solver.getParameters().setFillAdditionalSolutionsInResponse(true);
        solver.getParameters().setSolutionPoolSize(solutionCount);
        // Log the solver to std::out
        if (utils.debug) {
            solver.getParameters().setLogSearchProgress(true);
            solver.getParameters().setLogToStdout(true);
        }
    }

    /**
     * Adds a WCET objective for maximizing the sum of all WCETs integer variables.
     *
     * @param modelTaskMap the Model Task Map.
     * @param cspModel     the CP Model object.
     */
    public static void addWCETObjective(Map<Integer, ModelTask> modelTaskMap, CpModel cspModel) {
        // Maximize the WCET of all tasks -> makes the task set more difficult

        ArrayList<IntVar> WCETs = new ArrayList<>();
        for (ModelTask m : modelTaskMap.values()) {
            WCETs.add(m.getWcet());
        }
        LinearExpr totalWCET = LinearExpr.sum(WCETs.toArray(new IntVar[0]));
        cspModel.maximize(totalWCET);               // maximize the WCET of all tasks

        System.out.println("Added Objective to maximize WCETs.");
    }

    @Deprecated
    public static void addScheduleLengthObjective(int limit, ArrayList<Task> taskList, CpModel cspModel, Map<Integer, ModelTask> modelTaskMap) {

        IntVar objVar = cspModel.newIntVar(0, limit, "totalLength");
        List<IntVar> ends = new ArrayList<>();
        for (Task task : taskList) {
            ends.add(modelTaskMap.get(task.getIdentifier()).getEnd());
        }
        cspModel.addMaxEquality(objVar, ends);
        cspModel.minimize(objVar);                // minimize the length of the schedule
        //cspModel.minimize(dynamicConstraints[3]); // minimize the maximum deadline of non-periodic tasks and the first instance of periodic tasks
    }
}
