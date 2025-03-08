package dt.tsg.taskSet;

import com.google.ortools.sat.CpSolver;
import dt.tsg.InputParams.InputParameters;
import dt.tsg.cspModel.ModelTask;
import dt.tsg.task.Task;
import dt.tsg.task.TaskInstance;
import dt.tsg.utils.RandomNumberGeneration;
import dt.tsg.utils.Utils;

import java.io.*;
import java.util.*;

/**
 * This class describes all properties of the Task Set and includes methods for converting a Task Set into a given format.
 * A Task Set combines a collection of Tasks with metadata and relational information between Tasks.
 */
public class TaskSet {

    private final ArrayList<Task> taskList;

    //Task Set Attributes / Classifiers
    private int num_tasks;                   //total number of tasks in the task set
    private final int num_resources;              //total number of available resources
    private int num_precedence_graphs;       //total number of precedence subgraphs in the task set
    private int num_residency_constraints;   //total number of residency constraints in the task set
    private final CpSolver solver;                        //the solver with which the model has been solved
    private final Map<Integer, ModelTask> modelTaskMap;   //the map for the tasks represented in the model

    public TaskSet(ArrayList<Task> taskList, int num_resources, Map<Integer, ModelTask> modelTaskMap, CpSolver solver) {
        this.taskList = taskList;
        this.num_tasks = 0;
        this.num_resources = num_resources;
        this.num_precedence_graphs = 0;
        this.num_residency_constraints = 0;
        this.modelTaskMap = modelTaskMap;
        this.solver = solver;

        // calculate residency constraints
        List<Task> reducedList = new ArrayList<>();
        for (Task task : taskList) {
            if (task instanceof TaskInstance) continue;     // skip periodic task instances
            reducedList.add(task);
            this.num_tasks++;
            if (!task.getResourceConstraint().isEmpty()) this.num_residency_constraints++;
        }

        // calculate number of subgraphs
        boolean[] visited = new boolean[reducedList.size()];
        int index = 0;
        // execute a DFS for every node that has not been visited by the DFS
        while (index != -1) {
            depthFirstSearchPredSuc(reducedList, reducedList.get(index), visited);
            index = -1;
            for (int k = 0; k < reducedList.size(); k++) {
                if (!visited[k]) {
                    index = k;
                    break;
                }
            }
            this.num_precedence_graphs++;
        }
    }


    /**
     * Executes a recursive depth-first-search through both predecessor and successor lists.
     * Visited tasks are set to true in the visited boolean array.
     * This function can be used to find subgraphs within the task set.
     *
     * @param taskList The list of tasks
     * @param task     The current task that is being searched
     * @param visited  the boolean array object
     */
    public static void depthFirstSearchPredSuc(List<Task> taskList, Task task, boolean[] visited) {
        visited[task.getIdentifier()] = true;
        for (Integer successor : task.getSuccessors()) {

            if (!visited[successor]) {
                depthFirstSearchPredSuc(taskList, taskList.get(successor), visited);
            }

        }
        for (Integer predecessor : task.getPredecessors()) {

            if (!visited[predecessor]) {
                depthFirstSearchPredSuc(taskList, taskList.get(predecessor), visited);
            }

        }
    }

    /**
     * Generate the content of a .dot file for the task set.
     *
     * @param taskList the list of tasks.
     * @return the content of a .dot file for the task set.
     */
    public String generateDot(List<Task> taskList) {

        StringBuilder result = new StringBuilder("digraph {\n" +
                "\n");

        int taskIndex = 0;
        // copy all tasks that are not periodic task instances
        List<Task> reducedList = taskList.subList(0, this.num_tasks); // the right bound is exclusive

        for (Task task : reducedList) {

            if (!(task instanceof TaskInstance)) {
                // ignore instances of periodic tasks
                // iterate through the successor list
                if (task.getIdentifier() < taskIndex) continue; // skip tasks that have already been printed
                if (task.getSuccessors().isEmpty()) {
                    // task has no successors, print just the task
                    result.append("\t").append(task.getIdentifier()).append("\n");
                } else if (task.getSuccessors().size() == 1) {
                    // task has only one successor
                    if (task.getSuccessors().get(0) < this.num_tasks) {
                        // only add a successor if it isn't a periodic task instance
                        result.append("\t").append(task.getIdentifier()).append(" -> ").append(task.getSuccessors().get(0)).append(";\n");
                    } else {
                        result.append("\t").append(task.getIdentifier()).append("\n");
                    }
                } else {
                    // task has successors, iterate through all of them. for every branch, add a line
                    result.append("\t").append(task.getIdentifier()).append(" -> {");
                    for (int successorID : task.getSuccessors()) {
                        if (successorID < this.num_tasks) {
                            // only add successors if they are not periodic task instances
                            result.append(successorID).append("; ");
                        }
                    }
                    result.append("}\n");
                }
            }
        }

        result.append("}");

        return result.toString();
    }

