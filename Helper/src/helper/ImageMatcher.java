package helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class ImageMatcher {
	
	private List<MatchEntry> matchList;
	
	private boolean searchMode;
	
	public ImageMatcher() {
		//throw new UnsupportedOperationException();
	}
	
	public MatchEntry Pick(Calendar timepoint, int cnt)
	{
		List<MatchEntry> matches = new ArrayList<>();
		Iterator<MatchEntry> it = matchList.iterator();
		while(it.hasNext()){
			MatchEntry entry = it.next();
			if(entry.timePoint.equals(timepoint) && entry.cnt==cnt)
			{
				matches.add(entry);
			}
		}
		
		if(matches.size()!=1){
			if(searchMode){
				/*MatchEntry matchEntry=new MatchEntry(timepoint, matches, cnt);
				this.matchList.add(matchEntry);
				return matchEntry;*/
			}
		}
		/*var query = _matchList.Where(x => (x.Timepoint.Equals(timepoint) && x.Cnt == cnt));
        int qcnt = query.Count();
        if (qcnt != 1)
        {
            if (SearchMode)
            {
                var matches = FileList.Where(x => DateEqual(x.Timepoint, timepoint)).ToList();
                var matchEntry = new MatchEntry(timepoint, matches, cnt);
                _matchList.Add(matchEntry);
                return matchEntry;
            }
            else
            {
                throw new ArgumentException($"Invalid number of entries found ({qcnt}), 1 expected");
            }
        }*/

        //return query.First();
		throw new UnsupportedOperationException();
	}
	
	public MatchEntry Pick(Calendar timepoint){
		return Pick(timepoint,0);
	}
	
	public void LoadMatches(String todo){
		throw new UnsupportedOperationException();
	}
	
	public void LoadFiles(String todo){
		throw new UnsupportedOperationException();
	}
	
	public void Save(String path){
		//throw new UnsupportedOperationException();
	}
	
	public class FileEntry
    {
		private String relPath;
		private String fileName;
		private Calendar timePoint;
		
        public FileEntry(String fullpath, String prefix) throws ParseException
        {
            if (!fullpath.startsWith(prefix))
            {
            	throw new IllegalArgumentException(String.format("Fullpath '%s' does not start with '%s'",fullpath,prefix));
            }
            
            this.relPath=fullpath.substring(prefix.length()+1);
            this.fileName=FileHandler.getFileName(fullpath);
            this.timePoint = GetTimepoint(this.fileName);
        }
        
        public String getRelPath() {
			return relPath;
		}


		public String getFileName() {
			return fileName;
		}


		public Calendar getTimePoint() {
			return timePoint;
		}

        private Calendar GetTimepoint(String filename) throws ParseException
        {
            
        	String pattern = "IMG-[0-9]{8}-WA[0-9]{4}.jp";
        	// TODO check the filename starts with pattern
        	if(!filename.matches(pattern)){
        		throw new IllegalArgumentException(String.format("Invalid filename '%s'", filename));
        	}
        	
        	String dateStr=filename.substring(4,8);
        	SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        	Calendar calendar=Calendar.getInstance();
        	calendar.setTime(sdf.parse(dateStr));
        	
        	return calendar;
        }
    }
	
	public class MatchEntry
    {
        private Calendar timePoint;
        private List<FileEntry> fileMatches;
        private int cnt;
        private boolean isImage;
        

        public MatchEntry(Calendar timepoint, List<FileEntry> fileMatches, int cnt)
        {
        	this.timePoint = timepoint;
        	this.fileMatches = fileMatches;
            this.cnt = cnt;
            this.isImage = true;
        }        
    }
}
