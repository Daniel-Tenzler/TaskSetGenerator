package dt.tsg.InputParams;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class InputParameters {

    // Minimum Task Set Properties
    private int min_number_of_tasks;                //the minimum number of tasks in the task set
    private int max_number_of_tasks;                //the maximum number of tasks in the task set
    private int min_number_of_resources;            //the minimum number of resources available
    private int max_number_of_resources;            //the maximum number of resources available
    private int min_period_percentage;              //the minimum percentage of periodic tasks
    private int max_period_percentage;              //the maximum percentage of periodic tasks
    private int min_period_length;                  //the minimum period length of all tasks
    private int max_period_length;                  //the maximum period length of all tasks

    // Additional Task Set Properties
    private int min_residency_constraints;          //the minimum number of residency constraints
    private int max_residency_constraints;          //the maximum number of residency constraints
    private int min_total_precedence_relations;     //the minimum number of total precedence relations
    private int max_total_precedence_relations;     //the maximum number of total precedence relations
    private int min_deadline = -1;                  //the minimum deadline for all tasks
    private int max_deadline = -1;                  //the maximum deadline for all tasks
    private int min_releaseTime = -1;               //the minimum releaseTime for all tasks
    private int max_releaseTime = -1;               //the maximum releaseTime for all tasks
    private int min_WCET = -1;                      //the minimum Worst-Case Execution-Time
    private int max_WCET = -1;                      //the maximum Worst-Case Execution-Time
    private int subgraphNumber = 1;                 //the number of subgraphs in the task set
    private int schedulingLimit = -1;               //the limit of the schedule generation

    // Generation Options
    private boolean multiResidency = false;         // generates multiple resource ids per res-constraint
    private boolean phasedReleaseTimes = false;     // controls the generation of phases
    private boolean postHocPrecedence = false;      // generates precedence relations post CSP Model usage
    private boolean postHocResidency = false;       // generates residency constraints post CSP Model usage
    private boolean debug = false;                  // enables debug mode
    private boolean saveOutput = false;             // enables saving the generated task sets to files
    private int numberOfWorkerThreads = 1;          // controls the number of worker threads for the CP-Solver
    private int solutionCount = 1;                  // determines how many task sets should be generated
    private boolean maximizeWCET = false;           // controls max-WCET objective for the CP-Solver
    private boolean deadlineEqualToPeriod = false;  // controls the generation of implicit deadlines

    // Distributions for periodicity, period length, residency constraints, number of tasks, number of resources
    public enum Distribution {UNIFORM, GEOMETRIC, POISSON, BINOMIAL}

    private Distribution numberOfResidencyConstraints_distribution = Distribution.UNIFORM;    // select a number between min and max according to this Distribution
    private Distribution numberOfPrecedenceConstraints_distribution = Distribution.UNIFORM;
    private Distribution numberOfTasks_distribution = Distribution.UNIFORM;
    private Distribution numberOfResources_distribution = Distribution.UNIFORM;

    private Distribution residency_distribution = Distribution.UNIFORM;        // controls the Distribution of the residency constraints within a task set
    private Distribution periodicity_distribution = Distribution.UNIFORM;     // controls the Distribution of the periodicity within a task set
    private Distribution periodLength_distribution = Distribution.UNIFORM;     // controls the Distribution of the period length within a min and max bound for each task

    // Mean values for poisson distributed variables
    private double periodLengthPoissonMean = 0.0;               // for period length distribution
    private double periodicityPoissonMean = 0.0;                // for periodicity distribution
    private double residencyPoissonMean = 0.0;                  // for residency constraint distribution
    private double NOResidencyPoissonMean = 0.0;                // for Number of Residency distribution
    private double NOPrPoissonMean = 0.0;                       // for Number of Precedence constraints distribution
    private double NOTasksPoissonMean = 0.0;                    // for Number of Tasks distribution
    private double NOResourcesPoissonMean = 0.0;                // for Number of Resources distribution

    // Values for binomial distributed variables
    private double periodLengthBinomialP = 0.5;         // binomialP for period length distribution
    private int periodLengthBinomialN = 100;            // binomialN for period length distribution
    private double periodicityBinomialP = 0.5;          // binomialP for periodicity distribution
    private int periodicityBinomialN = 100;             // binomialN for periodicity distribution
    private double residencyBinomialP = 0.5;            // binomialP for residency Constraints distribution
    private int residencyBinomialN = 100;               // binomialN for residency Constraints distribution
    private double NOResidencyBinomialP = 0.5;          // binomialP for numberOfResidency distribution
    private int NOResidencyBinomialN = 100;             // binomialN for numberOfResidency distribution
    private double NOPrBinomialP = 0.5;                 // binomialP for numberOfPrecedence distribution
    private int NOPrBinomialN = 100;                    // binomialN for numberOfPrecedence distribution
    private double NOTasksBinomialP = 0.5;              // binomialP for number of tasks distribution
    private int NOTasksBinomialN = 100;                 // binomialN for number of tasks distribution
    private double NOResourcesBinomialP = 0.5;          // binomialP for number of resources distribution
    private int NOResourcesBinomialN = 100;             // binomialN for number of resources distribution

    // Empty Constructor
    public InputParameters() {
    }

    /**
     * Reads the InputParameters given in the file located at the specified path.
     *
     * @param path     location where the Input file is located.
     * @param warnings List of Strings in which warnings are saved.
     * @return InputParameter object.
     * @throws Exception if path variable is null.
     */
    public static InputParameters readInputFile(String path, ArrayList<String> warnings) throws Exception {
        if (path == null) throw new Exception("File path is null");
        File inputFile = new File(path);
        StringBuilder content = new StringBuilder();

        // read the .tgff file
        try (Scanner scanner = new Scanner(inputFile)) {
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append(System.lineSeparator());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }

        String[] allLines = content.toString().split("\n");
        // read the first line to determine what Generation Method is used
        if (allLines[0].toLowerCase().contains("basic")) {
            InputParameters inputs = new InputParameters();
            for (int i = 1; i < allLines.length; i++) {
                String line = allLines[i];
                System.out.println("currentline: " + line);
                String keywordsubstring;
                try {
                    // separate keyword from the rest of the line
                    keywordsubstring = line.substring(0, line.indexOf(" ")).toLowerCase();
                } catch (StringIndexOutOfBoundsException e) {
                    // in case the line is missing a whitespace, we assume that the keyword doesn't require an argument
                    keywordsubstring = line.substring(0, line.length() - 1).toLowerCase();
                }

                if (keywordsubstring.isEmpty()) {
                    System.out.println("Warning: keyword substring is empty, this could be due to an erroneous whitespace or linebreak. Please make sure the input-file has the same formatting as described in the readme.md.");
                    warnings.add("Warning: keyword substring is empty, this could be due to an erroneous whitespace or linebreak. Please make sure the input-file has the same formatting as described in the readme.md.");
                    continue;
                }

                boolean multiResidency;
                boolean phasedReleaseTimes;
                boolean postHocPrecedence;
                boolean postHocResidency;
                // check what keyword we are reading
                switch (keywordsubstring) {
                    case "tasknum" -> {
                        // get min and max values after tasknum
                        String mintaskString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("mintaskString=" + mintaskString);
                        int mintasknum = Integer.parseInt(mintaskString);
                        String maxtaskString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxtaskString=" + maxtaskString);
                        int maxtasknum = Integer.parseInt(maxtaskString);
                        // set inputs
                        inputs.setMin_number_of_tasks(mintasknum);
                        inputs.setMax_number_of_tasks(maxtasknum);
                    }
                    case "resourcenum" -> {
                        // get min and max values after resourcenum
                        String minresourceString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("minresourceString=" + minresourceString);
                        int minresourcenum = Integer.parseInt(minresourceString);
                        String maxresourceString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxresourceString=" + maxresourceString);
                        int maxresourcenum = Integer.parseInt(maxresourceString);
                        // set inputs
                        inputs.setMin_number_of_resources(minresourcenum);
                        inputs.setMax_number_of_resources(maxresourcenum);
                    }
                    case "residencyconstraints" -> {
                        // get min and max values after residencyconstraints
                        String minresidString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("minresidString=" + minresidString);
                        int minresidnum = Integer.parseInt(minresidString);
                        String maxresidString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxresidString=" + maxresidString);
                        int maxresidnum = Integer.parseInt(maxresidString);
                        // set inputs
                        inputs.setMin_residency_constraints(minresidnum);
                        inputs.setMax_residency_constraints(maxresidnum);
                    }
                    case "totalprecedencerelations" -> {
                        // get min and max values after totalprecedencerelations
                        String minPrecString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("minPrecString=" + minPrecString);
                        int minPrecnum = Integer.parseInt(minPrecString);
                        String maxPrecString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxPrecString=" + maxPrecString);
                        int maxPrecnum = Integer.parseInt(maxPrecString);
                        // set inputs
                        inputs.setMin_total_precedence_relations(minPrecnum);
                        inputs.setMax_total_precedence_relations(maxPrecnum);
                    }
                    case "periodpercentage" -> {
                        // get min and max values after periodpercentage
                        String minperiodpercentageString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("minperiodpercentageString=" + minperiodpercentageString);
                        int minperiodpercentage = Integer.parseInt(minperiodpercentageString);
                        String maxperiodpercentageString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxperiodpercentageString=" + maxperiodpercentageString);
                        int maxperiodpercentage = Integer.parseInt(maxperiodpercentageString);
                        // set inputs
                        inputs.setMin_period_percentage(minperiodpercentage);
                        inputs.setMax_period_percentage(maxperiodpercentage);
                    }
                    case "periodlength" -> {
                        // get min and max values after periodlength
                        String minperiodlengthString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("minperiodlengthString=" + minperiodlengthString);
                        int minperiodlength = Integer.parseInt(minperiodlengthString);
                        String maxperiodlengthString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxperiodlengthString=" + maxperiodlengthString);
                        int maxperiodlength = Integer.parseInt(maxperiodlengthString);
                        // set inputs
                        inputs.setMin_period_length(minperiodlength);
                        inputs.setMax_period_length(maxperiodlength);
                    }
                    case "multiresidency" -> {
                        multiResidency = Boolean.parseBoolean(line.substring(line.indexOf(" ") + 1, line.length() - 1));
                        inputs.setMultiResidency(multiResidency);
                    }
                    case "phasedreleasetimes" -> {
                        phasedReleaseTimes = Boolean.parseBoolean(line.substring(line.indexOf(" ") + 1, line.length() - 1));
                        inputs.setPhasedReleaseTimes(phasedReleaseTimes);
                    }
                    case "posthocprecedence" -> {
                        postHocPrecedence = Boolean.parseBoolean(line.substring(line.indexOf(" ") + 1, line.length() - 1));
                        inputs.setPostHocPrecedence(postHocPrecedence);
                    }
                    case "posthocresidency" -> {
                        postHocResidency = Boolean.parseBoolean(line.substring(line.indexOf(" ") + 1, line.length() - 1));
                        inputs.setPostHocResidency(postHocResidency);
                    }
                    case "subgraphnumber" -> {
                        String subgraphString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int subgraphNum = Integer.parseInt(subgraphString);
                        inputs.setSubgraphNumber(subgraphNum);
                    }
                    case "debug" -> inputs.setDebug(true);
                    case "saveoutput" -> inputs.setSaveOutput(true);
                    case "maximizewcet" -> inputs.setMaximizeWCET(true);
                    case "numberofworkerthreads" -> {
                        String workerString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int workerNum = Integer.parseInt(workerString);
                        inputs.setNumberOfWorkerThreads(workerNum);
                    }
                    case "solutioncount" -> {
                        String solutionCountString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int solutionCount = Integer.parseInt(solutionCountString);
                        inputs.setSolutionCount(solutionCount);
                    }
                    case "releasetimes" -> {
                        // get min and max releasetimes
                        String minRTString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("minRTString=" + minRTString);
                        int minRT = Integer.parseInt(minRTString);
                        String maxRTString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxRTString=" + maxRTString);
                        int maxRT = Integer.parseInt(maxRTString);
                        // set inputs
                        inputs.setMin_releaseTime(minRT);
                        inputs.setMax_releaseTime(maxRT);
                    }
                    case "deadlines" -> {
                        // get min and max deadlines
                        String minDLString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("minDLString=" + minDLString);
                        int minDL = Integer.parseInt(minDLString);
                        String maxDLString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxDLString=" + maxDLString);
                        int maxDL = Integer.parseInt(maxDLString);
                        // set inputs
                        inputs.setMin_deadline(minDL);
                        inputs.setMax_deadline(maxDL);
                    }
                    case "wcet" -> {
                        // get min and max wcet
                        String minWCETString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("minWCETString=" + minWCETString);
                        int minWCET = Integer.parseInt(minWCETString);
                        String maxWCETString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxWCETString=" + maxWCETString);
                        int maxWCET = Integer.parseInt(maxWCETString);
                        // set inputs
                        inputs.setMin_WCET(minWCET);
                        inputs.setMax_WCET(maxWCET);
                    }
                    case "residencydistribution" -> {
                        String distributionString = line.substring(line.indexOf(" ") + 1, line.length() - 1).toUpperCase();
                        Distribution distribution = Distribution.UNIFORM;
                        switch (distributionString) {
                            case "UNIFORM" -> distribution = Distribution.UNIFORM;
                            case "GEOMETRIC" -> distribution = Distribution.GEOMETRIC;
                            case "POISSON" -> distribution = Distribution.POISSON;
                            case "BINOMIAL" -> distribution = Distribution.BINOMIAL;
                            default -> {
                                System.out.println("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                                warnings.add("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                            }
                        }
                        inputs.setResidency_distribution(distribution);
                    }
                    case "periodicitydistribution" -> {
                        String distributionString = line.substring(line.indexOf(" ") + 1, line.length() - 1).toUpperCase();
                        Distribution distribution = Distribution.UNIFORM;
                        switch (distributionString) {
                            case "UNIFORM" -> distribution = Distribution.UNIFORM;
                            case "GEOMETRIC" -> distribution = Distribution.GEOMETRIC;
                            case "POISSON" -> distribution = Distribution.POISSON;
                            case "BINOMIAL" -> distribution = Distribution.BINOMIAL;
                            default -> {
                                System.out.println("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                                warnings.add("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                            }
                        }
                        inputs.setPeriodicity_distribution(distribution);
                    }
                    case "periodlengthdistribution" -> {
                        String distributionString = line.substring(line.indexOf(" ") + 1, line.length() - 1).toUpperCase();
                        Distribution distribution = Distribution.UNIFORM;
                        switch (distributionString) {
                            case "UNIFORM" -> distribution = Distribution.UNIFORM;
                            case "GEOMETRIC" -> distribution = Distribution.GEOMETRIC;
                            case "POISSON" -> distribution = Distribution.POISSON;
                            case "BINOMIAL" -> distribution = Distribution.BINOMIAL;
                            default -> {
                                System.out.println("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                                warnings.add("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                            }
                        }
                        inputs.setPeriodLength_distribution(distribution);
                    }
                    case "nordistribution" -> {
                        String distributionString = line.substring(line.indexOf(" ") + 1, line.length() - 1).toUpperCase();
                        Distribution distribution = Distribution.UNIFORM;
                        switch (distributionString) {
                            case "UNIFORM" -> distribution = Distribution.UNIFORM;
                            case "GEOMETRIC" -> distribution = Distribution.GEOMETRIC;
                            case "POISSON" -> distribution = Distribution.POISSON;
                            case "BINOMIAL" -> distribution = Distribution.BINOMIAL;
                            default -> {
                                System.out.println("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                                warnings.add("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                            }
                        }
                        inputs.setNumberOfResidencyConstraints_distribution(distribution);
                    }
                    case "noprdistribution" -> {
                        String distributionString = line.substring(line.indexOf(" ") + 1, line.length() - 1).toUpperCase();
                        Distribution distribution = Distribution.UNIFORM;
                        switch (distributionString) {
                            case "UNIFORM" -> distribution = Distribution.UNIFORM;
                            case "GEOMETRIC" -> distribution = Distribution.GEOMETRIC;
                            case "POISSON" -> distribution = Distribution.POISSON;
                            case "BINOMIAL" -> distribution = Distribution.BINOMIAL;
                            default -> {
                                System.out.println("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                                warnings.add("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                            }
                        }
                        inputs.setNumberOfPrecedenceConstraints_distribution(distribution);
                    }
                    case "notasksdistribution" -> {
                        String distributionString = line.substring(line.indexOf(" ") + 1, line.length() - 1).toUpperCase();
                        Distribution distribution = Distribution.UNIFORM;
                        switch (distributionString) {
                            case "UNIFORM" -> distribution = Distribution.UNIFORM;
                            case "GEOMETRIC" -> distribution = Distribution.GEOMETRIC;
                            case "POISSON" -> distribution = Distribution.POISSON;
                            case "BINOMIAL" -> distribution = Distribution.BINOMIAL;
                            default -> {
                                System.out.println("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                                warnings.add("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                            }
                        }
                        inputs.setNumberOfTasks_distribution(distribution);
                    }
                    case "noresdistribution" -> {
                        String distributionString = line.substring(line.indexOf(" ") + 1, line.length() - 1).toUpperCase();
                        Distribution distribution = Distribution.UNIFORM;
                        switch (distributionString) {
                            case "UNIFORM" -> distribution = Distribution.UNIFORM;
                            case "GEOMETRIC" -> distribution = Distribution.GEOMETRIC;
                            case "POISSON" -> distribution = Distribution.POISSON;
                            case "BINOMIAL" -> distribution = Distribution.BINOMIAL;
                            default -> {
                                System.out.println("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                                warnings.add("WARNING: Distribution not recognized during input-file reading.\nDistribution = " + distributionString + ". Using UNIFORM distribution.");
                            }
                        }
                        inputs.setNumberOfResources_distribution(distribution);
                    }
                    case "periodlengthpoissonmean" -> {
                        String poissonmeanString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double poissonmean = Double.parseDouble(poissonmeanString);
                        inputs.setPeriodLengthPoissonMean(poissonmean);
                    }
                    case "periodicitypoissonmean" -> {
                        String periodicityPoissonMeanString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double periodicityPoissonMean = Double.parseDouble(periodicityPoissonMeanString);
                        inputs.setPeriodicityPoissonMean(periodicityPoissonMean);
                    }
                    case "residencypoissonmean" -> {
                        String residencyPoissonMeanString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double residencyPoissonMean = Double.parseDouble(residencyPoissonMeanString);
                        inputs.setResidencyPoissonMean(residencyPoissonMean);
                    }
                    case "norpoissonmean" -> {
                        String NORPoissonMeanString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double NORPoissonMean = Double.parseDouble(NORPoissonMeanString);
                        inputs.setNOResidencyPoissonMean(NORPoissonMean);
                    }
                    case "noprpoissonmean" -> {
                        String NOPrPoissonMeanString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double NOPrPoissonMean = Double.parseDouble(NOPrPoissonMeanString);
                        inputs.setNOPrPoissonMean(NOPrPoissonMean);
                    }
                    case "notaskspoissonmean" -> {
                        String NOTasksPoissonMeanString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double NOTasksPoissonMean = Double.parseDouble(NOTasksPoissonMeanString);
                        inputs.setNOTasksPoissonMean(NOTasksPoissonMean);
                    }
                    case "norespoissonmean" -> {
                        String NOResPoissonMeanString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double NOResPoissonMean = Double.parseDouble(NOResPoissonMeanString);
                        inputs.setNOResourcesPoissonMean(NOResPoissonMean);
                    }
                    case "periodlengthbinomialp" -> {
                        String periodLengthBinomialPString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double periodLengthBinomialP = Double.parseDouble(periodLengthBinomialPString);
                        inputs.setPeriodLengthBinomialP(periodLengthBinomialP);
                    }
                    case "periodicitybinomialp" -> {
                        String periodicityBinomialPString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double periodicityBinomialP = Double.parseDouble(periodicityBinomialPString);
                        inputs.setPeriodicityBinomialP(periodicityBinomialP);
                    }
                    case "residencybinomialp" -> {
                        String residencyBinomialPString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double residencyBinomialP = Double.parseDouble(residencyBinomialPString);
                        inputs.setResidencyBinomialP(residencyBinomialP);
                    }
                    case "norbinomialp" -> {
                        String NORBinomialPString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double NORBinomialP = Double.parseDouble(NORBinomialPString);
                        inputs.setNOResidencyBinomialP(NORBinomialP);
                    }
                    case "noprbinomialp" -> {
                        String NOPrBinomialPString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double NOPrBinomialP = Double.parseDouble(NOPrBinomialPString);
                        inputs.setNOPrBinomialP(NOPrBinomialP);
                    }
                    case "notasksbinomialp" -> {
                        String NOTasksBinomialPString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double NOTasksBinomialP = Double.parseDouble(NOTasksBinomialPString);
                        inputs.setNOTasksBinomialP(NOTasksBinomialP);
                    }
                    case "noresbinomialp" -> {
                        String NOResBinomialPString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double NOResBinomialP = Double.parseDouble(NOResBinomialPString);
                        inputs.setNOResourcesBinomialP(NOResBinomialP);
                    }
                    case "periodlengthbinomialn" -> {
                        String periodLengthBinomialNString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int periodLengthBinomialN = Integer.parseInt(periodLengthBinomialNString);
                        inputs.setPeriodLengthBinomialN(periodLengthBinomialN);
                    }
                    case "periodicitybinomialn" -> {
                        String periodicityBinomialNString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int periodicityBinomialN = Integer.parseInt(periodicityBinomialNString);
                        inputs.setPeriodicityBinomialN(periodicityBinomialN);
                    }
                    case "residencybinomialn" -> {
                        String residencyBinomialNString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int residencyBinomialN = Integer.parseInt(residencyBinomialNString);
                        inputs.setResidencyBinomialN(residencyBinomialN);
                    }
                    case "norbinomialn" -> {
                        String NORBinomialNString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int NORBinomialN = Integer.parseInt(NORBinomialNString);
                        inputs.setNOResidencyBinomialN(NORBinomialN);
                    }
                    case "noprbinomialn" -> {
                        String NOPrBinomialNString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int NOPrBinomialN = Integer.parseInt(NOPrBinomialNString);
                        inputs.setNOPrBinomialN(NOPrBinomialN);
                    }
                    case "notasksbinomialn" -> {
                        String NOTasksBinomialNString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int NOTasksBinomialN = Integer.parseInt(NOTasksBinomialNString);
                        inputs.setNOTasksBinomialN(NOTasksBinomialN);
                    }
                    case "noresbinomialn" -> {
                        String NOResBinomialNString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int NOResBinomialN = Integer.parseInt(NOResBinomialNString);
                        inputs.setNOResourcesBinomialN(NOResBinomialN);
                    }
                    case "deadlineequaltoperiod" -> inputs.setDeadlineEqualToPeriod(true);
                    default -> {
                        System.out.println("Warning: Could not recognize the keyword when reading input-file.\nKeyword: " + keywordsubstring);
                        warnings.add("Warning: Could not recognize the keyword when reading input-file.\nKeyword: " + keywordsubstring);
                    }
                }


            }
            System.out.println(inputs);

            return inputs;
        } else if (allLines[0].toLowerCase().contains("tgff")) {

            TGFFInputParameters inputs = new TGFFInputParameters();
            for (int i = 1; i < allLines.length - 1; i++) {
                String line = allLines[i];
                System.out.println("currentline: " + line);
                String keywordsubstring;
                try {
                    // separate keyword from the rest of the line
                    keywordsubstring = line.substring(0, line.indexOf(" ")).toLowerCase();
                } catch (StringIndexOutOfBoundsException e) {
                    // in case the line is missing a whitespace, we assume that the keyword doesn't require an argument
                    keywordsubstring = line.substring(0, line.length() - 1).toLowerCase();
                }
                if (keywordsubstring.isEmpty()) {
                    System.out.println("Warning: keyword substring is empty, this could be due to an erroneous whitespace or linebreak. Please make sure the input-file has the same formatting as described in the readme.md.");
                    warnings.add("Warning: keyword substring is empty, this could be due to an erroneous whitespace or linebreak. Please make sure the input-file has the same formatting as described in the readme.md.");
                    continue;
                }

                boolean cleanUpFiles;
                boolean postHocResidency;
                boolean calculatePeriods;
                boolean multiresidency;
                switch (keywordsubstring) {
                    case "graphcount" -> {
                        String graphcountString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int graphcount = Integer.parseInt(graphcountString);
                        inputs.setGraphCount(graphcount);
                    }
                    case "taskcount" -> {
                        // get average and mult values after taskcount
                        String averagetaskString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("averagetaskString=" + averagetaskString);
                        int averagetaskcount = Integer.parseInt(averagetaskString);
                        String multtaskString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("multtaskString=" + multtaskString);
                        int multtaskcount = Integer.parseInt(multtaskString);
                        // set inputs
                        inputs.setTaskCountAverage(averagetaskcount);
                        inputs.setTaskCountMultiplier(multtaskcount);
                    }
                    case "periodpercentage" -> {
                        String periodpercentageString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double periodpercentage = Double.parseDouble(periodpercentageString);
                        inputs.setPeriodPercentage(periodpercentage);
                    }
                    case "multistartnodeprob" -> {
                        String multistartnodeprobString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double multistartnodeprob = Double.parseDouble(multistartnodeprobString);
                        inputs.setMultiStartNodeProb(multistartnodeprob);
                    }
                    case "startnodes" -> {
                        // get average and mult values after startnodes
                        String averagestartnodesString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("averagestartnodesString=" + averagestartnodesString);
                        int averagestartnodes = Integer.parseInt(averagestartnodesString);
                        String multstartnodesString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("multstartnodesString=" + multstartnodesString);
                        int multstartnodes = Integer.parseInt(multstartnodesString);
                        // set inputs
                        inputs.setStartNodesAverage(averagestartnodes);
                        inputs.setStartNodesMultiplier(multstartnodes);
                    }
                    case "taskdegree" -> {
                        // get IN and OUT values after taskdegree
                        String indegreeString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("indegreeString=" + indegreeString);
                        int indegree = Integer.parseInt(indegreeString);
                        String outdegreeString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("multstartnodesString=" + outdegreeString);
                        int outdegree = Integer.parseInt(outdegreeString);
                        // set inputs
                        inputs.setTaskDegreeIN(indegree);
                        inputs.setTaskDegreeOUT(outdegree);
                    }
                    case "harddeadlineprob" -> {
                        String harddeadlineprobString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double harddeadline = Double.parseDouble(harddeadlineprobString);
                        inputs.setHardDeadlineProb(harddeadline);
                    }
                    case "resourcenum" -> {
                        String resourcecountString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int resourcecount = Integer.parseInt(resourcecountString);
                        inputs.setNumberOfResources(resourcecount);
                    }
                    case "tasktransitiontime" -> {
                        String tasktransitiontimeString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double tasktranstime = Double.parseDouble(tasktransitiontimeString);
                        inputs.setTaskTransitionTime(tasktranstime);
                    }
                    case "cleanupfiles" -> {
                        cleanUpFiles = Boolean.parseBoolean(line.substring(line.indexOf(" ") + 1, line.length() - 1));
                        inputs.setCleanUpFiles(cleanUpFiles);
                    }
                    case "posthocresidency" -> {
                        postHocResidency = Boolean.parseBoolean(line.substring(line.indexOf(" ") + 1, line.length() - 1));
                        inputs.setPostHocResidency(postHocResidency);
                    }
                    case "multiresidency" -> {
                        multiresidency = Boolean.parseBoolean(line.substring(line.indexOf(" ") + 1, line.length() - 1));
                        inputs.setMultiResidency(multiresidency);
                    }
                    case "calculateperiods" -> {
                        calculatePeriods = Boolean.parseBoolean(line.substring(line.indexOf(" ") + 1, line.length() - 1));
                        inputs.setCalculatePeriods(calculatePeriods);
                    }
                    case "periodmul" -> {
                        List<Integer> period_mul = new ArrayList<>();
                        int index = line.indexOf(" ") + 1;
                        while (index < line.length() - 1) {
                            int upperindex = line.indexOf(" ", index);
                            if (upperindex == -1) upperindex = line.length() - 1;
                            String periodmulString = line.substring(index, upperindex);
                            period_mul.add(Integer.parseInt(periodmulString));
                            index = line.indexOf(" ", index) + 1;
                            if (index <= 0) break;
                        }
                        System.out.println("periodmul: " + Arrays.toString(period_mul.toArray()));
                        inputs.setPeriod_mul(period_mul);
                    }
                    case "numberofworkerthreads" -> {
                        String workerString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int workerNum = Integer.parseInt(workerString);
                        inputs.setNumberOfWorkerThreads(workerNum);
                    }
                    case "debug" -> inputs.setDebug(true);
                    case "saveoutput" -> inputs.setSaveOutput(true);
                    case "maximizewcet" -> inputs.setMaximizeWCET(true);
                    case "deadlineequaltoperiod" -> inputs.setDeadlineEqualToPeriod(true);
                    case "solutioncount" -> {
                        String solutionCountString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int solutionCount = Integer.parseInt(solutionCountString);
                        inputs.setSolutionCount(solutionCount);
                    }
                    case "releasetimes" -> {
                        // get min and max releasetimes
                        String minRTString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("minRTString=" + minRTString);
                        int minRT = Integer.parseInt(minRTString);
                        String maxRTString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxRTString=" + maxRTString);
                        int maxRT = Integer.parseInt(maxRTString);
                        // set inputs
                        inputs.setMin_releaseTime(minRT);
                        inputs.setMax_releaseTime(maxRT);
                    }
                    case "deadlines" -> {
                        // get min and max deadlines
                        String minDLString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("minDLString=" + minDLString);
                        int minDL = Integer.parseInt(minDLString);
                        String maxDLString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxDLString=" + maxDLString);
                        int maxDL = Integer.parseInt(maxDLString);
                        // set inputs
                        inputs.setMin_deadline(minDL);
                        inputs.setMax_deadline(maxDL);
                    }
                    case "wcet" -> {
                        // get min and max wcet
                        String minWCETString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("minDLString=" + minWCETString);
                        int minWCET = Integer.parseInt(minWCETString);
                        String maxWCETString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxDLString=" + maxWCETString);
                        int maxWCET = Integer.parseInt(maxWCETString);
                        // set inputs
                        inputs.setMin_WCET(minWCET);
                        inputs.setMax_WCET(maxWCET);
                    }
                    case "residencyconstraints" -> {
                        // get min and max values after residencyconstraints
                        String minresidString = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                        System.out.println("minresidString=" + minresidString);
                        int minresidnum = Integer.parseInt(minresidString);
                        String maxresidString = line.substring(line.indexOf(" ", line.indexOf(" ") + 1) + 1, line.length() - 1);
                        System.out.println("maxresidString=" + maxresidString);
                        int maxresidnum = Integer.parseInt(maxresidString);
                        // set inputs
                        inputs.setMin_residency_constraints(minresidnum);
                        inputs.setMax_residency_constraints(maxresidnum);
                    }
                    case "residencydistribution" -> {
                        String distributionString = line.substring(line.indexOf(" ") + 1, line.length() - 1).toUpperCase();
                        Distribution distribution = Distribution.UNIFORM;
                        switch (distributionString) {
                            case "UNIFORM" -> distribution = Distribution.UNIFORM;
                            case "GEOMETRIC" -> distribution = Distribution.GEOMETRIC;
                            case "POISSON" -> distribution = Distribution.POISSON;
                            case "BINOMIAL" -> distribution = Distribution.BINOMIAL;
                            default ->
                                    System.out.println("Distribution not recognized during input-file reading.\nDistribution = " + distributionString);
                        }
                        inputs.setResidency_distribution(distribution);
                    }
                    case "residencypoissonmean" -> {
                        String residencyPoissonMeanString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double residencyPoissonMean = Double.parseDouble(residencyPoissonMeanString);
                        inputs.setResidencyPoissonMean(residencyPoissonMean);
                    }
                    case "residencybinomialp" -> {
                        String residencyBinomialPString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        double residencyBinomialP = Double.parseDouble(residencyBinomialPString);
                        inputs.setResidencyBinomialP(residencyBinomialP);
                    }
                    case "residencybinomialn" -> {
                        String residencyBinomialNString = line.substring(line.indexOf(" ") + 1, line.length() - 1);
                        int residencyBinomialN = Integer.parseInt(residencyBinomialNString);
                        inputs.setResidencyBinomialN(residencyBinomialN);
                    }
                    default -> {
                        System.out.println("Warning: Could not recognize the keyword when reading input-file.\nKeyword: " + keywordsubstring);
                        warnings.add("Warning: Could not recognize the keyword when reading input-file.\nKeyword: " + keywordsubstring);
                    }
                }
            }

            System.out.println(inputs);

            return inputs;
        } else {
            System.out.println("Unable to recognize the Generation Method at line 0 of the input file.\nMake sure to specify the generation method (\"TGFF\" or \"Basic\") in the first line of the input file.");
            System.exit(-1);
        }
        return null;
    }

    @Override
    public String toString() {
        return "InputParameters{" +
                "\nmin_number_of_tasks=" + min_number_of_tasks +
                ", \nmax_number_of_tasks=" + max_number_of_tasks +
                ", \nmin_number_of_resources=" + min_number_of_resources +
                ", \nmax_number_of_resources=" + max_number_of_resources +
                ", \nmin_residency_constraints=" + min_residency_constraints +
                ", \nmax_residency_constraints=" + max_residency_constraints +
                ", \nmin_total_precedence_relations=" + min_total_precedence_relations +
                ", \nmax_total_precedence_relations=" + max_total_precedence_relations +
                ", \nmin_period_percentage=" + min_period_percentage +
                ", \nmax_period_percentage=" + max_period_percentage +
                ", \nmin_period_length=" + min_period_length +
                ", \nmax_period_length=" + max_period_length +
                ", \nmin_deadline=" + min_deadline +
                ", \nmax_deadline=" + max_deadline +
                ", \nmin_releaseTime=" + min_releaseTime +
                ", \nmax_releaseTime=" + max_releaseTime +
                ", \nmin_WCET=" + min_WCET +
                ", \nmax_WCET=" + max_WCET +
                ", \nhyperperiod=" + schedulingLimit +
                ", \nsubgraphNumber=" + subgraphNumber +
                ", \nmultiResidency=" + multiResidency +
                ", \nphasedReleaseTimes=" + phasedReleaseTimes +
                ", \npostHocPrecedence=" + postHocPrecedence +
                ", \npostHocResidency=" + postHocResidency +
                ", \ndebug=" + debug +
                ", \nsaveOutput=" + saveOutput +
                ", \nnumberOfWorkerThreads=" + numberOfWorkerThreads +
                ", \nsolutionCount=" + solutionCount +
                ", \nmaximizeWCET=" + maximizeWCET +
                ", \nnumberOfResidencyConstraints_distribution=" + numberOfResidencyConstraints_distribution +
                ", \nnumberOfPrecedenceConstraints_distribution=" + numberOfPrecedenceConstraints_distribution +
                ", \nnumberOfTasks_distribution=" + numberOfTasks_distribution +
                ", \nnumberOfResources_distribution=" + numberOfResources_distribution +
                ", \nresidency_distribution=" + residency_distribution +
                ", \nperiodicity_distribution=" + periodicity_distribution +
                ", \nperiodLength_distribution=" + periodLength_distribution +
                ", \nperiodLengthPoissonMean=" + periodLengthPoissonMean +
                ", \nperiodicityPoissonMean=" + periodicityPoissonMean +
                ", \nresidencyPoissonMean=" + residencyPoissonMean +
                ", \nNORPoissonMean=" + NOResidencyPoissonMean +
                ", \nNOPrPoissonMean=" + NOPrPoissonMean +
                ", \nNOTasksPoissonMean=" + NOTasksPoissonMean +
                ", \nNOResPoissonMean=" + NOResourcesPoissonMean +
                ", \nperiodLengthBinomialP=" + periodLengthBinomialP +
                ", \nperiodLengthBinomialN=" + periodLengthBinomialN +
                ", \nperiodicityBinomialP=" + periodicityBinomialP +
                ", \nperiodicityBinomialN=" + periodicityBinomialN +
                ", \nresidencyBinomialP=" + residencyBinomialP +
                ", \nresidencyBinomialN=" + residencyBinomialN +
                ", \nNORBinomialP=" + NOResidencyBinomialP +
                ", \nNORBinomialN=" + NOResidencyBinomialN +
                ", \nNOPrBinomialP=" + NOPrBinomialP +
                ", \nNOPrBinomialN=" + NOPrBinomialN +
                ", \nNOTasksBinomialP=" + NOTasksBinomialP +
                ", \nNOTasksBinomialN=" + NOTasksBinomialN +
                ", \nNOResBinomialP=" + NOResourcesBinomialP +
                ", \nNOResBinomialN=" + NOResourcesBinomialN +
                ", \ndeadlineEqualToPeriod=" + deadlineEqualToPeriod +
                '}';
    }

    /**
     * Executes sanity checks for Basic Generation InputParameters.
     *
     * @return a non-null String if a check was failed. Returns null if all checks are passed.
     */
    private String sanityChecks() {

        //WCET
        if (this.min_WCET == 0 || this.max_WCET == 0 || (this.min_WCET > this.max_deadline && this.max_deadline != -1) || (this.min_WCET > this.max_WCET && this.max_WCET != -1)) {
            return "Failed Check: NOT(this.min_WCET==0 || this.max_WCET == 0 || this.min_WCET > this.max_deadline || this.min_WCET>this.max_WCET)";
        }
        //Period
        if (this.min_period_length < 0 || this.max_period_length < 0 || this.max_period_percentage < 0 || this.min_period_percentage < 0 || this.min_period_length > this.max_period_length || this.min_period_percentage > this.max_period_percentage || this.max_period_percentage > 100) {
            return "Failed Check: NOT(this.min_period_length<0 || this.max_period_length<0 || this.max_period_percentage<0 || this.min_period_percentage<0 || this.min_period_length>this.max_period_length || this.min_period_percentage>this.max_period_percentage || this.max_period_percentage>100)";
        } else if ((this.max_period_length == 0 || this.min_period_length == 0) && this.min_period_percentage > 0) {
            return "Failed Check: NOT((this.max_period_length==0 || this.min_period_length==0) && this.min_period_percentage>0)";
        }
        //ReleaseTime
        if (this.min_releaseTime > this.max_releaseTime && this.max_releaseTime != -1) {
            return "Failed Check: NOT(this.min_releaseTime>this.max_releaseTime)";
        }
        //Deadline
        if (this.max_deadline == 0 || (this.min_deadline > this.max_deadline && this.max_deadline != -1)) {
            return "Failed Check: NOT(this.max_deadline <=0 || this.min_deadline>this.max_deadline)";
        }
        //Resources
        if (this.min_number_of_resources <= 0 || this.max_number_of_resources <= 0 || this.min_number_of_resources > this.max_number_of_resources) {
            return "Failed Check: NOT(this.min_number_of_resources<=0 || this.max_number_of_resources<=0 || this.min_number_of_resources>this.max_number_of_resources )";
        }
        //Tasks
        if (this.min_number_of_tasks <= 0 || this.max_number_of_tasks <= 0 || this.min_number_of_tasks > this.max_number_of_tasks) {
            return "Failed Check: NOT(this.min_number_of_tasks<=0 || this.max_number_of_tasks<=0 || this.min_number_of_tasks>this.max_number_of_tasks)";
        }
        //Precedence
        if (this.min_total_precedence_relations < 0 || this.max_total_precedence_relations < 0 || this.min_total_precedence_relations > this.max_total_precedence_relations) {
            return "Failed Check: NOT(this.min_total_precedence_relations<0 || this.max_total_precedence_relations<0 || this.min_total_precedence_relations>this.max_total_precedence_relations)";
        }
        //Subgraph
        if (this.subgraphNumber > this.max_number_of_tasks || this.subgraphNumber < 0 || (this.subgraphNumber > 0 && this.min_total_precedence_relations > 0) || (this.subgraphNumber > 0 && this.max_total_precedence_relations > 0)) {
            return "Failed Check: NOT(this.subgraphNumber > this.max_number_of_tasks || this.subgraphNumber < 0 || (this.subgraphNumber > 0 && this.min_total_precedence_relations > 0) || (this.subgraphNumber > 0 && this.max_total_precedence_relations > 0))";
        }
        //Residency
        if (this.min_residency_constraints < 0 || this.max_residency_constraints < 0 || this.min_residency_constraints > this.max_residency_constraints || this.min_residency_constraints > this.max_number_of_tasks) {
            return "Failed Check: NOT(this.min_residency_constraints<0 || this.max_residency_constraints<0 || this.min_residency_constraints>this.max_residency_constraints || this.min_residency_constraints > this.max_number_of_tasks)";
        }
        return null;
    }

    /**
     * Wrapper function for Sanity Checks. It executes the sanityChecks() function of Task Generation Input Parameters Class.
     */
    public void executeSanityChecks() {
        String sanitycheckstatus = this.sanityChecks();
        if (sanitycheckstatus != null) {
            System.out.println("=================CRITICAL ERROR, CONSTRAINTS DID NOT PASS SANITY CHECKS=================");
            System.out.println(sanitycheckstatus);
            System.exit(-1);
        }
    }

    public int getMin_number_of_tasks() {
        return min_number_of_tasks;
    }

    public void setMin_number_of_tasks(int min_number_of_tasks) {
        this.min_number_of_tasks = min_number_of_tasks;
    }

    public int getMax_number_of_tasks() {
        return max_number_of_tasks;
    }

    public void setMax_number_of_tasks(int max_number_of_tasks) {
        this.max_number_of_tasks = max_number_of_tasks;
    }

    public int getMin_number_of_resources() {
        return min_number_of_resources;
    }

    public void setMin_number_of_resources(int min_number_of_resources) {
        this.min_number_of_resources = min_number_of_resources;
    }

    public int getMax_number_of_resources() {
        return max_number_of_resources;
    }

    public void setMax_number_of_resources(int max_number_of_resources) {
        this.max_number_of_resources = max_number_of_resources;
    }

    public int getMin_period_length() {
        return min_period_length;
    }

    public void setMin_period_length(int min_period_length) {
        this.min_period_length = min_period_length;
    }

    public int getMax_period_length() {
        return max_period_length;
    }

    public void setMax_period_length(int max_period_length) {
        this.max_period_length = max_period_length;
    }

    public int getMin_residency_constraints() {
        return min_residency_constraints;
    }

    public void setMin_residency_constraints(int min_residency_constraints) {
        this.min_residency_constraints = min_residency_constraints;
    }

    public int getMax_residency_constraints() {
        return max_residency_constraints;
    }

    public void setMax_residency_constraints(int max_residency_constraints) {
        this.max_residency_constraints = max_residency_constraints;
    }

    public int getMin_period_percentage() {
        return min_period_percentage;
    }

    public void setMin_period_percentage(int min_period_percentage) {
        this.min_period_percentage = min_period_percentage;
    }

    public int getMax_period_percentage() {
        return max_period_percentage;
    }

    public void setMax_period_percentage(int max_period_percentage) {
        this.max_period_percentage = max_period_percentage;
    }

    public int getMin_total_precedence_relations() {
        return min_total_precedence_relations;
    }

    public void setMin_total_precedence_relations(int min_total_precedence_relations) {
        this.min_total_precedence_relations = min_total_precedence_relations;
    }

    public int getMax_total_precedence_relations() {
        return max_total_precedence_relations;
    }

    public void setMax_total_precedence_relations(int max_total_precedence_relations) {
        this.max_total_precedence_relations = max_total_precedence_relations;
    }

    public int getMin_deadline() {
        return min_deadline;
    }

    public void setMin_deadline(int min_deadline) {
        this.min_deadline = min_deadline;
    }

    public int getMax_deadline() {
        return max_deadline;
    }

    public void setMax_deadline(int max_deadline) {
        this.max_deadline = max_deadline;
    }

    public int getMin_releaseTime() {
        return min_releaseTime;
    }

    public void setMin_releaseTime(int min_releaseTime) {
        this.min_releaseTime = min_releaseTime;
    }

    public int getMax_releaseTime() {
        return max_releaseTime;
    }

    public void setMax_releaseTime(int max_releaseTime) {
        this.max_releaseTime = max_releaseTime;
    }

    public int getMin_WCET() {
        return min_WCET;
    }

    public void setMin_WCET(int min_WCET) {
        this.min_WCET = min_WCET;
    }

    public int getMax_WCET() {
        return max_WCET;
    }

    public void setMax_WCET(int max_WCET) {
        this.max_WCET = max_WCET;
    }

    public int getSchedulingLimit() {
        return schedulingLimit;
    }

    public void setSchedulingLimit(int schedulingLimit) {
        this.schedulingLimit = schedulingLimit;
    }

    public Distribution getNumberOfResidencyConstraints_distribution() {
        return numberOfResidencyConstraints_distribution;
    }

    public void setNumberOfResidencyConstraints_distribution(Distribution numberOfResidencyConstraints_distribution) {
        this.numberOfResidencyConstraints_distribution = numberOfResidencyConstraints_distribution;
    }

    public Distribution getNumberOfPrecedenceConstraints_distribution() {
        return numberOfPrecedenceConstraints_distribution;
    }

    public void setNumberOfPrecedenceConstraints_distribution(Distribution numberOfPrecedenceConstraints_distribution) {
        this.numberOfPrecedenceConstraints_distribution = numberOfPrecedenceConstraints_distribution;
    }

    public Distribution getNumberOfTasks_distribution() {
        return numberOfTasks_distribution;
    }

    public void setNumberOfTasks_distribution(Distribution numberOfTasks_distribution) {
        this.numberOfTasks_distribution = numberOfTasks_distribution;
    }

    public Distribution getNumberOfResources_distribution() {
        return numberOfResources_distribution;
    }

    public void setNumberOfResources_distribution(Distribution numberOfResources_distribution) {
        this.numberOfResources_distribution = numberOfResources_distribution;
    }

    public Distribution getResidency_distribution() {
        return residency_distribution;
    }

    public void setResidency_distribution(Distribution residency_distribution) {
        this.residency_distribution = residency_distribution;
    }

    public Distribution getPeriodicity_distribution() {
        return periodicity_distribution;
    }

    public void setPeriodicity_distribution(Distribution periodicity_distribution) {
        this.periodicity_distribution = periodicity_distribution;
    }

    public Distribution getPeriodLength_distribution() {
        return periodLength_distribution;
    }

    public void setPeriodLength_distribution(Distribution periodLength_distribution) {
        this.periodLength_distribution = periodLength_distribution;
    }

    public double getPeriodLengthPoissonMean() {
        return periodLengthPoissonMean;
    }

    public void setPeriodLengthPoissonMean(double periodLengthPoissonMean) {
        this.periodLengthPoissonMean = periodLengthPoissonMean;
    }

    public boolean MultiResidency() {
        return multiResidency;
    }

    public void setMultiResidency(boolean multiResidency) {
        this.multiResidency = multiResidency;
    }

    public boolean PhasedReleaseTimes() {
        return phasedReleaseTimes;
    }

    public void setPhasedReleaseTimes(boolean phasedReleaseTimes) {
        this.phasedReleaseTimes = phasedReleaseTimes;
    }

    public boolean PostHocPrecedence() {
        return postHocPrecedence;
    }

    public void setPostHocPrecedence(boolean postHocPrecedence) {
        this.postHocPrecedence = postHocPrecedence;
    }

    public boolean PostHocResidency() {
        return postHocResidency;
    }

    public void setPostHocResidency(boolean postHocResidency) {
        this.postHocResidency = postHocResidency;
    }

    public int getSubgraphNumber() {
        return subgraphNumber;
    }

    public void setSubgraphNumber(int subgraphNumber) {
        this.subgraphNumber = subgraphNumber;
    }

    public int getNumberOfWorkerThreads() {
        return numberOfWorkerThreads;
    }

    public void setNumberOfWorkerThreads(int numberOfWorkerThreads) {
        this.numberOfWorkerThreads = numberOfWorkerThreads;
    }

    public int getSolutionCount() {
        return solutionCount;
    }

    public void setSolutionCount(int solutionCount) {
        this.solutionCount = solutionCount;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isSaveOutput() {
        return saveOutput;
    }

    public void setSaveOutput(boolean saveOutput) {
        this.saveOutput = saveOutput;
    }

    public boolean isMaximizeWCET() {
        return maximizeWCET;
    }

    public void setMaximizeWCET(boolean maximizeWCET) {
        this.maximizeWCET = maximizeWCET;
    }

    public double getPeriodicityPoissonMean() {
        return periodicityPoissonMean;
    }

    public void setPeriodicityPoissonMean(double periodicityPoissonMean) {
        this.periodicityPoissonMean = periodicityPoissonMean;
    }

    public double getResidencyPoissonMean() {
        return residencyPoissonMean;
    }

    public void setResidencyPoissonMean(double residencyPoissonMean) {
        this.residencyPoissonMean = residencyPoissonMean;
    }

    public double getNOResidencyPoissonMean() {
        return NOResidencyPoissonMean;
    }

    public void setNOResidencyPoissonMean(double NOResidencyPoissonMean) {
        this.NOResidencyPoissonMean = NOResidencyPoissonMean;
    }

    public double getNOPrPoissonMean() {
        return NOPrPoissonMean;
    }

    public void setNOPrPoissonMean(double NOPrPoissonMean) {
        this.NOPrPoissonMean = NOPrPoissonMean;
    }

    public double getNOTasksPoissonMean() {
        return NOTasksPoissonMean;
    }

    public void setNOTasksPoissonMean(double NOTasksPoissonMean) {
        this.NOTasksPoissonMean = NOTasksPoissonMean;
    }

    public double getNOResourcesPoissonMean() {
        return NOResourcesPoissonMean;
    }

    public void setNOResourcesPoissonMean(double NOResourcesPoissonMean) {
        this.NOResourcesPoissonMean = NOResourcesPoissonMean;
    }

    public double getPeriodLengthBinomialP() {
        return periodLengthBinomialP;
    }

    public void setPeriodLengthBinomialP(double periodLengthBinomialP) {
        this.periodLengthBinomialP = periodLengthBinomialP;
    }

    public int getPeriodLengthBinomialN() {
        return periodLengthBinomialN;
    }

    public void setPeriodLengthBinomialN(int periodLengthBinomialN) {
        this.periodLengthBinomialN = periodLengthBinomialN;
    }

    public double getPeriodicityBinomialP() {
        return periodicityBinomialP;
    }

    public void setPeriodicityBinomialP(double periodicityBinomialP) {
        this.periodicityBinomialP = periodicityBinomialP;
    }

    public int getPeriodicityBinomialN() {
        return periodicityBinomialN;
    }

    public void setPeriodicityBinomialN(int periodicityBinomialN) {
        this.periodicityBinomialN = periodicityBinomialN;
    }

    public double getResidencyBinomialP() {
        return residencyBinomialP;
    }

    public void setResidencyBinomialP(double residencyBinomialP) {
        this.residencyBinomialP = residencyBinomialP;
    }

    public int getResidencyBinomialN() {
        return residencyBinomialN;
    }

    public void setResidencyBinomialN(int residencyBinomialN) {
        this.residencyBinomialN = residencyBinomialN;
    }

    public double getNOResidencyBinomialP() {
        return NOResidencyBinomialP;
    }

    public void setNOResidencyBinomialP(double NOResidencyBinomialP) {
        this.NOResidencyBinomialP = NOResidencyBinomialP;
    }

    public int getNOResidencyBinomialN() {
        return NOResidencyBinomialN;
    }

    public void setNOResidencyBinomialN(int NOResidencyBinomialN) {
        this.NOResidencyBinomialN = NOResidencyBinomialN;
    }

    public double getNOPrBinomialP() {
        return NOPrBinomialP;
    }

    public void setNOPrBinomialP(double NOPrBinomialP) {
        this.NOPrBinomialP = NOPrBinomialP;
    }

    public int getNOPrBinomialN() {
        return NOPrBinomialN;
    }

    public void setNOPrBinomialN(int NOPrBinomialN) {
        this.NOPrBinomialN = NOPrBinomialN;
    }

    public double getNOTasksBinomialP() {
        return NOTasksBinomialP;
    }

    public void setNOTasksBinomialP(double NOTasksBinomialP) {
        this.NOTasksBinomialP = NOTasksBinomialP;
    }

    public int getNOTasksBinomialN() {
        return NOTasksBinomialN;
    }

    public void setNOTasksBinomialN(int NOTasksBinomialN) {
        this.NOTasksBinomialN = NOTasksBinomialN;
    }

    public double getNOResourcesBinomialP() {
        return NOResourcesBinomialP;
    }

    public void setNOResourcesBinomialP(double NOResourcesBinomialP) {
        this.NOResourcesBinomialP = NOResourcesBinomialP;
    }

    public int getNOResourcesBinomialN() {
        return NOResourcesBinomialN;
    }

    public void setNOResourcesBinomialN(int NOResourcesBinomialN) {
        this.NOResourcesBinomialN = NOResourcesBinomialN;
    }

    public boolean isDeadlineEqualToPeriod() {
        return deadlineEqualToPeriod;
    }

    public void setDeadlineEqualToPeriod(boolean deadlineEqualToPeriod) {
        this.deadlineEqualToPeriod = deadlineEqualToPeriod;
    }
}
