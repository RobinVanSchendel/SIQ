package utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.reference.ReferenceSequence;

public class CigarAlignment {
	public ArrayList<CigarSingleAlignment> al = new ArrayList<CigarSingleAlignment>();
	private SAMRecord sam;
	private String ref;
	//private ReferenceSequence rs;
	
	public CigarAlignment(SAMRecord sam, ReferenceSequence rs) {
		this.sam = sam;
		setRef(new String(rs.getBases()));
		initialize();
	}
	
	public CigarAlignment(SAMRecord sam, String ref) {
		this.sam = sam;
		setRef(ref);
		initialize();
	}
	
	//Ensure the ref is made uppercase!
	private void setRef(String string) {
		this.ref = string.toUpperCase();
	}
	
	/**This it the workhorse of this method. It iterates the entire Cigar string
	 * of the SAMRecord to get the ref matching sequence.
	 * 
	 */
	private void initialize() {
        int queryPos = 0;
        int refPos = sam.getAlignmentStart()-1;
        
        String query = sam.getReadString();
        
        //safety to get rid of unmapped reads
        if(query.length()<15) {
        	return;
        }

        Pattern pattern = Pattern.compile("(\\d+)([MIDNSHP=X])");
        Matcher matcher = pattern.matcher(sam.getCigarString());

        char refc, queryc;
        int loc = 0;
        //System.out.println(sam.toString()+"\t"+sam.isSecondaryOrSupplementary());
        while (matcher.find()) {
            int length = Integer.parseInt(matcher.group(1));
            char op = matcher.group(2).charAt(0);
            switch (op) {
                case 'M': case '=': case 'X':
                    for (int i = 0; i < length; i++) {
                    	queryc = query.charAt(queryPos);
                    	refc = ref.charAt(refPos);
                    	CigarSingleAlignment csa = new CigarSingleAlignment(refc, queryc,refPos, queryPos, op , loc++);
                    	al.add(csa);
                    	queryPos++;
                    	refPos++;
                    }
                    break;
                case 'I': // Insertion to reference
                    for (int i = 0; i < length; i++) {
                    	queryc = query.charAt(queryPos);
                    	refc = '-';
                    	CigarSingleAlignment csa = new CigarSingleAlignment(refc, queryc,refPos, queryPos, op , loc++);
                    	al.add(csa);
                    	queryPos++;
                    }
                    break;
                case 'D': case 'N': // Deletion from reference
                    for (int i = 0; i < length; i++) {
                    	queryc = '-';
                    	refc = ref.charAt(refPos);;
                    	CigarSingleAlignment csa = new CigarSingleAlignment(refc, queryc,refPos, queryPos, op ,loc++);
                    	al.add(csa);
                    	refPos++;
                    }
                    break;
                case 'S': // Clipping: skip query bases
                    queryPos += length;
                    break;
                case 'H': // Clipping: skip hard clipped bases
                case 'P': // Padding: ignore
                    break;
            }
        }
	}
	public void printAlignment() {
		for(CigarSingleAlignment csa: al) {
        	System.out.println(csa);
        }
	}


	public CigarAlignmentSpan getLeftFlank(int leftFlankPos, int sizeOfFlank) {
		//find the location first
		CigarSingleAlignment end = getSubjectLoc(leftFlankPos);
		if(end == null) {
			return null;
		}
		
		CigarSingleAlignment start = end;
		int match = 0;
		
		for(int i = end.getLoc();i>=0;i--) {
			CigarSingleAlignment temp = al.get(i);
			if(temp.match()) {
				//update end accordingly
				if(!end.match()) {
					end = temp;
				}
				match++;
				start = temp;
			} 
			else {
				if(match >= sizeOfFlank ) {
					break;
				}
				//reset and start over
				else {
					end = temp;
					start = temp;
					match = 0;
				}
			}
		}
		if(start!= null && end != null && match >= sizeOfFlank) {
			return new CigarAlignmentSpan(start, end);
		}
		return null;
	}


	private CigarSingleAlignment getSubjectLoc(int pos) {
		boolean first = true;
		for(CigarSingleAlignment csa: al) {
			//perhaps we already overshot
			if(first && csa.getRefPos() > pos) {
				return csa;
			}
			if(csa.getRefPos() == pos) {
				return csa;
			}
			first = false;
        }
		return null;
	}


	public String getSpanRef(CigarAlignmentSpan span) {
		if(span == null) {
			return null;
		}
		return getSpan(span, "ref");
	}


	private String getSpan(CigarAlignmentSpan span, String string) {
		StringBuilder sb = new StringBuilder();
		boolean printRef = string == "ref";
		int start = span.getStart().getLoc();
		int end = span.getEnd().getLoc();
		for(int i = start;i<=end;i++) {
			if(printRef) {
				sb.append(al.get(i).getRef());
			}
			else {
				sb.append(al.get(i).getQuery());
			}
		}
		return sb.toString();
	}

