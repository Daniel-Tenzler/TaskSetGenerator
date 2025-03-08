package dt.tsg.residencyConstraints;

import dt.tsg.InputParams.InputParameters;
import dt.tsg.task.Task;
import dt.tsg.task.TaskInstance;
import dt.tsg.utils.RandomNumberGeneration;
import dt.tsg.utils.Utils;

import java.util.*;
import java.util.stream.IntStream;

public class ResidencyConstraints {

    /**
     * Adds residency constraints to tasks using our default approach.
     *
     * @param numResid        The number of residency constraints we try to hit.
     * @param numResou        The number of resources.
     * @param inputs          The Input Parameter object.
     * @param multi_residency boolean that controls the generation of multiple residency constraints.
     * @param taskList        the list of tasks.
     * @param utils           the Utils object.
     * @param warnings        the Warnings object.
     */
    public static void addResidencyConstraintsToTasks(int numResid, int numResou, InputParameters inputs, boolean multi_residency, ArrayList<Task> taskList, Utils utils, ArrayList<String> warnings) {
        int currentNumResid = 0;
        ArrayList<Integer> taskIDList = new ArrayList<>();
        for (Task task : taskList) {
            taskIDList.add(task.getIdentifier());
        }
        while (currentNumResid < numResid) {
            // Select a random Task from the taskIDList
            int field = RandomNumberGeneration.getNumWithDistribution(0, taskIDList.size() - 1, InputParameters.Distribution.UNIFORM, utils, 0.0, 0, 0, warnings);
            int taskID = taskIDList.get(field);

            // Select which resource this task must be scheduled on
            if (multi_residency) {
                // choose the number of resources that the task will be constrained to
                int numberOfResourceConstraints = utils.random.nextInt(1, numResou + 1);
                // select the specific resource IDs
                ArrayList<Integer> resourceIds = new ArrayList<>();
                while (resourceIds.size() < numberOfResourceConstraints) {
                    int resourceID = RandomNumberGeneration.getNumWithDistribution(0, numResou - 1, inputs.getResidency_distribution(), utils, inputs.getResidencyPoissonMean(), inputs.getResidencyBinomialP(), inputs.getResidencyBinomialN(), warnings);
                    if (!resourceIds.contains(resourceID)) {
                        resourceIds.add(resourceID);
                    }

                }
                // add the constraints
                taskList.get(taskID).addResourceConstraint(resourceIds);

            } else {
                // select only one resource to which this task will be constrained to
                int resourceID = RandomNumberGeneration.getNumWithDistribution(0, numResou - 1, inputs.getResidency_distribution(), utils, inputs.getResidencyPoissonMean(), inputs.getResidencyBinomialP(), inputs.getResidencyBinomialN(), warnings);
                // add the constraint
                taskList.get(taskID).addResourceConstraint(resourceID);
            }
            // remove the task from the taskIDList
            currentNumResid++;
            taskIDList.remove(field);
        }
    }

