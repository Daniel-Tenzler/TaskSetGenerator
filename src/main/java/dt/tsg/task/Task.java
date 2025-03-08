package dt.tsg.task;

import dt.tsg.InputParams.InputParameters;

import java.util.ArrayList;
import java.util.Random;

/**
 * A Task is a job scheduled on a resource. It can have multiple different classifiers (constraints).
 */
public class Task {

    private int phase;                      //Time it takes for the first instance of a task to be released
    private int releaseTime;                //Time at which the task is first released/released after its period
    private int deadline;                   //Time at which task must be completed
    private int period;                     //Time at which a periodic task is repeated
    private boolean periodic;               //Designates the task as being periodic or aperiodic
    private int identifier;                 //Identifier that must be unique within the task set
    private int WCET;                       //The Worst-Case Execution Time for the task
    // multi residency constraints
    private boolean residency_constrained;  //Determines if the task can only be scheduled on specific resources
    private ArrayList<Integer> resource_identifiers = new ArrayList<>();       //Contains resource identifiers on which the task can only be scheduled
    // precedence relations
    private ArrayList<Integer> successors = new ArrayList<>();               //Task(s) that directly succeed this task
    private ArrayList<Integer> predecessors = new ArrayList<>();            //Task(s) that directly precede this task
    private int subGraphLength = 1;         //The length of the subgraph this task belongs to

    /**
     * Empty constructor, only requires identifier
     *
     * @param identifier taskID
     */
    public Task(int identifier) {
        this.identifier = identifier;
    }

    public Task(int identifier, int period, boolean periodic, boolean residencyConstrained) {
        this.identifier = identifier;
        this.period = period;
        this.periodic = periodic;
        this.residency_constrained = residencyConstrained;
    }

    public Task(int period, boolean periodic, int identifier, boolean residency_constrained, ArrayList<Integer> resource_identifiers, ArrayList<Integer> successors, ArrayList<Integer> predecessors) {
        this.period = period;
        this.periodic = periodic;
        this.identifier = identifier;
        this.residency_constrained = residency_constrained;
        this.resource_identifiers = resource_identifiers;
        this.successors = successors;
        this.predecessors = predecessors;
    }

    @Deprecated
    public void calculateReleaseTime(InputParameters constraints) {
        Task task = this;
        Random random = new Random();

        // RELEASE TIME
        if (task.isPeriodic()) {
            // Task is periodic, release time and deadline depend on the period
            if (constraints.getMin_releaseTime() > task.getPeriod() + task.getWCET() && constraints.getMin_releaseTime() > 0) {
                // task is not schedulable with the specified min release time.
                task.setReleaseTime(constraints.getMin_releaseTime() - 1);
            } else if (constraints.getMin_releaseTime() > task.getPeriod() + task.getWCET() && constraints.getMin_releaseTime() == 0) {
                // task is not schedulable even with adjustments
                System.out.println("Specified minimum releasetime, period and WCET make the task un-schedulable");
                System.out.println("ReleaseTimeMinimum: " + constraints.getMin_releaseTime() + " TaskPeriod: " + task.getPeriod() + " TaskWCET " + task.getWCET());
                System.exit(-1);
            } else if (constraints.getMax_releaseTime() > task.getPeriod() + task.getWCET()) { // MinReleaseTime is ok, now test max Release time
                // The MaxReleaseTime is too high
                // catch random.nextInt(X,Y) where X==Y
                if (constraints.getMin_releaseTime() < task.getPeriod() - task.getWCET()) {
                    task.setReleaseTime(random.nextInt(constraints.getMin_releaseTime(), task.getPeriod() - task.getWCET() + 1));
                } else {
                    task.setReleaseTime(constraints.getMin_releaseTime());
                }
            } else {
                // Min and Max values are not out of bounds
                task.setReleaseTime(random.nextInt(constraints.getMin_releaseTime(), constraints.getMax_releaseTime() + 1));
            }

        } else {
            // Task is not periodic release time and deadline just have to be within the hyperperiod / limiter, but they are not constructed yet
            task.setReleaseTime(random.nextInt(constraints.getMin_releaseTime(), constraints.getMax_releaseTime() + 1));
        }
    }

    @Deprecated
    public void calculateDeadline(InputParameters constraints) {
        Task task = this;
        Random random = new Random();

        // DEADLINE
        if (task.isPeriodic()) {
            // Task is periodic, deadline is limited by the period and the chosen releasetime
            if (task.getPeriod() < constraints.getMin_deadline() || task.getReleaseTime() + task.getWCET() > constraints.getMax_deadline()) {
                // task is not schedulable with the chosen min_deadline, task-period and taskWCET
                System.out.println("Specified minimum deadline, period and WCET make the task un-schedulable");
                System.out.println("Deadline-Minimum: " + constraints.getMin_deadline() + " TaskPeriod: " + task.getPeriod() + " TaskWCET " + task.getWCET());
                System.exit(-1);
            } else if (constraints.getMax_deadline() > task.getPeriod()) {
                // Period is smaller than max deadline, set max_deadline -> period
                if (constraints.getMin_deadline() > task.getPeriod()) {
                    // the Period is smaller than the min deadline, the task cannot be scheduled with the current min/max values and period
                    System.out.println("Specified minimum deadline, period and WCET make the task un-schedulable, since the period is smaller than the minimum allowed deadline");
                    System.out.println("Deadline-Minimum: " + constraints.getMin_deadline() + " TaskPeriod: " + task.getPeriod() + " TaskWCET " + task.getWCET());
                    System.exit(-1);
                } else {

                    // catch random.nextInt(X,Y) where X==Y
                    if (constraints.getMin_deadline() == task.getPeriod()) {
                        task.setDeadline(task.getPeriod());
                    } else {
                        task.setDeadline(random.nextInt(constraints.getMin_deadline(), task.getPeriod() + 1));
                    }
                }
            } else {
                // Min and Max values are not out of bounds
                task.setDeadline(random.nextInt(constraints.getMin_deadline(), constraints.getMax_deadline() + 1));
            }
        } else {
            // Task is not periodic
            task.setDeadline(random.nextInt(constraints.getMin_deadline(), constraints.getMax_deadline() + 1));
        }
    }

