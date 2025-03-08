package dt.tsg.utils;

import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import dt.tsg.cspModel.DynamicConstraints;
import dt.tsg.cspModel.ModelTask;
import dt.tsg.task.Task;
import dt.tsg.task.TaskInstance;

import java.util.*;
import java.util.stream.IntStream;

public class Utils {

    public boolean debug;
    public Random random;

    public Utils(boolean debug, Random random) {
        this.debug = debug;
        this.random = random;
    }

    /**
     * Defines a task scheduled by the solver.
     *
     * @param taskID      the task id.
     * @param start       the time at which the scheduled task begins.
     * @param end         the time at which the scheduled task ends.
     * @param wcet        the wcet of the task.
     * @param resource    the resource on which the task is scheduled.
     * @param predecessor the predecessor ids of the task.
     * @param period      the period of the task.
     */
    public record ScheduledTask(int taskID, int start, int end, int wcet, int resource, int[] predecessor, int period) {


        /**
         * Reads the actual values for the task from the scheduled task.
         *
         * @param taskList            the list of tasks.
         * @param phased_releaseTimes controls the generation of phases.
         * @param st                  the scheduled tasks from which the values are read.
         */
        public static void readScheduledTaskValues(ArrayList<Task> taskList, boolean phased_releaseTimes, ScheduledTask st) {
            Task actualtask = taskList.get(st.taskID());

            if (!(actualtask instanceof TaskInstance)) {
                // skip all taskInstances since they will not be part of the finished task set
                readReleaseTimes(phased_releaseTimes, st, actualtask);

                readDeadlines(phased_releaseTimes, st, actualtask);
            }
            actualtask.setWCET(st.wcet());  // read WCET
        }

        /**
         * Reads the deadline from the scheduled task and adds it to the actual task.
         *
         * @param phased_releaseTimes controls the generation of phases.
         * @param st                  the scheduled task.
         * @param actualtask          the actual task to which the deadline is assigned
         */
        private static void readDeadlines(boolean phased_releaseTimes, ScheduledTask st, Task actualtask) {
            // read deadlines
            int deadline;
            if (actualtask.isPeriodic()) {
                // task is periodic, we need to take phased releasetimes into account
                if (phased_releaseTimes) {
                    // deadline of task is end time - phase
                    int phase = actualtask.getPhase();
                    deadline = st.end() - phase;
                } else {
                    // deadline of task is end tim
                    deadline = st.end();
                }
            } else {
                // task is aperiodic, we can simply read the releasetime from the scheduled task
                deadline = st.end();
            }
            if (actualtask.getDeadline() != 0)
                throw new RuntimeException("ERROR: Expected deadline of task to be 0. TaskID: " + actualtask.getIdentifier() + " deadline: " + actualtask.getDeadline());
            actualtask.setDeadline(deadline);
            //System.out.println("read deadline \"" + deadline + "\" for task " + actualtask.getIdentifier());
        }

        /**
         * Reads the release time from the scheduled task and adds it to the actual task.
         *
         * @param phased_releaseTimes controls the generation of phases.
         * @param st                  the scheduled task.
         * @param actualtask          the actual task to which the deadline is assigned.
         */
        private static void readReleaseTimes(boolean phased_releaseTimes, ScheduledTask st, Task actualtask) {
            // read releasetimes
            int releasetime;
            if (actualtask.isPeriodic()) {
                // task is periodic, we need to take phased releasetimes into account
                if (phased_releaseTimes) {
                    // releaseTime of the task is Start Time - phase
                    int phase = actualtask.getPhase();
                    releasetime = st.start() - phase;
                } else {
                    // releaseTime of the task is the Start Time since the task has no phase
                    releasetime = st.start();
                }
            } else {
                // task is aperiodic, we can simply read the releasetime from the scheduled task
                releasetime = st.start();
            }
            if (actualtask.getReleaseTime() != 0)
                throw new RuntimeException("ERROR: Expected releasetime of task to be 0. TaskID: " + actualtask.getIdentifier() + " releaseTime: " + actualtask.getReleaseTime());
            actualtask.setReleaseTime(releasetime);
            //System.out.println("read releasetime \"" + releasetime + "\" for task " + actualtask.getIdentifier());
        }

