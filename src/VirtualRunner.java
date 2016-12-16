public class VirtualRunner {
    public static void main(String[] args) {
        /*String[] evaluators = new String[]{"WRA","SEN","SPEC","X2"};
        String[] minQuality = new String[]{"0.02","0.9","0.9","300"};*/
        String[] evaluators = new String[]{"SEN"};
        String[] minQuality = new String[]{"0.9"};

        for(int i = 0; i < evaluators.length; i++) {
            String[] arguments = new String[]{"-quality-measure", evaluators[i], minQuality[i], "-blacklist", "decision,decision_o", "-t", "-d", "1", "-w", "20", "-set-length", "20","-null-is-zero"};
            Core.main(arguments);
        }
    }
}
