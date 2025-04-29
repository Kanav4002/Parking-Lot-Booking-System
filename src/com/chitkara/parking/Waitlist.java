package com.chitkara.parking;

import java.util.*;

public class Waitlist {
    private Queue<User> queue = new LinkedList<>();

    public void addToWaitlist(User u) { queue.add(u); }
    public User removeFromWaitlist()  { return queue.poll(); }
    public User viewNextInWaitlist()  { return queue.peek(); }
    public boolean isEmpty()          { return queue.isEmpty(); }
    public Queue<User> getWaitlist() {
        return queue;
    }

}
