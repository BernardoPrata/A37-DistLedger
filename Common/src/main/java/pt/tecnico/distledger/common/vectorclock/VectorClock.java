package pt.tecnico.distledger.common.vectorclock;

import java.util.ArrayList;
import java.util.List;

public class VectorClock implements Comparable<VectorClock> {

    private List<Integer> vectorClock;

    /* Size defaults to 2: there are atleast two servers */
    public VectorClock() {
        this.vectorClock = new ArrayList<Integer>(2);
        vectorClock.add(0);
        vectorClock.add(0);
    }
    public VectorClock(List<Integer> vectorClock) {
        this.vectorClock = vectorClock;
    }
    public List<Integer> getVectorClock() {
        return vectorClock;
    }

    public List<Integer> increment(int index){
        vectorClock.set(index,vectorClock.get(index)+1);
        return vectorClock;
    }

    public void VectorClock(List<Integer> vectorClock) {
        this.vectorClock = vectorClock;
        vectorClock.add(0);
        vectorClock.add(0);
    }

    /* Adds an entry to the vector clock (when a new server is added) */
    public void addVectorClockEntry(int i) {
        vectorClock.add(i);
    }

    /* Builds a string of the form '<1, 2, 3, ...>' that resembles a Vector Clock representation */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder("<");
        for (int el : vectorClock){
            builder.append(el).append(", ");
        }

        builder.delete(builder.length() - 2, builder.length());
        builder.append(">");

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VectorClock that = (VectorClock) o;
        return vectorClock.equals(that.vectorClock);
    }

    /* Compares the current Vector Clock vc1 with another Vector Clock vc2.
     * The vc1 is greater (more recent) than vc2 if vc1[i] >= vc2[i]
     * for all their entries.
     *
     * (For tiebreakers)
     * To allow equal results for double-sided comparisions, the greater Vector Clock
     * is the one with the first biggest number.
     * Ex:
     * vc1 = <2, 1, 3>
     * vc2 = <3, 1, 2>
     * The greater Vector Clock is vc2 because the first greater number is 3 (3 > 2).
     * */
    @Override
    public int compareTo(VectorClock o) {
        if (o == null || getClass() != o.getClass()) return -1;

        List<Integer> vc1 = vectorClock;
        List<Integer> vc2 = o.getVectorClock();
        int firstGreaterEntryDifference = 0;
        boolean allGreaterOrEq = true;

        if (vc1.size() != vc2.size()) {
            System.err.println("[ERROR] - VectorClock.java - sizes are different ");
        }

        // Checks if the current VectorClock is greater than the other's
        for (int i = 0; i < vc1.size(); ++i){

            // Saves the first non-zero difference of entries
            if (firstGreaterEntryDifference == 0){
                firstGreaterEntryDifference = vc1.get(i) - vc2.get(i);
            }
            // If atleast one element of vc1 isn't greater than vc2,
            // then vc1 is not greater than vc2
            if ( !(vc1.get(i) >= vc2.get(i)) ){
                allGreaterOrEq = false;
            }
        }

        // If all the elements are greater or equal, then vc1 is greater
        if (allGreaterOrEq) return 1;
        allGreaterOrEq = true;

        // Does the same checking but for vc2
        for (int i = 0; i < vc1.size(); ++i){
            if ( !(vc2.get(i) >= vc1.get(i)) ){
                allGreaterOrEq = false;
                break;
            }
        }

        if (allGreaterOrEq) return -1;

        // Returns the difference. It is 0 if they are the same, positive if
        // vc1 has the first greater number and negative if vc2 has the
        // first greater number
        return firstGreaterEntryDifference;
    }
}