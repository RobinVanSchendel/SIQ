package batch;

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

import gui.GUI;
import gui.PropertiesManager;
import main.SV_Analyzer;
import utils.MyOptions;

public class HPRTFastQ {

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
		if(options.getHelp() || !options.hasOptions()) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "HPRTFastQ", optionsApache );
			System.exit(0);
		}
		
		if(options.startGUI()) {
			try {
				UIManager.setLookAndFeel(
				        UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedLookAndFeelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			PropertiesManager pm = new PropertiesManager();
			GUI g = new GUI(SV_Analyzer.VERSION, pm);
			g.setMaxError(options.getMaxError());
		}
		else {
			System.out.println(options.printParameters());
			
			SequenceController sq = new SequenceController();
			if(options.getMinNumber()>0 && !options.collapseEvents()) {
				System.err.println("You specified a minimalCount to only output events that have been seen ["+options.getMinNumber()+"] times, but this does not work if you do not set the collapse to true");
				System.err.println("Either remove this argument or set collapse to true");
				System.exit(0);
			}
			//set the output file
			File output = new File(options.getOutput());
			if(output.exists() && !options.overwrite()) {
				System.out.println("Output file "+output.getAbsolutePath()+" already exists");
				System.out.println("Add option -f to overwrite, exiting now!");
				System.exit(0);
			}
			sq.setOutputFile(output);
			
			sq.setCollapseEvents(options.collapseEvents());
			
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
		
		//input file
		Option input = Option.builder("in")
				.longOpt("input")
				.hasArg()
				.argName("FILE")
				.desc("File containing a tab separated table containing the following info:\n"
						+ "Files<tab>Subject<tab>LeftFlank<tab>RightFlank<tab>Type<tab>AdditionalSearch\r\n\n" + 
						"<DIR><tab><ref_fasta><tab><leftFlank><tab><rightFlank><tab><Type><tab><additionalRefFastaFile>")
				.build();
		o.addOption(input);
		
		Option out = Option.builder("o")
				.longOpt("output")
				.hasArg()
				.argName("FILE")
				.desc("The output file will merge all the analyzed files into one single file")
				.build();
		o.addOption(out);

		//maxBaseError
		Option maxBError   = Option.builder( "e" )
				.longOpt("maxError")
                .hasArg()
                .type(Number.class)
                .argName("NUMBER")
                .desc("The maximum error probability for a base that will be used to determine high-quality ranges within a read (default is 0.05)" )
                .build();
				o.addOption(maxBError);
		
		//collapseNumber
		Option minEvent   = Option.builder( "m" )
				.longOpt("minimum")
                .hasArg()
                .type(Number.class)
                .argName("NUMBER")
                .desc("The minimal number of times an event has to be seen to be reported (default 5)" )
                .build();
				o.addOption(minEvent);
		
		Option collapse   = Option.builder( "c" )
				.longOpt("collapse")
                .desc("Collapse the same events into one and count the number of occurrences")
                .build();
				o.addOption(collapse);
		
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
				.type(Number.class)
                .desc("The number of reads that should be analyzed (all by default)")
                .build();
				o.addOption(reads);
		
		Option infile = Option.builder("infile")
				.hasArg()
				.argName("infile")
				.desc("A single fastq file that will be analysed")
				.build();
		o.addOption(infile);
		
		Option infileF = Option.builder("infileF")
				.hasArg()
				.argName("infile")
				.desc("A single fastq file that will be analysed")
				.build();
		o.addOption(infileF);
		
		Option infileR = Option.builder("infileR")
				.hasArg()
				.argName("infile")
				.desc("A single fastq file that will be analysed")
				.build();
		o.addOption(infileR);
		
		Option subject = Option.builder("subject")
				.hasArg()
				.argName("subject")
				.desc("A reference file to map the fastq file against")
				.build();
		o.addOption(subject);
		
		Option left = Option.builder("left")
				.hasArg()
				.argName("left")
				.desc("leftFlank seq")
				.build();
		o.addOption(left);
		
		Option right = Option.builder("right")
				.hasArg()
				.argName("right")
				.desc("right seq")
				.build();
		o.addOption(right);
		
		Option add = Option.builder("a")
				.longOpt("additionalSearch")
				.hasArg()
				.argName("right")
				.desc("right seq")
				.build();
		o.addOption(add);
		
		Option leftP = Option.builder("leftPrimer")
				.hasArg()
				.argName("leftPrimer")
				.desc("leftPrimer seq")
				.build();
		o.addOption(leftP);
		
		Option rightP = Option.builder("rightPrimer")
				.hasArg()
				.argName("rightPrimer")
				.desc("rightPrimer seq")
				.build();
		o.addOption(rightP);
		
		Option minPassedPrimer   = Option.builder( "minPassedPrimer" )
				.longOpt("minPassedPrimer")
                .hasArg()
                .type(Number.class)
                .argName("NUMBER")
                .desc("The minimal number of bases an event should start passed the primer (default 5)" )
                .build();
				o.addOption(minPassedPrimer);
		
		Option alias   = Option.builder( "alias" )
				.longOpt("alias")
                .hasArg()
                .desc("The sample name that will be included in the final output" )
                .build();
				o.addOption(alias);
		
		return o;
	}

}
