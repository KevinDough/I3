//package robot; 
public class Robot2 
{
    /* Initial probability distibution of postiion */ 
    double[] p;
    /* The world */
    String[] world;
    /* Grid cells in the world */
    int n ;
    /* switch to decide if the sensor value was hit or miss */
    short hit, miss;
    /* sensor accuracy - probability of the sensor reporting right or wrong reading*/ 
    double pSenseRight,pSenseWrong;
    /* probability of motion hitting the mark, overshooting, or undershooting */ 
    double pHitMark,pOverShoot,pUnderShoot;
    public Robot2(int n, double[] p, String[] world, double pSenseRight, double pSenseWrong,double pHitMark, double pOverShoot, double pUnderShoot )
    {
        this.n=n;
        this.p=p;
        this.world=world; 
        this.pSenseRight=pSenseRight; 
        this.pSenseWrong=pSenseWrong; 
        this.pHitMark=pHitMark; 
        this.pOverShoot=pOverShoot; 
        this.pUnderShoot=pUnderShoot;
    }

    /* Measurement update. After executing this method, the p array will hold an updated probability distribution */
    public void afterSensing(String sensedValue){
        //YOUR CODE
    }

    /* Motion update. After executing this method, the p array will hold an updated probability distribution.
     * We are assuming here that if the robot is at X_i, it will move right to X_(i+2) with probability pHitMark, 
     * overshoot to X_(i+3) with probability pOverShoot, and undershoot to X_(i+1) with probability pUnderShoot */
    public void afterMoving(){
        //YOUR CODE
    }
    /** Utility methods you may want to use or Not! */
    /** This modified modulus method ensures that the returned value is always non-negative, in contrast to Java's % operator */
    private int mod(int x, int y) 
    {
        int result = x % y;
        return result < 0? result + y : result; 

    }
    /* * Method to Normalize a vector so that the elements sum to 1 */ 
    public double[] normalize(double[] q)
    {
        double[] r;
        r=new double[n];
        double sum=0;
        for (int i=0; i< q.length; i++)
            sum +=q[i]; 

        for (int i=0; i<q.length; i++)
            r[i]=q[i]/sum;
        return r; 

    }
    public static void printArray(double[] q) 
    {
        for(int i=0; i<q.length; i++)
        {
            System.out.println(i+" "+q[i]); 
        }
    }
    
    public static void main(String[] args)
    {
        double[] p={.0,.0,1.0,.0,.0,.0,.0,.0,.0,.0,.0,.0,.0,.0,.0,.0,.0,.0,.0,.0};
        String[] world={"R", "R", "G", "G","R","R", "G", "G","R", "R", "R", "G", "R", "R",
                "G", "G","R", "R", "R", "G"};
        String[] sensorReadings={"G", "R","G", "R","R","R","G"}; /*Assume one motion step after each sensing */
        Robot2 r2 =new Robot2(p.length,p, world, .85, .15, .8, .1, .1); for (int i=0; i<sensorReadings.length; i++){
            r2.afterSensing(sensorReadings[i]); r2.afterMoving();
            printArray(r2.p); System.out.println(); System.out.println();
        } 
    }
}