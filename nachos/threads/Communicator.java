package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	//thread is being spoken here
    	lock.acquire();
    	//acquire it and check if there is already a msg
    	//or if there is nothing listening				*if either of the conditions, sleep the speaker
    	while(msg != null || listenflag == 0) speak.sleep();
    	
    	//taking the parameter, make a message out of it and make someone listen to it
    	msg = new Integer(word);
    	listen.wake();
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	lock.acquire();
    	//increment the flag indicating there is someone listening
    	listenflag++;
    	//now there is a listener, we need to check if there is even anyone speaking
    	while(msg == null){
    		//if there is no one sppeaking, wake a speaker and set the listener to sleep
    		speak.wake();
    		listen.sleep();
    	}
    	
    	//createa a thread to hold the value of the message that was woken up
    	int currMsg = msg.intValue();
    	msg = null;
    	//decrement the flag as now there is a msg that can be listened to
    	listenflag--;
    	//return the message
    	return currMsg;
    }
    
    private static class Speak implements Runnable{
    	Speak(Communicator communicator, String name){
    		this.communicator = communicator;
    		this.name = name;
    	}
    	
    	public void run(){
    		//int i = 0;
    		//while(int i != )
    		for(int i = 0; i <=1; i++){
    			communicator.speak(i);
    			System.out.println(name + " said " +i);
    		}
    		
    	}
    	
    	private Communicator communicator;
    	private String name;
    }
    
    
    private static class Listen implements Runnable{
    	Listen(Communicator communicator, String name){
    		this.communicator = communicator;
    		this.name = name;
    	}
    	
    	public void run(){
    		//int i = 0;
    		//while(int i != )
    		for(int i = 0; i <=1; i++){
    			int ear = communicator.listen();
    			System.out.println(name + " said " + ear);
    		}
    		
    	}
    	
    	private Communicator communicator;
    	private String name;
    }
    
    public static void testing(){
    	//create the main communicator
    	Communicator comy = new Communicator();
    	//add the threads to it
    	KThread t1 = new KThread(new Speak(comy, "CSE"));
    	KThread t2 = new KThread(new Listen(comy, "120"));
    	KThread t3 = new KThread(new Speak(comy, "420"));
    	//fork em threads
    	t1.fork();
    	t2.fork();
    	t3.fork();
    	//listen to dem threads
    	new Listen(comy, "When is Friday").run();
    }
    
    private Lock lock = new Lock();
    private Condition2 listen = new Condition2(lock);
    private Condition2 speak = new Condition2(lock);
    
    //initialize as Integer since it has easy methods for str to int and vice versa
    private Integer msg = null;
    private int listenflag = 0;
}
