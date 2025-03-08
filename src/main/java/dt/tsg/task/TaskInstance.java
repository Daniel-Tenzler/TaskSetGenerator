package dt.tsg.task;

import java.util.ArrayList;

public class TaskInstance extends Task {

    private int instance_number;    // The instance number of the TaskInstance. Starts at 1 (The original task is implied to be 0)
    private int original_task_ID;   // The ID of the original periodic task of which this task is an instance of
    private ArrayList<Integer> original_predecessor;   // The predecessor of the original task
    private ArrayList<Integer> original_successors;   // The predecessor of the original task

    @Override
    protected TaskInstance clone() throws CloneNotSupportedException {
        TaskInstance cloneTask = new TaskInstance(this.getIdentifier());
        cloneTask.setPhase(this.getPhase());
        cloneTask.setReleaseTime(this.getReleaseTime());
        cloneTask.setDeadline(this.getDeadline());
        cloneTask.setPeriodic(this.isPeriodic());
        cloneTask.setPeriod(this.getPeriod());
        cloneTask.setWCET(this.getWCET());
        cloneTask.setResidency_constrained(this.isResidency_constrained());
        cloneTask.setSubGraphLength(this.getSubGraphLength());
        cloneTask.addSuccessors(this.getSuccessors());
        cloneTask.addPredecessor(this.getPredecessors());
        cloneTask.addResourceConstraint(this.getResourceConstraint());
        cloneTask.setInstance_number(this.instance_number);
        cloneTask.setOriginal_task_ID(this.original_task_ID);
        cloneTask.setOriginal_predecessor(this.original_predecessor);
        cloneTask.setOriginal_successors(this.original_successors);
        return cloneTask;
    }

    public TaskInstance(int identifier, int period, boolean periodic, boolean residency_constrained) {
        super(identifier, period, periodic, residency_constrained);
    }

    public TaskInstance(int identifier) {
        super(identifier);
    }

    public int getInstance_number() {
        return instance_number;
    }

    public void setInstance_number(int instance_number) {
        this.instance_number = instance_number;
    }

    public int getOriginal_task_ID() {
        return original_task_ID;
    }

    public void setOriginal_task_ID(int original_task_ID) {
        this.original_task_ID = original_task_ID;
    }

    public ArrayList<Integer> getOriginal_predecessor() {
        return original_predecessor;
    }

    public void setOriginal_predecessor(ArrayList<Integer> original_predecessor) {
        this.original_predecessor = original_predecessor;
    }

    public ArrayList<Integer> getOriginal_successors() {
        return original_successors;
    }

    public void setOriginal_successors(ArrayList<Integer> original_successors) {
        this.original_successors = original_successors;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", instance_num=" + instance_number +
                ", orig_taskID=" + original_task_ID +
                ", orig_predecessor=" + original_predecessor +
                ", orig_successors=" + original_successors +
                '}';
    }
}
