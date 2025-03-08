package dt.tsg.precedenceGraph;

import dt.tsg.InputParams.TGFFInputParameters;
import dt.tsg.task.Task;
import dt.tsg.taskSet.TGFFTaskSet;
import dt.tsg.utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class TGFFGraphGeneration {

    /**
     * Executes Sanity Checks for the TGFF input parameters.
     *
     * @param inputs the TGFF Input parameter object.
     * @return null if all sanity checks are passed, returns a non-null string if any sanity check was failed.
     */
    private static String TGFFSanityChecks(TGFFInputParameters inputs) {

        if (inputs.getGraphCount() < 1) return "Failed Check: graphCount >= 1";
        if (inputs.getTaskCountAverage() < 1) return "Failed Check: taskCountAverage >= 1";
        if (inputs.getTaskCountMultiplier() < 1) return "Failed Check: taskCountMultiplier >= 1";
        if (inputs.getPeriodPercentage() < 0.0 || inputs.getPeriodPercentage() > 1.0)
            return "Failed Check: periodPercentage >= 0.0 AND periodPercentage <= 1.0";
        if (inputs.getMultiStartNodeProb() < 0.0 || inputs.getMultiStartNodeProb() > 1.0)
            return "Failed Check: multiStartNodeProb >= 0.0 AND multiStartNodeProb <= 1.0";
        if (inputs.getStartNodesAverage() < 1) return "Failed Check: startNodesAverage >= 1";
        if (inputs.getStartNodesMultiplier() < 1) return "Failed Check: startNodesMultiplier >= 1";
        if (inputs.getTaskDegreeIN() < 0) return "Failed Check: taskDegreeIN >= 0";
        if (inputs.getTaskDegreeOUT() < 0) return "Failed Check: taskDegreeOUT >= 0";
        if (inputs.getHardDeadlineProb() < 0.0 || inputs.getHardDeadlineProb() > 1.0)
            return "Failed Check: hardDeadlineProb >= 0.0 AND hardDeadlineProb <= 1.0";
        if (inputs.getTaskTransitionTime() < 0.0) return "Failed Check: taskTransitionTime >= 0.0";
        if (inputs.getPeriod_mul().isEmpty()) return "Failed Check: period_mul Array must contain at least one value!";
        //ReleaseTime
        if (inputs.getMin_releaseTime() > inputs.getMax_releaseTime() && inputs.getMax_releaseTime() != -1) {
            return "Failed Check: NOT(this.min_releaseTime>this.max_releaseTime)";
        }
        //Deadline
        if (inputs.getMax_deadline() == 0 || (inputs.getMin_deadline() > inputs.getMax_deadline() && inputs.getMax_deadline() != -1)) {
            return "Failed Check: NOT(this.max_deadline <=0 || this.min_deadline>this.max_deadline)";
        }
        //WCET
        if (inputs.getMin_WCET() == 0 || inputs.getMax_WCET() == 0 || (inputs.getMin_WCET() > inputs.getMax_deadline() && inputs.getMax_deadline() != -1) || (inputs.getMin_WCET() > inputs.getMax_WCET() && inputs.getMax_WCET() != -1)) {
            return "Failed Check: NOT(this.min_WCET==0 || this.max_WCET == 0 || this.min_WCET > this.max_deadline || this.min_WCET>this.max_WCET)";
        }

        return null;
    }

    /**
     * Generates a TGFF input file and runs TGFF.
     *
     * @param inputs The TGFF input parameter object.
     * @param utils  the Utils object.
     * @return the input path for the TGFF output file.
     */
    public static String runTGFF(TGFFInputParameters inputs, Utils utils) {

        // run sanity Checks
        String sanityCheckStatus = TGFFSanityChecks(inputs);
        if (sanityCheckStatus != null) {
            System.out.println("ERROR: Failed the following TGFF sanity check:");
            throw new RuntimeException(sanityCheckStatus);
        }

        // create input file (.tgffopt)
        StringBuilder fileContent = buildInputFile(inputs, utils);
        System.out.println(fileContent);

        String inputDirPath = System.getProperty("user.dir") + "/TGFF-FILES/inputs/";
        File inputDir = new File(inputDirPath);

        //check if inputs directory exists
        if (!inputDir.exists()) {
            boolean dirCreated = inputDir.mkdirs();
            if (!dirCreated) {
                System.out.println("Failed creating directory for TGFF file at " + inputDirPath);
                System.exit(-1);
            }
        }

        String inputFilename = "test";
        //check if the file already exists
        File inputFile = new File(inputDir, inputFilename + ".tgffopt");

        while (inputFile.exists()) {
            // find a new random file name that doesn't already exist in the directory
            inputFilename = "test" + utils.random.nextLong();
            inputFile = new File(inputDir, inputFilename + ".tgffopt");
        }

        try {
            //create the new tgffopt file
            boolean fileCreated = inputFile.createNewFile();
            if (!fileCreated) {
                System.err.println("Failed creating file for TGFF at " + inputDirPath);
                System.exit(-1);
            }
            FileWriter writer = new FileWriter(inputFile);
            writer.write(fileContent.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Error creating file: " + e.getMessage());
        }

        // Get the correct TGFF path
        String relativeTGFFExePath = "/TGFF-FILES/tgff3_1.exe";
        // Copy tgff to a temporary location
        String tgffDir = System.getProperty("user.dir") + "/TGFF-FILES/";
        File tgffEXE = new File(tgffDir, "tgff3_1.exe");
        try (InputStream inputStream = TGFFGraphGeneration.class.getResourceAsStream(relativeTGFFExePath)) {
            if (inputStream != null) {
                Files.copy(inputStream, tgffEXE.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("ERROR: Failed to extract TGFF-EXE from resources: " + e.getMessage());
            try {
                System.out.println("Attempting to extract TGFF-EXE again...");
                Thread.sleep(5000);
                try (InputStream inputStream = TGFFGraphGeneration.class.getResourceAsStream(relativeTGFFExePath)) {
                    if (inputStream != null) {
                        Files.copy(inputStream, tgffEXE.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException error) {
                    throw new RuntimeException(error.getMessage());
                }
            } catch (InterruptedException error) {
                throw new RuntimeException(error.getMessage());
            }
        }

        utils.DebugPrint("inputPath: " + inputFile.getAbsolutePath());
        utils.DebugPrint("used input path: " + "/inputs/" + inputFilename);
        utils.DebugPrint("tgff path: " + tgffEXE.getAbsolutePath());

        String userRelativeInputFile = tgffDir + "/inputs/" + inputFilename;

        // Run TGFF
        try {
            ProcessBuilder pb = new ProcessBuilder(tgffEXE.getAbsolutePath(), userRelativeInputFile);
            pb.directory(null);
            Process process = pb.start();
            // Wait for TGFF to finish
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // Read the result from the file
                File resultFile = new File(userRelativeInputFile + ".tgff");
                try (BufferedReader reader = new BufferedReader(new FileReader(resultFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        utils.DebugPrint(line);
                    }
                }
                process.destroy();
            } else {
                System.out.println("TGFF returned exit code " + exitCode);
                System.out.println(process.getErrorStream());
                System.exit(-1);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error during TGFF execution: " + e.getMessage());
        }

        // return the path to the output file
        return userRelativeInputFile + ".tgff";
    }

    /**
     * Builds the TGFF input file according to the TGFF input parameters specified by the user.
     *
     * @param inputs the TGFF input parameter object.
     * @param utils  the Utils object.
     * @return the content of the Input file for TGFF
     */
    private static StringBuilder buildInputFile(TGFFInputParameters inputs, Utils utils) {

        StringBuilder fileContent = new StringBuilder();
        //set seed
        fileContent.append("seed ").append(utils.random.nextInt(10000)).append("\n");
        //set graph count
        fileContent.append("tg_cnt ").append(inputs.getGraphCount()).append("\n");
        //set task count
        fileContent.append("task_cnt ").append(inputs.getTaskCountAverage()).append(" ").append(inputs.getTaskCountMultiplier()).append("\n");
        //set periodicity
        fileContent.append("prob_periodic ").append(inputs.getPeriodPercentage()).append("\n");
        //set multiple start nodes
        fileContent.append("prob_multi_start_nodes ").append(inputs.getMultiStartNodeProb()).append("\n");
        //set start node num
        fileContent.append("start_node ").append(inputs.getStartNodesAverage()).append(" ").append(inputs.getStartNodesMultiplier()).append("\n");
        //set task degree
        fileContent.append("task_degree ").append(inputs.getTaskDegreeIN()).append(" ").append(inputs.getTaskDegreeOUT()).append("\n");
        //set hard deadline probability
        fileContent.append("prob_hard_deadline ").append(inputs.getHardDeadlineProb()).append("\n");
        //set tasks to be unique
        fileContent.append("task_unique true\n");
        //set task transition time
        fileContent.append("task_trans_time ").append(inputs.getTaskTransitionTime()).append("\n");
        //set period_mul parameter
        fileContent.append("period_mul ");
        for (int i = 0; i < inputs.getPeriod_mul().size(); i++) {
            if (i == inputs.getPeriod_mul().size() - 1) {
                fileContent.append(inputs.getPeriod_mul().get(i)).append("\n");
            } else {
                fileContent.append(inputs.getPeriod_mul().get(i)).append(",");
            }
        }
        fileContent.append("period_laxity 1.0\n");
        fileContent.append("period_g_deadline true\n");
        fileContent.append("deadline_jitter 0.0\n");
        //write output to tg
        fileContent.append("tg_write\n");
        fileContent.append("eps_write\n");
        return fileContent;
    }

    /**
     * Reads the TGFF output file and optionally deletes the TGFF constructed files.
     *
     * @param path         the path where the TGFF output file is located.
     * @param cleanUpFiles boolean that controls whether to delete TGFF generated files.
     * @param warnings     the Warnings object.
     * @return the content of the TGFF output file.
     * @throws Exception if the file path is null.
     */
    public static String readTGFF(String path, boolean cleanUpFiles, ArrayList<String> warnings) throws Exception {

        if (path == null) throw new Exception("File path is null");
        File tgffFile = new File(path);
        StringBuilder content = new StringBuilder();

        // read the .tgff file
        try (Scanner scanner = new Scanner(tgffFile)) {
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append(System.lineSeparator());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }

        if (cleanUpFiles) {
            // delete the generated TGFF files
            String pathPrefix = path.substring(0, path.indexOf(".tgff"));
            File tgffoptFile = new File(pathPrefix + ".tgffopt");
            File epsFile = new File(pathPrefix + ".eps");
            File vcgFile = new File(pathPrefix + ".vcg");
            boolean deleted = tgffFile.delete(); //delete .tgff
            if (!deleted) {
                System.out.println("WARNING: could not delete .tgff File");
                warnings.add("WARNING: could not delete .tgff File");
            }
            deleted = tgffoptFile.delete(); //delete .tgff
            if (!deleted) {
                System.out.println("WARNING: could not delete .tgffopt File");
                warnings.add("WARNING: could not delete .tgffopt File");
            }
            deleted = epsFile.delete(); //delete .tgff
            if (!deleted) {
                System.out.println("WARNING: could not delete .eps File");
                warnings.add("WARNING: could not delete .eps File");
            }
            deleted = vcgFile.delete(); //delete .tgff
            if (!deleted) {
                System.out.println("WARNING: could not delete .vcg File");
                warnings.add("WARNING: could not delete .vcg File");
            }

        }

        // return the content of the tgff file
        return content.toString();
    }

    /**
     * Wrapper function for the modes of reading the TGFF output content. Returns the TGFF-based task set.
     *
     * @param content     the content of the TGFF output file.
     * @param calcPeriods boolean that controls whether we use TGFF periods (false) or calculate our own periods (true).
     * @param period_mul  the period multiplier list from the input parameters.
     * @param utils       the Utils object.
     * @param warnings    the Warnings object.
     * @return the generated TGFF-based Task set.
     * @throws Exception if the task set is empty.
     */
    public static TGFFTaskSet readGraphs(String content, boolean calcPeriods, List<Integer> period_mul, Utils utils, ArrayList<String> warnings) throws Exception {

        TGFFTaskSet tgffTaskSet;
        // call correct function depending on calcPeriods
        if (calcPeriods) {
            tgffTaskSet = readGraphsAndCalculatePeriods(content, period_mul, utils, warnings);
        } else {
            tgffTaskSet = readGraphsWithTGFFPeriods(content, utils, warnings);
        }

        if (tgffTaskSet.getTaskList().isEmpty())
            throw new RuntimeException("ERROR: Failure during TGFF task set generation.");
        return tgffTaskSet;
    }

    /**
     * Creates a task set from the content of the TGFF output file. Calculates periods for each graph.
     *
     * @param content    the content of the TGFF output file.
     * @param period_mul the period multiplier list from the input parameters.
     * @param utils      the Utils object.
     * @param warnings   the Warnings object.
     * @return the generated TGFF-based Task set.
     * @throws Exception if the TGFF content does not follow our expected template.
     */
    public static TGFFTaskSet readGraphsAndCalculatePeriods(String content, List<Integer> period_mul, Utils utils, ArrayList<String> warnings) throws Exception {

        String taskGraphStart = "@TASK_GRAPH";

        if (!content.contains(taskGraphStart)) {
            throw new Exception("TGFF File does not contain a Task Graph with the Label: \"@TASK_GRAPH\"");
        }

        int taskGraphStartIndex = content.indexOf(taskGraphStart);
        int taskGraphEndIndex = content.indexOf("}", taskGraphStartIndex);

        ArrayList<Task> taskList = new ArrayList<>();
        Map<String, Integer> taskStringToTaskID = new HashMap<>();
        int identifier = 0;
        int currentSubgraphStartIdentifier = 0; // the first task of the current subgraph
        int numberOfSubgraphs = 0;          // the number of subgraphs
        int largestSubgraphPeriod = 0;      // the largest subgraph period
        Map<Integer, ArrayList<Integer>> subgraphToTaskIds = new HashMap<>();    // maps subgraph index to taskIDs
        ArrayList<Integer> subgraphLengths = new ArrayList<>();         // contains the lengths of the subgraphs

        // Read graph by graph
        while (taskGraphStartIndex != -1) {
            numberOfSubgraphs++;
            subgraphToTaskIds.put(numberOfSubgraphs, new ArrayList<>());

            // Read all tasks in the graph
            ArrayList<String> taskStrings = getStringsFromPrefix(taskGraphStartIndex, taskGraphEndIndex, content, "TASK ");
            // Read all Arcs in the graph
            ArrayList<String> ArcStrings = getStringsFromPrefix(taskGraphStartIndex, taskGraphEndIndex, content, "ARC ");
            if (taskStrings.isEmpty()) {
                throw new RuntimeException("ERROR: Could not read tasks from subgraph. Make sure the generated TGFF file matches the required format.\n\nTGFF-FILE:\n" + content);
            }

            // Create tasks
            for (String tString : taskStrings) {
                int startIndex = tString.indexOf("t");
                int endIndex = tString.indexOf("\t", startIndex);
                String taskName = tString.substring(startIndex, endIndex);

                Task newTask = new Task(
                        -1,
                        false,
                        identifier,
                        false,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>()
                );
                identifier++;

                // add the task to list
                taskList.add(newTask);
                taskStringToTaskID.put(taskName, newTask.getIdentifier());
                // add task to subgraph map
                subgraphToTaskIds.get(numberOfSubgraphs).add(newTask.getIdentifier());

            }

            // add Precedence relations from ArcStrings
            addPrecedenceRelations(ArcStrings, taskStringToTaskID, taskList, warnings);

            // find the largest chain of tasks, and select a period for the subgraph based on this chain
            // iterate through the tasks of the current subgraph
            int maximumDepth = 1;
            for (int i = currentSubgraphStartIdentifier; i < taskList.size(); i++) {
                Task currentTask = taskList.get(i);
                if (!currentTask.getPredecessors().isEmpty())
                    continue; // since TGFF generates DAGs, the largest chain must start with a node that has no predecessors
                // iterate through all successors to find the largest chain
                int depth = BasicPrecedenceConstraints.dfsFindHeight(currentTask, taskList, 1);
                maximumDepth = Math.max(maximumDepth, depth);
            }
            subgraphLengths.add(maximumDepth);
            utils.DebugPrint("Subgraph has the length: " + maximumDepth);
            largestSubgraphPeriod = Math.max(largestSubgraphPeriod, maximumDepth);

            // update String indices
            taskGraphStartIndex = content.indexOf(taskGraphStart, taskGraphEndIndex);
            taskGraphEndIndex = content.indexOf("}", taskGraphStartIndex);
            currentSubgraphStartIdentifier = identifier;
        }

        if (utils.debug) {
            subgraphToTaskIds.values().forEach(e -> {
                System.out.println("\nSubgraph:");
                System.out.println(Arrays.toString(e.toArray()));
            });
        }

        // assign all tasks a multiple of the largest period
        Random random = new Random();
        int subgraphIndex = 0;
        for (ArrayList<Integer> subgraphTaskList : subgraphToTaskIds.values()) {
            int chosenPeriod = period_mul.get(random.nextInt(0, period_mul.size())) * largestSubgraphPeriod;
            utils.DebugPrint("Chosen period for subgraph: " + chosenPeriod);
            for (int taskID : subgraphTaskList) {
                taskList.get(taskID).setPeriodic(true);
                taskList.get(taskID).setPeriod(chosenPeriod);
                taskList.get(taskID).setSubGraphLength(subgraphLengths.get(subgraphIndex));
            }
            subgraphIndex++;

        }

        return new TGFFTaskSet(taskList.size(), taskList, numberOfSubgraphs, subgraphToTaskIds);

    }

    /**
     * Creates a task set from the content of the TGFF output file. Uses the TGFF generated periods.
     *
     * @param content  the content of the TGFF output file.
     * @param utils    the Utils object.
     * @param warnings the Warnings object.
     * @return the generated TGFF-based Task set.
     * @throws Exception if the TGFF content does not follow our expected template.
     */
    public static TGFFTaskSet readGraphsWithTGFFPeriods(String content, Utils utils, ArrayList<String> warnings) throws Exception {

        String taskGraphStart = "@TASK_GRAPH";

        if (!content.contains(taskGraphStart)) {
            throw new Exception("TGFF File does not contain a Task Graph with the Label: \"@TASK_GRAPH\"");
        }

        int taskGraphStartIndex = content.indexOf(taskGraphStart);
        int taskGraphEndIndex = content.indexOf("}", taskGraphStartIndex);

        ArrayList<Task> taskList = new ArrayList<>();
        Map<String, Integer> taskStringToTaskID = new HashMap<>();
        int identifier = 0;
        int totalPeriodMultiplier = 0;      // this value is used for scaling the periods if TGFF generates floating point periods
        int numberOfSubgraphs = 0;          // the number of subgraphs
        Map<Integer, ArrayList<Integer>> subgraphToTaskIds = new HashMap<>();    // maps subgraph index to taskIDs

        // Read graph by graph
        while (taskGraphStartIndex != -1) {
            numberOfSubgraphs++;
            subgraphToTaskIds.put(numberOfSubgraphs, new ArrayList<>());

            int taskGraphPeriod = -1;
            double floatingPointTaskGraphPeriod = -1.0;     // we need this to catch potential floating point periods that would crash the program
            // Read period
            String PERIOD = "PERIOD";
            if (content.contains(PERIOD)) {

                String taskgraphsubstring = content.substring(taskGraphStartIndex, taskGraphEndIndex);
                taskgraphsubstring = taskgraphsubstring.toUpperCase();

                if (taskgraphsubstring.contains(PERIOD)) {
                    int periodIndex = taskgraphsubstring.indexOf(PERIOD);
                    if (periodIndex == -1) throw new Exception("Period not found in Task Graph");
                    int periodStartIndex = taskgraphsubstring.indexOf(" ", periodIndex) + 1;
                    int periodEndIndex = taskgraphsubstring.indexOf("\n", periodStartIndex) - 1;
                    try {
                        taskGraphPeriod = Integer.parseInt(taskgraphsubstring.trim().substring(periodStartIndex, periodEndIndex));
                    } catch (NumberFormatException e) {
                        floatingPointTaskGraphPeriod = Double.parseDouble(taskgraphsubstring.trim().substring(periodStartIndex, periodEndIndex));
                        System.out.println("FOUND FLOATING POINT PERIOD: " + floatingPointTaskGraphPeriod);
                    }
                }
            } else {
                throw new Exception("Period not found in Task Graph");

            }

            // Read all tasks in the graph
            ArrayList<String> taskStrings = getStringsFromPrefix(taskGraphStartIndex, taskGraphEndIndex, content, "TASK ");
            // Read all Arcs in the graph
            ArrayList<String> ArcStrings = getStringsFromPrefix(taskGraphStartIndex, taskGraphEndIndex, content, "ARC ");

            // adjust the found period if necessary
            if (floatingPointTaskGraphPeriod != -1.0) {
                // find the least common multiple between floatingPointTaskGraphPeriod and 1.0
                int multiplier = 1;
                double calc = floatingPointTaskGraphPeriod * (double) multiplier;
                while (calc % 1.0 != 0) {
                    multiplier++;
                    calc = floatingPointTaskGraphPeriod * (double) multiplier;
                }
                // update the totalPeriodMultiplier
                if (totalPeriodMultiplier < multiplier) totalPeriodMultiplier = multiplier;

                // adjust taskGraphPeriod
                taskGraphPeriod = (int) (floatingPointTaskGraphPeriod * totalPeriodMultiplier);
                System.out.println("ADJUSTED PERIOD: " + taskGraphPeriod);
            }

            if (taskStrings.isEmpty()) {
                throw new RuntimeException("ERROR: Could not read tasks from subgraph. Make sure the generated TGFF file matches the required format.\n\nTGFF-FILE:\n" + content);
            }

            // Create tasks
            for (String tString : taskStrings) {
                int startIndex = tString.indexOf("t");
                int endIndex = tString.indexOf("\t", startIndex);
                String taskName = tString.substring(startIndex, endIndex);

                Task newTask = new Task(
                        -1,
                        false,
                        identifier,
                        false,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>()
                );
                identifier++;
                // use the TGFF generated periods
                if (taskGraphPeriod >= 1) {
                    newTask.setPeriodic(true);
                    newTask.setPeriod(taskGraphPeriod);
                }

                // add the task to list
                taskList.add(newTask);
                taskStringToTaskID.put(taskName, newTask.getIdentifier());
                // add task to subgraph map
                subgraphToTaskIds.get(numberOfSubgraphs).add(newTask.getIdentifier());
            }

            // add Precedence relations from ArcStrings
            addPrecedenceRelations(ArcStrings, taskStringToTaskID, taskList, warnings);

            // update String indices
            taskGraphStartIndex = content.indexOf(taskGraphStart, taskGraphEndIndex);
            taskGraphEndIndex = content.indexOf("}", taskGraphStartIndex);
        }

        if (utils.debug) {
            subgraphToTaskIds.values().forEach(e -> {
                System.out.println("\nSubgraph:");
                System.out.println(Arrays.toString(e.toArray()));
            });
        }

        return new TGFFTaskSet(taskList.size(), taskList, numberOfSubgraphs, subgraphToTaskIds);

    }

    /**
     * Adds precedence relations depending on the Arc Strings read from the TGFF file content.
     *
     * @param ArcStrings         the arc string list.
     * @param taskStringToTaskID maps the task strings to the task ids.
     * @param taskList           the list of tasks.
     * @param warnings           the Warnings object.
     */
    private static void addPrecedenceRelations(ArrayList<String> ArcStrings, Map<String, Integer> taskStringToTaskID, ArrayList<Task> taskList, ArrayList<String> warnings) {

        if (ArcStrings.isEmpty()) {
            System.out.println("WARNING: ArcStrings Array is empty. Could not generate any precedence relations between tasks.");
            warnings.add("WARNING: ArcStrings Array is empty. Could not generate any precedence relations between tasks.");
            return;
        }

        // add precedence constraints between the tasks
        for (String aString : ArcStrings) {
            // read from and to from ArcString
            int fromIndexStart = aString.indexOf("t");
            int fromIndexEnd = aString.indexOf(" ", fromIndexStart);
            String fromString = aString.substring(fromIndexStart, fromIndexEnd);

            int toIndexStart = aString.indexOf("t", fromIndexEnd);
            int toIndexEnd = aString.indexOf(" ", toIndexStart);
            String toString = aString.substring(toIndexStart, toIndexEnd);

            int fromTaskID = taskStringToTaskID.get(fromString);
            int toTaskID = taskStringToTaskID.get(toString);

            // set predecessors and successors
            taskList.get(toTaskID).addPredecessor(fromTaskID);
            taskList.get(fromTaskID).addSuccessors(toTaskID);
        }
    }

    /**
     * Returns the object strings depending on the chosen prefix.
     *
     * @param taskGraphStartIndex the start index.
     * @param taskGraphEndIndex   the end index.
     * @param content             the content of the TGFF output file.
     * @param PREFIX              the chosen prefix.
     * @return the object strings depending on the chosen prefix.
     */
    private static ArrayList<String> getStringsFromPrefix(int taskGraphStartIndex, int taskGraphEndIndex, String content, String PREFIX) {
        int index = taskGraphStartIndex;
        ArrayList<String> objectStrings = new ArrayList<>();

        while (index < taskGraphEndIndex) {
            int stringIndex = content.indexOf(PREFIX, index);

            if (stringIndex > taskGraphEndIndex || stringIndex == -1) break;

            int linebreakIndex = content.indexOf("\n", stringIndex);

            objectStrings.add(content.substring(stringIndex, linebreakIndex - 1));
            index = linebreakIndex;
        }
        return objectStrings;
    }


}