        /**
         * Builds a list of scheduled tasks from the solver result.
         *
         * @param taskList       the list of tasks.
         * @param solver         the solver object.
         * @param modelTaskMap   maps the task ids to the corresponding Model Tasks.
         * @param scheduledTasks the scheduled task list.
         */
        public static void buildScheduledTaskList(ArrayList<Task> taskList, CpSolver solver, Map<Integer, ModelTask> modelTaskMap, ArrayList<ScheduledTask> scheduledTasks) {
            for (int taskID = 0; taskID < taskList.size(); ++taskID) {
                ScheduledTask scheduledTask = new ScheduledTask(
                        taskID,
                        (int) solver.value(modelTaskMap.get(taskID).getStart()),
                        (int) solver.value(modelTaskMap.get(taskID).getEnd()),
                        (int) solver.value(modelTaskMap.get(taskID).getWcet()),
                        (int) solver.value(modelTaskMap.get(taskID).getResource()),
                        taskList.get(taskID).getPredecessors().stream().mapToInt(i -> i).toArray(),
                        taskList.get(taskID).getPeriod());
                scheduledTasks.add(scheduledTask);
            }
        }
    }

    /**
     * Simple Comparator for sorting the scheduled tasks.
     */
    static class SortTasks implements Comparator<ScheduledTask> {
        @Override
        public int compare(ScheduledTask a, ScheduledTask b) {
            if (a.start != b.start) {
                return a.start - b.start;
            } else {
                return a.wcet - b.wcet;
            }
        }
    }

    /**
     * Prints the content of String if this classes Debug boolean is true (default:false).
     *
     * @param string Content to be printed.
     */
    public void DebugPrint(String string) {
        if (this.debug) {
            System.out.println(string);
        }
    }

