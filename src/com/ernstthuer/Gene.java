package com.ernstthuer;

import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ethur on 7/26/16.
 */
public class Gene {

    ArrayList<SNP> snpsOnGene = new ArrayList<>();
    private DNASequence sequence = new DNASequence();
    private String chromosome;
    private int start;
    private int stop;
    private String ident;

    // storing SNPs on the gene, and reads in a barebone form for reference.  this might be irrelevant due to Sam locus iteration...
    private List<SNP> geneSNPList = new ArrayList<>();
    private List<SimpleRead> geneReadList = new ArrayList<>();
    private String ASE;

    public Gene(String chromosome, int start, int stop, String ident) {
        this.chromosome = chromosome;
        this.start = start;
        this.stop = stop;
        this.ident = ident;
    }

    public String getChromosome() {
        return chromosome;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    public String getIdent() {
        return ident;
    }

    public void loadSequence(DNASequence fullGenome, boolean fullChrom) {
        if (fullChrom) {
            try {
                this.sequence = new DNASequence(fullGenome.getSubSequence(this.start, this.stop).toString());
            } catch (CompoundNotFoundException e) {
                System.out.println("coldn't parse fasta sequence " + e);
            }
        } else {
            try {
                this.sequence = fullGenome;
            } catch (Exception e) {
                System.out.println("coldn't parse fasta sequence " + e);
            }
        }

    }

    public void addRead(SimpleRead read){
        this.geneReadList.add(read);
    }

}




