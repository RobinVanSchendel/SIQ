package batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

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
import main.SV_Analyzer;
import utils.CompareSequence;
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
		
		if(options.printTemplate()) {
			printTemplate();
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
			GUI g = new GUI(SV_Analyzer.VERSION);
			g.setMaxError(options.getMaxError());
		}
		else {
			System.out.println(options.printParameters());
			
			SequenceController sq = new SequenceController();
			//for IS color parts
			/*
			HashMap<String, String> colorMap = new HashMap<String,String>();
			colorMap.put("px458_Cas9-GFP", "brown");
			colorMap.put("mmHPRT_sequence_Fasta", "purple");
			colorMap.put("Flank insert", "orange");
			colorMap.put("Flank insert rc", "red");
			colorMap.put("Tandem duplication", "green");
			colorMap.put("Tandem duplication2", "darkgreen");
			*/
			//System.out.println("input : "+cmd.getOptionValue("in"));
			//System.out.println("out : "+cmd.getOptionValue("o"));
			//System.out.println("postfix : "+cmd.getOptionValue("p"));
			if(options.getMinNumber()>0 && !options.collapseEvents()) {
				System.err.println("You specified a minimalCount to only output events that have been seen ["+options.getMinNumber()+"] times, but this does not work if you do not set the collapse to true");
				System.err.println("Either remove this argument or set collapse to true");
				System.exit(0);
			}
			//Print formatter
			//HelpFormatter formatter = new HelpFormatter();
			//formatter.printHelp( "HPRTFastQ", optionsApache );
			//System.exit(0);
			
			String containsString = null; //".assembled"; //null
			int[] startPositions = {81,216}; //these are the start positions (81, 216) and the end positions (326, 553) of the primers
			int[] endPositions = {326,552}; //these are the end positions (326, 552) and the end positions (326, 553) of the primers
			boolean keepOnlyPositions = false;
			//because we check the positions now we don't have to keep the start_end in the key
			boolean includeStartEnd = false;
			
			
			//if(collapse){
			//	name +="_collapse";
			//}
			//name +=".txt";
			
			//set the output file
			File output = new File(options.getOutput());
			if(output.exists() && !options.overwrite()) {
				System.out.println("Output file "+output.getAbsolutePath()+" already exists");
				System.out.println("Add option -f to overwrite, exiting now!");
				System.exit(0);
			}
			sq.setOutputFile(output);
			
			sq.setCollapseEvents(options.collapseEvents());
			
			//sq.setPrintOnlyISParts();
			//sq.setColorMap(colorMap);
			sq.setMinimalCount(options.getMinNumber());
			sq.setIncludeStartEnd(includeStartEnd);
			//if(!includeStartEnd) {
				//name+= "_ignoreStartEndPosInKey";
			//	
			//}
			String fileName = options.getFile();
			File file = null;
			if(fileName == null) {
				sq.readFilesFASTQMultiThreaded(options);
				//sq.readFilesFASTQMultiThreaded(files, subject, leftFlank, rightFlank, type, search, true, containsString, options);
				System.exit(0);
			}
			else {
				file = new File(options.getFile());
			}
			
			if(sq.getOutputFile() != null){
				sq.writeln("Type\t"+CompareSequence.getOneLineHeader());
			}
			else{
				System.out.println("Type\t"+CompareSequence.getOneLineHeader());
			}
			
			Scanner s = null;
			final long startTime = System.nanoTime();
			try {
				s = new Scanner(file);
				boolean first = true;
				int fileColumn = -1;
				int subjectColumn = -1;
				int lFColumn = -1;
				int rFColumn = -1;
				int typeColumn = -1;
				int addSearchColumn = -1;
				while(s.hasNextLine()){
					String line = s.nextLine();
					String[] parts = line.split("\t");
					if(first){
						int i = 0;
						for(String str: parts){
							if(str.equals("Files")){
								fileColumn = i;
							}
							if(str.equals("Subject")){
								subjectColumn = i;
							}
							if(str.equals("LeftFlank")){
								lFColumn = i;
							}
							if(str.equals("RightFlank")){
								rFColumn = i;
							}
							if(str.equals("Type")){
								typeColumn = i;
							}
							if(str.equals("AdditionalSearch")){
								addSearchColumn = i;
							}
							i++;
						}
						first = false;
					}
					else{
						String files = parts[fileColumn];
						String subject = parts[subjectColumn];
						String leftFlank = parts[lFColumn];
						String rightFlank = parts[rFColumn];
						String type = parts[typeColumn];
						File search = null;
						if(addSearchColumn>-1 && parts.length>addSearchColumn){
							search = new File(parts[addSearchColumn]);
						}
						if(keepOnlyPositions) {
							sq.setStartEndPositions(startPositions, endPositions);
						}
						sq.readFilesFASTQMultiThreaded(files, subject, leftFlank, rightFlank, type, search, true, containsString, options);
					}
				}
				s.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			final long duration = (System.nanoTime() - startTime);
			long seconds = TimeUnit.NANOSECONDS.toSeconds(duration);
			//System.out.println("This script took "+seconds+" seconds");
		}
	}

	private static void printTemplate() {
		File template = new File("template.txt");
		if(template.exists()) {
			System.err.println("template file: "+template.getAbsolutePath()+" already exists...");
			System.exit(0);
		}
		try {
			FileWriter fw = new FileWriter(template);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Files\tSubject\tLeftFlank\tRightFlank\tType\tAdditionalSearch\n");
			bw.write("/home/rvanschendel/data/hprt\t/home/rvanschendel/ref/hprt.fasta\tCCGGGGACGGAGCCTGGGCG\tGGCCGAGAGGGCGGGCCGAG\tCRISPR\t/home/rvanschendel/data/vectors/\n");
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Created template file: "+template.getPath());
	}

	private static void setOptions(Options options) {
		if(!options.hasShortOption("e")) {
			//options.getOption("e").se
			
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
		
		//output prefix
		Option outP = Option.builder("p")
				.longOpt("output_postfix")
				.hasArg()
				.argName("FILE")
				.desc("The output will be set in the same location as the input and will add the postfix to the name")
				.build();
		o.addOption(outP);
		
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
		
		Option printFile   = Option.builder( "x" )
				.longOpt("print_template")
                .desc("Prints an empty template file to template.txt containing the tab separated file that is required for the 'in' argument")
                .build();
				o.addOption(printFile);
		
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
