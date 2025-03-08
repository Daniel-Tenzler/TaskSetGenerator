package dt.tsg.precedenceGraph;

import dt.tsg.InputParams.InputParameters;
import dt.tsg.task.Task;
import dt.tsg.task.TaskInstance;
import dt.tsg.utils.RandomNumberGeneration;
import dt.tsg.utils.Utils;

import java.util.*;

public class BasicPrecedenceConstraints {

    /**
     * Executes a recursive depth-first-search through all successors of the current task.
     * This function returns true if the subgraph in which the current task is in contains a cycle.
     *
     * @param taskList the list of tasks
     * @param task     the current task
     * @param visited  boolean array
     * @param indexed  boolean array
     * @return true if the task is part of a cycle, false if not.
     */
    public static boolean dfsFindCycle(List<Task> taskList, Task task, boolean[] visited, boolean[] indexed) {
        if (visited[task.getIdentifier()]) return false;
        visited[task.getIdentifier()] = true;
        indexed[task.getIdentifier()] = true;
        for (Integer successor : task.getSuccessors()) {
            if (indexed[successor]) return true;
            boolean result = dfsFindCycle(taskList, taskList.get(successor), visited, indexed);
            if (result) return true;

        }
        indexed[task.getIdentifier()] = false;
        return false;
    }

