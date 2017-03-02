# ASEbyBayes

Due to restructuring, currently set back to pre-alpha version 0.22

### <i class="icon-file"></i> **Introduction**

ASEbyBayes is a flexible tool to estimate _Allele Specific Expression_ from RNAseq data.
The analysis is based on the measurement of individual SNPs. SNPs can either be provided as a vcf file or are called directly by the software. The original interest in the development lay with the analysis of nonmodel yeast species. More specifically hybrids.  
The process implemented by the software follows the assumption that the system is unregulated, and Allele specific expression is manifests as a pattern, that cannot be explained by the expected variation. The priors are modeled according to this expectation to be either weak bimodal to model the noise derived from RNAseq data, or strong central informative to model unregulated 0.5 ratio transcription of a hypothetical unregulated system. The true observations are evaluated against those assumptions.

Reads are assumed to follow a negative binomial distribution. To model our primers we used the conjugate Beta distribution. With preset weak bimodial primers for noise detection and strong central informative primers for the assumption of equal allelic expression. Classification occurs via evaluation of posteriors, by defining credible regions following the above assumptions.


### Workflow

The workflow starts downstream of the mapping of raw reads to a reference. Mapping should be carried out by any short read mapper of your choice. Due to output format standardization, any BAM file format can be used. The files needed are either BAM or SAM file formatted raw reads,  a reference genome, and a gene annotation file. 

ASEbyBayes takes over from the mapped reference genome. As shown in the workflow graphics, mapped reads are screened for SNPs in a first iteration step, initiated by setting the *firstpass* argument to true [default is false]. Technically, this is not obligatory, but will remove up to 15 % mapping bias against the known allele. Depending on the read length used,  below 70bp, this step is highly recommended. Above it becomes more optional. 


![alt tag](http://ernstthuer.eu/workflow_v2.png "Essential workflow")


### How it works 


ASEbyBayes reads the regions indicated by the gff file, and assesses SNPS on the genes.  SNP expression is considered as a ratio between 0 and 1. In a diploid organism, if a SNP is present on one allele, and the organism does not regulate the expression, the observed ratio will be 0.5. 

Our classification approach, like other commonly used workflows in RNAseq data analysis, are build around hypothesis testing. For example, differential gene expression (DE) models distributions according to the genes coverage of reads over different conditions. In our approach, we are limited to a range between 0 and 1 for our expression values, so we model a central hypothesis, always dependant on the coverage of SNPs.

The analysis starts with two core hypothesis. The central limit hypothesis (mean 0.5) tests SNPs whether or not they belong to unregulated Equal Allelic Expression. The noise analysis tests, whether SNPs belong to the noise category, meaning false positives. Any Sequencing data contains a certain amount of technical noise, by chance those technical miscalls for nucleotides will interfere with the analysis.

Yet SNPs contain more than just information on their coverage. Allele SPecific Expression is still under selective pressure. Even a biallelic SNP has to get fixed if detrimental. Without trying to derade the importance of functional SNP analysis. Synonymous SNPs, meaning SNPs that will not change the aminoacid sequence, are more likely to appear naturally. We therefore modelled an additional impact for synonymity. This especially comes to bear when distinguishing low coverage SNPs from noise. 

Replicates are an important part of RNA sequencing experimental design. As other software solutions, e.g for above mentioned DE analysis. ASEbyBayes uses replicates to confirm SNPs of low coverage. Conservation over replicates is also used to estimate the true coverage of SNPs.

####The whole is more than the sum of it's SNPs.
SNPs, even on the same gene, may derive from different sources.  With each additional SNP detected giving more evidence to any hypothesis for the final qualification of the whole gene.
In our core hypothesis, SNPs are modelled around the central limit, meaning the most highly weighted analysis is the Equal Allelic Expression hypothesis. Given enough evidence, the gene will be marked as Equal allelic expressed. While SNPs that contradict the hypothesis will be marked as CONT/ISO , meaning derived from contamination, or an isoform of the gene.
Contamination in this regard refers to the possibility of subpopulations. SNPs that are not derived from the diploidy of the organism, but rather from a subpopulation of the cells observed. This analysis is strengthened by the repeteated observation of the same expression pattern.

![alt tag](http://ernstthuer.eu/triplot_1.jpeg "Distributions")

####Fig2 : 
Examples of distributions,  plot left: showing Priors before addition of SNP values,  plot center,  Equal Allelic expression does not shift the distributions, returns a high density for the observed SNPs expression value and does not trigger new distributions to be considered. Plot right: observed allele specific expression. Triggers adaption of existing hypothesis, but fails for the mean to be classified as equal allelic expression. An allele specific Expression hypothesis takes the SNP.

#### Behind the curtain
ASEbyBayes as the name suggests uses a Bayes inspired approach for classification. Instead of modelling frequentist distributions, we model a set of priors into each hypothesis test. The idea behind the approach stems from the limited set of potential hypothesis that can explain the observed patterns. Not all possibilities are equally likely, and the approach has both the large amount of data and knowledge about the biological background (synonymity and availablitiy over replicates). We found that modeling mildly informative, but robust, priors improved qualification. 

Our analysis is based around the Beta distribution, a conjugate to the Negative Binomial used by Deseq2, but also the conjugate function to the otherwise used Poisson distribution.

The implementation of the Beta function used was obtained from the apache Math module 
http://commons.apache.org/proper/commons-math/javadocs/api-3.5/org/apache/commons/math3/distribution/BetaDistribution.html

Details on the Beta function can be found in Abramowitz and Steguns Handbook of Mathematical formula, it is defined as  *Γ(a+b)/(Γ(a)Γ(b))x^(a-1)(1-x)^(b-1)*,   

Γ (the gamma function) is a shifted down version of the factorial function Γ(n) = (n-1)!




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







