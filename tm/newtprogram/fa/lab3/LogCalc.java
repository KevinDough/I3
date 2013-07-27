import java.lang.*;
public class LogCalc{
    public static void main(String[] args){
        
        int x = 3;
        System.out.println(solve(27));
        for(int i = 1; i < 31; i++){
            System.out.print("T(" + i + ") = ");
            //System.out.println(Math.floor(((x-1)/Math.log10(x))* Math.log10(i)) +1);
            //System.out.println(Math.floor(Math.log10(i)/Math.log10(x))+1);
        }
        


    }
    public static double solve(int i){
        if(i == 1)
            return i;
        else        
            return i*i + (Math.log10(solve(i-1))/Math.log10(3))*5;
    }

}