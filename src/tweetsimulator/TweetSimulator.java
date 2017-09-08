package tweetsimulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TweetSimulator {

    public static Map<String, WordClass> map = new HashMap<>();

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        //
        // Get file where tweets are stored.
        //
        System.out.print("Enter file containing existing tweets: ");
        String file = scan.nextLine();
        File tweetFile = new File(file);
        if (!tweetFile.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        //
        // Get number of tweets to generate.
        //
        System.out.print("Enter number of tweets to generate: ");
        int numberOfTweets = 0;
        try {
            numberOfTweets = scan.nextInt();
            if (numberOfTweets < 1) {
                System.out.println("Number must be at least 1.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Invalid number.");
            return;
        }

        try {
            //
            // Read tweets line by line.
            //
            ArrayList<String> tweetList = new ArrayList<>();
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-16"));
            String line = in.readLine();
            while (line != null) {
                tweetList.add(line.toLowerCase());
                line = in.readLine();
            }
            //
            // First 1000 tweets are considered "recent" so there is extra weight given to the contents.
            //
            int recentTweetCount = 0;
            for (String s : tweetList) {
                String[] split = s.split("\\s+");
                recentTweetCount++;
                //
                // 
                //
                String currentWord = "";
                String nextWord = "";
                //
                // Adds every word from each tweet into the Hashmap and gathers
                // neighboring word data and increments word weight for each
                // appearance
                //
                for (int i = 0; i < split.length; i++) {
                    currentWord = split[i];
                    //
                    // Leading words
                    //
                    if (i == 0) {
                        if (!map.containsKey(currentWord)) {
                            map.put(currentWord, new WordClass(currentWord));
                        }
                        map.get(currentWord).start++;
                        if (recentTweetCount < 1000) {
                            map.get(currentWord).start++;
                        }
                    } //
                    // Ending word
                    //
                    else if (i == split.length - 1) {
                        if (!map.containsKey(currentWord)) {
                            map.put(currentWord, new WordClass(currentWord));
                        }
                        if (recentTweetCount < 1000) {
                            map.get(currentWord).end++;
                        }
                        map.get(currentWord).end++;
                    } //
                    // Middle words
                    //
                    else {
                        nextWord = split[i + 1];
                        if (!map.containsKey(currentWord)) {
                            map.put(currentWord, new WordClass(currentWord));
                        }
                        map.get(currentWord).middle++;
                        if (recentTweetCount < 1000) {
                            map.get(currentWord).middle++;
                        }
                        map.get(currentWord).nextWord.add(nextWord);
                    }
                }
            }
            //
            // Generates tweets under 140 characters.
            //
            for (int i = 0; i < numberOfTweets; i++) {
                String tweet = generateTweet();
                while (tweet.length() > 140) {
                    tweet = generateTweet();
                }
                System.out.println(tweet);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TweetSimulator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TweetSimulator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TweetSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Generates tweets.
     *
     * @return generated tweet string
     */
    public static String generateTweet() {
        String tweet = getFirstWord();
        String lastWord = tweet;
        while (!isEnd(lastWord)) {
            lastWord = getNextWord(lastWord);
            tweet += " " + lastWord;
        }
        return tweet;
    }

    /**
     * Randomly determines if a word will be the last word of a tweet based on
     * existing tweets.
     *
     * @param word
     * @return whether word shall end tweet or not
     */
    public static boolean isEnd(String word) {
        if (map.get(word).getContinuationWeight() == 0) {
            return true;    // Shouldn't reach here, used for debugging earlier on.
        }
        double r = Math.random() * map.get(word).getContinuationWeight();
        return map.get(word).middle < r;
    }

    /**
     * Gets a random word as the leading word of the tweet.
     *
     * @return first word
     */
    public static String getFirstWord() {
        ArrayList<WordClass> wordList = new ArrayList<>();
        for (String word : map.keySet()) {
            WordClass wc = map.get(word);
            if (wc.start > 1) {
                wordList.add(wc);
            }
        }
        double totalWeight = 0.0;
        for (WordClass wc : wordList) {
            totalWeight += wc.start;
        }
        double r = Math.random() * totalWeight;
        double countWeight = 0.0;
        for (WordClass wc : wordList) {
            countWeight += wc.start;
            if (countWeight >= r) {
                return wc.word;
            }
        }
        return "###";
    }

    /**
     * Gets the next word of a tweet based on the preceding word.
     * @param precedingWord
     * @return next word
     */
    public static String getNextWord(String precedingWord) {
        ArrayList<String> nextWords = map.get(precedingWord).nextWord;

        ArrayList<WordClass> wordList = new ArrayList<>();
        for (String word : nextWords) {
            WordClass wc = map.get(word);
            if (wc.getContinuationWeight() > 0) {
                wordList.add(wc);
            }
        }
        double totalWeight = 0.0;
        for (WordClass wc : wordList) {
            totalWeight += wc.getContinuationWeight();
        }
        double r = Math.random() * totalWeight;
        double countWeight = 0.0;
        for (WordClass wc : wordList) {
            countWeight += wc.getContinuationWeight();
            if (countWeight >= r) {
                return wc.word;
            }
        }
        return "###";
    }

    /**
     * WordClass keeps track of all the data associated with a word. This
     * includes the number of times the word appears at certain parts of the
     * tweets and words that commonly follow the given word.
     */
    public static class WordClass {

        public int start = 0;
        public int middle = 0;
        public int end = 0;
        public String word;
        public ArrayList<String> nextWord;

        /**
         * Constructor
         *
         * @param word
         */
        public WordClass(String word) {
            this.word = word;
            nextWord = new ArrayList<>();
        }

        /**
         * Gets the weight of a word as a continuation word.
         *
         * @return continuation weight of word
         */
        public int getContinuationWeight() {
            return middle + end;
        }

        /**
         * Gets the overall weight of a word.
         *
         * @return total weight of word
         */
        public int totalWeight() {
            return start + middle + end;
        }
    }
}