    /**
     * Formats the task set and returns the formatted .json string.
     *
     * @param name                the name of the task set.
     * @param phased_releaseTimes boolean that controls the generation of phases.
     * @param inputs              the Input Parameters object.
     * @param limit               the scheduling limit.
     * @param utils               the Utils object.
     * @param warnings            the Warnings object.
     * @return the formatted string for the task set .json file.
     * @throws Exception if the task list is empty, the number of tasks is 0 or the number of resources is 0
     */
    public String toFormat(String name, boolean phased_releaseTimes, InputParameters inputs, int limit, Utils utils, ArrayList<String> warnings) throws Exception {

        if (this.taskList.isEmpty() || this.num_tasks == 0 || this.num_resources == 0) {
            throw new Exception("Error while formatting task set. Make sure all attributes are set correctly.");
        }

        //taskList.forEach(System.out::println);

        // calculate total utilization
        int totalRuntime = taskList.stream().mapToInt(Task::getWCET).sum();
        double totalUtilization = (double) totalRuntime / (limit * this.num_resources);
        totalUtilization = Math.round(totalUtilization * 100.0) / 100.0;

        // calculate per resource utilization
        String properties;
        if (!inputs.MultiResidency()) {
            ArrayList<Integer> wcetPerResource = new ArrayList<>();
            for (int i = 0; i < this.num_resources; i++) {
                wcetPerResource.add(i, 0);
                for (Task task : taskList) {
                    int resourceID = (int) solver.value(modelTaskMap.get(task.getIdentifier()).getResource());
                    if (resourceID == i) wcetPerResource.set(i, wcetPerResource.get(i) + task.getWCET());
                }
            }
            ArrayList<Double> utilPerResource = new ArrayList<>();

            for (Integer wcet : wcetPerResource) {
                double util = (double) wcet / (double) limit;
                utilPerResource.add(Math.round(util * 100.0) / 100.0);
            }

            properties = "{\n" +
                    "\t\"properties\": {\n" +
                    "\t\t\"name\" : " + "\"" + name + "\",\n" +
                    "\t\t\"n\" : " + this.num_tasks + ",\n" +
                    "\t\t\"p\" : " + this.num_resources + ",\n" +
                    "\t\t\"x_prec_num\" : 1,\n" +
                    "\t\t\"x_prec_denom\" : " + this.num_precedence_graphs + ",\n" +
                    "\t\t\"x_resid_num\" : " + this.num_residency_constraints + ",\n" +
                    "\t\t\"x_resid_denom\" : " + this.num_tasks + ",\n" +
                    "\t\t\"total_utilization\" : " + totalUtilization + ",\n" +
                    "\t\t\"per_resource_utilization\" : " + utilPerResource + "\n" +
                    "\n" +
                    "\t},\n";
        } else {
            properties = "{\n" +
                    "\t\"properties\": {\n" +
                    "\t\t\"name\" : " + "\"" + name + "\",\n" +
                    "\t\t\"n\" : " + this.num_tasks + ",\n" +
                    "\t\t\"p\" : " + this.num_resources + ",\n" +
                    "\t\t\"x_prec_num\" : 1,\n" +
                    "\t\t\"x_prec_denom\" : " + this.num_precedence_graphs + ",\n" +
                    "\t\t\"x_resid_num\" : " + this.num_residency_constraints + ",\n" +
                    "\t\t\"x_resid_denom\" : " + this.num_tasks + ",\n" +
                    "\t\t\"total_utilization\" : " + totalUtilization + "\n" +
                    "\n" +
                    "\t},\n";
        }

        StringBuilder data = new StringBuilder("\t\"data\" : [\n");

        Random random = new Random();
        // build a String for each task
        for (Task task : taskList) {

            if (task instanceof TaskInstance) continue;     // skip instances of periodic tasks
            String taskString = "\t\t{\n" +
                    "\t\t\"id\" : " + task.getIdentifier() + ",\n" +
                    "\t\t\"constraints\" : {\n" +
                    "\t\t\t\"wcet\" : " + task.getWCET() + ",\n";

            // add releasetime, deadline, period
            int releaseTime;
            int deadline;
            // add phase if we calculated phased releasetime
            if (!phased_releaseTimes) {

                int releasetime_lowerbound;
                int releasetime_upperbound;
                if (inputs.getMin_releaseTime() > -1) {
                    // use the user input for min releasetime
                    releasetime_lowerbound = inputs.getMin_releaseTime();
                } else {
                    // do not use user defined min releasetime
                    releasetime_lowerbound = 0;
                }
                if (inputs.getMax_releaseTime() > -1) {
                    // use the user input for max releasetime
                    releasetime_upperbound = Math.min(task.getReleaseTime(), inputs.getMax_releaseTime());
                } else {
                    releasetime_upperbound = task.getReleaseTime();
                }
                // choose a releasetime from the interval [minimum releasetime, MIN(relative start time (relative to the period) , maximum releasetime)]
                releaseTime = RandomNumberGeneration.getNumWithDistribution(releasetime_lowerbound, releasetime_upperbound, InputParameters.Distribution.UNIFORM, utils, 0.0, -1, -1, warnings); //random.nextInt(releasetime_lowerbound, releasetime_upperbound + 1);

                if (inputs.isDeadlineEqualToPeriod()) {
                    deadline = task.getPeriod();
                } else {
                    int deadline_lowerbound;
                    int deadline_upperbound;
                    if (inputs.getMin_deadline() > -1) {
                        deadline_lowerbound = Math.max(task.getDeadline(), inputs.getMin_deadline());
                    } else {
                        deadline_lowerbound = task.getDeadline();
                    }
                    if (inputs.getMax_deadline() > -1) {
                        if (task.isPeriodic()) {
                            deadline_upperbound = Math.min(task.getPeriod(), inputs.getMax_deadline()); // deadline cannot be larger than the period
                        } else {
                            deadline_upperbound = Math.min(limit, inputs.getMax_deadline());        // deadline cannot be larger than the limit
                        }
                    } else {
                        if (task.isPeriodic()) {
                            deadline_upperbound = task.getPeriod();
                        } else {
                            deadline_upperbound = limit;
                        }

                    }
                    // choose a deadline from the interval of lowerbound to upperbound
                    if (deadline_upperbound == deadline_lowerbound) {
                        deadline = deadline_lowerbound;
                    } else {
                        deadline = random.nextInt(deadline_lowerbound, deadline_upperbound + 1);
                    }
                }

                taskString += "\t\t\t\"releaseTime\" : " + releaseTime + ",\n" +
                        "\t\t\t\"deadline\" : " + deadline + ",\n" +
                        "\t\t\t\"period\" : " + task.getPeriod() + ",\n";

            } else {

                int releasetime_lowerbound;
                int releasetime_upperbound;
                if (inputs.getMin_releaseTime() > -1) {
                    // use the user input for min releasetime
                    releasetime_lowerbound = inputs.getMin_releaseTime();
                } else {
                    // do not use user defined min releasetime
                    releasetime_lowerbound = 0;
                }
                if (inputs.getMax_releaseTime() > -1) {
                    // use the user input for max releasetime
                    releasetime_upperbound = Math.min(task.getReleaseTime(), inputs.getMax_releaseTime());
                } else {
                    releasetime_upperbound = task.getReleaseTime();
                }
                // choose a releasetime from the interval [minimum releasetime, MIN(relative start time (relative to the period) , maximum releasetime)]
                releaseTime = RandomNumberGeneration.getNumWithDistribution(releasetime_lowerbound, releasetime_upperbound, InputParameters.Distribution.UNIFORM, utils, 0.0, -1, -1, warnings); //random.nextInt(releasetime_lowerbound, releasetime_upperbound + 1);
                if (inputs.isDeadlineEqualToPeriod()) {
                    deadline = task.getPeriod();
                } else {
                    int deadline_lowerbound;
                    int deadline_upperbound;
                    if (inputs.getMin_deadline() > -1) {
                        deadline_lowerbound = Math.max(task.getDeadline(), inputs.getMin_deadline());
                    } else {
                        deadline_lowerbound = task.getDeadline();
                    }
                    if (inputs.getMax_deadline() > -1) {
                        if (task.isPeriodic()) {
                            deadline_upperbound = Math.min(task.getPeriod(), inputs.getMax_deadline());
                        } else {
                            deadline_upperbound = Math.min(limit, inputs.getMax_deadline());
                        }
                    } else {
                        if (task.isPeriodic()) {
                            deadline_upperbound = task.getPeriod();
                        } else {
                            deadline_upperbound = limit;
                        }

                    }
                    // choose a deadline from the interval of lowerbound to upperbound
                    if (deadline_lowerbound == deadline_upperbound) {
                        deadline = deadline_lowerbound;
                    } else {
                        deadline = random.nextInt(deadline_lowerbound, deadline_upperbound + 1);
                    }
                }
                int phase = task.getPhase();

                taskString += "\t\t\t\"releaseTime\" : " + releaseTime + ",\n" +
                        "\t\t\t\"deadline\" : " + deadline + ",\n" +
                        "\t\t\t\"phase\" : " + phase + ",\n" +
                        "\t\t\t\"period\" : " + task.getPeriod() + ",\n";
            }

            if (task.getResourceConstraint().isEmpty()) {
                taskString += "\t\t\t\"resid\" : null\n";
            } else {
                taskString += "\t\t\t\"resid\" : " + task.getResourceConstraint() + "\n";
            }

            if (task.getIdentifier() == this.num_tasks - 1) {
                taskString += "\t\t}\n" +       // the last task has no comma at the end
                        "\t\t}\n";
            } else {
                taskString += "\t\t}\n" +
                        "\t\t},\n";
            }
            data.append(taskString);

        }

        data.append("\t]\n" +
                "}\n");


        return properties + data;

    }