    /**
     * Generates precedence constraints according to our default method.
     *
     * @param numberOfPrecedenceRelations the total number of precedence relations.
     * @param taskList                    the list of tasks.
     * @param utils                       the Utils object.
     * @param inputs                      the InputParameters object.
     * @param warnings                    the Warnings object.
     * @return a map that maps task ids to subgraph ids.
     */
    public static Map<Integer, ArrayList<Integer>> GenerateBasicPrecedenceConstraints(int numberOfPrecedenceRelations, ArrayList<Task> taskList, Utils utils, InputParameters inputs, ArrayList<String> warnings) {

        int min_precedence_constraints = inputs.getMin_total_precedence_relations();
        utils.DebugPrint("\nBegin Generating Basic Precedence Constraints:\nTarget Number of Precedence Relations: " + numberOfPrecedenceRelations + "\n");

        Map<Integer, ArrayList<Integer>> precedence_map = new HashMap<>();
        //build map
        for (int i = 0; i < taskList.size(); i++) {
            precedence_map.put(i, new ArrayList<>());
        }

        // calculate precedence constraints
        if (numberOfPrecedenceRelations > 0) {
            // Build precedence graph with the specified number of edges
            for (int i = 0; i < numberOfPrecedenceRelations; i++) {
                // calculate all possible precedence relations
                ArrayList<int[]> edgeMap = getEdgeMap(taskList, precedence_map, null);
                if (edgeMap.isEmpty()) {
                    // cannot generate any another precedence relation
                    if (i > min_precedence_constraints) {
                        System.out.println("WARNING: We attempted to generate " + numberOfPrecedenceRelations + " precedence relations, but could only generate " + i + "!");
                        warnings.add("WARNING: We attempted to generate " + numberOfPrecedenceRelations + " precedence relations, but could only generate " + i + "!");
                    } else {
                        System.out.println("WARNING: Could not generate as many precedence relations as defined by input constraint!");
                        warnings.add("WARNING: Could not generate as many precedence relations as defined by input constraint!");
                    }
                    break;
                }

                utils.DebugPrint("Map of possible edges:");
                edgeMap.forEach(e -> {
                    if (utils.debug) System.out.print(Arrays.toString(e) + " ");
                });
                utils.DebugPrint("");

                // remove any edges that would create subgraphs with a height larger than the minimum period
                removeEdgesBeyondMinPeriodHeight(taskList, utils, inputs, edgeMap, precedence_map);

                // @User: more task graph parameters can be implemented by filtering the edgeMap

                if (edgeMap.isEmpty()) {
                    // cannot generate another precedence relation
                    if (i > min_precedence_constraints) {
                        System.out.println("WARNING: We attempted to generate " + numberOfPrecedenceRelations + " precedence relations, but could only generate " + i + "!");
                        warnings.add("WARNING: We attempted to generate " + numberOfPrecedenceRelations + " precedence relations, but could only generate " + i + "!");
                    } else {
                        System.out.println("WARNING: Could not generate as many precedence relations as defined by input constraint!");
                        warnings.add("WARNING: Could not generate as many precedence relations as defined by input constraint!");
                    }
                    break;
                }

                // choose a random edge from the edgeMap
                int edgeFieldNum = RandomNumberGeneration.getNumWithDistribution(0, edgeMap.size() - 1, InputParameters.Distribution.UNIFORM, utils, 0.0, -1, -1, warnings);
                int[] edge = edgeMap.get(edgeFieldNum);
                // add the precedence relation
                int predecessorID = edge[0];
                int successorID = edge[1];
                addEdgeBetweenTasks(taskList, utils, predecessorID, successorID, precedence_map);

            }

        } else if (inputs.getSubgraphNumber() > 0) {
            // Build precedence graph with the specified number of subgraphs
            // Choose random tasks that will denote the subgraphs
            ArrayList<Task> copyList = new ArrayList<>(taskList);
            HashMap<Integer, Integer> taskIDsToSubgraphs = new HashMap<>();
            taskList.forEach(task -> {
                taskIDsToSubgraphs.put(task.getIdentifier(), -1);        // -1 denotes that the task does not belong to any subgraph
            });
            for (int i = 0; i < inputs.getSubgraphNumber(); i++) {
                int field = RandomNumberGeneration.getNumWithDistribution(0, copyList.size() - 1, InputParameters.Distribution.UNIFORM, utils, 0.0, -1, -1, warnings);
                Task task = copyList.remove(field);
                taskIDsToSubgraphs.put(task.getIdentifier(), task.getIdentifier());
            }
            System.out.println("Chosen Subgraphs:");
            taskIDsToSubgraphs.forEach((key, value) -> System.out.println(key.toString() + " : " + value.toString()));

            while (!checkSubgraphMap(taskIDsToSubgraphs)) {   // Currently, we stop adding edges when all tasks are part of a subgraph. This is not necessarily desirable.
                // Calculate all possible precedence relations
                ArrayList<int[]> edgeMap = getEdgeMap(taskList, precedence_map, taskIDsToSubgraphs);
                if (edgeMap.isEmpty()) {
                    // cannot generate any another precedence relation
                    if (!checkSubgraphMap(taskIDsToSubgraphs)) {
                        System.out.println("WARNING: We failed to generate the predetermined number of subgraphs!");
                        warnings.add("WARNING: We failed to generate the predetermined number of subgraphs!");
                    }
                    break;
                }

                utils.DebugPrint("Map of possible edges:");
                edgeMap.forEach(e -> {
                    if (utils.debug) System.out.print(Arrays.toString(e) + " ");
                });
                utils.DebugPrint("");

                // remove any edges that would create subgraphs with a height larger than the minimum period
                removeEdgesBeyondMinPeriodHeight(taskList, utils, inputs, edgeMap, precedence_map);

                if (edgeMap.isEmpty()) {
                    // cannot generate any another precedence relation
                    if (!checkSubgraphMap(taskIDsToSubgraphs)) {
                        System.out.println("WARNING: We failed to generate the predetermined number of subgraphs!");
                        warnings.add("WARNING: We failed to generate the predetermined number of subgraphs!");
                    }
                    break;
                }

                // choose a random edge from the edgeMap
                int edgeFieldNum = RandomNumberGeneration.getNumWithDistribution(0, edgeMap.size() - 1, InputParameters.Distribution.UNIFORM, utils, 0.0, -1, -1, warnings);
                int[] edge = edgeMap.get(edgeFieldNum);
                // add the precedence relation
                int predecessorID = edge[0];
                int successorID = edge[1];

                if (taskIDsToSubgraphs.get(predecessorID) == -1 && taskIDsToSubgraphs.get(successorID) == -1) {
                    throw new RuntimeException("Invalid Edge added during subgraph precedence generation");
                } else if (taskIDsToSubgraphs.get(predecessorID) != -1 && taskIDsToSubgraphs.get(successorID) == -1) {
                    // add successor to the subgraph of the predecessor
                    taskIDsToSubgraphs.put(successorID, taskIDsToSubgraphs.get(predecessorID));
                } else if (taskIDsToSubgraphs.get(predecessorID) == -1 && taskIDsToSubgraphs.get(successorID) != -1) {
                    // add successor to the subgraph of the predecessor
                    taskIDsToSubgraphs.put(predecessorID, taskIDsToSubgraphs.get(successorID));
                } else if (Objects.equals(taskIDsToSubgraphs.get(predecessorID), taskIDsToSubgraphs.get(successorID))) {
                    // both are already in the same subgraph
                    utils.DebugPrint("Predecessor and successor are in the same subgraph. Add the edge: [" + predecessorID + "," + successorID + "]");
                } else {
                    System.out.println("Unknown situation: predecessorGraphValue: " + taskIDsToSubgraphs.get(predecessorID) + " successorGraphValue: " + taskIDsToSubgraphs.get(successorID));
                }

                addEdgeBetweenTasks(taskList, utils, predecessorID, successorID, precedence_map);

            }

        }
        return precedence_map;
    }

