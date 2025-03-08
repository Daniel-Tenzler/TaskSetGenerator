package dt.tsg.cspModel;

import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;

public class ModelTask {

    private IntVar start;
    private IntVar end;
    private IntVar wcet;
    private IntervalVar interval;
    private IntVar resource;

    @Override
    public String toString() {
        return "ModelTask{" +
                "start=" + start +
                ", end=" + end +
                ", wcet=" + wcet +
                ", interval=" + interval +
                ", resource=" + resource +
                '}';
    }

    public IntVar getStart() {
        return start;
    }

    public void setStart(IntVar start) {
        this.start = start;
    }

    public IntVar getEnd() {
        return end;
    }

    public void setEnd(IntVar end) {
        this.end = end;
    }

    public IntVar getWcet() {
        return wcet;
    }

    public void setWcet(IntVar wcet) {
        this.wcet = wcet;
    }

    public IntervalVar getInterval() {
        return interval;
    }

    public void setInterval(IntervalVar interval) {
        this.interval = interval;
    }

    public IntVar getResource() {
        return resource;
    }

    public void setResource(IntVar resource) {
        this.resource = resource;
    }
}
