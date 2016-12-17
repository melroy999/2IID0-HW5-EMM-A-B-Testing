public class VirtualRunner {
    public static void main(String[] args) {
        String[] evaluators = new String[]{"WRA","SEN","SPEC","X2"};
        String[] minQuality = new String[]{"0.02","0.9","0.9","300"};

        for(int i = 0; i < evaluators.length; i++) {
            String[] arguments = new String[]{"-quality-measure", evaluators[i], minQuality[i], "-blacklist", "decision,decision_o", "-t", "-d", "4", "-w", "20", "-set-length", "20","-null-is-zero","-target","match","true","-dataset-file","data/dataset.arff", "-target-comparison","EQ"};
            Core.main(arguments);
        }

        /*String[] evaluators = new String[]{"WRA","SEN","SPEC","X2"};
        String[] minQuality = new String[]{"0.0","0.0","0.0","0"};

        for(int i = 0; i < evaluators.length; i++) {
            String[] arguments = new String[]{"-quality-measure", evaluators[i], minQuality[i], "-t", "-d", "4", "-w", "20", "-set-length", "20","-null-is-zero","-target","condition","1-Control","-dataset-file","data/experiment_details_5.arff", "-target-comparison","EQ"};
            Core.main(arguments);
        }*/
    }
}
