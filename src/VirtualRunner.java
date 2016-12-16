public class VirtualRunner {
    public static void main(String[] args) {
        String[] evaluators = new String[]{"WRA","SEN","SPEC","X2"};
        String[] minQuality = new String[]{"0.02","0.5","0.5","300"};

        for(int i = 0; i < evaluators.length; i++) {
            String[] arguments = new String[]{"-quality-measure", evaluators[i], minQuality[i], "-blacklist", "decision,decision_o", "-t", "-d", "2", "-w", "20", "-set-length", "20"};
            Main.main(arguments);
        }
    }
}
