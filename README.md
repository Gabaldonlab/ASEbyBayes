# ASEbyBayes

Due to restructuring, currently set back to pre-alpha version 0.22

### <i class="icon-file"></i> **Introduction**

ASEbyBayes is a flexible tool to estimate _Allele Specific Expression_ from RNAseq data.
The analysis is based on the measurement of individual SNPs. SNPs can either be provided as a vcf file or are called directly by the software. The original interest in the development lay with the analysis of nonmodel yeast species. More specifically hybrids.  
The process implemented by the software follows the assumption that the system is unregulated, and Allele specific expression is manifests as a pattern, that cannot be explained by the expected variation. The priors are modeled according to this expectation to be either weak bimodal to model the noise derived from RNAseq data, or strong central informative to model unregulated 0.5 ratio transcription of a hypothetical unregulated system. The true observations are evaluated against those assumptions.

Reads are assumed to follow a negative binomial distribution. To model our primers we used the conjugate Beta distribution. With preset weak bimodial primers for noise detection and strong central informative primers for the assumption of equal allelic expression. Classification occurs via evaluation of posteriors, by defining credible regions following the above assumptions.


### Workflow
![alt tag](http://ernstthuer.eu/workflow.png)

### <i class="icon-file"></i> **Documents**

> #### <i class="icon-download"></i>**Input:     ** [optional]
>- GFF file in the format of Ensembl / CGD / UCSC 
>- FASTA reference of one of the parentals 
>- BAM files of mapped reads 
>- [ VCF file containing the known SNPs,  SNPcalling will be deactivated]

> #### <i class="icon-upload"></i> **Output: ** [optional]
>- VCF like output containing the SNPs with their most likely association in expression
>- [silenced FASTA,  all SNPs replaced by 'N'improves remapping accuracy be removing association bias of mapper]

### <i class="icon-file"></i> **Commands**
java -jar ASEbyBayes.jar -h 


### <i class="icon-file"></i> **Output**

The output produced by ASEbyBayes consists of two lists.  The first list contains genes and their evaluation for allele specific expression according. 
The second list contains a .vcf like investigation of SNPs that were considered in the analysis.


### <i class="icon-file"></i> **Pipeline**





### <i class="icon-file"></i> **Example**