    /**
     * Saves the provided content to a file with the given name and suffix.
     *
     * @param content the content of the file.
     * @param name    the name of the file.
     * @param suffix  the suffix of the file.
     * @return a boolean depending on if saving the file was successfull.
     * @throws IOException if saving the file fails.
     */
    public boolean saveToFile(String content, String name, String suffix) throws IOException {

        File dir = new File("output/");
        if (!dir.exists()) {
            boolean dirCreated = dir.mkdirs();
            if (!dirCreated) {
                System.out.println("Failed creating directory for output file at \"output/\"");
                System.exit(-1);
            }
        }

        File newFile = new File("output/" + name + suffix);
        int index = 0;
        // Check that a file with this name does not already exist
        while (newFile.exists()) {
            newFile = new File("output/" + name + index + suffix);
            index++;
        }

        // Write to the file and catch exceptions
        BufferedWriter bufferedWriter = null;
        boolean success = false;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(newFile));
            bufferedWriter.write(content);
        } finally {
            if (bufferedWriter != null) {
                bufferedWriter.close();
                success = true;
            }
        }

        return success;

    }

    @Override
    public String toString() {
        return "TaskSet{" +
                "taskList=" + taskList.toString() +
                ", num_tasks=" + num_tasks +
                ", num_resources=" + num_resources +
                ", num_precedence_graphs=" + num_precedence_graphs +
                ", num_residency_constraints=" + num_residency_constraints +
                ", solver=" + solver +
                ", modelTaskMap=" + modelTaskMap +
                '}';
    }

    public ArrayList<Task> getTaskList() {
        return taskList;
    }

    public int getNum_tasks() {
        return num_tasks;
    }

    public int getNum_resources() {
        return num_resources;
    }

    public int getNum_precedence_graphs() {
        return num_precedence_graphs;
    }

    public int getNum_residency_constraints() {
        return num_residency_constraints;
    }

    public Map<Integer, ModelTask> getModelTaskMap() {
        return modelTaskMap;
    }
}
