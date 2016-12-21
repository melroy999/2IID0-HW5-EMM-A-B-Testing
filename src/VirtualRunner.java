/**
 * A helper class used for batch running beam searches.
 */
public class VirtualRunner {
    /**
     * Main class that runs the Core.main class for multiple parameter configurations.
     * @param args Not used.
     */
    public static void main(String[] args) {
        /*String[] evaluators = new String[]{"WRA","SEN","SPEC","X2"};
        String[] minQuality = new String[]{"0.02","0.9","0.9","300"};*/

        /*String[] arguments = new String[]{"--help"};
        Core.main(arguments);*/

        /*for(int i = 0; i < evaluators.length; i++) {
            arguments = new String[]{"-quality-measure", evaluators[i], minQuality[i], "-blacklist", "decision,decision_o", "-t", "-d", "4", "-w", "20", "-set-length", "20","-null-is-zero","-target","match","true","-dataset-file","data/dataset.arff", "-target-comparison","EQ"};
            Core.main(arguments);
        }*/

        /*String[] evaluators = new String[]{"WRA","SEN","SPEC","X2"};
        String[] minQuality = new String[]{"0.0","0.0","0.0","0"};

        for(int i = 0; i < evaluators.length; i++) {
            String[] arguments = new String[]{"-quality-measure", evaluators[i], minQuality[i], "-t", "-d", "4", "-w", "20", "-set-length", "20","-null-is-zero","-target","condition","1-Control","-dataset-file","data/experiment_details_5.arff", "-target-comparison","EQ"};
            Core.main(arguments);
        }*/

        String[] arguments = new String[]{"-quality-measure", "WRA", "0.0", "-t", "-d", "1", "-w", "20", "-set-length", "20","-null-is-zero","-target","action,clic,condition,2-Buttony-Conversion-Buttons","-dataset-file","data/collective_data_5.arff", "-target-comparison","EQ,EQ","-blacklist", "page_url,refr_source,geo_region,dvce_ismobile,page_title,dvce_created_tstamp,domain_sessionidx,platform,collector_tstamp,browser_colordepth,etl_tstamp,domain_userid,geo_timezone,geo_city,event,browser_language,root_tstamp,browser_cookies,experiment_id,experiment_collector_tstamp,event_id,user_id,domain_sessionid,geo_region_name,page_referrer,dvce_type,os_timezone"};
        Core.main(arguments);

        //, -blacklist, platform,etl_tstamp,collector_tstamp,dvce_created_tstamp,event_id,domain_userid,domain_sessionidx,domain_sessionid,user_id,

        //condition,action_label,action_type,location_label,location_type,action,geo_country,useragent,browser_viewwidth,browser_viewheight,os_name,dvce_screenwidth,dvce_screenheight,doc_charset,doc_width,doc_height,pp_xoffset_min,pp_xoffset_max,pp_yoffset_min,pp_yoffset_max
    }
}