    /**
     * Removes edges from the edgeMap that would expand a subgraph beyond the minimum period limit.
     *
     * @param taskList       the list of tasks.
     * @param utils          the Utils object.
     * @param inputs         the InputParameter object.
     * @param edgeMap        the list of edges.
     * @param precedence_map the map of task ids to subgraphs.
     */
    private static void removeEdgesBeyondMinPeriodHeight(ArrayList<Task> taskList, Utils utils, InputParameters inputs, ArrayList<int[]> edgeMap, Map<Integer, ArrayList<Integer>> precedence_map) {
        // for each edge, check that adding the edge will not increase the height of the specific subgraphs beyond the minimum period
        ArrayList<int[]> markedForRemoval = new ArrayList<>();
        for (int[] edge : edgeMap) {
            Task predecessor = taskList.get(edge[0]);
            Task successor = taskList.get(edge[1]);
            // add the edge, then iterate through all elements of the predecessors precedence map
            predecessor.addSuccessors(successor.getIdentifier());
            // iterate through all elements of the predecessors precedence map and check the height of their subtrees
            for (Integer precedenceMapTaskID : precedence_map.get(predecessor.getIdentifier())) {
                // check if the height exceeds the min period
                int height = dfsFindHeight(taskList.get(precedenceMapTaskID), taskList, 1);
                utils.DebugPrint("Found height " + height + " for task " + precedenceMapTaskID);
                if (height > inputs.getMin_period_length()) {
                    //utils.DebugPrint("The edge would create a subgraph which exceeds the minimum period.");
                    // mark edge for removal
                    markedForRemoval.add(edge);
                    break;
                }
            }
            // remove the edge
            predecessor.removeSuccessor(successor.getIdentifier());
        }

        // remove all invalid edges
        utils.DebugPrint("Removed edges: ");
        for (int[] edge : markedForRemoval) {
            // remove the edge from the edgeMap
            boolean removed = edgeMap.remove(edge);
            if (!removed) throw new RuntimeException("Failed to remove an invalid edge from the edgeMap");
            System.out.print(Arrays.toString(edge));
        }
        utils.DebugPrint("\n");
    }