    /**
     * Prints the schedule in the console.
     *
     * @param limit              the scheduling limit.
     * @param status             the solver status.
     * @param taskList           the list of tasks.
     * @param solver             the solver object.
     * @param modelTaskMap       maps the task ids to the corresponding Model Tasks.
     * @param dynamicConstraints the dynamic Constraints object.
     * @param numResou           the number of resources.
     */
    public void PrintSchedule(int limit, CpSolverStatus status, ArrayList<Task> taskList, CpSolver solver, Map<Integer, ModelTask> modelTaskMap, DynamicConstraints dynamicConstraints, int numResou) {

        // Print Scheduling Limit and Solution status
        System.out.println("\nLimit: " + limit);
        System.out.println("Solution: " + status);

        final int[] allResources = IntStream.range(0, numResou).toArray();
        Map<Integer, List<ScheduledTask>> scheduledTasksPerResource = new HashMap<>();
        ArrayList<ScheduledTask> scheduledTasks = new ArrayList<>();
        ScheduledTask.buildScheduledTaskList(taskList, solver, modelTaskMap, scheduledTasks);
        for (int taskID = 0; taskID < taskList.size(); ++taskID) {
            // skip assigning the scheduled task to resource if IntVar ID is invalid
            if (scheduledTasks.get(taskID).resource < 0) try {
                throw new Exception("ERROR: TaskID " + taskID + " is scheduled on resource " + scheduledTasks.get(taskID).resource);
            } catch (Exception e) {
                continue;   //skips the following lines
            }
            // assign task to the correct resource
            scheduledTasksPerResource.computeIfAbsent(scheduledTasks.get(taskID).resource, (Integer k) -> new ArrayList<>());
            scheduledTasksPerResource.get(scheduledTasks.get(taskID).resource).add(scheduledTasks.get(taskID));
        }

        // Print dynamic Constraints
        dynamicConstraints.printSolvedDynamicConstraints(solver);

        // Create per resource output lines
        StringBuilder output = new StringBuilder();
        for (int res : allResources) {
            // Sort by starting time
            if (scheduledTasksPerResource.get(res) != null) {
                // Build lines per resource
                scheduledTasksPerResource.get(res).sort(new SortTasks());
                StringBuilder taskNameLine = new StringBuilder("Resource " + res + ":       ");
                StringBuilder intervalLine = new StringBuilder("--Schedule:      ");
                StringBuilder domainLine = new StringBuilder("--Domain:        ");
                StringBuilder cpdomainLine = new StringBuilder("--CP-Domain:     ");
                StringBuilder phasePeriodLine = new StringBuilder("--Phase/Period:  ");
                StringBuilder predecessorLine = new StringBuilder("--Predecessor:   ");

                for (ScheduledTask assignedTask : scheduledTasksPerResource.get(res)) {
                    // Generate Name Line
                    String nameLineTmp = getTaskNames(taskList, assignedTask);
                    taskNameLine.append(String.format("%-20s", nameLineTmp));
                    // Generate Interval Line
                    String intervTmp =
                            "[" + assignedTask.start + "," + (assignedTask.start + assignedTask.wcet) + "]";
                    if (taskList.get(assignedTask.taskID) instanceof TaskInstance) {
                        intervTmp += " Orig:" + ((TaskInstance) taskList.get(assignedTask.taskID)).getOriginal_task_ID();
                    }
                    intervalLine.append(String.format("%-20s", intervTmp));
                    // Generate domain lines
                    Task task = taskList.get(assignedTask.taskID);
                    int periodStart;
                    int periodEnd;
                    if (task instanceof TaskInstance) {
                        int originalTaskPhase = taskList.get(((TaskInstance) task).getOriginal_task_ID()).getPhase();
                        // the task must start and end within the period
                        periodStart = ((TaskInstance) task).getInstance_number() * task.getPeriod() + originalTaskPhase;
                        periodEnd = (((TaskInstance) task).getInstance_number() + 1) * task.getPeriod() + originalTaskPhase;
                    } else {
                        periodStart = task.getPhase();
                        periodEnd = task.getPhase() + task.getPeriod();
                    }
                    String domainTmp = "[" + periodStart + "," + periodEnd + "]";
                    domainLine.append(String.format("%-20s", domainTmp));
                    String cpdomainTmp = "[" + modelTaskMap.get(assignedTask.taskID).getStart().getDomain().min() + "," + modelTaskMap.get(assignedTask.taskID).getEnd().getDomain().max() + "]";
                    cpdomainLine.append(String.format("%-20s", cpdomainTmp));
                    // Generate phase/period line
                    String phasePeriod = taskList.get(assignedTask.taskID).getPhase() + "/" + assignedTask.period;
                    phasePeriodLine.append(String.format("%-20s", phasePeriod));
                    // Generate predecessor line
                    String predecessors = Arrays.toString(Arrays.stream(assignedTask.predecessor).toArray());
                    predecessorLine.append(String.format("%-20s", predecessors));
                }
                // add lines to output
                output.append(taskNameLine).append("%n");
                output.append(domainLine).append("%n");
                output.append(cpdomainLine).append("%n");
                output.append(intervalLine).append("%n");
                output.append(phasePeriodLine).append("%n");
                output.append(predecessorLine).append("%n%n");
            } else {
                output.append("======================Empty resource: ").append(res).append("======================\n");
            }
        }
        // print generated output
        System.out.printf("\nSolver objective value: %f%n\n", solver.objectiveValue());
        System.out.printf(output.toString());

    }

    /**
     * Returns the name of the scheduled task as a string. The name is "Task(ID)".
     *
     * @param taskList      the list of tasks.
     * @param scheduledTask the scheduled task in question.
     * @return the name of the scheduled task.
     */
    private static String getTaskNames(ArrayList<Task> taskList, ScheduledTask scheduledTask) {
        String name = "Task(" + scheduledTask.taskID + ")";
        if (taskList.get(scheduledTask.taskID).isPeriodic()) {
            //task is periodic, add its instanceNumber
            if (taskList.get(scheduledTask.taskID) instanceof TaskInstance) {
                name += "_" + ((TaskInstance) taskList.get(scheduledTask.taskID)).getOriginal_task_ID() + ":" + ((TaskInstance) taskList.get(scheduledTask.taskID)).getInstance_number();
            } else {
                name += "_" + scheduledTask.taskID + ":0";        //original periodic task has implicit instanceNumber 0
            }
        }
        return name;
    }

}
