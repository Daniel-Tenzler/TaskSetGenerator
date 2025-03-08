package dt.tsg.phasesAndSchedulingLimit;

import dt.tsg.InputParams.InputParameters;
import dt.tsg.task.Task;
import dt.tsg.utils.Utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class PhasesAndSchedulingLimit {

    /**
     * This function returns the scheduling problem limit depending on the nature of the taskList.
     * (1) taskList contains only periodic tasks -> The function returns the Hyperperiod of all tasks
     * (2) taskList contains only aperiodic tasks -> The function returns a multiple of the total number of tasks, up to 20*taskList.size()
     * (3) taskList contains periodic and aperiodic tasks -> The function returns a multiple of the hyperperiod, up to 3*hyperperiod
     * In The third case, the returned limit might be too small, and the CSP Solver will return INFEASIBLE.
     *
     * @param taskList  the list of tasks.
     * @param periodic  whether the task set is periodic.
     * @param aperiodic whether the task set is aperiodic.
     * @return the scheduling limit as explained above.
     */
    public static int calculateSchedulingLimit(ArrayList<Task> taskList, boolean periodic, boolean aperiodic) {

        Random random = new Random();

        if (periodic && aperiodic)
            throw new RuntimeException("boolean values \"periodic\" and \"aperiodic\" are both true. This should not be possible");

        if (periodic) {
            // taskList contains no aperiodic tasks -> return the Hyperperiod as the scheduling limit
            return getHyperperiod(taskList);
        } else if (aperiodic) {
            // taskList contains no periodic tasks. See "The Hyperperiod Problem" section.
            // Return a multiple of taskList.size() as the scheduling limit
            int schedulingLimit = taskList.size();
            int maximumMultiple = 20;
            while (random.nextInt(0, 2) == 0) {
                maximumMultiple--;
                if (maximumMultiple == 0) break;
                schedulingLimit += taskList.size();
            }
            return schedulingLimit;
        } else {
            // taskList is a mixed periodic Task Set. See "The Hyperperiod Problem". This needs more research.
            // Return a multiple of the hyperperiod as the scheduling limit
            int hyperperiod = getHyperperiod(taskList);
            int schedulingLimit = hyperperiod;
            int maximumMultiple = 3;
            while (random.nextInt(0, 2) == 0) {
                maximumMultiple--;
                if (maximumMultiple == 0) break;
                schedulingLimit += hyperperiod;     // In some cases, this is too small, and the solver returns INFEASIBLE. We could restart the solver and try again in these cases
            }
            return schedulingLimit;
        }
    }

    /**
     * calculates a scheduling limit while minding the phased release-times of periodic tasks
     *
     * @param taskList  the list of tasks.
     * @param periodic  whether the task set is periodic.
     * @param aperiodic whether the task set is aperiodic.
     * @return the scheduling limit.
     */
    public static int calculateSchedulingLimitWithPhasedReleaseTimes(ArrayList<Task> taskList, boolean periodic, boolean aperiodic) {

        Random random = new Random();

        int maximumPhase = 0;
        for (Task task : taskList) {
            maximumPhase = Math.max(maximumPhase, task.getPhase());
        }
        // if all phases are 0, then calculate the usual Scheduling Limit
        if (maximumPhase == 0) return calculateSchedulingLimit(taskList, periodic, aperiodic);

        if (periodic) {
            // taskList contains no aperiodic tasks -> return the hyperperiod+1 after the largest phase
            int hyperperiod = getHyperperiod(taskList);
            int nextLargestHyperperiod = hyperperiod;
            while (nextLargestHyperperiod < maximumPhase) {
                nextLargestHyperperiod += hyperperiod;
            }
            // the hyperperiod starting after the largest release time is the one where all tasks are released from the start
            nextLargestHyperperiod += hyperperiod;
            return nextLargestHyperperiod;

        } else if (aperiodic) {
            // taskList contains no periodic tasks. See "The Hyperperiod Problem" section.
            // Return a multiple of taskList.size() as the scheduling limit
            int schedulingLimit = taskList.size();
            int maximumMultiple = 20;
            while (random.nextInt(0, 2) == 0) {
                maximumMultiple--;
                if (maximumMultiple == 0) break;
                schedulingLimit += taskList.size();
            }
            return schedulingLimit;
        } else {
            // taskList is a mixed periodic Task Set. See "The Hyperperiod Problem". This needs more research.
            // Return a multiple of the hyperperiod as the scheduling limit, while also minding the largest release time
            int hyperperiod = getHyperperiod(taskList);
            int schedulingLimit = hyperperiod;
            int maximumMultiple = 10;
            while (random.nextInt(0, 2) == 0) {
                maximumMultiple--;
                if (maximumMultiple == 0) break;
                schedulingLimit += hyperperiod;     // In some cases, this is too small, and the solver returns INFEASIBLE. We could restart the solver and try again in these cases
            }

            int nextLargestHyperperiod = hyperperiod;
            while (nextLargestHyperperiod < maximumPhase) {
                nextLargestHyperperiod += hyperperiod;
            }
            // the hyperperiod starting after the largest release time is the one where all tasks are released from the start
            nextLargestHyperperiod += hyperperiod;
            // if the previously generated schedulingLimit is larger than the next largest hyperperiod,
            // we choose the scheduling limit since it is also a multiple of the hyperperiod
            return Math.max(nextLargestHyperperiod, schedulingLimit);
        }
    }

    /**
     * Calculates phased releaseTimes for periodic tasks. The phase is a multiple of the task period, up to 20 times the task period
     *
     * @param taskList the list of tasks.
     */
    public static void calculatePhasedReleaseTimes(ArrayList<Task> taskList, Utils utils, Map<Integer, ArrayList<Integer>> precedence_map) {

        final int max_multiplier = 20;      // sets the maximum multiplier for phase calculation

        ArrayList<Boolean> setPhases = new ArrayList<>();
        taskList.forEach(e -> setPhases.add(false));

        for (Task task : taskList) {
            utils.DebugPrint("========================== TASK " + task.getIdentifier() + " ======================== precedence map: " + precedence_map.get(task.getIdentifier()));
            if (setPhases.get(task.getIdentifier())) {
                utils.DebugPrint("skipped task " + task.getIdentifier() + " since it already has a phase");
                continue;        // skip setting the phase if it is already set
            }

            if (task.isPeriodic()) {
                // the phase is geometrically distributed around 0 with stepSize == period length
                int phase = 0;
                while (utils.random.nextInt(2) == 0) {
                    phase += task.getPeriod();
                    if (phase >= max_multiplier * task.getPeriod()) break;
                }
                //set the phase, also set phase for all connected tasks
                if (precedence_map.get(task.getIdentifier()).isEmpty()) {
                    // task has no related tasks
                    task.setPhase(phase);
                    setPhases.set(task.getIdentifier(), true);
                    utils.DebugPrint("Added phase " + phase + " to task " + task.getIdentifier());
                } else {
                    // task has related task, read them from the precedence map
                    for (Integer taskID : precedence_map.get(task.getIdentifier())) {       // TODO do not assign a phase to aperiodic tasks
                        // precedence map contains related tasks as well as the task itself
                        taskList.get(taskID).setPhase(phase);
                        setPhases.set(taskID, true);
                        utils.DebugPrint("set task phase: id:" + taskID + " phase:" + phase);
                    }
                }
            }
        }
    }

    /**
     * Returns the hyperperiod for all periodic tasks in the given taskList.
     *
     * @param taskList Array of Tasks.
     * @return Hyperperiod of all periodic tasks.
     */
    private static int getHyperperiod(ArrayList<Task> taskList) {
        int hyperperiod = 0;

        //calculate hyperperiod
        for (Task task : taskList) {
            // calculate least common multiple
            if (task.isPeriodic() && task.getPeriod() > 0) {
                int period = task.getPeriod();
                if (hyperperiod == 0) {
                    hyperperiod = period;
                } else {
                    if (hyperperiod > period) {
                        int increment = hyperperiod;
                        while (hyperperiod % period != 0) {
                            hyperperiod += increment;
                        }
                    } else {
                        int increment = period;
                        while (period % hyperperiod != 0) {
                            period += increment;
                        }
                        hyperperiod = period;
                    }
                }
            }
        }
        return hyperperiod;
    }

    /**
     * Wrapper function for the scheduling limit functions.
     *
     * @param inputs              The Input Parameters object.
     * @param taskList            The list of tasks.
     * @param phased_releaseTimes controls whether phases are generated for tasks.
     * @return the scheduling limit.
     */
    public static int getPeriodicAperiodicSchedulingLimit(InputParameters inputs, ArrayList<Task> taskList, boolean phased_releaseTimes) {
        boolean periodic = true;
        boolean aperiodic = true;
        for (Task task : taskList) {
            if (task.isPeriodic()) {
                aperiodic = false;
            } else {
                periodic = false;
            }
        }

        // Calculate Limit
        int limit;
        if (phased_releaseTimes) {
            limit = calculateSchedulingLimitWithPhasedReleaseTimes(taskList, periodic, aperiodic);
        } else {
            if (inputs.getSchedulingLimit() < 0) {
                // input constraints do not contain a hyperperiod
                limit = calculateSchedulingLimit(taskList, periodic, aperiodic);
            } else {
                limit = inputs.getSchedulingLimit();
            }
        }
        return limit;
    }
}
