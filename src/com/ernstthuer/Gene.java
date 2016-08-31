package com.ernstthuer;

import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by ethur on 7/26/16.
 */
public class Gene implements Cloneable {

    ArrayList<SNP> snpsOnGene = new ArrayList<>();
    private DNASequence sequence = new DNASequence();
    private String chromosome;
    private int start;
    private int stop;
    private String ident;
    private String orientation = "forward";
    private HashMap<Integer, Codon> codonList = new HashMap<>();
    private ArrayList<SimpleRead> geneReadList = new ArrayList<>();
    private String ASE;
    private LinkedHashMap<Integer,Integer> positioncount = new LinkedHashMap<>();



    @Override
    protected Object clone() throws CloneNotSupportedException {

        this.snpsOnGene = new ArrayList<SNP>();
        return super.clone();

    }

    public Gene(String chromosome, int start, int stop, String ident) {
        this.chromosome = chromosome;
        this.start = start;
        this.stop = stop;
        this.ident = ident;

        if(start < stop){
            orientation = "forward";
        }
        if(start == stop){
            System.out.println("Gene " + this.ident + " has no length" );
        }
        if(start > stop){
            orientation = "reverse";
            int intermed = start;
            this.start = stop;
            this.stop = intermed;
        }

        //System.out.println("Created gene " + ident);
    }


    public void addCoverage(int start, int stop){

        for(int i = start; i < stop; i++){
            int count = this.positioncount.containsKey(i) ? positioncount.get(i) : 0;
            positioncount.put(i,count+1);
        }

    }

    public LinkedHashMap<Integer, Integer> getPositioncount() {
        return positioncount;
    }

    public ArrayList<SNP> getSnpsOnGene() {
        return snpsOnGene;
    }

    public boolean addSNP(SNP snp, boolean existingKnowledge) {

        if(existingKnowledge){
            if(snpsOnGene.contains(snp)){
                int idx = snpsOnGene.indexOf(snp);
                snpsOnGene.get(idx).increaseAltCoverage();
                // all SNPs are validated
                snpsOnGene.get(idx).setValidated(2);
            }

            return false;  // if SNPs are already known from an imported vcf file, no need to add new ones
        }

        if (!snpsOnGene.contains(snp)) {
            if(snp.getPosition() > this.start && snp.getPosition() < this.stop) {
                snpsOnGene.add(snp);
                return true;
            }return false;
        }

        else {
            int idx = snpsOnGene.indexOf(snp);
            snpsOnGene.get(idx).increaseAltCoverage();
            if (snpsOnGene.get(idx).getALTcov() > 2) {
                snpsOnGene.get(idx).setValidated(1);
            }
            return false;
        }
    }




