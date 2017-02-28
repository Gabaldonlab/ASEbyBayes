package com.ernstthuer;

/**
 * Created by ethur on 7/27/16.
 *
 *
 * Deprecate this
 *
 */
//public class SimpleRead implements Comparable<SimpleRead> {
class SimpleRead {

    /**
     * Has to be simplified
     */
    private int start;
    private int stop;


    public SimpleRead(int start, int stop) {
        this.start = start;
        this.stop = stop;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }


    /*
    @Override
    public int compareTo(SimpleRead o) {
        if(o.getStart() < this.start){
            return 1;
        }if(o.getStart() == this.start){
            return 0;
        }
        else {
            return -1;
        }
    }

*/
    /*
    private String MZ;
    private String CIGAR = null;

    public SimpleRead(int start, String MZ) {
        this.start = start;
        this.MZ = MZ;

    }

    public SimpleRead(int start, String MZ, String CIGAR) {
        this.start = start;
        this.MZ = MZ;
        this.CIGAR = CIGAR;
    }

    public int getStart() {
        return start;
    }

    public String getMZ() {
        return MZ;
    }


    @Override
    public int compareTo(SimpleRead o) {
        if(this.start == o.start){
            if(this.MZ.length() > o.MZ.length()){
                return 2;
            }
            return 1;
        }
        return 0;
    }
*/
}