    /**
     * Adds an edge between two tasks, and updates the precedence map.
     *
     * @param taskList       the list of tasks.
     * @param utils          the Utils object.
     * @param predecessorID  the id of the predecessor task.
     * @param successorID    the id of the successor task.
     * @param precedence_map the map of task ids to subgraphs.
     */
    private static void addEdgeBetweenTasks(ArrayList<Task> taskList, Utils utils, int predecessorID, int successorID, Map<Integer, ArrayList<Integer>> precedence_map) {
        taskList.get(predecessorID).addSuccessors(successorID);
        taskList.get(successorID).addPredecessor(predecessorID);

        utils.DebugPrint("added predecessor " + predecessorID + " to task " + successorID);
        utils.DebugPrint("predecessor Map : " + Arrays.toString(precedence_map.get(predecessorID).toArray()) + " \nsuccessor Map : " + Arrays.toString(precedence_map.get(successorID).toArray()));

        // update the precedence maps of the two tasks
        // add the predecessor task to its own map if it is not already in it
        if (!precedence_map.get(predecessorID).contains(predecessorID))
            precedence_map.get(predecessorID).add(predecessorID);
        // add the successor task to the predecessor map
        if (!precedence_map.get(predecessorID).contains(successorID))
            precedence_map.get(predecessorID).add(successorID);
        // add the successor task to its own map if it is not already in it
        if (!precedence_map.get(successorID).contains(successorID)) precedence_map.get(successorID).add(successorID);
        // add the predecessor task to the successor map
        if (!precedence_map.get(successorID).contains(predecessorID))
            precedence_map.get(successorID).add(predecessorID);

        // merge lists
        ArrayList<Integer> mergedList = new ArrayList<>();
        for (Integer id : precedence_map.get(predecessorID)) {
            if (!mergedList.contains(id)) mergedList.add(id);
        }
        for (Integer id : precedence_map.get(successorID)) {
            if (!mergedList.contains(id)) mergedList.add(id);
        }

        utils.DebugPrint("merged List: " + Arrays.toString(mergedList.toArray()));

        // for all tasks in the merged List, set the merged List as their precedence map
        for (Integer id : mergedList) {
            precedence_map.put(id, mergedList);
        }

        utils.DebugPrint("updated precedence map: " + precedence_map);
    }

    /**
     * Checks if all tasks in the hashmap are part of a subgraph. This is determined by checking if all tasks have non -1 associated values
     *
     * @param hashMap Maps the taskIds to the corresponding subgraph
     * @return true, if all tasks are part of a subgraph. false if at least one task is not assigned to a subgraph
     */
    private static boolean checkSubgraphMap(HashMap<Integer, Integer> hashMap) {
        for (int value : hashMap.values()) {
            if (value == -1) return false;
        }
        return true;
    }