    public void findORGCoverageOfSNPs() {
        // trigger this after the SNPs were loaded and the simplified reads are stored on the genes.

        // geneReadList.sort();
        // Collections.sort(geneReadList);

        for (SNP snip : snpsOnGene) {
            try {

                // ToDO remove sout


                // only for SNPs with at least 2 observations
                if (snip.isValidated() > 0) {
                    int count = 0;

                    int index = snpsOnGene.indexOf(snip);

                    if(positioncount.keySet().contains(snip.getPosition())) {
                        snip.setORGcov(positioncount.get(snip.getPosition()));
                    }

                    /*

                    for (SimpleRead splRd : geneReadList) {


                        try {
                            if (splRd.getStart() <= snip.getPosition() && (splRd.getStop()) >= snip.getPosition()) {
                                // overlap, check if the positions are absolut or on gene level

                                try {
                                    snpsOnGene.get(index).increaseORGcov();
                                } catch (Exception e) {
                                    System.out.println("Failed on line 116 ");
                                }
                                // System.out.println(" increased SNP coverage with read : " );
                            }
                        } catch (Exception e) {
                            count ++;
                            //System.out.println("Failed to compare SNP to read " + this.ident + "  " + snip.getPosition() + count );
                        }
                    }

                    */
                    //System.out.println(snip.getPosition() + "  " + snip.getALTcov() + " ORG :" + snip.getORGcov() + " count : " +  count );
                }
            }catch (Exception e){
                System.out.println("Failed on line 101");
            }
        }
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public void geneToCodon(String codingSequence){
        // Codon list for all positions, for each position
        // HashMap<Integer, org.biojava.nbio.core.sequence.template.Sequence> CodonList = new HashMap<>();
        // for each codon on sequence

        // make sure coding sequence is divisible by 3
        int overshot = codingSequence.length() % 3;



        for(int i = 0; i < codingSequence.length() - overshot ; i = i+3) {
            //org.biojava.nbio.core.sequence.template.Sequence codon = sequence.getSubSequence(i,i+2).getViewedSequence();
            Codon codon = new Codon(codingSequence.substring(i,i+3));
            for(int j=i;j<(i+3);j++) {
                codonList.put(j, codon);
            }
        }
        }

    public String getAltCodon(int pos, char altChar, String orgCodon){
        StringBuilder newCodon = new StringBuilder(orgCodon);
        newCodon.setCharAt(pos,altChar);
        return newCodon.toString();
    }

    public void findSynonymity(int validationLVL) {
        if (orientation.equals("forward")) {
            geneToCodon(this.sequence.getSequenceAsString());

        }
        if (orientation.equals("reverse")){
            geneToCodon(this.sequence.getInverse().getSequenceAsString());
        }
            for (SNP snp : snpsOnGene) {
                if (snp.isValidated() >= validationLVL) {

                    // find SNP on codon
                    int relativePosition = snp.getPosition() - start;
                    int remain = relativePosition % 3;


                    Codon refCodon = codonList.get(relativePosition);


                    try {

                        StringBuilder altSeq = new StringBuilder(refCodon.getSequence());
                        altSeq.setCharAt(remain, snp.getALT());

                        Codon altCodon = new Codon(altSeq.toString());

                        snp.setSynonymous(altCodon.equals(refCodon));

                        //System.out.println(refCodon.getSequence() + "  " +  altCodon.getSequence() + "  " + altCodon.equals(refCodon) );


                    }catch(NullPointerException e){
                        System.out.println(snp.getPosition() + "  " + snp.getGene().getIdent());
                    }
                    //String altCodon = getAltCodon(0, snp.getALT(), refCodon.getSequence());
                    //altCodon = snp.getALT() + altCodon.substring(1,2);
                    //StringBuilder newCodon = new StringBuilder(altCodon);
                    //newCodon.setCharAt(remain, snp.getALT());

                }
            }

    }

    public void evaluateGeneWiseExpression(){

        
    }


    public DNASequence getSequence() {
        return sequence;
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


    public void setSnpsOnGene(ArrayList<SNP> snpsOnGene) {
        this.snpsOnGene = snpsOnGene;
    }

    public void addSnpInformation (ArrayList<SNP> outsideSnpsOnGene){

        for(SNP snp:outsideSnpsOnGene){
            if(snpsOnGene.contains(snp)){
                snpsOnGene.get(snpsOnGene.indexOf(snp)).setFoundInReplicates(snpsOnGene.get(snpsOnGene.indexOf(snp)).getFoundInReplicates() + 1);
                snpsOnGene.get(snpsOnGene.indexOf(snp)).setALTcov(( snpsOnGene.get(snpsOnGene.indexOf(snp)).getALTcov() + snp.getALTcov() / 2));  // ToDo :  improve mean calculation
                snpsOnGene.get(snpsOnGene.indexOf(snp)).setORGcov(( snpsOnGene.get(snpsOnGene.indexOf(snp)).getORGcov() + snp.getORGcov() / 2)); // ToDo :  improve mean calculation
                // for the moment a simple mean will do  ToDo :  improve mean calculation
            }
            else{
                snpsOnGene.add(snp);
            }
        }
    }

    public void loadSequence(DNASequence fullGenome, boolean fullChrom) {


        //System.out.println(fullGenome.getSubSequence(100 , 102).getSequenceAsString());

        if (fullChrom) {
            try {
                //System.out.println( " Gene from here "+this.start);
                try {

                    this.sequence = new DNASequence(fullGenome.getSubSequence(this.start, this.stop).getSequenceAsString());
                } catch (CompoundNotFoundException e) {
                    System.out.println("Caught error in sequence parsing ");
                    //System.out.println(e);
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    sw.toString();
                    System.out.println(sw);
                }

            } catch (Exception e) {
                System.out.println("coldn't parse fasta sequence " + e);
            }
        } else {
            try {
                this.sequence = fullGenome;
            } catch (Exception e) {
                System.out.println("couldn't parse fasta sequence " + e);
            }
        }

    }



    public void evaluateGeneExpression(){

        // THis is for the whole gene
      /*  for (SNP snp: snpsOnGene) {

            boolean FullSNPevidence = snp.validateSNP(bimodalPrimersForNoise, bimodalPrimersForNoise, 1);
            boolean CentralExpressionEvidence = snp.validateSNP(strongCentralInformativePrimers, strongCentralInformativePrimers, 2);



            if (FullSNPevidence) {
                snp.setExpression("FULLSNP");
            }
            if (CentralExpressionEvidence) {
                snp.setExpression("EQUALALLELICEXPRESSION");
            }
        }
        */

    }

    public void evaluateSNPs(){
        for(SNP snp: snpsOnGene){
            if(snp.isValidated() >= 1){
                //snpsOnGene.remove(snp);
            //}
            //else{
                if(!snp.validateSNP(10,-1,0)){
                    //snpsOnGene.remove(snp);
                    snp.disableValidation();
                }else{
                    snp.raiseValidation();
                }

                if(snp.validateSNP(-1,10,1)){
                    snp.setExpression("FULLSNP");
                }else{
                    snp.raiseValidation();
                }

                if(snp.validateSNP(10,10,2)){
                    snp.setExpression("EQUALALLELICEXPRESSION");

                }else  {
                    snp.raiseValidation();
                }


            }

        }
    }


    public void addRead(SimpleRead read) {
        this.geneReadList.add(read);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gene gene = (Gene) o;

        if (start != gene.start) return false;
        if (stop != gene.stop) return false;
        return chromosome != null ? chromosome.equals(gene.chromosome) : gene.chromosome == null;

    }

    @Override
    public int hashCode() {
        int result = chromosome != null ? chromosome.hashCode() : 0;
        result = 31 * result + start;
        result = 31 * result + stop;
        return result;
    }

    public List<SimpleRead> getGeneReadList() {
        return geneReadList;
    }
}




