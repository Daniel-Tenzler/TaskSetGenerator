package dt.tsg.utils;

import com.google.ortools.sat.CpSolver;
import dt.tsg.cspModel.DynamicConstraints;
import dt.tsg.cspModel.ModelTask;
import dt.tsg.taskSet.TaskSet;

import java.util.ArrayList;
import java.util.Map;

public class GenerationResult {

    public ArrayList<Integer> startHints = new ArrayList<>();
    public ArrayList<Integer> wcetHints = new ArrayList<>();
    public ArrayList<Integer> endHints = new ArrayList<>();
    public ArrayList<Integer> resourceHints = new ArrayList<>();
    public ArrayList<Integer> dynamicHints = new ArrayList<>();

    public ArrayList<Integer> startValues = new ArrayList<>();
    public ArrayList<Integer> wcetValues = new ArrayList<>();
    public ArrayList<Integer> endValues = new ArrayList<>();
    public ArrayList<Integer> resourceValues = new ArrayList<>();
    public ArrayList<Integer> dynamicValues = new ArrayList<>();
    public TaskSet taskSet = null;

    public GenerationResult(ArrayList<Integer> startHints, ArrayList<Integer> wcetHints, ArrayList<Integer> endHints, ArrayList<Integer> dynamicHints) {
        this.startHints = startHints;
        this.wcetHints = wcetHints;
        this.endHints = endHints;
        this.dynamicHints = dynamicHints;
    }

    public GenerationResult() {
        // empty constructor
    }


    /**
     * Saves the generated values by the solver in this objects' lists.
     *
     * @param solver             the solver object.
     * @param dynamicConstraints the dynamic constraints object.
     * @param modelTaskMap       maps the task ids to the corresponding Model Tasks.
     */
    public void storeValues(CpSolver solver, DynamicConstraints dynamicConstraints, Map<Integer, ModelTask> modelTaskMap) {

        for (ModelTask modelTask : modelTaskMap.values()) {
            this.startValues.add((int) solver.value(modelTask.getStart()));
            this.wcetValues.add((int) solver.value(modelTask.getWcet()));
            this.endValues.add((int) solver.value(modelTask.getEnd()));
            this.resourceValues.add((int) solver.value(modelTask.getResource()));
        }

        this.dynamicValues.add((int) solver.value(dynamicConstraints.getCp_releasetime_min()));
        this.dynamicValues.add((int) solver.value(dynamicConstraints.getCp_releasetime_max()));
        this.dynamicValues.add((int) solver.value(dynamicConstraints.getCp_deadline_min()));
        this.dynamicValues.add((int) solver.value(dynamicConstraints.getCp_deadline_max()));
        this.dynamicValues.add((int) solver.value(dynamicConstraints.getCp_wcet_min()));
        this.dynamicValues.add((int) solver.value(dynamicConstraints.getCp_wcet_max()));


    }


}