    /**
     * Calculates all possible edges between all tasks and stores them in a nested integer list.
     *
     * @param taskList           the list of tasks.
     * @param precedence_map     the map of task ids to subgraphs.
     * @param taskIDsToSubgraphs the map of task ids to the subgraph ids they belong to.
     * @return a nested list containing all possible edges.
     */
    public static ArrayList<int[]> getEdgeMap(ArrayList<Task> taskList, Map<Integer, ArrayList<Integer>> precedence_map, HashMap<Integer, Integer> taskIDsToSubgraphs) {
        ArrayList<int[]> edgeMap = new ArrayList<>();
        for (Task task1 : taskList) {
            for (Task task2 : taskList) {
                // if the taskIDsToSubgraphs map is not null, we need to make sure that we only add an edge between tasks that belong to a subgraph and don't belong to a subgraph
                if (taskIDsToSubgraphs != null) {
                    int task1SubgraphValue = taskIDsToSubgraphs.get(task1.getIdentifier());
                    int task2SubgraphValue = taskIDsToSubgraphs.get(task2.getIdentifier());
                    if (task1SubgraphValue == -1 && task2SubgraphValue == -1) {
                        // both do not belong to a subtree, do not add an edge between them
                        continue;
                    } else if (task1SubgraphValue != -1 && task2SubgraphValue != -1 && task1SubgraphValue != task2SubgraphValue) {
                        // both belong to different subtrees, if we add an edge, we would merge the trees
                        continue;
                    }
                }
                if (task1.getIdentifier() != task2.getIdentifier()) {
                    if (precedence_map.get(task1.getIdentifier()).isEmpty() && precedence_map.get(task2.getIdentifier()).isEmpty()) {
                        // both maps are empty, we can safely add an edge to between the tasks
                        edgeMap.add(new int[]{task1.getIdentifier(), task2.getIdentifier()});
                    } else if (!precedence_map.get(task1.getIdentifier()).isEmpty() && !precedence_map.get(task2.getIdentifier()).isEmpty()) {
                        // both maps are not empty, we have to check if adding an edge creates a cycle
                        // check if they are in the same subgraph
                        if (precedence_map.get(task1.getIdentifier()).contains(task2.getIdentifier())
                                || precedence_map.get(task2.getIdentifier()).contains(task1.getIdentifier())) {
                            // The tasks are in the same subgraph. Check if adding an edge in one direction or another would create a cycle
                            if (!task1.getSuccessors().contains(task2.getIdentifier()) && !task2.getSuccessors().contains(task1.getIdentifier())) {
                                // there is no direct edge between them
                                // add an edge from task1 to task2
                                task1.addSuccessors(task2.getIdentifier());
                                boolean[] visited = new boolean[taskList.size()];
                                boolean[] indexed = new boolean[taskList.size()];
                                boolean cyclic = dfsFindCycle(taskList, task1, visited, indexed);
                                // remove the edge
                                task1.removeSuccessor(task2.getIdentifier());
                                if (!cyclic) {
                                    // the edge would not create a cycle
                                    edgeMap.add(new int[]{task1.getIdentifier(), task2.getIdentifier()});
                                }
                            }
                        } else {
                            // the tasks are not in the same subgraph. we can add an edge between them without creating a cycle
                            edgeMap.add(new int[]{task1.getIdentifier(), task2.getIdentifier()});
                        }
                    } else {
                        // one of the maps is empty, the other is not. we can add an edge between them without creating a cycle
                        edgeMap.add(new int[]{task1.getIdentifier(), task2.getIdentifier()});
                    }
                }
            }
        }
        return edgeMap;
    }

    /**
     * Executes a depth-first-search through all successors of the current task.
     * Returns the depth of the search, which is equal to the height of the subgraph if the search is started at the root.
     *
     * @param task     the current task
     * @param taskList the list of all tasks
     * @param depth    the depth/height of the subgraph
     * @return the height of the subgraph
     */
    public static int dfsFindHeight(Task task, ArrayList<Task> taskList, int depth) {
        if (task.getSuccessors().isEmpty()) return depth;
        int maxDepth = depth;
        for (Integer id : task.getSuccessors()) {
            int result = dfsFindHeight(taskList.get(id), taskList, depth + 1);
            if (result > maxDepth) maxDepth = result;
        }
        return maxDepth;
    }

