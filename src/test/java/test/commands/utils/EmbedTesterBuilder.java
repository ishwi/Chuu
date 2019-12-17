package test.commands.utils;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmbedTesterBuilder {
    private final String command;
    private Pattern footerPatern;
    private Predicate<Matcher> footerMatch;
    private Pattern titlePattern;
    private Predicate<Matcher> titleMatch;
    private Pattern descriptionPattern;
    private Predicate<Matcher> descriptionMatch;
    private Pattern noEmbbed;
    private Predicate<Matcher> noEmbbedMatcher;
    private int timeout = 45;
    private List<FieldRowMatcher> fieldRowMatcher;
    private boolean hasThumbnail;
    private String thumbnailUrl;

    public EmbedTesterBuilder(String command) {
        this.command = command;
    }

    public EmbedTesterBuilder footernPattern(Pattern footerPatern) {
        this.footerPatern = footerPatern;
        return this;
    }

    public EmbedTesterBuilder descriptionPattern(Pattern descriptionPattern) {
        this.descriptionPattern = descriptionPattern;
        return this;
    }

    public EmbedTesterBuilder titlePattern(Pattern titlePattern) {
        this.titlePattern = titlePattern;
        return this;
    }

    public EmbedTesterBuilder noEmbbed(Pattern noEmbbed) {
        this.noEmbbed = noEmbbed;
        return this;
    }

    public EmbedTesterBuilder footerMatch(Predicate<Matcher> footerMatch) {
        this.footerMatch = footerMatch;
        return this;
    }

    public EmbedTesterBuilder titleMatch(Predicate<Matcher> titleMatch) {
        this.titleMatch = titleMatch;
        return this;
    }

    public EmbedTesterBuilder noEmbbedMatcher(Predicate<Matcher> noEmbbedMatcher) {
        this.noEmbbedMatcher = noEmbbedMatcher;
        return this;
    }

    public EmbedTesterBuilder fieldRowMatch(List<FieldRowMatcher> fieldRowMatcher) {
        this.fieldRowMatcher = fieldRowMatcher;
        return this;
    }

    public EmbedTesterBuilder timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public EmbedTesterBuilder hasThumbnail(boolean hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
        return this;
    }

    public EmbedTesterBuilder thumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }


    public EmbedTester build() {
        return new EmbedTester(
                command, footerPatern, footerMatch, titlePattern, titleMatch, descriptionPattern, descriptionMatch, noEmbbed, noEmbbedMatcher, timeout, fieldRowMatcher, hasThumbnail, thumbnailUrl);

    }


    public EmbedTesterBuilder descriptionMatcher(Predicate<Matcher> matcherDescription) {
        this.descriptionMatch = matcherDescription;
        return this;
    }
}


