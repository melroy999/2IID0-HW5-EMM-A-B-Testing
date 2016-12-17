public class VirtualRunner {
    public static void main(String[] args) {
        String[] evaluators = new String[]{"WRA","SEN","SPEC","X2"};
        String[] minQuality = new String[]{"0.02","0.9","0.9","300"};

        for(int i = 0; i < evaluators.length; i++) {
            String[] arguments = new String[]{"-quality-measure", evaluators[i], minQuality[i], "-blacklist", "decision,decision_o", "-t", "-d", "4", "-w", "20", "-set-length", "20","-null-is-zero","-target","match","1","-dataset-file","data/dataset.arff", "-target-comparison","EQ"};
            Core.main(arguments);
        }

        /*for(int i = 0; i < evaluators.length; i++) {
            String[] arguments = new String[]{"-quality-measure", evaluators[i], minQuality[i], "-t", "-d", "4", "-w", "20", "-set-length", "20","-null-is-zero","-target","action","-dataset-file","data/clicking_data_5.arff"};
            Core.main(arguments);
        }*/
    }
}
