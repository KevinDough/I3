public class Derpy implements Runnable
{
    private int number;
    public Derpy(int aNumber)
    {
        number = aNumber;
    }
    public void run()
    {
        System.out.println("Number " + number + " taking a nap boss!");
        try{Thread.sleep(5);}//so its not immeasureable
        catch(InterruptedException e) {}
        System.out.println("Im up boss! Number " + number + " reporting!"); //using this and line 10 to make sure thread are concurrent
    }
    
}