package com.loyid.grammarbook;

import android.net.Uri;
import android.provider.BaseColumns;

public final class GrammarProviderContract {
	public static final String AUTHORITY = "com.loyid.grammarprovider";
	
	private GrammarProviderContract() {
	}
	
	public static final class Grammars implements BaseColumns {
		/**
        * The table name offered by this provider
         */
		public static final String TABLE_NAME = "grammars";
		
		/*
        * URI definitions
         */
		/**
        * The table name offered by this provider
         */
		private static final String SCHEME = "content://";
		
		/*
        * Path parts for the URIs
         */
		/**
        * Path part for the Grammars URI
         */
		private static final String PATH_GRAMMAR = "/grammars";
		
		/**
        * Path part for the Grammar ID URI
         */
		private static final String PATH_GRAMMAR_ID = "/grammars/";
		
		/**
        * 0-relative position of a grammar ID segment in the path part of a grammar ID URI
         */
		public static final int GRAMMAR_ID_PATH_POSITION = 1;
		
		/**
        * The content:// style URL for this table
         */
		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_GRAMMAR);
		
		/**
        * The content URI base for a single grammar. Callers must
        * append a numeric grammar id to this Uri to retrieve a grammar
         */
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_GRAMMAR_ID);
		
		/**
        * The content URI match pattern for a single grammar, specified by its ID. Use this to match
        * incoming URIs or to construct an Intent.
         */
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_GRAMMAR_ID + "/#");
		
		/**
        * The default sort order for this table
         */
		public static final String DEFAULT_SORT_ORDER = "grammar ASC"; //ASC or DESC
		
		/*
        * Column definitions
         */
		/**
        * Column name for the grammar
        * <P>Type: TEXT</P>
         */
		public static final String COLUMN_NAME_GRAMMAR = "grammar";
		
		/**
		 * Column name for the type of the grammar
		 * <P>Type: INTEGER</P>
		 */	
		public static final String COLUMN_NAME_TYPE = "type";
		
		/**
		 * Column name for the meaning of the grammar
		 * <P>Type: TEXT</P>
		 */	
		public static final String COLUMN_NAME_MEANING = "meaning";
		
		/**
		 * Column name for the creation timestamp
		 * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
		 */	
		public static final String COLUMN_NAME_CREATED_DATE = "created";	
		
		/**
		 * Column name for the creation timestamp
		 * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
		 */	
		public static final String COLUMN_NAME_MODIFIED_DATE = "modified";		
	}
}
