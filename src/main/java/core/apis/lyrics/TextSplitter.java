package core.apis.lyrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TextSplitter {
    public static List<String> split(String content) {
        return split(content, 2000);
    }

    public static List<String> split(String content, int limit) {
        List<String> pages = new ArrayList<>();
        List<String> lines = Arrays.stream(content.trim().split("\n")).dropWhile(String::isBlank).collect(Collectors.toList());
        StringBuilder chunk = new StringBuilder();
        for (String line : lines) {
            if (chunk.length() != 0 && chunk.length() + line.length() > limit) {
                pages.add(chunk.toString());
                chunk = new StringBuilder();
            }

            if (line.length() > limit) {
                var lineChunks = line.length() / limit;

                for (int i = 0; i < lineChunks; i++) {

                    var start = limit * i;
                    var end = start + limit;
                    pages.add(line.substring(start, end));
                }
            } else {
                chunk.append(line).append("\n");
            }
        }

        if (chunk.length() != 0) {
            pages.add(chunk.toString());
        }

        return pages;
    }
}

