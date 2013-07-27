public class DerpyMain //By Kevin Dougherty
{
    public static void main(String args[])
    {
        long start = System.nanoTime();
        ThreadGroup total = new ThreadGroup("all"); // a group for all the threads to test if any are active
        for (int i = 0; i <= 500; i = i +2) //pump out a number of threads, running concurrently
        {
            new Thread(total, new Derpy(i)).start();
            new Thread(total, new Derpy(i+1)).start();
        }
        while (total.activeCount() != 0); //do nothing if there are active threads in the group
        
        System.out.println(System.nanoTime() - start + " Nanoseconds"); // print total nanoseconds
    }
}