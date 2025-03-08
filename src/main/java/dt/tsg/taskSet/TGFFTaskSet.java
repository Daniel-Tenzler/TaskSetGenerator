package dt.tsg.taskSet;

import dt.tsg.task.Task;

import java.util.ArrayList;
import java.util.Map;

public class TGFFTaskSet {

    private int numTasks;
    private ArrayList<Task> taskList;
    private int numTGFFSubgraphs;
    private Map<Integer, ArrayList<Integer>> subgraphMap;

    public TGFFTaskSet(int numTasks, ArrayList<Task> taskList, int numTGFFSubgraphs, Map<Integer, ArrayList<Integer>> subgraphMap) {
        this.numTasks = numTasks;
        this.taskList = taskList;
        this.numTGFFSubgraphs = numTGFFSubgraphs;
        this.subgraphMap = subgraphMap;
    }

    public int getNumTasks() {
        return numTasks;
    }

    public void setNumTasks(int numTasks) {
        this.numTasks = numTasks;
    }

    public ArrayList<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }

    public int getNumTGFFSubgraphs() {
        return numTGFFSubgraphs;
    }

    public void setNumTGFFSubgraphs(int numTGFFSubgraphs) {
        this.numTGFFSubgraphs = numTGFFSubgraphs;
    }

    public Map<Integer, ArrayList<Integer>> getSubgraphMap() {
        return subgraphMap;
    }

    public void setSubgraphMap(Map<Integer, ArrayList<Integer>> subgraphMap) {
        this.subgraphMap = subgraphMap;
    }
}
