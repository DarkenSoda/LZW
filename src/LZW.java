import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LZW {
    public void compress(String inputFile, String outputFile) {
        if (!inputFile.endsWith(".txt")) {
            System.err.println("Input file must be a .txt file.");
            return;
        }

        if (!outputFile.endsWith(".bin")) {
            System.err.println("Output file must be a .bin (binary) file.");
            return;
        }

        try {
            String content = Files.readString(Path.of(inputFile));

            List<LZWTag> compressedData = compressor(content);

            File outputFileObj = new File(outputFile);
            if (!outputFileObj.exists()) {
                if (!outputFileObj.createNewFile()) {
                    System.err.println("Failed to create the output file.");
                    return;
                }
            }

            DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile));
            for (LZWTag tag : compressedData) {
                dos.write((byte) (tag.position & 0xFF));
            }

            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<LZWTag> compressor(String data) {
        List<LZWTag> tagList = new ArrayList<LZWTag>();

        Map<String, Integer> dictionary = populateMapCompression();
        int i = 0;
        while (i < data.length()) {
            if (dictionary.size() >= 255) {
                dictionary = populateMapCompression();
            }

            String longestMatch = "" + data.charAt(i);
            String lastFoundPosition;

            int k = i + 1;
            while (k < data.length() && dictionary.get(longestMatch) != null) {
                longestMatch += data.charAt(k++);
            }

            if (dictionary.get(longestMatch) != null) {
                lastFoundPosition = longestMatch;
            } else {
                lastFoundPosition = longestMatch.substring(0, longestMatch.length() - 1);
            }

            LZWTag tag = new LZWTag();
            tag.position = dictionary.get(lastFoundPosition);
            tagList.add(tag);

            dictionary.put(longestMatch, dictionary.size());

            i += lastFoundPosition.length();
        }
        return tagList;
    }

    public void decompress(String inputFile, String outputFile) {
        if (!inputFile.endsWith(".bin")) {
            System.err.println("Input file must be a .bin (binary) file.");
            return;
        }

        if (!outputFile.endsWith(".txt")) {
            System.err.println("Output file must be a .txt file.");
            return;
        }

        List<LZWTag> tagList = new ArrayList<LZWTag>();
        try {
            FileInputStream fis = new FileInputStream(inputFile);
            DataInputStream dis = new DataInputStream(fis);
            while (dis.available() > 0) {
                byte tagValue = dis.readByte();
                LZWTag tag = new LZWTag();
                tag.position = tagValue & 0xFF;
                tagList.add(tag);
            }
            dis.close();

            String result = decompressor(tagList);

            FileWriter writer = new FileWriter(outputFile);
            writer.write(result);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String decompressor(List<LZWTag> data) {
        String result = "";
        Map<Integer, String> dictionary = populateMapDeCompression();

        Integer lastPosition = null;
        for (LZWTag lzwTag : data) {
            if (dictionary.size() >= 255) {
                dictionary = populateMapDeCompression();
            }

            String foundString = dictionary.get(lzwTag.position);

            if (foundString == null) {
                foundString = dictionary.get(lastPosition);
                foundString += foundString.charAt(0);
            }

            result += foundString;

            if (lastPosition != null) {
                dictionary.put(dictionary.size(), dictionary.get(lastPosition) + foundString.charAt(0));
            }

            lastPosition = lzwTag.position;
        }

        return result;
    }

    private Map<String, Integer> populateMapCompression() {
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 128; i++) {
            String character = String.valueOf((char) i);
            dictionary.put(character, i);
        }
        return dictionary;
    }

    private Map<Integer, String> populateMapDeCompression() {
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 128; i++) {
            String character = String.valueOf((char) i);
            dictionary.put(i, character);
        }
        return dictionary;
    }
}
