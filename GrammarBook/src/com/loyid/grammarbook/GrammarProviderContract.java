package com.loyid.grammarbook;

import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.BaseColumns;

public final class GrammarProviderContract {
	public static final String AUTHORITY = "com.loyid.grammarbook.provider";
	
	private GrammarProviderContract() {}
	
	public static final String UNKNOWN_STRING = "<unknown>";
	
	public static String keyFor(String name) {
		if (name != null)  {
			boolean sortfirst = false;
			if (name.equals(UNKNOWN_STRING)) {
				return "\001";
			}
			
			// Check if the first character is \001. We use this to
			// force sorting of certain special files, like the silent ringtone.
			if (name.startsWith("\001")) {
				sortfirst = true;
			}
			
			name = name.trim().toLowerCase();
			if (name.startsWith("the ")) {
				name = name.substring(4);
			}
			
			if (name.startsWith("an ")) {
				name = name.substring(3);
			}
			
			if (name.startsWith("a ")) {
				name = name.substring(2);
			}
			
			if (name.endsWith(", the") || name.endsWith(",the") ||
					name.endsWith(", an") || name.endsWith(",an") ||
					name.endsWith(", a") || name.endsWith(",a")) {
				name = name.substring(0, name.lastIndexOf(','));
			}
			
			name = name.replaceAll("[\\[\\]\\(\\)\"'.,?!]", "").trim();
			if (name.length() > 0) {
				// Insert a separator between the characters to avoid
				// matches on a partial character. If we ever change
				// to start-of-word-only matches, this can be removed.
				StringBuilder b = new StringBuilder();
				b.append('.');
				int nl = name.length();
				for (int i = 0; i < nl; i++) {
					b.append(name.charAt(i));
					b.append('.');
				}
				
				name = b.toString();
				String key = DatabaseUtils.getCollationKey(name);
				if (sortfirst) {
					key = "\001" + key;
				}
				return key;
			} else {
				return "";
			}
		}
		
		return null;
	}
	public static class GBaseColumns implements BaseColumns {
		/*
		 * URI definitions
		 */
		
		/**
		 * The table name offered by this provider
		 */
		public static final String SCHEME = "content://";
	}
	
	public static final class Meanings extends GBaseColumns {
		public static final String TABLE_NAME = "meanings";		
		
		/*
		 * Path parts for the URIs
		 */
		/**
		 * Path part for the Meanings URI
		 */
		private static final String PATH_MEANING = "/meanings";
		
		/**
		 * Path part for the Meaning ID URI
		 */
		private static final String PATH_MEANING_ID = "/meanings/";
		
		/**
		 * 0-relative position of a Meaning ID segment in the path part of a Meaning ID URI
		 */
		public static final int MEANING_ID_PATH_POSITION = 1;
		
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_MEANING);
		
		/**
		 * The content URI base for a single meaning. Callers must
		 * append a numeric meaning id to this URI to retrieve a meaning
		 */
		public static final Uri CONTENT_MEANING_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_MEANING_ID);
		
		/**
		 * The content URI match pattern for a single meaning, specified by its ID. Use this to match
		 * incoming URIs or to construct an Intent.
		 */
		public static final Uri CONTENT_MEANING_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_MEANING_ID + "/#");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.loyid.grammarbook.provider.meanings";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.loyid.grammarbook.provider.meanings";
		
		public static final String COLUMN_NAME_WORD = "word";
		
		public static final String COLUMN_NAME_TYPE = "type";
		
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
		
		public static final String COLUMN_NAME_REFER_COUNT = "refer_count";
		
		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_TYPE + " ASC"; //ASC or DESC
	}
	
	public static final class Mappings extends GBaseColumns {
		public static final String TABLE_NAME = "mappings";
		
		/*
		 * URI definitions
		 */
		
		/*
		 * Path parts for the URIs
		 */
		/**
		 * Path part for the Mappings URI
		 */
		private static final String PATH_MAPPING = "/mappings";
		
		/**
		 * Path part for the Mapping ID URI
		 */
		private static final String PATH_MAPPING_ID = "/mappings/";
		
		/**
		 * 0-relative position of a Mapping ID segment in the path part of a Mapping ID URI
		 */
		public static final int MAPPING_ID_PATH_POSITION = 1;
		
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_MAPPING);
		
		/**
		 * The content URI base for a single mapping. Callers must
		 * append a numeric mapping id to this URI to retrieve a mapping
		 */
		public static final Uri CONTENT_MAPPING_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_MAPPING_ID);
		
		/**
		 * The content URI match pattern for a single mapping, specified by its ID. Use this to match
		 * incoming URIs or to construct an Intent.
		 */
		public static final Uri CONTENT_MAPPING_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_MAPPING_ID + "/#");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.loyid.grammarbook.provider.mappings";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.loyid.grammarbook.provider.mappings";
		
		public static final String COLUMN_NAME_GRAMMAR_ID = "grammar_id";
		
		public static final String COLUMN_NAME_MEANING_ID = "meaing_id";
		
		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_GRAMMAR_ID + " ASC"; //ASC or DESC
	}
	
	public static final class Grammars extends GBaseColumns {
		/**
        * The table name offered by this provider
         */
		public static final String TABLE_NAME = "grammars";	
		
		/*
        * URI definitions
         */
		
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
		public static final Uri CONTENT_GRAMMAR_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_GRAMMAR_ID);
		
		/**
        * The content URI match pattern for a single grammar, specified by its ID. Use this to match
        * incoming URIs or to construct an Intent.
         */
		public static final Uri CONTENT_GRAMMAR_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_GRAMMAR_ID + "/#");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.loyid.grammarbook.provider.grammars";
		
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.loyid.grammarbook.provider.grammars";
		
		/*
        * Column definitions
         */
		/**
        * Column name for the grammar
        * <P>Type: TEXT</P>
         */
		public static final String COLUMN_NAME_GRAMMAR = "grammar";
		
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
		
		/**
        * The default sort order for this table
         */
		public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_GRAMMAR + " ASC"; //ASC or DESC
	}
}
