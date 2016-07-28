package com.ernstthuer;

/**
 * Created by ethur on 7/27/16.
 */
public class SimpleRead implements Comparable<SimpleRead> {


    /**
     * Created by ethur on 7/21/16.
     */
    private int start;
    private String MZ;

    public SimpleRead(int start, String MZ) {
        this.start = start;
        this.MZ = MZ;
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
}


