package dt.tsg.task;

import dt.tsg.InputParams.InputParameters;
import dt.tsg.InputParams.TGFFInputParameters;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

public class InputFileTest {


    @Test
    public void testReadingInputFileLine() {

        String line = "tasknum 0 9\n";

        // get min and max values after tasknum
        int mintasknum;
        int maxtasknum;
        String mintaskString = line.substring(line.indexOf(" ")+1,line.indexOf(" ", line.indexOf(" ")+1));
        System.out.println("mintaskString=" +  mintaskString);
        mintasknum = Integer.parseInt(mintaskString);
        System.out.println(mintasknum);
        String maxtaskString = line.substring(line.indexOf(" ",line.indexOf(" ")+1) + 1, line.indexOf("\n"));
        System.out.println("maxtaskString=" + maxtaskString);
        maxtasknum = Integer.parseInt(maxtaskString);
        System.out.println(maxtasknum);

    }

    @Test
    public void testReadingDistributionLine() {

        String line = "residencydistribution UNIFORM\n";

        String distributionString = line.substring(line.indexOf(" ") + 1, line.indexOf("\n")).toUpperCase();
        InputParameters.Distribution distribution = InputParameters.Distribution.UNIFORM;
        switch (distributionString) {
            case "UNIFORM" -> distribution = InputParameters.Distribution.UNIFORM;
            case "GEOMETRIC" -> distribution = InputParameters.Distribution.GEOMETRIC;
            case "POISSON" -> distribution = InputParameters.Distribution.POISSON;
            default ->
                    System.out.println("Distribution not recognized during input-file reading.\nDistribution = " + distributionString);
        }
        System.out.println(distributionString);
        System.out.println(distribution);
    }

    @Test
    public void testReadingDoubleLine() {

        String line = "periodlengthpoissonmean 1.0\n";

        String poissonmeanString = line.substring(line.indexOf(" ")+1, line.indexOf("\n"));
        double poissonmean = Double.parseDouble(poissonmeanString);
        System.out.println("poissonmean=" + poissonmean);
    }

    @Test
    public void testInputFileBasicGeneration() throws Exception {
        InputParameters inputs = InputParameters.readInputFile("inputFiles/exampleInput.txt", new ArrayList<>());

        System.out.println(inputs);
    }

    @Test
    public void testInputFileBasicGeneration2() throws Exception {
        TaskGenerationFramework tgf = new TaskGenerationFramework(true, new Random());

        InputParameters inputs = InputParameters.readInputFile("inputFiles/exampleInput.txt", new ArrayList<>());

        System.out.println(inputs);

        tgf.GenerateBasicTaskSet(inputs, false);
    }

    @Test
    public void testInputFileTGFFGeneration1() throws Exception {
        InputParameters inputs = InputParameters.readInputFile("inputFiles/exampleTGFFInput.txt", new ArrayList<>());

        System.out.println("\n" + inputs);
    }

    @Test
    public void testInputFileTGFFGeneration2() throws Exception {
        TaskGenerationFramework tgf = new TaskGenerationFramework(true, new Random());

        InputParameters inputs = InputParameters.readInputFile("inputFiles/exampleTGFFInput.txt", new ArrayList<>());

        System.out.println("\n" + inputs);

        tgf.GenerateTaskSetFromTGFF((TGFFInputParameters) inputs);
    }
}
