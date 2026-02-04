//            This is just a sample class and method to illustrate how we can pull the
//             variables sent from the addButton click event on the form and man handle them
//             this just outputs to the console we can delete this file later


public class SampleConfig {

    public static void handleConfig(
            int weeksToSimulate,
            int schoolDaysPerWeek,
            int dailyStartOffsetSec,
            double visibleMeanSec,
            double visibleStdDevSec,
            int defaultSlideSec
    ) {
        System.out.println("=== SampleConfig.handleConfig() ===");
        System.out.println("weeksToSimulate     = " + weeksToSimulate);
        System.out.println("schoolDaysPerWeek   = " + schoolDaysPerWeek);
        System.out.println("dailyStartOffsetSec = " + dailyStartOffsetSec);
        System.out.println("visibleMeanSec      = " + visibleMeanSec);
        System.out.println("visibleStdDevSec    = " + visibleStdDevSec);
        System.out.println("defaultSlideSec     = " + defaultSlideSec);
        System.out.println("===================================");
    }
}
