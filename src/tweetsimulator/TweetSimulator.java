package tweetsimulator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TweetSimulator {

    public static Map<String, WordClass> map = new HashMap<>();

    public static void main(String[] args) {
        try {
            ArrayList<String> tweetList = new ArrayList<>();
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("R:\\tweetz.txt"), "UTF-16"));
            String line = in.readLine();
            while (line != null) {
                tweetList.add(line.toLowerCase());
                line = in.readLine();
            }
            int recentTweetCount = 0;
            for (String s : tweetList) {
                String[] split = s.split("\\s+");
                recentTweetCount++;

                String currentWord = "";
                String nextWord = "";
                for (int i = 0; i < split.length; i++) {
                    currentWord = split[i];
                    if (i == 0) {
                        if (!map.containsKey(currentWord)) {
                            map.put(currentWord, new WordClass(currentWord));
                        }
                        map.get(currentWord).start++;
                        if (recentTweetCount < 1000) {
                            map.get(currentWord).start++;
                        }
                    } else if (i == split.length - 1) {
                        if (!map.containsKey(currentWord)) {
                            map.put(currentWord, new WordClass(currentWord));
                        }
                        if (recentTweetCount < 1000) {
                            map.get(currentWord).end++;
                        }
                        map.get(currentWord).end++;
                    } else {
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
            for (int i = 0; i < 1000; i++) {
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

    public static String generateTweet() {
        String tweet = getFirstWord();
        String lastWord = tweet;
        while (!isEnd(lastWord)) {
            lastWord = getNextWord(lastWord);
            tweet += " " + lastWord;
        }
        return tweet;
    }

    public static boolean isEnd(String word) {
        if (map.get(word).continuationWeight() == 0) {
            return true;
        }
        double r = Math.random() * map.get(word).continuationWeight();
        return map.get(word).middle < r;
    }

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

    public static String getNextWord(String precedingWord) {
        ArrayList<String> nextWords = map.get(precedingWord).nextWord;

        ArrayList<WordClass> wordList = new ArrayList<>();
        for (String word : nextWords) {
            WordClass wc = map.get(word);
            if (wc.continuationWeight() > 0) {
                wordList.add(wc);
            }
        }
        double totalWeight = 0.0;
        for (WordClass wc : wordList) {
            totalWeight += wc.continuationWeight();
        }
        double r = Math.random() * totalWeight;
        double countWeight = 0.0;
        for (WordClass wc : wordList) {
            countWeight += wc.continuationWeight();
            if (countWeight >= r) {
                return wc.word;
            }
        }
        return "###";
    }

    public static class WordClass {

        public int start = 0;
        public int middle = 0;
        public int end = 0;
        public String word;
        public ArrayList<String> nextWord;

        public WordClass(String word) {
            this.word = word;
            nextWord = new ArrayList<>();
        }

        public int continuationWeight() {
            return middle + end;
        }

        public int totalWeight() {
            return start + middle + end;
        }
    }
}
