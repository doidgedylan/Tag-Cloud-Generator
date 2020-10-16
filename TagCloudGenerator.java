import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Generates a tag cloud from a given text file.
 *
 * @author Dylan Doidge and Colin Rafferty
 *
 */
public final class TagCloudGenerator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudGenerator() {
    }

    /**
     * Minimum count of words.
     */
    private static int minCount = 0;
    /**
     * Maximum count of words.
     */
    private static int maxCount = 0;

    /**
     * Returns a negative integer, zero, or a positive integer as the first
     * argument is less than, equal to, or greater than the second.
     */
    private static class StringLT implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    }

    /**
     * Returns a negative integer, zero, or a positive integer as the second
     * argument is less than, equal to, or greater than the second.
     */
    private static class IntegerLT
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }

    /**
     * Populates the map with counts for each word from the input text file.
     *
     * @param wordAndCount
     *            map of words along with their count
     * @param file
     *            input file
     */
    public static void generateMap(Map<String, Integer> wordAndCount,
            BufferedReader file) {

        // For length of the file, separate words and their counts into map
        try {
            String sep = "!,?. \"'\t\n\r&*()-_{}[];:";
            Set<Character> separators = new HashSet<>();

            // Make a set of separators
            generateElements(sep, separators);
            String s = file.readLine();
            while (s != null) {
                int i = 0;
                while (i < s.length()) {

                    // Obtain the next word or separator
                    String word = nextWordOrSeparator(s, i, separators)
                            .toLowerCase();
                    i = i + word.length();
                    if (!separators.contains(word.charAt(0))) {
                        if (wordAndCount.containsKey(word)) {
                            int count = wordAndCount.remove(word);
                            count++;
                            wordAndCount.put(word, count);
                        } else {
                            wordAndCount.put(word, 1);
                        }
                    }
                }
                s = file.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error reading from file");
        }
    }

    /**
     * Sorts the given maps.
     *
     * @param wordsAndCounts
     *            top words and their counts
     * @param n
     *            number of words
     * @return a sorted map
     */
    public static Queue<String> sortCount(Map<String, Integer> wordsAndCounts,
            int n) {
        Queue<String> commonWords = new LinkedList<>();
        Comparator<Map.Entry<String, Integer>> order = new IntegerLT();
        Set<Map.Entry<String, Integer>> keys = wordsAndCounts.entrySet();
        List<Map.Entry<String, Integer>> sorter = new LinkedList<>();
        for (Map.Entry<String, Integer> m : keys) {
            sorter.add(m);
        }
        sorter.sort(order);
        Map.Entry<String, Integer> m = sorter.remove(0);
        maxCount = m.getValue();
        for (int i = 0; i < n; i++) {
            commonWords.add(m.getKey());
            m = sorter.remove(0);
            minCount = m.getValue();
        }
        return commonWords;
    }

    /**
     * Sorts words in alphabetical order.
     *
     * @param commonWords
     *            map of words and counts that is sorted
     */
    public static void sortWords(Queue<String> commonWords) {
        Comparator<String> order = new StringLT();
        SortedSet<String> sorter = new TreeSet<>(order);
        while (commonWords.size() > 0) {
            sorter.add(commonWords.poll());
        }
        while (sorter.size() > 0) {
            String s = sorter.first();
            commonWords.add(s);
            sorter.remove(s);
        }
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    public static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        // The end of the string starts at the given position
        int end = position;

        // Increment end until the string is full of separators or characters
        if (separators.contains(text.charAt(end))) {
            while (text.length() > end
                    && separators.contains(text.charAt(end))) {
                end++;
            }
        } else {
            while (end < text.length()
                    && !separators.contains(text.charAt(end))) {
                end++;
            }
        }
        String word = text.substring(position, end);
        return word;
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param strSet
     *            the {@code Set} to be replaced
     * @replaces strSet
     * @ensures strSet = entries(str)
     */
    public static void generateElements(String str, Set<Character> strSet) {
        assert str != null : "Violation of: str is not null";
        assert strSet != null : "Violation of: strSet is not null";

        // Iterate through string to create set of characters
        int i = 0;
        char part = 'i';
        while (i < str.length()) {
            if (!strSet.contains(str.charAt(i))) {
                part = str.charAt(i);
                strSet.add(part);
            }
            i++;
        }
    }

    /**
     * Generates output for webpage.
     *
     * @param outFile
     *            the file that is written to
     * @param sortedWords
     *            top words in sorted order
     * @param inFile
     *            writing file
     * @param n
     *            number of words
     * @param words
     *            map of words and their counts
     */
    public static void createOutput(PrintWriter outFile,
            Queue<String> sortedWords, String inFile, int n,
            Map<String, Integer> words) {

        //WebPage Title
        outFile.println("<html>");
        outFile.println("<head> ");
        outFile.println("<title>Top " + n + " words in " + inFile + "</title>");
        outFile.println("<link href=" + '"'
                + "http://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/projects/tag-cloud-generator/data/tagcloud.css"
                + '"' + " rel=" + '"' + "stylesheet" + '"' + " type =" + '"'
                + "text/css" + '"' + ">");
        outFile.println("</head>");
        outFile.println("<body>");
        //Header
        outFile.println("<h2>Top " + n + " words in " + inFile + "</h2>");
        outFile.println("<hr>");

        outFile.println("<div class=\"cdiv\">");

        outFile.println("<p class =" + '"' + "cbox" + '"' + ">");
        while (sortedWords.size() > 0) {
            String word = sortedWords.remove();
            fontSize(word, words.get(word).intValue(), outFile);
        }
        outFile.println("</p>");
        outFile.println("</div>");
        outFile.println("</body>");
        outFile.println("</html>");

    }

    /**
     * Determines the font size for words on the webpage.
     *
     * @param word
     *            a given word in the webpage
     * @param count
     *            number of occurances for a word
     * @param out
     *            output of word
     */
    private static void fontSize(String word, int count, PrintWriter out) {
        int fontSize = 11;
        if (maxCount != minCount) {
            fontSize = (((48 - 11) * (count - minCount))
                    / (maxCount - minCount)) + 11;
        }
        out.println("<span style=\"cursor:default\" class=\"f" + fontSize
                + "\" title=\"count: " + count + "\">" + word + "</span>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(System.in));
            System.out.print("Enter a file to read from: ");
            String file = in.readLine();
            System.out.print("Enter a file to be written to: ");
            String write = in.readLine();
            PrintWriter pageOut = new PrintWriter(
                    new BufferedWriter(new FileWriter(write)));
            BufferedReader fileIn = new BufferedReader(new FileReader(file));
            Map<String, Integer> words = new HashMap<>();
            generateMap(words, fileIn);
            boolean valid = false;
            int numWords = words.size();
            int maxWords = 0;
            try {
                while (!valid) {
                    System.out.print(
                            "Enter a valid number of words for the tag cloud: ");
                    maxWords = Integer.parseInt(in.readLine());
                    if (maxWords >= 0 && maxWords <= numWords) {
                        valid = true;
                    } else {
                        System.out.println("Input outside range.");
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("Error: Input is not an integer.");
                System.exit(0);
            }
            Queue<String> commonWords = sortCount(words, maxWords);
            sortWords(commonWords);
            createOutput(pageOut, commonWords, file, maxWords, words);
            try {
                in.close();
                pageOut.close();
                fileIn.close();
            } catch (IOException e) {
                System.err.println("Error closing files.");
                System.exit(0);
            }
        } catch (IOException e) {
            System.err.println("Error opening files.");
            System.exit(0);
        }

    }

}
