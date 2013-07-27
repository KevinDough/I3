public class Q2 {
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
    double pHitMark,pOverShoot,pUnderShoot; /*There is no overShoot; underShoot means stay
    put */
    public Q2(int n, double[] p, String[] world, double pSenseRight, double pSenseWrong,double pHitMark, double pUnderShoot, double pOverShoot ){
        this.n=n;
        this.p=p;
        this.world=world; this.pSenseRight=pSenseRight; this.pSenseWrong=pSenseWrong; this.pHitMark=pHitMark;
        this.pOverShoot = pOverShoot;

        this.pUnderShoot=pUnderShoot; }

    /* Measurement update. After executing this method, the p array will hold an updated probability distribution */
    public void afterSensing(String sensedValue){ double[] q;
        q=new double[n];
        hit=0;
        miss=0;
        for (int i=0; i < world.length; i++){
            if(sensedValue.compareTo(world[i])==0){ hit=1;
                miss=0; }else{
                hit=0;
                miss=1; }
            q[i]= p[i]*(hit*pSenseRight+miss*pSenseWrong); }
        this.p= this.normalize(q); }

    /* Motion update. After executing this method, the p array will hold an updated probability distribution */
    public void afterMoving(){ 
        double[] q;
        q=new double[n]; hit=0;
        miss=0;
        int len=p.length;
        for (int i=0; i<len;i++){
            q[i]=pHitMark*p[mod(i-1, len)]+pUnderShoot*p[mod(i, len)]+pOverShoot*p[mod(i-2, len)];
        }
        this.p=q; }
    /** Utility methods you may want to use or Not! */
    /*This modified modulus method ensures that the returned value is always non-negative, in contrast to Java's % operator */
    private int mod(int x, int y) {
        int result = x % y;
        return result < 0? result + y : result; 
    }
    public double[] normalize(double[] q){ double[] r;
        r=new double[n];
        double sum=0;
        for (int i=0; i< q.length; i++){
            sum +=q[i]; }
        for (int i=0; i<q.length; i++){ r[i]=q[i]/sum;
        }
        return r;
    }
    public static void printArray(double[] q){ for(int i=0; i<q.length; i++){
            System.out.println(i+" "+q[i]); 
        }
    }
    public static void main(String[] args){
        double[] p={0, 1, 0, 0, 0};
        String[] world={"R", "R", "G", "G", "R"}; String[] sensorReadings={"R","G","R","R", "G"}; /*Assume one motion step after each sensing */ 
        Q2 r2 =new Q2(p.length,p, world, .85, .15, .7, .1, .2); 
        for (int i=0; i<sensorReadings.length; i++){
            r2.afterSensing(sensorReadings[i]); r2.afterMoving();
            printArray(r2.p); System.out.println(); System.out.println();
        } 
    }
}