    /**
     * Adds precedence relations between tasks according to the posthoc precedence generation option.
     *
     * @param taskList            the list of tasks.
     * @param scheduledTasks      the list of scheduled task objects.
     * @param phased_releaseTimes boolean that controls the generation of phases for tasks.
     * @param inputs              the Input Parameter object.
     * @param warnings            the Warnings object.
     * @param utils               the Utils object.
     * @param solutionNum         the number of the current solution for debugging purposes.
     */
    public static void addPostHocPrecedenceConstraints(ArrayList<Task> taskList, ArrayList<Utils.ScheduledTask> scheduledTasks, boolean phased_releaseTimes, InputParameters inputs, List<String> warnings, Utils utils, int solutionNum) {

        ArrayList<int[]> edgeMap = new ArrayList<>();
        // collect possible edges between tasks
        for (Utils.ScheduledTask st1 : scheduledTasks) {
            for (Utils.ScheduledTask st2 : scheduledTasks) {
                // filter out tasks that have already been checked
                if (st1.taskID() < st2.taskID()) continue;
                Task task1 = taskList.get(st1.taskID());
                Task task2 = taskList.get(st2.taskID());
                // skip periodic task instances
                if (task1 instanceof TaskInstance || task2 instanceof TaskInstance) {
                    continue;
                }
                // skip if tasks are the same
                if (task1.getIdentifier() == task2.getIdentifier()) continue;
                // edges can only be reliably added between aperiodic tasks and between an aperiodic and periodic task
                // if we add an edge between two periodic tasks, we need to add edges to all periodic task instances of these tasks
                if (!task1.isPeriodic() && !task2.isPeriodic()) {
                    // tasks are both aperiodic, add precedence relation if the start of one task is larger or equal to the end of the other task
                    if (st1.start() >= st2.end()) {
                        // add precedence relation
                        edgeMap.add(new int[]{task2.getIdentifier(), task1.getIdentifier()});
                    } else if (st1.end() <= st2.start()) {
                        // add precedence relation
                        edgeMap.add(new int[]{task1.getIdentifier(), task2.getIdentifier()});
                    }
                    // else: tasks overlap, we cannot add a precedence relation
                } else if (task1.isPeriodic() && !task2.isPeriodic()) {
                    // tasks are periodic and aperiodic
                    // add an edge if the aperiodic task is scheduled before the periodic task
                    if (st1.start() >= st2.end()) {
                        // add precedence relation
                        edgeMap.add(new int[]{task2.getIdentifier(), task1.getIdentifier()});
                    }
                } else if (!task1.isPeriodic() && task2.isPeriodic()) {
                    // tasks are periodic and aperiodic
                    // add an edge if the aperiodic task is scheduled before the periodic task
                    if (st2.start() >= st1.end()) {
                        // add precedence relation
                        edgeMap.add(new int[]{task1.getIdentifier(), task2.getIdentifier()});
                    }
                } else {
                    // both tasks are periodic
                    // to preserve schedulability we must only add a precedence relation
                    // if the periodic task instances of the two tasks can also have the same relationship
                    if (task1.getPeriod() != task2.getPeriod()) {
                        // periods of the two tasks are not equal, TODO implement PostHoc Precedence for harmonic periods
                        continue;
                    }
                    // tasks have equal periods, check if they can have a precedence relation
                    // collect all periodic task instances
                    ArrayList<Task> task1instances = new ArrayList<>();
                    ArrayList<Task> task2instances = new ArrayList<>();
                    for (Task taskInstance : taskList) {
                        if (taskInstance instanceof TaskInstance) {
                            if (((TaskInstance) taskInstance).getOriginal_task_ID() == task1.getIdentifier())
                                task1instances.add(taskInstance);
                            if (((TaskInstance) taskInstance).getOriginal_task_ID() == task2.getIdentifier())
                                task2instances.add(taskInstance);
                        }
                    }


                    if (task1instances.size() == task2instances.size()) {
                        // instance lists are equal
                        if (st1.start() >= st2.end()) {
                            // task 1 can have task 2 as a predecessor
                            // check for each pair of task instances if they fulfill the relationship requirement
                            boolean poss = true;
                            for (int index = 0; index < task1instances.size(); index++) {
                                int instance1ID = task1instances.get(index).getIdentifier();
                                int instance2ID = task2instances.get(index).getIdentifier();
                                if (scheduledTasks.get(instance1ID).start() < scheduledTasks.get(instance2ID).end()) {
                                    poss = false;
                                    break;
                                }
                            }
                            if (poss) {
                                // add precedence relation
                                edgeMap.add(new int[]{task2.getIdentifier(), task1.getIdentifier()});
                            }

                        } else if (st2.start() >= st1.end()) {
                            // task 2 can have task 1 as predecessor
                            // check for each pair of task instances if they fulfill the relationship requirement
                            boolean poss = true;
                            for (int index = 0; index < task1instances.size(); index++) {
                                int instance1ID = task1instances.get(index).getIdentifier();
                                int instance2ID = task2instances.get(index).getIdentifier();
                                if (scheduledTasks.get(instance2ID).start() < scheduledTasks.get(instance1ID).end()) {
                                    poss = false;
                                    break;
                                }
                            }
                            if (poss) {
                                // add precedence relation
                                edgeMap.add(new int[]{task1.getIdentifier(), task2.getIdentifier()});
                            }
                        }

                    } else {
                        // the sizes of the task instance lists are not equal, this should only happen if we are using phased release times
                        if (!phased_releaseTimes) {
                            System.out.println("[Solution #" + solutionNum + "] WARNING: sizes of task instance lists are not equal during post-hoc precedence generation!\nTaskID: " + task1.getIdentifier() + " - Number of Instances: " + task1instances.size() + "\nTaskID: " + task2.getIdentifier() + " - Number of Instances: " + task2instances.size());
                            warnings.add("[Solution #" + solutionNum + "] WARNING: sizes of task instance lists are not equal during post-hoc precedence generation!\nTaskID: " + task1.getIdentifier() + " - Number of Instances: " + task1instances.size() + "\nTaskID: " + task2.getIdentifier() + " - Number of Instances: " + task2instances.size());
                            continue;
                        }
                        // if the two tasks have equal phase, then the TaskInstance lists are broken
                        if (task1.getPhase() == task2.getPhase()) {
                            System.out.println("[Solution #" + solutionNum + "] WARNING: sizes of task instance lists are not equal during post-hoc precedence generation!\nTaskID: " + task1.getIdentifier() + " - Number of Instances: " + task1instances.size() + "\nTaskID: " + task2.getIdentifier() + " - Number of Instances: " + task2instances.size());
                            warnings.add("[Solution #" + solutionNum + "] WARNING: sizes of task instance lists are not equal during post-hoc precedence generation!\nTaskID: " + task1.getIdentifier() + " - Number of Instances: " + task1instances.size() + "\nTaskID: " + task2.getIdentifier() + " - Number of Instances: " + task2instances.size());
                            continue;
                        }
                        // Tasks have unequal phase and equal periods. We can add a precedence relation regardless of the status of the periodic task instances, since the domains of each task pair never intersect
                        // add precedence relation
                        if (st1.start() >= st2.end()) {
                            edgeMap.add(new int[]{task2.getIdentifier(), task1.getIdentifier()});
                        } else {
                            edgeMap.add(new int[]{task1.getIdentifier(), task2.getIdentifier()});
                        }

                    }
                }
            }
        }

        utils.DebugPrint("all possible edges: ");
        for (int[] edge : edgeMap) {
            utils.DebugPrint(edge[0] + " -> " + edge[1]);
        }
        utils.DebugPrint("add edges");
        int nonPeriodicTaskInstanceNumber = (int) taskList.stream().filter(e -> !(e instanceof TaskInstance)).count();
        if (edgeMap.isEmpty() && (inputs.getMin_total_precedence_relations() > 0 || (inputs.getSubgraphNumber() < nonPeriodicTaskInstanceNumber && inputs.getSubgraphNumber() != -1))) {
            System.out.println("[Solution #" + solutionNum + "] WARNING: Failed to add any precedence relations during postHoc Precedence Generation. EdgeMap was Empty.");
            warnings.add("[Solution #" + solutionNum + "] WARNING: Failed to add any precedence relations during postHoc Precedence Generation. EdgeMap was Empty.");
            return;
        }

        // Add edges depending on the chosen input parameter.
        // We can add edges without having to worry about other edges, since a valid schedule already exists. We cannot generate cycles by adding edges
        if (inputs.getMin_total_precedence_relations() > 0) {
            // add edges until we fall within [min,max] precedence relations
            // shuffle the edgeMap
            Collections.shuffle(edgeMap);
            int currentNumRelations = 0;
            while (currentNumRelations < inputs.getMin_total_precedence_relations()) {
                int[] edge = edgeMap.remove(0);
                currentNumRelations = addPrecedenceRelation(taskList.get(edge[0]), taskList.get(edge[1]), currentNumRelations);
                if (edgeMap.isEmpty()) {
                    break;
                }
            }
            if (currentNumRelations < inputs.getMin_total_precedence_relations()) {
                System.out.println("\n[Solution #" + solutionNum + "] WARNING: We were only able to add " + currentNumRelations + " precedence relations with postHoc precedence generation!\n");
                warnings.add("[Solution #" + solutionNum + "] WARNING: We were only able to add " + currentNumRelations + " precedence relations with postHoc precedence generation!\n");
            }

        } else if (inputs.getSubgraphNumber() < nonPeriodicTaskInstanceNumber && inputs.getSubgraphNumber() != 0) {
            // add edges until we have the exact number of subgraphs
            HashMap<Integer, Integer> taskIDToSubgraphs = new HashMap<>();
            // add each task to its own subgraph
            taskList.stream().filter(e -> !(e instanceof TaskInstance)).forEach(task -> taskIDToSubgraphs.put(task.getIdentifier(), task.getIdentifier()));
            int currentNumSubgraphs = nonPeriodicTaskInstanceNumber;
            Collections.shuffle(edgeMap);
            while (currentNumSubgraphs > inputs.getSubgraphNumber()) {
                // add an edge from the edgeMap, then update the subgraphs
                int[] edge = edgeMap.remove(0);
                addPrecedenceRelation(taskList.get(edge[0]), taskList.get(edge[1]), 0);
                // update subgraphs: change every tasks' value entry, which has the successor as its value to the predecessor
                for (Map.Entry<Integer, Integer> taskEntry : taskIDToSubgraphs.entrySet()) {
                    boolean mergedSubgraphs = false;
                    if (taskEntry.getValue() == edge[1]) {
                        // Entry has the successor as its subgraph, change it to the predecessor
                        taskEntry.setValue(edge[0]);
                        mergedSubgraphs = true;
                    }
                    // if we merged two subgraphs, reduce the number of current subgraphs
                    if (mergedSubgraphs) currentNumSubgraphs--;
                }


                if (edgeMap.isEmpty()) {
                    break;
                }
            }
            if (currentNumSubgraphs != inputs.getSubgraphNumber()) {
                System.out.println("[Solution #" + solutionNum + "] WARNING: Failed to generate the desired number of subgraphs in PostHoc Precedence Generation");
                warnings.add("[Solution #" + solutionNum + "] WARNING: Failed to generate the desired number of subgraphs in PostHoc Precedence Generation");
            }
        } else {
            // neither parameter is set
            utils.DebugPrint("Neither TotalPrecedenceRelations nor SubgraphCount parameters can be used.");
        }
    }

    /**
     * Adds a precedence relation between two tasks. Updates the predecessor and successor lists within the tasks.
     *
     * @param predecessor         the predecessor task.
     * @param successor           the successor task.
     * @param currentNumRelations the number of precedence relations.
     * @return the updated number of precedence relations.
     */
    private static int addPrecedenceRelation(Task predecessor, Task successor, int currentNumRelations) {
        successor.addPredecessor(predecessor.getIdentifier());
        predecessor.addSuccessors(successor.getIdentifier());
        currentNumRelations++;
        //Utils.DebugPrint("added precedence relation: " + successor.getIdentifier() + " has pred: " + predecessor.getIdentifier());
        return currentNumRelations;
    }
}
