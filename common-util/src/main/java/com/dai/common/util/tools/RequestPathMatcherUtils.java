package com.dai.common.util.tools;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestPathMatcherUtils {

    /**
     * 默认分割符号: "/".
     */
    public static final String DEFAULT_PATH_SEPARATOR = "/";

    private static final int CACHE_TURNOFF_THRESHOLD = 65536;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{[^/]+?\\}");

    private static final char[] WILDCARD_CHARS = {'*', '?', '{'};


    private String pathSeparator;

    private PathSeparatorPatternCache pathSeparatorPatternCache;

    private boolean caseSensitive = true;

    private boolean trimTokens = false;

    private volatile Boolean cachePatterns;

    private final Map<String, String[]> tokenizedPatternCache = new ConcurrentHashMap<>(256);

    final Map<String, PathStringMatcher> stringMatcherCache = new ConcurrentHashMap<>(256);


    public RequestPathMatcherUtils() {
        pathSeparator = DEFAULT_PATH_SEPARATOR;
        pathSeparatorPatternCache = new PathSeparatorPatternCache(DEFAULT_PATH_SEPARATOR);
    }

    public RequestPathMatcherUtils(String pathSeparator) {
        if (null == pathSeparator) {
            throw new IllegalArgumentException("'pathSeparator' is required");
        }
        pathSeparator = pathSeparator;
        pathSeparatorPatternCache = new PathSeparatorPatternCache(pathSeparator);
    }


    public void setPathSeparator(String pathSeparator) {
        pathSeparator = (pathSeparator != null ? pathSeparator : DEFAULT_PATH_SEPARATOR);
        pathSeparatorPatternCache = new PathSeparatorPatternCache(pathSeparator);
    }


    public void setCaseSensitive(boolean caseSensitive) {
        caseSensitive = caseSensitive;
    }


    public void setTrimTokens(boolean trimTokens) {
        trimTokens = trimTokens;
    }


    public void setCachePatterns(boolean cachePatterns) {
        cachePatterns = cachePatterns;
    }

    private void deactivatePatternCache() {
        cachePatterns = false;
        tokenizedPatternCache.clear();
        stringMatcherCache.clear();
    }


    public boolean isPattern(String path) {
        boolean uriVar = false;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '*' || c == '?') {
                return true;
            }
            if (c == '{') {
                uriVar = true;
                continue;
            }
            if (c == '}' && uriVar) {
                return true;
            }
        }
        return false;
    }

    public boolean match(String pattern, String path) {
        return doMatch(pattern, path, true, null);
    }

    public boolean matchStart(String pattern, String path) {
        return doMatch(pattern, path, false, null);
    }


    protected boolean doMatch(String pattern, String path, boolean fullMatch,
                              Map<String, String> uriTemplateVariables) {

        if (path.startsWith(pathSeparator) != pattern.startsWith(pathSeparator)) {
            return false;
        }

        String[] pattDirs = tokenizePattern(pattern);
        if (fullMatch && caseSensitive && !isPotentialMatch(path, pattDirs)) {
            return false;
        }

        String[] pathDirs = tokenizePath(path);

        int pattIdxStart = 0;
        int pattIdxEnd = pattDirs.length - 1;
        int pathIdxStart = 0;
        int pathIdxEnd = pathDirs.length - 1;

        // Match all elements up to the first **
        while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            String pattDir = pattDirs[pattIdxStart];
            if ("**".equals(pattDir)) {
                break;
            }
            if (!matchStrings(pattDir, pathDirs[pathIdxStart], uriTemplateVariables)) {
                return false;
            }
            pattIdxStart++;
            pathIdxStart++;
        }

        if (pathIdxStart > pathIdxEnd) {
            // Path is exhausted, only match if rest of pattern is * or **'s
            if (pattIdxStart > pattIdxEnd) {
                return (pattern.endsWith(pathSeparator) == path.endsWith(pathSeparator));
            }
            if (!fullMatch) {
                return true;
            }
            if (pattIdxStart == pattIdxEnd && pattDirs[pattIdxStart].equals("*") && path.endsWith(pathSeparator)) {
                return true;
            }
            for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
                if (!pattDirs[i].equals("**")) {
                    return false;
                }
            }
            return true;
        } else if (pattIdxStart > pattIdxEnd) {
            // String not exhausted, but pattern is. Failure.
            return false;
        } else if (!fullMatch && "**".equals(pattDirs[pattIdxStart])) {
            // Path start definitely matches due to "**" part in pattern.
            return true;
        }

        // up to last '**'
        while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            String pattDir = pattDirs[pattIdxEnd];
            if (pattDir.equals("**")) {
                break;
            }
            if (!matchStrings(pattDir, pathDirs[pathIdxEnd], uriTemplateVariables)) {
                return false;
            }
            pattIdxEnd--;
            pathIdxEnd--;
        }
        if (pathIdxStart > pathIdxEnd) {
            // String is exhausted
            for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
                if (!pattDirs[i].equals("**")) {
                    return false;
                }
            }
            return true;
        }

        while (pattIdxStart != pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            int patIdxTmp = -1;
            for (int i = pattIdxStart + 1; i <= pattIdxEnd; i++) {
                if (pattDirs[i].equals("**")) {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == pattIdxStart + 1) {
                // '**/**' situation, so skip one
                pattIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - pattIdxStart - 1);
            int strLength = (pathIdxEnd - pathIdxStart + 1);
            int foundIdx = -1;

            strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    String subPat = pattDirs[pattIdxStart + j + 1];
                    String subStr = pathDirs[pathIdxStart + i + j];
                    if (!matchStrings(subPat, subStr, uriTemplateVariables)) {
                        continue strLoop;
                    }
                }
                foundIdx = pathIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            pattIdxStart = patIdxTmp;
            pathIdxStart = foundIdx + patLength;
        }

        for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
            if (!pattDirs[i].equals("**")) {
                return false;
            }
        }

        return true;
    }

    private boolean isPotentialMatch(String path, String[] pattDirs) {
        if (!trimTokens) {
            int pos = 0;
            for (String pattDir : pattDirs) {
                int skipped = skipSeparator(path, pos, pathSeparator);
                pos += skipped;
                skipped = skipSegment(path, pos, pattDir);
                if (skipped < pattDir.length()) {
                    return (skipped > 0 || (pattDir.length() > 0 && isWildcardChar(pattDir.charAt(0))));
                }
                pos += skipped;
            }
        }
        return true;
    }

    private int skipSegment(String path, int pos, String prefix) {
        int skipped = 0;
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            if (isWildcardChar(c)) {
                return skipped;
            }
            int currPos = pos + skipped;
            if (currPos >= path.length()) {
                return 0;
            }
            if (c == path.charAt(currPos)) {
                skipped++;
            }
        }
        return skipped;
    }

    private int skipSeparator(String path, int pos, String separator) {
        int skipped = 0;
        while (path.startsWith(separator, pos + skipped)) {
            skipped += separator.length();
        }
        return skipped;
    }

    private boolean isWildcardChar(char c) {
        for (char candidate : WILDCARD_CHARS) {
            if (c == candidate) {
                return true;
            }
        }
        return false;
    }


    protected String[] tokenizePattern(String pattern) {
        String[] tokenized = null;
        Boolean cachePatterns = this.cachePatterns;
        if (cachePatterns == null || cachePatterns.booleanValue()) {
            tokenized = tokenizedPatternCache.get(pattern);
        }
        if (tokenized == null) {
            tokenized = tokenizePath(pattern);
            if (cachePatterns == null && tokenizedPatternCache.size() >= CACHE_TURNOFF_THRESHOLD) {
                deactivatePatternCache();
                return tokenized;
            }
            if (cachePatterns == null || cachePatterns.booleanValue()) {
                tokenizedPatternCache.put(pattern, tokenized);
            }
        }
        return tokenized;
    }


    protected String[] tokenizePath(String path) {
        return tokenizeToStringArray(path, pathSeparator, trimTokens, true);
    }

    public static String[] tokenizeToStringArray(
            String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

        if (str == null) {
            return new String[0];
        }

        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }

    public static String[] toStringArray(Collection<String> collection) {
        return (collection != null ? collection.toArray(new String[0]) : new String[0]);
    }

    private boolean matchStrings(String pattern, String str,
                                 Map<String, String> uriTemplateVariables) {

        return getStringMatcher(pattern).matchStrings(str, uriTemplateVariables);
    }


    protected PathStringMatcher getStringMatcher(String pattern) {
        PathStringMatcher matcher = null;
        Boolean cachePatterns = this.cachePatterns;
        if (cachePatterns == null || cachePatterns.booleanValue()) {
            matcher = stringMatcherCache.get(pattern);
        }
        if (matcher == null) {
            matcher = new PathStringMatcher(pattern, caseSensitive);
            if (cachePatterns == null && stringMatcherCache.size() >= CACHE_TURNOFF_THRESHOLD) {
                deactivatePatternCache();
                return matcher;
            }
            if (cachePatterns == null || cachePatterns.booleanValue()) {
                stringMatcherCache.put(pattern, matcher);
            }
        }
        return matcher;
    }


    public String extractPathWithinPattern(String pattern, String path) {
        String[] patternParts = tokenizeToStringArray(pattern, pathSeparator, trimTokens, true);
        String[] pathParts = tokenizeToStringArray(path, pathSeparator, trimTokens, true);
        StringBuilder builder = new StringBuilder();
        boolean pathStarted = false;

        for (int segment = 0; segment < patternParts.length; segment++) {
            String patternPart = patternParts[segment];
            if (patternPart.indexOf('*') > -1 || patternPart.indexOf('?') > -1) {
                for (; segment < pathParts.length; segment++) {
                    if (pathStarted || (segment == 0 && !pattern.startsWith(pathSeparator))) {
                        builder.append(pathSeparator);
                    }
                    builder.append(pathParts[segment]);
                    pathStarted = true;
                }
            }
        }

        return builder.toString();
    }

    public Map<String, String> extractUriTemplateVariables(String pattern, String path) {
        Map<String, String> variables = new LinkedHashMap<>();
        boolean result = doMatch(pattern, path, true, variables);
        if (!result) {
            throw new IllegalStateException("Pattern \"" + pattern + "\" is not a match for \"" + path + "\"");
        }
        return variables;
    }


    public String combine(String pattern1, String pattern2) {
        if (!hasText(pattern1) && !hasText(pattern2)) {
            return "";
        }
        if (!hasText(pattern1)) {
            return pattern2;
        }
        if (!hasText(pattern2)) {
            return pattern1;
        }

        boolean pattern1ContainsUriVar = (pattern1.indexOf('{') != -1);
        if (!pattern1.equals(pattern2) && !pattern1ContainsUriVar && match(pattern1, pattern2)) {
            // /* + /hotel -> /hotel ; "/*.*" + "/*.html" -> /*.html
            // However /user + /user -> /usr/user ; /{foo} + /bar -> /{foo}/bar
            return pattern2;
        }

        // /hotels/* + /booking -> /hotels/booking
        // /hotels/* + booking -> /hotels/booking
        if (pattern1.endsWith(pathSeparatorPatternCache.getEndsOnWildCard())) {
            return concat(pattern1.substring(0, pattern1.length() - 2), pattern2);
        }

        // /hotels/** + /booking -> /hotels/**/booking
        // /hotels/** + booking -> /hotels/**/booking
        if (pattern1.endsWith(pathSeparatorPatternCache.getEndsOnDoubleWildCard())) {
            return concat(pattern1, pattern2);
        }

        int starDotPos1 = pattern1.indexOf("*.");
        if (pattern1ContainsUriVar || starDotPos1 == -1 || pathSeparator.equals(".")) {
            // simply concatenate the two patterns
            return concat(pattern1, pattern2);
        }

        String ext1 = pattern1.substring(starDotPos1 + 1);
        int dotPos2 = pattern2.indexOf('.');
        String file2 = (dotPos2 == -1 ? pattern2 : pattern2.substring(0, dotPos2));
        String ext2 = (dotPos2 == -1 ? "" : pattern2.substring(dotPos2));
        boolean ext1All = (ext1.equals(".*") || ext1.isEmpty());
        boolean ext2All = (ext2.equals(".*") || ext2.isEmpty());
        if (!ext1All && !ext2All) {
            throw new IllegalArgumentException("Cannot combine patterns: " + pattern1 + " vs " + pattern2);
        }
        String ext = (ext1All ? ext2 : ext1);
        return file2 + ext;
    }

    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private String concat(String path1, String path2) {
        boolean path1EndsWithSeparator = path1.endsWith(pathSeparator);
        boolean path2StartsWithSeparator = path2.startsWith(pathSeparator);

        if (path1EndsWithSeparator && path2StartsWithSeparator) {
            return path1 + path2.substring(1);
        } else if (path1EndsWithSeparator || path2StartsWithSeparator) {
            return path1 + path2;
        } else {
            return path1 + pathSeparator + path2;
        }
    }

    public Comparator<String> getPatternComparator(String path) {
        return new PatternComparator(path);
    }


    protected static class PathStringMatcher {

        private static final Pattern GLOB_PATTERN = Pattern.compile("\\?|\\*|\\{((?:\\{[^/]+?\\}|[^/{}]|\\\\[{}])+?)\\}");

        private static final String DEFAULT_VARIABLE_PATTERN = "(.*)";

        private final Pattern pattern;

        private final List<String> variableNames = new LinkedList<>();

        public PathStringMatcher(String pattern) {
            this(pattern, true);
        }

        public PathStringMatcher(String pattern, boolean caseSensitive) {
            StringBuilder patternBuilder = new StringBuilder();
            Matcher matcher = GLOB_PATTERN.matcher(pattern);
            int end = 0;
            while (matcher.find()) {
                patternBuilder.append(quote(pattern, end, matcher.start()));
                String match = matcher.group();
                if ("?".equals(match)) {
                    patternBuilder.append('.');
                } else if ("*".equals(match)) {
                    patternBuilder.append(".*");
                } else if (match.startsWith("{") && match.endsWith("}")) {
                    int colonIdx = match.indexOf(':');
                    if (colonIdx == -1) {
                        patternBuilder.append(DEFAULT_VARIABLE_PATTERN);
                        this.variableNames.add(matcher.group(1));
                    } else {
                        String variablePattern = match.substring(colonIdx + 1, match.length() - 1);
                        patternBuilder.append('(');
                        patternBuilder.append(variablePattern);
                        patternBuilder.append(')');
                        String variableName = match.substring(1, colonIdx);
                        this.variableNames.add(variableName);
                    }
                }
                end = matcher.end();
            }
            patternBuilder.append(quote(pattern, end, pattern.length()));
            this.pattern = (caseSensitive ? Pattern.compile(patternBuilder.toString()) :
                    Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE));
        }

        private String quote(String s, int start, int end) {
            if (start == end) {
                return "";
            }
            return Pattern.quote(s.substring(start, end));
        }


        public boolean matchStrings(String str, Map<String, String> uriTemplateVariables) {
            Matcher matcher = pattern.matcher(str);
            if (matcher.matches()) {
                if (uriTemplateVariables != null) {
                    // SPR-8455
                    if (variableNames.size() != matcher.groupCount()) {
                        throw new IllegalArgumentException("The number of capturing groups in the pattern segment " +
                                pattern + " does not match the number of URI template variables it defines, " +
                                "which can occur if capturing groups are used in a URI template regex. " +
                                "Use non-capturing groups instead.");
                    }
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        String name = variableNames.get(i - 1);
                        String value = matcher.group(i);
                        uriTemplateVariables.put(name, value);
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }


    protected static class PatternComparator implements Comparator<String> {

        private final String path;

        public PatternComparator(String path) {
            this.path = path;
        }

        @Override
        public int compare(String pattern1, String pattern2) {
            PatternInfo info1 = new PatternInfo(pattern1);
            PatternInfo info2 = new PatternInfo(pattern2);

            if (info1.isLeastSpecific() && info2.isLeastSpecific()) {
                return 0;
            } else if (info1.isLeastSpecific()) {
                return 1;
            } else if (info2.isLeastSpecific()) {
                return -1;
            }

            boolean pattern1EqualsPath = pattern1.equals(this.path);
            boolean pattern2EqualsPath = pattern2.equals(this.path);
            if (pattern1EqualsPath && pattern2EqualsPath) {
                return 0;
            } else if (pattern1EqualsPath) {
                return -1;
            } else if (pattern2EqualsPath) {
                return 1;
            }

            if (info1.isPrefixPattern() && info2.getDoubleWildcards() == 0) {
                return 1;
            } else if (info2.isPrefixPattern() && info1.getDoubleWildcards() == 0) {
                return -1;
            }

            if (info1.getTotalCount() != info2.getTotalCount()) {
                return info1.getTotalCount() - info2.getTotalCount();
            }

            if (info1.getLength() != info2.getLength()) {
                return info2.getLength() - info1.getLength();
            }

            if (info1.getSingleWildcards() < info2.getSingleWildcards()) {
                return -1;
            } else if (info2.getSingleWildcards() < info1.getSingleWildcards()) {
                return 1;
            }

            if (info1.getUriVars() < info2.getUriVars()) {
                return -1;
            } else if (info2.getUriVars() < info1.getUriVars()) {
                return 1;
            }

            return 0;
        }


        /**
         * Value class that holds information about the pattern, e.g. number of
         * occurrences of "*", "**", and "{" pattern elements.
         */
        private static class PatternInfo {
            private final String pattern;

            private int uriVars;

            private int singleWildcards;

            private int doubleWildcards;

            private boolean catchAllPattern;

            private boolean prefixPattern;


            private Integer length;

            public PatternInfo(String pattern) {
                this.pattern = pattern;
                if (this.pattern != null) {
                    initCounters();
                    this.catchAllPattern = this.pattern.equals("/**");
                    this.prefixPattern = !this.catchAllPattern && this.pattern.endsWith("/**");
                }
                if (this.uriVars == 0) {
                    this.length = (this.pattern != null ? this.pattern.length() : 0);
                }
            }

            protected void initCounters() {
                int pos = 0;
                if (this.pattern != null) {
                    while (pos < this.pattern.length()) {
                        if (this.pattern.charAt(pos) == '{') {
                            this.uriVars++;
                            pos++;
                        } else if (this.pattern.charAt(pos) == '*') {
                            if (pos + 1 < this.pattern.length() && this.pattern.charAt(pos + 1) == '*') {
                                this.doubleWildcards++;
                                pos += 2;
                            } else if (pos > 0 && !this.pattern.substring(pos - 1).equals(".*")) {
                                this.singleWildcards++;
                                pos++;
                            } else {
                                pos++;
                            }
                        } else {
                            pos++;
                        }
                    }
                }
            }

            public int getUriVars() {
                return this.uriVars;
            }

            public int getSingleWildcards() {
                return this.singleWildcards;
            }

            public int getDoubleWildcards() {
                return this.doubleWildcards;
            }

            public boolean isLeastSpecific() {
                return (this.pattern == null || this.catchAllPattern);
            }

            public boolean isPrefixPattern() {
                return this.prefixPattern;
            }

            public int getTotalCount() {
                return this.uriVars + this.singleWildcards + (2 * this.doubleWildcards);
            }

            /**
             * Returns the length of the given pattern, where template variables are considered to be 1 long.
             */
            public int getLength() {
                if (this.length == null) {
                    this.length = (this.pattern != null ?
                            VARIABLE_PATTERN.matcher(this.pattern).replaceAll("#").length() : 0);
                }
                return this.length;
            }
        }
    }

    private static class PathSeparatorPatternCache {

        private final String endsOnWildCard;

        private final String endsOnDoubleWildCard;

        public PathSeparatorPatternCache(String pathSeparator) {
            endsOnWildCard = pathSeparator + "*";
            endsOnDoubleWildCard = pathSeparator + "**";
        }

        public String getEndsOnWildCard() {
            return endsOnWildCard;
        }

        public String getEndsOnDoubleWildCard() {
            return endsOnDoubleWildCard;
        }
    }

}
