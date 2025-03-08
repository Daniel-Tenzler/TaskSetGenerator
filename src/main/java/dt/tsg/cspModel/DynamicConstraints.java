package dt.tsg.cspModel;

import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.IntVar;

public class DynamicConstraints {

    private IntVar cp_releasetime_min;
    private IntVar cp_releasetime_max;
    private IntVar cp_deadline_min;
    private IntVar cp_deadline_max;
    private IntVar cp_wcet_min;
    private IntVar cp_wcet_max;

    public DynamicConstraints(IntVar cp_releasetime_min, IntVar cp_releasetime_max, IntVar cp_deadline_min, IntVar cp_deadline_max, IntVar cp_wcet_min, IntVar cp_wcet_max) {
        this.cp_releasetime_min = cp_releasetime_min;
        this.cp_releasetime_max = cp_releasetime_max;
        this.cp_deadline_min = cp_deadline_min;
        this.cp_deadline_max = cp_deadline_max;
        this.cp_wcet_min = cp_wcet_min;
        this.cp_wcet_max = cp_wcet_max;
    }

    public IntVar getCp_releasetime_min() {
        return cp_releasetime_min;
    }

    public void setCp_releasetime_min(IntVar cp_releasetime_min) {
        this.cp_releasetime_min = cp_releasetime_min;
    }

    public IntVar getCp_releasetime_max() {
        return cp_releasetime_max;
    }

    public void setCp_releasetime_max(IntVar cp_releasetime_max) {
        this.cp_releasetime_max = cp_releasetime_max;
    }

    public IntVar getCp_deadline_min() {
        return cp_deadline_min;
    }

    public void setCp_deadline_min(IntVar cp_deadline_min) {
        this.cp_deadline_min = cp_deadline_min;
    }

    public IntVar getCp_deadline_max() {
        return cp_deadline_max;
    }

    public void setCp_deadline_max(IntVar cp_deadline_max) {
        this.cp_deadline_max = cp_deadline_max;
    }

    public IntVar getCp_wcet_min() {
        return cp_wcet_min;
    }

    public void setCp_wcet_min(IntVar cp_wcet_min) {
        this.cp_wcet_min = cp_wcet_min;
    }

    public IntVar getCp_wcet_max() {
        return cp_wcet_max;
    }

    public void setCp_wcet_max(IntVar cp_wcet_max) {
        this.cp_wcet_max = cp_wcet_max;
    }

    public void printSolvedDynamicConstraints(CpSolver solver) {
        System.out.println("cp_releasetime_min " + solver.value(this.cp_releasetime_min));
        System.out.println("cp_releasetime_max " + solver.value(this.cp_releasetime_max));
        System.out.println("cp_deadline_min " + solver.value(this.cp_deadline_min));
        System.out.println("cp_deadline_max " + solver.value(this.cp_deadline_max));
        System.out.println("cp_wcet_min " + solver.value(this.cp_wcet_min));
        System.out.println("cp_wcet_max " + solver.value(this.cp_wcet_max));
    }
}
