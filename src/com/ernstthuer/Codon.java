package com.ernstthuer;

import java.util.HashMap;

import static com.ernstthuer.Main.codonConversion;

public class Codon implements Comparable<Codon> {

    /**
     * simple implementation of Codon class, to ease access to the CodonList
     */


    private String sequence;
    private String aminoAcid;


    public Codon(String sequence) {
        this.sequence = sequence;
        seqToAA();
        //this.conversion = conversion;
    }

    public void seqToAA() {
        if (codonConversion.containsKey(sequence)) {
            this.aminoAcid = codonConversion.get(sequence);
        }
    }


    public String getSequence() {
        return sequence;
    }

    public String getAminoAcid() {
        return aminoAcid;
    }

    @Override
    public int compareTo(Codon o) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Codon codon = (Codon) o;

        return aminoAcid != null ? aminoAcid.equals(codon.aminoAcid) : codon.aminoAcid == null;

    }

    @Override
    public int hashCode() {
        return aminoAcid != null ? aminoAcid.hashCode() : 0;
    }
}