	/**Important problem is now that the rightFlank continues past the location of the target location
	 * that is not guaranteed as we now pas the end of the leftflank in pos.
	 * TODO: fix that!
	 * 
	 * @param pos
	 * @param sizeOfFlank
	 * @return
	 */
	public CigarAlignmentSpan getRightFlank(int pos, int sizeOfFlank) {
		
		//find the location first
		CigarSingleAlignment start = getSubjectLoc(pos);
		//it is possible that this location is null as the left flank consumed all the query read
		//and this is position +1
		if(start == null) {
			return null;
		}
		
		CigarSingleAlignment end = start;
		int match = 0;
		
		for(int i = end.getLoc();i<al.size();i++) {
			CigarSingleAlignment temp = al.get(i);
			if(temp.match()) {
				//update end accordingly
				if(!start.match()) {
					start = temp;
				}
				match++;
				end = temp;
			} 
			else {
				if(match >= sizeOfFlank ) {
					break;
				}
				//reset and start over
				else {
					end = temp;
					start = temp;
					match = 0;
				}
			}
		}
		if(start != null && end != null && match >= sizeOfFlank) {
			return new CigarAlignmentSpan(start, end);
		}
		return null;
	}


	public String getDeletion(CigarAlignmentSpan spanLeft, CigarAlignmentSpan spanRight) {
		if(!checkOK(spanLeft) && !checkOK(spanRight)) {
			return null;
		}
		CigarSingleAlignment start = spanLeft.getEnd();
		CigarSingleAlignment end = spanRight.getStart();
		StringBuilder sb = new StringBuilder();
		for(int i = start.getLoc()+1;i<=end.getLoc();i++) {
			CigarSingleAlignment temp = al.get(i);
			if(!temp.equals(end)) {
				Character ref = temp.getRef();
				if(ref!=null) {
					sb.append(ref);
				}
			}
		}
		return sb.toString();
	}
	
	/**method to check the span. It needs to contain a valid start and end.
	 * In addition start needs to be have a reference position smaller than the end
	 * 
	 * @param span
	 * @return
	 */
	private static boolean checkOK(CigarAlignmentSpan span) {
		if(span == null) {
			return false;
		}
		if(span.getStart() == null || span.getEnd() == null) {
			return false;
		}
		if(span.getStart().getRefPos() < span.getEnd().getRefPos()) {
			return true;
		}
		return false;
	}

	public String getInsert(CigarAlignmentSpan spanLeft, CigarAlignmentSpan spanRight) {
		if(!checkOK(spanLeft) && !checkOK(spanRight)) {
			return null;
		}
		CigarSingleAlignment start = spanLeft.getEnd();
		CigarSingleAlignment end = spanRight.getStart();
		StringBuilder sb = new StringBuilder();
		for(int i = start.getLoc()+1;i<=end.getLoc();i++) {
			CigarSingleAlignment temp = al.get(i);
			if(!temp.equals(end)) {
				Character query = temp.getQuery();
				if(query!=null) {
					sb.append(query);
				}
			}
		}
		return sb.toString();
	}
	
	/**This helper method can align a query to a reference using
	 * 
	 * @param cigar
	 * @param query
	 * @param reference
	 * @param refStart
	 * @return
	 */
	public static String[] align(SAMRecord sam, String reference) {
        StringBuilder alignedQuery = new StringBuilder();
        StringBuilder alignedRef = new StringBuilder();
        StringBuilder cigarString = new StringBuilder();

        int queryPos = 0;
        int refPos = sam.getAlignmentStart() - 1; // 0-based index
        
        String query = sam.getReadString();

        Pattern pattern = Pattern.compile("(\\d+)([MIDNSHP=X])");
        Matcher matcher = pattern.matcher(sam.getCigarString());

        while (matcher.find()) {
            int length = Integer.parseInt(matcher.group(1));
            char op = matcher.group(2).charAt(0);

            switch (op) {
                case 'M': case '=': case 'X':
                    for (int i = 0; i < length; i++) {
                        alignedQuery.append(query.charAt(queryPos++));
                        alignedRef.append(reference.charAt(refPos++));
                        cigarString.append(op);
                    }
                    break;
                case 'I': // Insertion to reference
                    for (int i = 0; i < length; i++) {
                        alignedQuery.append(query.charAt(queryPos++));
                        alignedRef.append('-');
                        cigarString.append(op);
                    }
                    break;
                case 'D': case 'N': // Deletion from reference
                    for (int i = 0; i < length; i++) {
                        alignedQuery.append('-');
                        alignedRef.append(reference.charAt(refPos++));
                        cigarString.append(op);
                    }
                    break;
                case 'S': // Clipping: skip query bases
                    queryPos += length;
                    break;
                case 'P': case 'H': // Padding and Hard clip: ignore
                    break;
            }
        }
        return new String[] { alignedRef.toString(), alignedQuery.toString(), cigarString.toString() };
    }
}
