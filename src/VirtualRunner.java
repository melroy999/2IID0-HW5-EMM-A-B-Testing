/**
 * A helper class used for batch running beam searches.
 */
public class VirtualRunner {
    /**
     * Main class that runs the Core.main class for multiple parameter configurations.
     * @param args Not used.
     */
    public static void main(String[] args) {
        String[] evaluators = new String[]{"WRA","SEN","SPEC","X2"};
        String[] minQuality = new String[]{"0.02","0.9","0.9","300"};

        String[] arguments;
        for(int i = 0; i < evaluators.length; i++) {
            arguments = new String[]{"-quality-measure", evaluators[i], minQuality[i], "-t", "-d", "1", "-w", "20", "-set-length", "20","-null-is-zero", "-target", "match,EQ,true", "-dataset-file", "data/speed_dating.arff", "-blacklist", "decision,decision_o"};
            Core.main(arguments);
        }
    }
}
