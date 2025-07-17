package main;
import gui.GUI;
import gui.PropertiesManager;
import utils.MyOptions;

import java.io.File;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import batch.SequenceController;

public class SV_Analyzer {
	public final static String VERSION = "2.1";
	public static void main(String[] args) throws ParseException {
		Options optionsApache = createOptions();
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse( optionsApache, args);
		} catch (ParseException e) {
			System.err.println(e);
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "HPRTFastQ", optionsApache );
			System.exit(0);
		}
		MyOptions options = new MyOptions(cmd);
		
		if(!options.hasOptions() || options.startGUI()) {
			try {
				UIManager.setLookAndFeel(
				        UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
			PropertiesManager pm = new PropertiesManager();
			new GUI(VERSION, pm);
		}
		else if(options.getHelp()) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "SIQ -i <FQ FILE> -o <FILE> -subject <FASTA FILE>", optionsApache );
			System.exit(0);
		}
		else {
			System.out.println(options.printParameters());
			
			SequenceController sq = new SequenceController();
			File output = new File(options.getOutput());
			if(output.exists() && !options.overwrite()) {
				System.out.println("Output file "+output.getAbsolutePath()+" already exists");
				System.out.println("Add option -f to overwrite, exiting now!");
				System.exit(0);
			}
			sq.setOutputFile(output);
			
			sq.readFilesFASTQMultiThreaded(options);
		}
	}

	private static Options createOptions() {
		Options o = new Options();
		//threads
		Option threads   = Option.builder( "t" )
				.longOpt("threads")
                .hasArg()
                .argName("NUMBER")
                .type(Number.class)
                .desc(  "number of threads to use concurrently. If not specified the maximum number of available threads will be used" )
                .build();
		o.addOption(threads);
		
		Option out = Option.builder("o")
				.longOpt("output")
				.hasArg()
				.argName("FILE")
				.desc("The output file will merge all the analyzed files into one single file. (REQUIRED)")
				.build();
		o.addOption(out);

		Option maxBError   = Option.builder( "e" )
				.longOpt("maxError")
                .hasArg()
                .type(Number.class)
                .argName("NUMBER")
                .desc("The maximum error probability for a base that will be used to determine high-quality ranges within a read (default is 0.08)" )
                .build();
				o.addOption(maxBError);
		
		Option minEvent   = Option.builder( "m" )
				.longOpt("minimum")
                .hasArg()
                .type(Number.class)
                .argName("NUMBER")
                .desc("The minimal number of times an event has to be seen to be reported (default 5)." )
                .build();
				o.addOption(minEvent);
		
		Option help   = Option.builder( "h" )
				.longOpt("help")
                .desc("Prints the usage for this program")
                .build();
				o.addOption(help);
		
		Option over   = Option.builder( "f" )
				.longOpt("overwrite")
                .desc("Overwrite the output file when it already exists")
                .build();
				o.addOption(over);
		
		Option gui   = Option.builder( "g" )
				.longOpt("gui")
                .desc("Start a gui to input files")
                .build();
				o.addOption(gui);
		
		Option reads   = Option.builder( "r" )
				.longOpt("reads")
				.hasArg()
				.argName("NUMBER")
				.type(Number.class)
                .desc("The number of reads that should be analyzed (all by default)")
                .build();
				o.addOption(reads);
		
		Option infile = Option.builder("infile")
				.hasArg()
				.argName("FILE")
				.desc("A single fastq file that will be analysed. (REQUIRED)")
				.build();
		o.addOption(infile);
		
		Option infileF = Option.builder("infileF")
				.hasArg()
				.argName("FILE")
				.desc("A single fastq file that will be analysed, this is typically the file containing the unmerged R1 reads. (OPTIONAL)")
				.build();
		o.addOption(infileF);
		
		Option infileR = Option.builder("infileR")
				.hasArg()
				.argName("FILE")
				.desc("A single fastq file that will be analysed, this is typically the file containing the unmerged R2 reads. (OPTIONAL)")
				.build();
		o.addOption(infileR);
		
		Option subject = Option.builder("subject")
				.hasArg()
				.argName("FILE")
				.desc("A reference file to map the input file against. In FASTA format. (REQUIRED)")
				.build();
		o.addOption(subject);
		
		Option left = Option.builder("left")
				.hasArg()
				.argName("STRING")
				.desc("leftt flank sequence determining the cut site (>=15bp). (OPTIONAL)")
				.build();
		o.addOption(left);
		
		Option right = Option.builder("right")
				.hasArg()
				.argName("STRING")
				.desc("right flank sequence determining the cut site (>=15bp). (OPTIONAL)")
				.build();
		o.addOption(right);
		
		Option add = Option.builder("a")
				.longOpt("additionalSearch")
				.hasArg()
				.argName("FILE")
				.desc("File containing sequences in fasta format that are additionally seached to determine"
						+ " templated inserts. (OPTIONAL)")
				.build();
		o.addOption(add);
		
		Option leftP = Option.builder("leftPrimer")
				.hasArg()
				.desc("leftPrimer  sequence 5' to 3' (min. 15bp). (OPTIONAL)")
				.build();
		o.addOption(leftP);
		
		Option rightP = Option.builder("rightPrimer")
				.hasArg()
				.argName("STRING")
				.desc("rightPrimer sequence 5' to 3' (min. 15bp). (OPTIONAL)")
				.build();
		o.addOption(rightP);
		
		Option minPassedPrimer   = Option.builder( "minPassedPrimer" )
				.longOpt("minPassedPrimer")
                .hasArg()
                .type(Number.class)
                .argName("NUMBER")
                .desc("The minimal number of bases an event should start passed the primer (Default 5)" )
                .build();
				o.addOption(minPassedPrimer);
		
		Option alias   = Option.builder( "alias" )
                .hasArg()
                .argName("STRING")
                .desc("The sample name that will be included in the final output. (REQUIRED0" )
                .build();
				o.addOption(alias);
				
		Option hr   = Option.builder( "hdr" )
				.longOpt("hdr")
                .hasArg()
                .argName("FILE")
                .desc("The fasta file representing an HDR event. If primers are provided they should also be present in this file. (OPTIONAL)" )
                .build();
				o.addOption(hr);
				
		Option search = Option.builder("search")
				.hasArg()
				.argName("NUMBER")
				.type(Number.class)
				.desc("the search space used to call templated insertions around junctions in forward and reverse complement orientation (Default is 100bp).")
				.build();
				o.addOption(search);
		
		return o;
	}

}