    @Override
    protected Task clone() throws CloneNotSupportedException {
        Task cloneTask = new Task(this.identifier);
        cloneTask.setPhase(this.phase);
        cloneTask.setReleaseTime(this.releaseTime);
        cloneTask.setDeadline(this.deadline);
        cloneTask.setPeriodic(this.periodic);
        cloneTask.setPeriod(this.period);
        cloneTask.setWCET(this.WCET);
        cloneTask.setResidency_constrained(this.residency_constrained);
        cloneTask.setSubGraphLength(this.subGraphLength);
        cloneTask.addSuccessors(this.successors);
        cloneTask.addPredecessor(this.predecessors);
        cloneTask.addResourceConstraint(this.resource_identifiers);
        return cloneTask;
    }


    public void addPredecessor(int predID) {
        if (this.identifier == predID)
            throw new RuntimeException("ERROR: TaskID and predecessorID cannot be the same!");
        if (this.predecessors == null) this.predecessors = new ArrayList<>();
        if (!this.predecessors.contains(predID)) {
            this.predecessors.add(predID);
        }
    }

    public void addPredecessor(ArrayList<Integer> predID) {
        if (predID.isEmpty()) return;
        for (int i : predID) {
            if (!this.predecessors.contains(i)) this.addPredecessor(i);
        }
    }

    public void addSuccessors(int sucID) {
        if (this.identifier == sucID) throw new RuntimeException("ERROR: TaskID and successorID cannot be the same!");
        if (this.successors == null) this.successors = new ArrayList<>();
        if (!this.successors.contains(sucID)) {
            this.successors.add(sucID);
        }
    }

    public void removeSuccessor(int sucID) {
        if (this.successors == null) this.successors = new ArrayList<>();
        if (this.successors.contains(sucID)) {
            for (int i = 0; i < this.successors.size(); i++) {
                if (this.successors.get(i) == sucID) {
                    this.successors.remove(i);
                    break;
                }
            }
        }
    }

    public void addSuccessors(ArrayList<Integer> successors) {
        if (successors.isEmpty()) return;
        for (int i : successors) {
            if (!this.successors.contains(i)) this.addSuccessors(i);
        }
    }


    public void addResourceConstraint(int resource_id) {
        if (this.resource_identifiers == null) this.resource_identifiers = new ArrayList<>();
        if (!this.resource_identifiers.contains(resource_id)) {
            this.resource_identifiers.add(resource_id);
            this.residency_constrained = true;
        }
    }

    /**
     * removes all resource constraints from this task, and sets residency_constrained to false if a resource constraint was removed.
     */
    public void removeResourceConstraints() {
        if (this.resource_identifiers == null) this.resource_identifiers = new ArrayList<>();
        if (!this.resource_identifiers.isEmpty()) {
            this.resource_identifiers = new ArrayList<>();
            this.residency_constrained = false;
        }
    }

    public void addResourceConstraint(int[] resource_id) {
        for (int i : resource_id) {
            this.addResourceConstraint(i);
        }
    }

    public void addResourceConstraint(ArrayList<Integer> resource_id) {
        for (int i : resource_id) {
            this.addResourceConstraint(i);
        }
    }


    public int getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(int releaseTime) {
        this.releaseTime = releaseTime;
    }


    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public boolean isPeriodic() {
        return periodic;
    }

    public void setPeriodic(boolean periodic) {
        this.periodic = periodic;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public boolean isResidency_constrained() {
        return residency_constrained;
    }

    public void setResidency_constrained(boolean residency_constrained) {
        this.residency_constrained = residency_constrained;
    }

    public int getWCET() {
        return WCET;
    }

    public void setWCET(int WCET) {
        this.WCET = WCET;
    }

    public ArrayList<Integer> getSuccessors() {
        return successors;
    }

    public ArrayList<Integer> getPredecessors() {
        return predecessors;
    }

    public ArrayList<Integer> getResourceConstraint() {
        return resource_identifiers;
    }

    @Override
    public String toString() {
        return "Task{" +
                "ID=" + identifier +
                ", phase=" + phase +
                ", releasetime=" + releaseTime +
                ", deadline=" + deadline +
                ", WCET=" + WCET +
                ", period=" + period +
                ", periodic=" + periodic +
                ", residency_constrained=" + residency_constrained +
                ", resource_identifiers=" + resource_identifiers +
                ", predecessors=" + predecessors +
                ", successors=" + successors +
                '}';
    }

    public int getSubGraphLength() {
        return subGraphLength;
    }

    public void setSubGraphLength(int subGraphLength) {
        this.subGraphLength = subGraphLength;
    }
}
