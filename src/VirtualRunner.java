/**
 * A helper class used for batch running beam searches.
 */
public class VirtualRunner {
    /**
     * Main class that runs the Core.main class for multiple parameter configurations.
     * @param args Not used.
     */
    public static void main(String[] args) {

        String[] arguments = new String[]{
                "-d", "3",
                "-w", "10",
                "-set-length", "20",
                "-y-target", "like",
                "-x-targets", "attractive_partner,sincere_partner,intelligence_partner,funny_partner,ambition_partner,shared_interests_partner",
                "-dataset-file", "data/speed_dating_altered.arff",
                "-blacklist", "decision,decision_o",
                "-min-group-size", "200",
                "-output-file", "result_1.csv",
        };

        Core.main(arguments);

    }
}
