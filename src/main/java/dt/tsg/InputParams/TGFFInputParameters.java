package dt.tsg.InputParams;

import java.util.List;

public class TGFFInputParameters extends InputParameters {

    private int graphCount; // the number of subgraphs that TGFF will generate
    private int taskCountAverage;   // the average minimum number of tasks per graph
    private int taskCountMultiplier;    // multiplier for the average number of tasks per graph
    private double periodPercentage = 1.0;    // percentage of periodic tasks for TGFF, default = 1.0
    private double multiStartNodeProb;   // probability that a subgraph has multiple start nodes
    private int startNodesAverage;      // the average number of start nodes if the subgraph contains multiple start nodes
    private int startNodesMultiplier;   // multiplier for the average number of start nodes
    private int taskDegreeIN;       // the maximum in-degree for all tasks
    private int taskDegreeOUT;      // the maximum out-degree for all tasks
    private double hardDeadlineProb = 1.0;    // the probability for all deadlines to be hard-deadlines, default = 1.0
    private int numberOfResources;      // the number of resources
    private double taskTransitionTime = 1.0;  // the task transition time, default = 1.0
    private List<Integer> period_mul;   // List of Integers that contain multipliers for the period of each subgraph
    private boolean cleanUpFiles = true;       // if true, clean up files after generation, default = true
    private boolean postHocResidency = true;  // if true, use postHoc residency constraints, default = true
    private boolean calculatePeriods = true;   // if true, calculate our own periods for each subgraph instead of using TGFF generated periods, default = true
    private boolean multiResidency = false;     // if true, tasks that are residency constrained can be restricted to multiple resources.
    private int numberOfWorkerThreads = 1;
    private int solutionCount = 1;
    private boolean debug = false;
    private boolean saveOutput = false;

    public TGFFInputParameters() {
        // empty Constructor
    }

    public int getGraphCount() {
        return graphCount;
    }

    public void setGraphCount(int graphCount) {
        this.graphCount = graphCount;
    }

    public int getTaskCountAverage() {
        return taskCountAverage;
    }

    public void setTaskCountAverage(int taskCountAverage) {
        this.taskCountAverage = taskCountAverage;
    }

    public int getTaskCountMultiplier() {
        return taskCountMultiplier;
    }

    public void setTaskCountMultiplier(int taskCountMultiplier) {
        this.taskCountMultiplier = taskCountMultiplier;
    }

    public double getPeriodPercentage() {
        return periodPercentage;
    }

    public void setPeriodPercentage(double periodPercentage) {
        this.periodPercentage = periodPercentage;
    }

    public double getMultiStartNodeProb() {
        return multiStartNodeProb;
    }

    public void setMultiStartNodeProb(double multiStartNodeProb) {
        this.multiStartNodeProb = multiStartNodeProb;
    }

    public int getStartNodesAverage() {
        return startNodesAverage;
    }

    public void setStartNodesAverage(int startNodesAverage) {
        this.startNodesAverage = startNodesAverage;
    }

    public int getStartNodesMultiplier() {
        return startNodesMultiplier;
    }

    public void setStartNodesMultiplier(int startNodesMultiplier) {
        this.startNodesMultiplier = startNodesMultiplier;
    }

    public int getTaskDegreeIN() {
        return taskDegreeIN;
    }

    public void setTaskDegreeIN(int taskDegreeIN) {
        this.taskDegreeIN = taskDegreeIN;
    }

    public int getTaskDegreeOUT() {
        return taskDegreeOUT;
    }

    public void setTaskDegreeOUT(int taskDegreeOUT) {
        this.taskDegreeOUT = taskDegreeOUT;
    }

    public double getHardDeadlineProb() {
        return hardDeadlineProb;
    }

    public void setHardDeadlineProb(double hardDeadlineProb) {
        this.hardDeadlineProb = hardDeadlineProb;
    }

    public int getNumberOfResources() {
        return numberOfResources;
    }

    public void setNumberOfResources(int numberOfResources) {
        this.numberOfResources = numberOfResources;
    }

    public double getTaskTransitionTime() {
        return taskTransitionTime;
    }

    public void setTaskTransitionTime(double taskTransitionTime) {
        this.taskTransitionTime = taskTransitionTime;
    }

    public List<Integer> getPeriod_mul() {
        return period_mul;
    }

    public void setPeriod_mul(List<Integer> period_mul) {
        this.period_mul = period_mul;
    }

    public boolean isCleanUpFiles() {
        return cleanUpFiles;
    }

    public void setCleanUpFiles(boolean cleanUpFiles) {
        this.cleanUpFiles = cleanUpFiles;
    }

    @Override
    public boolean PostHocResidency() {
        return postHocResidency;
    }

    public void setPostHocResidency(boolean postHocResidency) {
        this.postHocResidency = postHocResidency;
    }

    public boolean isCalculatePeriods() {
        return calculatePeriods;
    }

    public void setCalculatePeriods(boolean calculatePeriods) {
        this.calculatePeriods = calculatePeriods;
    }

    @Override
    public boolean MultiResidency() {
        return multiResidency;
    }

    @Override
    public void setMultiResidency(boolean multiResidency) {
        this.multiResidency = multiResidency;
    }


    @Override
    public boolean PhasedReleaseTimes() {
        // always false, since phased releaseTimes are not implemented for TGFF
        return false;
    }

    @Override
    public boolean PostHocPrecedence() {
        // always false, since we do not support postHoc precedence constraints for TGFF
        return false;
    }


    @Override
    public String toString() {
        return "TGFFInputParameters{" +
                "\ngraphCount=" + graphCount +
                ", \ntaskCountAverage=" + taskCountAverage +
                ", \ntaskCountMultiplier=" + taskCountMultiplier +
                ", \nperiodPercentage=" + periodPercentage +
                ", \nmultiStartNodeProb=" + multiStartNodeProb +
                ", \nstartNodesAverage=" + startNodesAverage +
                ", \nstartNodesMultiplier=" + startNodesMultiplier +
                ", \ntaskDegreeIN=" + taskDegreeIN +
                ", \ntaskDegreeOUT=" + taskDegreeOUT +
                ", \nhardDeadlineProb=" + hardDeadlineProb +
                ", \nnumberOfResources=" + numberOfResources +
                ", \ntaskTransitionTime=" + taskTransitionTime +
                ", \nperiod_mul=" + period_mul +
                ", \ncleanUpFiles=" + cleanUpFiles +
                ", \npostHocResidency=" + postHocResidency +
                ", \ncalculatePeriods=" + calculatePeriods +
                ", \nmultiResidency=" + multiResidency +
                ", \nnumberOfWorkerThreads=" + numberOfWorkerThreads +
                ", \nsolutionCount=" + solutionCount +
                ", \ndebug=" + debug +
                ", \nsaveOutput=" + saveOutput +
                '}';
    }

    @Override
    public int getNumberOfWorkerThreads() {
        return numberOfWorkerThreads;
    }

    @Override
    public void setNumberOfWorkerThreads(int numberOfWorkerThreads) {
        this.numberOfWorkerThreads = numberOfWorkerThreads;
    }

    @Override
    public int getSolutionCount() {
        return solutionCount;
    }

    @Override
    public void setSolutionCount(int solutionCount) {
        this.solutionCount = solutionCount;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public boolean isSaveOutput() {
        return saveOutput;
    }

    @Override
    public void setSaveOutput(boolean saveOutput) {
        this.saveOutput = saveOutput;
    }
}
