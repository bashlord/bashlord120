package nachos.threads;

import nachos.machine.*;
import java.util.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
	PriorityQueue<timeNode> timeStruct;
	Lock lock;
    public Alarm() {
    timeStruct = new PriorityQueue(1, new Priority());
    lock = new Lock();
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    long Wtimer = Machine.timer().getTime();	//variable that holds the current time   
    timeNode node = timeStruct.peek();
    
    //if the node in the current PQ struct that is being peeked at is 
    //a null, then end that one and more to the next one
    if(node == null){
    	KThread.currentThread().yield();
    }
    else{
    	timeNode tnode = timeStruct.peek();
    	long waker = tnode.wtimer;//get the time value of the current
    	while(waker <= Wtimer) //while the time of the current is less that the timer of the machine timer
    	{
    		tnode = timeStruct.poll(); //return and remove the head of the struct
    		tnode.semaphore.V();//call V() which increments and allows or disables the number of threads running
    		tnode = timeStruct.peek();//check the next timer
    		
    		if(tnode == null){
    			break;//same rules apply if its null then dont do anything
    		}
    		Wtimer = tnode.wtimer;//set the new timer
    		
    	}
    	KThread.currentThread().yield();
    }
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
	long wakeTime = Machine.timer().getTime() + x;
	Semaphore semaphore = new Semaphore(0);
	timeNode node = new timeNode(wakeTime, semaphore);//create a new tnode to be added
													//to the struct and waited on
	
	lock.acquire();//puts a lock on the 
	timeStruct.add(node);//adds the node to the struct, the node gets organized in the struct and gets released in order
	lock.release();//this is when the next lock gets released and things can get with the thread now
	semaphore.P();//uses P(), which either decrements for the next node or sleeps until there is something to decrement
	

    }
    /*
     * THIS IS THE CLASS THAT JUST HOLDS TIMES AND USES A SEMAPHORE TO DISH OUT THE
     * PERMITS NEEDED FOR THREADS TO BE INTERRUPTSED/UNINTERRUPTED IN A TIMELY
     * FASHION.  PUT A BUNCH OF THESE IN THE STRUCT.  
     */
    static class timeNode{
    	private long wtimer; //the time when it wakes
    	private Semaphore semaphore;
    	
    	timeNode(long wtimer,Semaphore semaphore){
    		this.wtimer = wtimer;
    		this.semaphore = semaphore;
    	}
    	
    	public String stringValue(){
    		String stringbean = String.valueOf(wtimer);
    		return stringbean;
    		
    	}
    	
    	
    }
    /*
     * THIS IS THE CLASS THAT COMPARES THE STUFF IN THE TIMESTRUCT SO THEY ARE
     * ALL NEAT AND IN ORDER AND IN QUEUE SO THE ALARM WORKS IN ORDER
     */
    public class Priority implements Comparator{
    	public int compare(Object a, Object b){
    		timeNode o1 = (timeNode) a;
    		timeNode o2 = (timeNode) b;
    		if(o1.wtimer < o2.wtimer)
    		{
    			return 1;//return 1 if first param is LESS THAN second
    		}
    		else if(o1.wtimer > o2.wtimer){
    			return -1;//return -1 if first param IS GREATER THAN second
    		}
    		else{
    			return 0;//return 0 IF THE PARAMS ARE EQUAL
    		}
    	}
    	
    }
}
