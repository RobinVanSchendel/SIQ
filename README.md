# SIQ
Sequence Interrogation and Qualification is there to perform analysis on sequencing data on user-defined DNA

It is written in Java so that anyone with a computer can use this program to analyse their sequences.

We have designed and implemented this tool for anyone without a background in informatics to be able to analyse NGS data.

Contents
========

 * [Installation](#installation)
 * [Running SIQ](#running)
 * [Troubleshooting](#troubleshooting)

### Installation

* latest Java .jar file from this repository

Download the latest .jar file from this repository. You can double click it and it should work directly. The only thing you require is a working Java version (1.8 and up). 

### Running

when SIQ is started you should see the following screen:

<TODO>

SIQ can be used to analyze the following type of data:

* Sanger sequences (.ab1 files)
* Illumina single and paired-end sequence data (.fastq or .fastq.gz files)
* PacBio data (.fastq or .fastq.gz files)

When setting up your targeted sequencing experiments you generally use two primers to amplify your locus of interest. Additionally, you (optionally) have expected target sites for your experiments. This is for example the case if you use CRISPR\Cas9, Cas13, I-SceI or any other enzyme to target the DNA. We have also used SIQ to analyze different sites, such as transposon sites, sites of G-quadruplex sequences. SIQ uses these locations to 1) try to identify sequence alterations preferably at this location. 2) to ensure your PCR primers amplified from the expected location (see [Filters](#filters) below).

SIQ has the possibilities for the following input:

* R1 - sequencing file (required)
* R2 - paired end sequencing file. If provided SIQ will merge R1 and R2 using FLASH (optional)
* reference - reference file containing your DNA sequence in FASTA format. Needs to contain the primer sequences as well if supplied. Keep the reference file small as this determines the runtime of SIQ (required)
* alias - your sample name (required)
* left flank - the stretch of DNA that just touches your expected target site. See below for a graphical example (required)
* right flank - the stretch of DNA that just touches your expected target site. See below for a graphical example. In the case of for example Cas9 nickases this can designate the second sgRNA target site (required)
* #bases past primer - the number of bases your sequence reads at least have to pass the primer to be included as a real event. This filter is there to make sure your primers annealed at the target site in the DNA (default: 5, 0 disables this filter)

Optional settings:
* Max reads to analyze - the maximum number of reads you want SIQ to analyze per sample (0 is unlimited)
* Min support - the minimum number an event has to be seen to be part of SIQ's output (default: 2)
* Max base error - the maximum per base error that is allowed in a read to be analyzed (default: 0.05)
* Max cpus - the maximum number of CPUs SIQ can use at any given time. Note that SIQ uses a maximum of 1 CPU per file (default: All)
* TINS search distance - the distance relative to the event junction to be included in the search space to search for the origin of insertions. A TINS is a templated insertion (default: 100)


### Filters

### Troubleshooting








