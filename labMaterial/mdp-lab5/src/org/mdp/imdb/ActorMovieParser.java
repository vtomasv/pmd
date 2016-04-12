package org.mdp.imdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mdp.imdb.ActorMovieParser.ActorToMovies;

public class ActorMovieParser implements Iterator<ActorToMovies>{
	// header lines in IMDB
	public static int SKIP_HEADER_LINES = 239;
	
	public static final String YEAR_REGEX = "\\([12][890]\\d{2}\\/?[IVX]*\\)";
	public static final Pattern YEAR_PATTERN = Pattern.compile(YEAR_REGEX);
	
	private final BufferedReader input;
	private ActorToMovies next = null;
	private int read = 0;
	
	public ActorMovieParser(BufferedReader input) throws IOException{
		this(input, SKIP_HEADER_LINES);
	}
	
	public ActorMovieParser(BufferedReader input, int skip) throws IOException{
		this.input = input;
		while(--skip>=0){
			input.readLine();
		}
		loadNextActor();
	}
	
	public void loadNextActor() throws IOException{
		next = null;
		String line = input.readLine();
		if(line==null){
			return;
		}
		read++;
		
		// we expect the first line to be an actor's name with the first movie
		// it should not be empty or start with whitespace
		if(line.isEmpty()){
			System.err.println("Found unexpected empty line at line "+read);
			loadNextActor();
		} else if(Character.isWhitespace(line.charAt(0))){
			System.err.println("Found line starting with whitspace unexpectedly at line "+read);
		}
		
		String[] tabs = line.split("\t");
		String actor = tabs[0].trim();
		
		if(actor.isEmpty()){
			System.err.println("Empty actor found at line "+read);
			loadNextActor();
		}
		
		TreeSet<MovieRole> roles = new TreeSet<MovieRole>();
		
		line = line.substring(actor.length());
		do{
			line = line.trim();
			if(line.isEmpty())
				break;
			MovieRole mr = parseMovieRole(line);
			if(mr!=null)
				roles.add(mr);
		} while((line=input.readLine())!=null);
		
		if(roles.isEmpty()){
			System.err.println("No roles found for actor at line "+read);
			
			loadNextActor();
		}
		
		next = new ActorToMovies(actor, roles);
	}
	
	@Override
	public boolean hasNext() {
		return next!=null;
	}