    /**
     * Adds residency constraints to tasks using the posthoc residency approach.
     *
     * @param taskList                  the list of tasks.
     * @param numResou                  the number of resources.
     * @param scheduledTasks            the list of scheduled tasks.
     * @param multi_residency           boolean that controls the generation of multiple residency constraints.
     * @param min_residency_constraints the minimum number of residency constraints as desired by the user.
     * @param max_residency_constraints the maximum number of residency constraints as desired by the user.
     * @param inputs                    the input parameters object.
     * @param warnings                  the warnings object.
     * @param utils                     the utils object.
     * @param solutionNum               the number of the current solution. For debugging purposes.
     * @return the number of resources after all empty resources were removed.
     */
    public static int addPostHocResidencyConstraints(ArrayList<Task> taskList, int numResou, ArrayList<Utils.ScheduledTask> scheduledTasks, boolean multi_residency, int min_residency_constraints, int max_residency_constraints, InputParameters inputs, ArrayList<String> warnings, Utils utils, int solutionNum) {

        // instantly return if the desired maximum number of residency constraints is 0
        if (max_residency_constraints == 0) return numResou;

        Random random = new Random();

        // collect all possible residency constraints and store them in the lists
        ArrayList<Integer> nonEmptyResources = new ArrayList<>();
        Map<Integer, ArrayList<Integer>> resourceToTasks = new HashMap<>();
        Map<Integer, Integer> taskIDsToResources = new HashMap<>();
        int currentNumberOfResidencyConstraints = 0;
        ArrayList<Integer> resourcesWithActiveResidencyConstraints = new ArrayList<>();
        for (Utils.ScheduledTask st : scheduledTasks) {
            // read residency constraints from schedule
            resourceToTasks.putIfAbsent(st.resource(), new ArrayList<>());
            resourceToTasks.get(st.resource()).add(st.taskID());
            taskIDsToResources.put(st.taskID(), st.resource());
            // add resource to the non-empty resources list if it is not already in it
            if (!nonEmptyResources.contains(st.resource())) nonEmptyResources.add(st.resource());
        }

        // create a list for all taskIds, we use this list to add residency constraints to random tasks
        ArrayList<Integer> taskIDList = new ArrayList<>();
        for (Task task : taskList) {
            if (task instanceof TaskInstance) continue;
            taskIDList.add(task.getIdentifier());
        }
        // while the desired number of residency constraints is not reached, choose a random task from the taskIDList and add a residency constraint to it
        while (currentNumberOfResidencyConstraints <= max_residency_constraints && currentNumberOfResidencyConstraints < taskList.size()) {
            if (max_residency_constraints == currentNumberOfResidencyConstraints) {
                // if a maximum limit is set, we break after reaching it
                System.out.println("\nFinished adding postHoc residency constraints, added " + currentNumberOfResidencyConstraints + " residency constraints.\n");
                break;
            }
            if (taskIDList.isEmpty()) {
                System.out.println("\n[Solution #" + solutionNum + "] WARNING: TaskIDList is empty, we cannot add any more residency constraints to the task set.\nCurrent Number of residency constraints: " + currentNumberOfResidencyConstraints + "\nDesired Number of Residency constraints: [" + min_residency_constraints + "," + max_residency_constraints + "]\n");
                warnings.add("[Solution #" + solutionNum + "] WARNING: TaskIDList is empty, we cannot add any more residency constraints to the task set.\nCurrent Number of residency constraints: " + currentNumberOfResidencyConstraints + "\nDesired Number of Residency constraints: [" + min_residency_constraints + "," + max_residency_constraints + "]\n");
                break;
            }
            // get a random taskID from the taskIDList
            int field = RandomNumberGeneration.getNumWithDistribution(0, taskIDList.size() - 1, inputs.getResidency_distribution(), utils, inputs.getResidencyPoissonMean(), inputs.getResidencyBinomialP(), inputs.getResidencyBinomialN(), warnings);
            int taskID = taskIDList.remove(field);
            // get the corresponding task and resource
            Task task = taskList.get(taskID);
            int resourceId = taskIDsToResources.get(taskID);
            // add the residency constraint
            task.addResourceConstraint(resourceId);
            task.setResidency_constrained(true);
            if (!resourcesWithActiveResidencyConstraints.contains(resourceId))
                resourcesWithActiveResidencyConstraints.add(resourceId);
            utils.DebugPrint("Added postHoc residency constraint to task " + task.getIdentifier() + ": " + resourceId);
            // add residency constraints to task instances if the task is periodic
            if (task.isPeriodic()) {
                for (Task taskInstance : taskList) {
                    if (taskInstance instanceof TaskInstance) {
                        if (((TaskInstance) taskInstance).getOriginal_task_ID() == taskID) {
                            // set residency constraint for corresponding task instances
                            taskInstance.setResidency_constrained(true);
                            taskInstance.addResourceConstraint(task.getResourceConstraint());
                        }
                    }
                }
            }
            currentNumberOfResidencyConstraints++;
        }

        // remove empty resources and rename them
        int numberOfActiveResources = resourcesWithActiveResidencyConstraints.size();
        utils.DebugPrint("numberOfActiveResources " + numberOfActiveResources);
        if (numberOfActiveResources != numResou) {
            // not all resources are active, rename residency constraints appropriately
            int newResourceID = 0;
            for (int resourceID : resourcesWithActiveResidencyConstraints) {
                for (int taskID : resourceToTasks.get(resourceID)) {
                    if (taskList.get(taskID).isResidency_constrained()) {
                        // assign all the tasks the same new resource ID
                        taskList.get(taskID).removeResourceConstraints();
                        taskList.get(taskID).addResourceConstraint(newResourceID);
                        taskList.get(taskID).setResidency_constrained(true);
                        utils.DebugPrint("renamed residency constraint of task " + taskID + " to " + newResourceID);
                    }
                }
                newResourceID++;
            }
        }

        // to guarantee schedulability, we set the formatted number of resources to the number of non-empty resources.
        int formattedTaskSetNumberOfResources = nonEmptyResources.size();
        utils.DebugPrint("formatted number of resources: " + formattedTaskSetNumberOfResources);

        utils.DebugPrint("\nAll residency constraints:");
        if (utils.debug)
            taskList.forEach(e -> System.out.println(e.getIdentifier() + ": " + e.getResourceConstraint()));

        // multi residency constraints
        if (multi_residency) {
            // add random additional resources to the original tasks and their periodic task instances
            for (Task task : taskList) {
                if (!(task instanceof TaskInstance)) {
                    if (task.getResourceConstraint().isEmpty()) continue;
                    // task is an original task, calculate multi residency constraints
                    int numberOfResourcesForThisTask = random.nextInt(1, formattedTaskSetNumberOfResources + 1);
                    if (numberOfResourcesForThisTask == 1)
                        continue; // if this task only has 1 residency constraint, we can skip all the following lines
                    // build an array of resources to which this task is not constrained
                    int[] resourceArray = IntStream.range(0, formattedTaskSetNumberOfResources).toArray();
                    ArrayList<Integer> resourceList = new ArrayList<>();
                    for (int i : resourceArray) {
                        // don't add the resource to the list if the task is constrained to it
                        if (i == task.getResourceConstraint().get(0)) continue;
                        resourceList.add(i);
                    }
                    // add random resources from the list to the task's resource constraint list
                    for (int i = 1; i < numberOfResourcesForThisTask; i++) {
                        if (resourceList.isEmpty()) {
                            throw new RuntimeException("ResourceList Array is empty before we could add all of the desired residency constraints!\nTotal number of resources: " + formattedTaskSetNumberOfResources + "\nNumber of residency constraints for this task: " + i + "\nDesired number of residency constraints for this task: " + numberOfResourcesForThisTask);
                        }
                        int field = RandomNumberGeneration.getNumWithDistribution(0, resourceList.size() - 1, InputParameters.Distribution.UNIFORM, utils, 0.0, -1, -1, warnings); // TODO implement distribution
                        int resourceID = resourceList.remove(field);
                        task.addResourceConstraint(resourceID);
                        utils.DebugPrint("Added resource constraint " + resourceID + " to task " + task.getIdentifier());
                    }

                } else {
                    // task is a periodic task instance, copy the residency constraints from the original task
                    Task originalTask = taskList.get(((TaskInstance) task).getOriginal_task_ID());
                    for (int resourceID : originalTask.getResourceConstraint()) {
                        if (!task.getResourceConstraint().contains(resourceID)) {
                            task.addResourceConstraint(resourceID);
                        }

                    }
                    utils.DebugPrint("Added resource constraints to task instance " + task.getIdentifier() + ": " + task.getResourceConstraint());
                }
            }
        }

        if (currentNumberOfResidencyConstraints < min_residency_constraints) {
            System.out.println("\n[Solution #" + solutionNum + "] WARNING: We were only able to add " + currentNumberOfResidencyConstraints + " residency constraints with postHoc residency generation!\n");
            warnings.add("[Solution #" + solutionNum + "] WARNING: We were only able to add " + currentNumberOfResidencyConstraints + " residency constraints with postHoc residency generation!\n");
        }

        return formattedTaskSetNumberOfResources;
    }

