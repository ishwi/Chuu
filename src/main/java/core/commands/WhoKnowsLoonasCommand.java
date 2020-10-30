package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.CollageGenerator;
import core.imagerenderer.WhoKnowsMaker;
import core.otherlisteners.Reactionary;
import core.parsers.LOOONAParser;
import core.parsers.Parser;
import core.parsers.params.LOONAParameters;
import dao.ChuuService;
import dao.entities.LOONA;
import dao.entities.ReturnNowPlaying;
import dao.entities.WhoKnowsMode;
import dao.entities.WrapperReturnNowPlaying;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class WhoKnowsLoonasCommand extends WhoKnowsBaseCommand<LOONAParameters> {

    private static MultiValuedMap<LOONA, String> loonas;
    private static Map<String, LOONA> reverseLookUp;

    static {
        refreshLOONAS();
    }

    public WhoKnowsLoonasCommand(ChuuService dao) {
        super(dao);
        parser.removeOptional("pie");
        parser.removeOptional("list");
    }

    public static void refreshLOONAS() {
        try (InputStreamReader in = new InputStreamReader(WhoKnowsLoonasCommand.class.getResourceAsStream("/loonas.json"), StandardCharsets.UTF_8)) {
            MultiValuedMap<LOONA, String> temp = new HashSetValuedHashMap<>();
            JSONArray jsonArray = new JSONArray(new JSONTokener(in));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                StreamSupport.stream(jsonObject.getJSONArray("group").spliterator(), false).
                        map(JSONObject.class::cast).map(x -> x.getString("type")).map(LOONA::get).forEach(l -> temp.put(l, name));
            }
            loonas = temp;
            Map<String, LOONA> temp2 = new HashMap<>();
            temp.asMap().forEach((key, value) -> value.forEach(y -> temp2.put(y, key)));
            reverseLookUp = temp2;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not init class.", exception);
        }
    }

    private static <T> BiConsumer<String, Map.Entry<T, List<ReturnNowPlaying>>> getConsumer1() {
        return (representative, x) -> x.getValue().stream().peek(l -> l.setArtist(representative)).forEach(l -> l.setDiscordName(representative));

    }

    private static <T> BiConsumer<String, Map.Entry<T, List<ReturnNowPlaying>>> getConsumer2() {
        return (representative, x) -> x.getValue().stream().peek(l -> l.setLastFMId(representative)).forEach(l -> l.setDiscordName(representative));

    }

    public static Map<String, ReturnNowPlaying> groupByUser(List<WrapperReturnNowPlaying> whoKnowsArtistSet) {
        return whoKnowsArtistSet.stream().flatMap(x -> x.getReturnNowPlayings().stream()).collect(Collectors.groupingBy(ReturnNowPlaying::getLastFMId,
                Collectors.collectingAndThen(Collectors.toList(),
                        (List<ReturnNowPlaying> t) ->
                                t.stream().reduce((z, x) -> {
                                    z.setPlayNumber(z.getPlayNumber() + x.getPlayNumber());
                                    return z;
                                }).orElse(null))));
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        LOONAParameters parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        @Nullable String nullableOwner = parse.getSubject() == LOONAParameters.Subject.ME ? parse.getLastFMData().getName() : null;
        LOONAParameters.Display display = parse.getDisplay();
        LOONAParameters.SubCommand subCommand = parse.getSubCommand();
        int limit = display == LOONAParameters.Display.COLLAGE ? 40 : Integer.MAX_VALUE;

        Set<String> artists;
        switch (subCommand) {
            case GENERAL:
                artists = new HashSet<>(loonas.values());
                break;
            case SPECIFIC:
                assert parse.getTargetedLOONA() != null;
                artists = new HashSet<>(loonas.get(parse.getTargetedLOONA()));
                break;
            case GROUPED:
                assert parse.getTargetedType() != null;
                artists = loonas.asMap().entrySet().stream().filter(x -> x.getKey().getType() == parse.getTargetedType()).flatMap(x -> x.getValue().stream()).collect(Collectors.toSet());
                break;
            default:
                throw new IllegalStateException();
        }
        LOONAParameters.Mode mode = parse.getMode();
        List<WrapperReturnNowPlaying> whoKnowsArtistSet = getService().getWhoKnowsArtistSet(artists, e.getGuild().getIdLong(), limit, nullableOwner);
        whoKnowsArtistSet.stream().flatMap(x -> x.getReturnNowPlayings().stream()).forEach(x -> x.setDiscordName(CommandUtil.getUserInfoNotStripped(e, x.getDiscordId()).getUsername()));

        switch (display) {
            case COLLAGE:
                boolean fixedSize = false;
                int xSize = 5;
                int y = 5;
                if (mode == LOONAParameters.Mode.GROUPED) {
                    if (subCommand == LOONAParameters.SubCommand.GROUPED) {
                        Map<LOONA, List<ReturnNowPlaying>> groupedByType = whoKnowsArtistSet.stream().flatMap(x -> x.getReturnNowPlayings().stream()).collect(Collectors.groupingBy(x -> reverseLookUp.get(x.getArtist()), Collectors.toList()));
                        whoKnowsArtistSet = groupedByType.entrySet().stream()

                                .sorted(Comparator.comparingInt((Map.Entry<LOONA, List<ReturnNowPlaying>> t) -> t.getValue().stream().mapToInt(ReturnNowPlaying::getPlayNumber).sum()).reversed())
                                .map(x ->
                                {
                                    String representative = LOONA.getRepresentative(x.getKey());
                                    String artistUrl = getService().getArtistUrl(representative);
                                    return new WrapperReturnNowPlaying(x.getValue(), x.getValue().size(), artistUrl, representative);
                                })
                                .collect(Collectors.toList());
                        switch (parse.getTargetedType()) {
                            case GROUP:
                            case MISC:
                                xSize = 1;
                                y = 1;
                                break;
                            case SUBUNIT:
                                xSize = 3;
                                y = 1;
                                break;
                            case MEMBER:
                                xSize = 4;
                                y = 3;
                                break;
                        }

                    } else if (subCommand == LOONAParameters.SubCommand.GENERAL) {
                        Map<LOONA.Type, List<ReturnNowPlaying>> groupedByType = whoKnowsArtistSet.stream().flatMap(x -> x.getReturnNowPlayings().stream()).collect(Collectors.groupingBy(x -> reverseLookUp.get(x.getArtist()).getType(), Collectors.toList()));
                        whoKnowsArtistSet = groupedByType.entrySet().stream()

                                .sorted(Comparator.comparingInt((Map.Entry<LOONA.Type, List<ReturnNowPlaying>> t) -> t.getValue().stream().mapToInt(ReturnNowPlaying::getPlayNumber).sum()).reversed())
                                .map(x ->
                                {
                                    String representative = LOONA.getRepresentative(x.getKey());
                                    String artistUrl = getService().getArtistUrl(representative);
                                    return new WrapperReturnNowPlaying(x.getValue(), x.getValue().size(), artistUrl, representative);
                                })
                                .collect(Collectors.toList());
                        xSize = 2;
                        y = 2;
                    } else {
                        fixedSize = true;
                    }

                } else {
                    whoKnowsArtistSet = List.of(handleRepresentatives(parse, whoKnowsArtistSet));
                }


                int size = whoKnowsArtistSet.size();

                AtomicInteger atomicInteger = new AtomicInteger(0);

                if (!fixedSize && size < xSize * y) {

                    xSize = Math.max((int) Math.ceil(Math.sqrt(size)), 1);
                    if (xSize * (xSize - 1) > size) {
                        y = xSize - 1;
                    } else {
                        //noinspection SuspiciousNameCombination
                        y = xSize;
                    }
                }

                LinkedBlockingQueue<Pair<BufferedImage, Integer>> collect = whoKnowsArtistSet.stream().


                        map(x -> Pair.of(doImage(parse, x), atomicInteger.getAndIncrement())).collect(Collectors.toCollection(LinkedBlockingQueue::new));
                BufferedImage bufferedImage = CollageGenerator.generateCollageThreaded(xSize, y, collect, ChartQuality.PNG_BIG);
                sendImage(bufferedImage, e, ChartQuality.PNG_BIG);

                break;
            case SUM:
                if (mode == LOONAParameters.Mode.GROUPED && subCommand == LOONAParameters.SubCommand.GROUPED) {
                    Map<LOONA, List<ReturnNowPlaying>> groupedByType = whoKnowsArtistSet.stream().flatMap(x -> x.getReturnNowPlayings().stream()).collect(Collectors.groupingBy(x -> reverseLookUp.get(x.getArtist()), Collectors.toList()));
                    whoKnowsArtistSet = group(groupedByType, getConsumer2(), LOONA::getRepresentative);

                } else if (mode == LOONAParameters.Mode.GROUPED && subCommand == LOONAParameters.SubCommand.GENERAL) {
                    Map<LOONA.Type, List<ReturnNowPlaying>> groupedByType = whoKnowsArtistSet.stream().flatMap(x -> x.getReturnNowPlayings().stream()).collect(Collectors.groupingBy(x -> reverseLookUp.get(x.getArtist()).getType(), Collectors.toList()));
                    whoKnowsArtistSet = group(groupedByType, getConsumer2(), LOONA::getRepresentative);

                }
                Map<String, ReturnNowPlaying> collect1 = groupByUser(whoKnowsArtistSet);
                WrapperReturnNowPlaying wrapperReturnNowPlaying = handleRepresentatives(parse, collect1);

                super.doImage(parse, wrapperReturnNowPlaying);
                break;
            case COUNT:
                if (mode == LOONAParameters.Mode.GROUPED && subCommand == LOONAParameters.SubCommand.GROUPED) {
                    Map<LOONA, List<ReturnNowPlaying>> groupedByLoonas = whoKnowsArtistSet.stream().flatMap(x -> x.getReturnNowPlayings().stream()).collect(Collectors.groupingBy(x -> reverseLookUp.get(x.getArtist()), Collectors.toList()));
                    whoKnowsArtistSet = group(groupedByLoonas, getConsumer1(), LOONA::getRepresentative);

                } else if (mode == LOONAParameters.Mode.GROUPED && subCommand == LOONAParameters.SubCommand.GENERAL) {
                    Map<LOONA.Type, List<ReturnNowPlaying>> groupedByType = whoKnowsArtistSet.stream().flatMap(x -> x.getReturnNowPlayings().stream()).collect(Collectors.groupingBy(x -> reverseLookUp.get(x.getArtist()).getType(), Collectors.toList()));
                    whoKnowsArtistSet = group(groupedByType, getConsumer1(), LOONA::getRepresentative);
                }


                collect1 = whoKnowsArtistSet.stream().flatMap(x -> x.getReturnNowPlayings().stream()).peek(x -> x.setPlayNumber(1)).collect(
                        Collectors.groupingBy(ReturnNowPlaying::getArtist,
                                Collectors.collectingAndThen(Collectors.toList(),
                                        (List<ReturnNowPlaying> t) ->
                                                t.stream().reduce((z, x) -> {
                                                    z.setPlayNumber(z.getPlayNumber() + x.getPlayNumber());
                                                    return z;
                                                }).orElse(null))));
                wrapperReturnNowPlaying = handleRepresentatives(parse, collect1);
                doList(parse, wrapperReturnNowPlaying);
                break;
        }

    }

    private WrapperReturnNowPlaying handleRepresentatives(LOONAParameters parse, List<WrapperReturnNowPlaying> whoKnowsArtistSet) {
        Map<String, ReturnNowPlaying> collect = groupByUser(whoKnowsArtistSet);
        return handleRepresentatives(parse, collect);
    }

    public <T> List<WrapperReturnNowPlaying> group(Map<T, List<ReturnNowPlaying>> whoKnowsArtistSet, BiConsumer<String, Map.Entry<T, List<ReturnNowPlaying>>> consumer, Function<T, String> mapper) {
        return whoKnowsArtistSet.entrySet().stream()
                .map(x ->
                {
                    String representative = mapper.apply(x.getKey());
                    String artistUrl = getService().getArtistUrl(representative);
                    consumer.accept(representative, x);
                    return new WrapperReturnNowPlaying(x.getValue(), x.getValue().size(), artistUrl, representative);
                }).collect(Collectors.toList());
    }

    private WrapperReturnNowPlaying handleRepresentatives(LOONAParameters
                                                                  parse, Map<String, ReturnNowPlaying> collect1) {
        String representativeArtist;
        String represenentativeUrl;
        switch (parse.getSubCommand()) {
            case GENERAL:
                representativeArtist = "LOONAVERSE";
                represenentativeUrl = getService().getArtistUrl(representativeArtist);
                break;
            case SPECIFIC:
                representativeArtist = LOONA.getRepresentative(parse.getTargetedLOONA());
                represenentativeUrl = getService().getArtistUrl(representativeArtist);
                break;
            case GROUPED:
                representativeArtist = LOONA.getRepresentative(parse.getTargetedType());
                represenentativeUrl = getService().getArtistUrl(representativeArtist);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + parse.getSubCommand());
        }
        return new WrapperReturnNowPlaying(collect1.values().stream().sorted(Comparator.comparingInt(ReturnNowPlaying::getPlayNumber).reversed()).collect(Collectors.toList()), 0, represenentativeUrl, representativeArtist);
    }

    @Override
    BufferedImage doImage(LOONAParameters ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        MessageReceivedEvent e = ap.getE();
        BufferedImage logo = null;
        String title;
        if (e.isFromGuild()) {
            logo = CommandUtil.getLogo(getService(), e);
            title = e.getGuild().getName();
        } else {
            title = e.getJDA().getSelfUser().getName();
        }
        return WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, title, logo);

    }

    void doList(LOONAParameters ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();
        StringBuilder builder = new StringBuilder();

        MessageReceivedEvent e = ap.getE();


        int counter = 1;
        for (ReturnNowPlaying returnNowPlaying : wrapperReturnNowPlaying.getReturnNowPlayings()) {
            builder.append(counter++)
                    .append(countString(returnNowPlaying));
            if (counter == 11)
                break;
        }
        String usable;
        if (e.isFromGuild()) {
            usable = CommandUtil.cleanMarkdownCharacter(e.getGuild().getName());
        } else {
            usable = e.getJDA().getSelfUser().getName();
        }
        embedBuilder.setTitle(getTitle(ap, usable)).
                setThumbnail(CommandUtil.noImageUrl(wrapperReturnNowPlaying.getUrl())).setDescription(builder)
                .setColor(CommandUtil.randomColor());
        e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build())
                .queue(message1 ->
                        new Reactionary<>(wrapperReturnNowPlaying
                                .getReturnNowPlayings(), message1, embedBuilder));
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<LOONAParameters> getParser() {
        return new LOOONAParser(getService());
    }


    @Override
    public String getDescription() {
        return "LOONA";
    }

    @Override
    public List<String> getAliases() {
        return List.of("LOOΠΔ", "wkl", "whoknowsloona", "loona", "loopidelta");
    }

    @Override
    public String getName() {
        return "LOONA";
    }


    @Override
    WhoKnowsMode getWhoknowsMode(LOONAParameters params) {
        return WhoKnowsMode.IMAGE;
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(LOONAParameters params, WhoKnowsMode whoKnowsMode) {
        return null;
    }

    @Override
    public String getTitle(LOONAParameters params, String baseTitle) {
        return null;
    }

    public String countString(ReturnNowPlaying returnNowPlaying) {
        return ". " +
                "[" + CommandUtil.cleanMarkdownCharacter(returnNowPlaying.getArtist()) + "](" +
                CommandUtil
                        .getLastFmArtistUrl(returnNowPlaying.getArtist()) +
                ") - " +
                returnNowPlaying.getPlayNumber() + " listeners\n";
    }
}