	public ActorToMovies next() {
		ActorToMovies atm = next;
		try {
			loadNextActor();
		} catch (IOException e) {
			e.printStackTrace();
			throw new NoSuchElementException();
		}
		return atm;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * Probably far from perfect but the IMDb grammar is so
	 * informal, it's the best we can do for now
	 */
	public static MovieRole parseMovieRole(String movieRole){
		movieRole = movieRole.trim();
		boolean tv = movieRole.startsWith("\"");
		
		// lets find the index of the last bracketted year
		Matcher m = YEAR_PATTERN.matcher(movieRole);
		int yearStart = -1, yearEnd = -1;
		while(m.find()){
			yearStart = m.start();
			yearEnd = m.end();
		}
		
		boolean yearKnown = true;
		if(yearStart==-1 || yearEnd==-1){
			int unknownYear = movieRole.indexOf("(????");
			if(unknownYear!=-1){
				yearKnown = false;
				yearStart = unknownYear;
				yearEnd = movieRole.indexOf(")", unknownYear+5)+1;
			} else{
				System.err.println("Could not find year in movie role "+movieRole);
				return null;
			}
		}
		
		// movie name is to the left of that
		String name = movieRole.substring(0,yearStart-1);
		
		// movie details to the right of the year
		String details = movieRole.substring(yearEnd);

		MovieRole.MovieType type = MovieRole.MovieType.THEATRICAL_MOVIE;
		// if TV, need to scrub off the quotes
		if(tv){
			name = name.substring(1,name.length()-1);
			
			// and check if its a mini series
			if(movieRole.contains("(mini")){
				type = MovieRole.MovieType.TV_MINI_SERIES;
			} else{
				type = MovieRole.MovieType.TV_SERIES;
			}
		} else{
			// check substrings to indicate video or TV movie
			if(details.indexOf("(TV)")>=0){
				type = MovieRole.MovieType.TV_MOVIE;
			} else if(details.indexOf("(V)")>=0){
				type = MovieRole.MovieType.VIDEO_MOVIE;
			}
		}
		
		int year = -1;
		if(yearKnown)
			year = Integer.parseInt(movieRole.substring(yearStart+1,yearStart+5));
		
		
		MovieRole mr = new MovieRole(name, type, year);
		if(tv){
			mr.setEpisode(getFirstToken(details,"{","}"));
		}
		
		String billing = getFirstToken(details,"<",">");
		try{
			mr.setBilling(Integer.parseInt(billing));
		} catch(NumberFormatException e) {};
		
		mr.setRole(getFirstToken(details,"[","]"));
		
		// means we have the same movie name with different ID
		//e.g., Movie Name (2005/VIII)
		if(yearStart<(yearEnd-6)){
			if(yearStart+6>yearEnd-1)
				System.err.println(movieRole);
			mr.setMovieNumber(movieRole.substring(yearStart+6,yearEnd-1));
		}
		
		return mr;
	}
	
	private static String getFirstToken(String str, String open, String close){
		int openI = str.indexOf(open);
		if(openI>=0){
			int closeI = str.indexOf(close, openI+1);
			if(closeI>=0){
				return str.substring(openI+1,closeI);
			}
		}
		
		return null;
	}
	
	/**
	 * Represents data about a role in a movie.
	 * 
	 * All that's guaranteed here is the type, the name and the year
	 * 
	 * @author Aidan
	 *
	 */
	public static class MovieRole implements Comparable<MovieRole>{
		public static enum MovieType {
			THEATRICAL_MOVIE, TV_SERIES, TV_MINI_SERIES, TV_MOVIE, VIDEO_MOVIE
		}
		String movieName;
		String movieNum;
		int year = -1;
		MovieType movieType;
		int billing = -1;
		String role = null;
		String episode = null;
		
		String toString = null;
		
		public MovieRole(String movieName, MovieType movieType, int year){
			this.movieName = movieName;
			this.movieType = movieType;
			this.year = year;
		}
		
		public String toString(){
			return movieName+ "\t"+year+"\t"+movieNum+"\t"+movieType.name()+"\t"+episode+"\t"+billing+"\t"+role;
		}
		
		public String toPrettyString(){
			StringBuilder sb = new StringBuilder();
			sb.append(movieName+ "\t("+year+")\t:"+movieType.name()+":");
			if(movieNum!=null){
				sb.append("#"+movieNum+"#");
			}
			if(episode!=null){
				sb.append("\t{"+episode+"}");
			}
			if(billing!=-1){
				sb.append("\t<"+billing+">");
			}
			if(role!=null){
				sb.append("\t["+role+"]");
			}
			return sb.toString();
		}
		
		public int getBilling(){
			return billing;
		}
		
		public String getRole(){
			return role;
		}

		public String getMovieName() {
			return movieName;
		}

		public int getYear() {
			return year;
		}

		public MovieType getMovieType() {
			return movieType;
		}
		
		public String getMovieNumber() {
			return movieNum;
		}

		private void setBilling(int billing) {
			toString = null;
			this.billing = billing;
		}

		private void setRole(String role) {
			toString = null;
			this.role = role;
		}
		
		private void setEpisode(String episode) {
			toString = null;
			this.episode = episode;
		}
		
		private void setMovieNumber(String movieNum) {
			toString = null;
			this.movieNum = movieNum;
		}
		
		public int hashCode(){
			if(toString==null){
				toString = toString();
			}
			// hashCode will be cached inside the
			// string object!
			return toString.hashCode();
		}
		
		public int compareTo(MovieRole mr){
			int comp = movieName.compareTo(mr.movieName);
			if(comp!=0) return comp;
			
			comp = year - mr.year;
			if(comp!=0) return comp;
			
			comp = movieType.ordinal() - mr.movieType.ordinal();
			if(comp!=0) return comp;
			
			if(role!=null || mr.role!=null){
				if(role!=null && mr.role!=null){
					comp = role.compareTo(mr.role);
					if(comp!=0) return comp;
				} else if(role==null){
					return -1;
				} else {
					return 1;
				}
			}
			
			return billing - mr.billing;
		}
		
		public boolean equals(Object o){
			if(o==this) return true;
			if(o==null) return false;
			if(o instanceof MovieRole){
				MovieRole mr = (MovieRole)o;
				
				// lazy implementation
				// could be optimised!
				return compareTo(mr) == 0;
			}
			return false;
		}
	}
	
	
	
	public static class ActorToMovies {
		String actor;
		TreeSet<MovieRole> movies;
		
		public ActorToMovies(String actor, TreeSet<MovieRole> movies){
			this.actor = actor;
			this.movies = movies;
		}
		
		public String getActor(){
			return actor;
		}
		
		public TreeSet<MovieRole> getMovies(){
			return movies;
		}
		
		public String toString(){
			return actor+" "+movies;
		}
		
//		private boolean addMovie(MovieRole movieRole){
//			return movies.add(movieRole);
//		}
	}
}