    /**
     * Approximates the width of the taskLists precedence graph using the successor relationships between tasks.
     * The function calculates the sizes of all levels for a subtree, then adds the size of the largest level to the total width.
     * This is repeated for all subtrees.
     * The returned value is at least the actual width of the tree, but may be higher depending on the precedence relations.
     *
     * @param taskList    List of Tasks.
     * @param subgraphMap Maps subgraphs to taskIds that belong to the subgraph.
     * @param utils       Utils object
     * @return at least the actual width of the graph
     */
    public static int calculateWidth(List<Task> taskList, Map<Integer, ArrayList<Integer>> subgraphMap, Utils utils) {
        utils.DebugPrint(subgraphMap.toString());
        int totalWidth = 0;
        // iterate through all subgraphs
        for (ArrayList<Integer> subgraphTasks : subgraphMap.values()) {
            int maxWidth = 0;
            ArrayList<ArrayList<Integer>> tasksPerDepth = new ArrayList<>();
            // iterate through all tasks within this subgraph
            for (Integer task : subgraphTasks) {
                // if the task has no predecessors, fill the tasksPerDepth List
                if (taskList.get(task).getPredecessors().isEmpty()) {// TODO in order to not overestimate the actual width of the tree, we should remove taskIDs from the depthLists if they also occur on a higher depth
                    fillTasksPerDepthList(taskList.get(task), taskList, tasksPerDepth, 0);
                    utils.DebugPrint("Array for task " + task + ": " + tasksPerDepth);
                }

            }
            // the width of the subtree is the largest list in the tasksPerDepth List
            for (ArrayList<Integer> taskLists : tasksPerDepth) {
                maxWidth = Math.max(maxWidth, taskLists.size());
            }
            totalWidth += maxWidth;
            utils.DebugPrint("Subtree has a width of " + maxWidth);
        }
        utils.DebugPrint("Found total width of " + totalWidth);
        return totalWidth;
    }

    /**
     * Fills the depth list with appropriate tasks. A task will be placed in the depth list depending on its predecessors.
     * All tasks with no predecessors have depth 0. The direct successors of these tasks have depth 1, etc.
     *
     * @param task          current Task to be placed in the depthList.
     * @param taskList      List of Tasks.
     * @param tasksPerDepth Depth List.
     * @param depth         current depth.
     */
    private static void fillTasksPerDepthList(Task task, List<Task> taskList, ArrayList<ArrayList<Integer>> tasksPerDepth, int depth) {
        if (task == null) {
            return;
        }
        // add a new ArrayList if we have reached a new depth
        if (tasksPerDepth.size() < depth + 1) tasksPerDepth.add(new ArrayList<>());
        // add the current task to this depths' ArrayList if it is not already in it
        if (!tasksPerDepth.get(depth).contains(task.getIdentifier()))
            tasksPerDepth.get(depth).add(task.getIdentifier());

        // iterate through all successors
        for (int successorID : task.getSuccessors()) {
            fillTasksPerDepthList(taskList.get(successorID), taskList, tasksPerDepth, depth + 1);
        }
    }
}
