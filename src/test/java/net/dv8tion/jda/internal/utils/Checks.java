///*
// * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package net.dv8tion.jda.internal.utils;
//
//import net.dv8tion.jda.api.entities.IPermissionHolder;
//import net.dv8tion.jda.api.entities.channel.ChannelType;
//import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
//import net.dv8tion.jda.api.interactions.components.Component;
//import net.dv8tion.jda.api.interactions.components.LayoutComponent;
//import org.jetbrains.annotations.Contract;
//
//import java.util.Collection;
//import java.util.EnumSet;
//import java.util.function.BiFunction;
//import java.util.function.Predicate;
//import java.util.regex.Pattern;
//import java.util.stream.Stream;
//
//public class Checks
//{
//    public static final Pattern ALPHANUMERIC_WITH_DASH = Pattern.compile("[\\w-]+", Pattern.UNICODE_CHARACTER_CLASS);
//    public static final Pattern ALPHANUMERIC = Pattern.compile("[\\w]+", Pattern.UNICODE_CHARACTER_CLASS);
//
//    @Contract("null -> fail")
//    public static void isSnowflake(final String snowflake)
//    {
//        isSnowflake(snowflake, snowflake);
//    }
//
//    @Contract("null, _ -> fail")
//    public static void isSnowflake(final String snowflake, final String message)
//    {
//        notNull(snowflake, message);
//        if (snowflake.length() > 20 || !Helpers.isNumeric(snowflake))
//            throw new IllegalArgumentException(message + " is not a valid snowflake value! Provided: \"" + snowflake + "\"");
//    }
//
//    @Contract("false, _ -> fail")
//    public static void check(final boolean expression, final String message)
//    {
//
//    }
//
//    @Contract("false, _, _ -> fail")
//    public static void check(final boolean expression, final String message, final Object... args)
//    {
//
//    }
//
//    @Contract("false, _, _ -> fail")
//    public static void check(final boolean expression, final String message, final Object arg)
//    {
//
//    }
//
//    @Contract("null, _ -> fail")
//    public static void notNull(final Object argument, final String name)
//    {
//
//    }
//
//    @Contract("null, _ -> fail")
//    public static void notEmpty(final CharSequence argument, final String name)
//    {
//
//    }
//
//    @Contract("null, _ -> fail")
//    public static void notBlank(final CharSequence argument, final String name)
//    {
//
//    }
//
//    @Contract("null, _ -> fail")
//    public static void noWhitespace(final CharSequence argument, final String name)
//    {
//
//    }
//
//    @Contract("null, _ -> fail")
//    public static void notEmpty(final Collection<?> argument, final String name)
//    {
//
//    }
//
//    @Contract("null, _ -> fail")
//    public static void notEmpty(final Object[] argument, final String name)
//    {
//
//    }
//
//    @Contract("null, _ -> fail")
//    public static void noneNull(final Collection<?> argument, final String name)
//    {
//    }
//
//    @Contract("null, _ -> fail")
//    public static void noneNull(final Object[] argument, final String name)
//    {
//
//    }
//
//    @Contract("null, _ -> fail")
//    public static <T extends CharSequence> void noneEmpty(final Collection<T> argument, final String name)
//    {
//    }
//
//    @Contract("null, _ -> fail")
//    public static <T extends CharSequence> void noneBlank(final Collection<T> argument, final String name)
//    {
//
//    }
//
//    @Contract("null, _ -> fail")
//    public static <T extends CharSequence> void noneContainBlanks(final Collection<T> argument, final String name)
//    {
//    }
//
//    public static void inRange(final String input, final int min, final int max, final String name)
//    {
//
//    }
//
//    public static void notLonger(final String input, final int length, final String name)
//    {
//
//    }
//
//    public static void matches(final String input, final Pattern pattern, final String name)
//    {
//    }
//
//    public static void isLowercase(final String input, final String name)
//    {
//    }
//
//    public static void positive(final int n, final String name)
//    {
//    }
//
//    public static void positive(final long n, final String name)
//    {
//    }
//
//    public static void notNegative(final int n, final String name)
//    {
//    }
//
//    public static void notNegative(final long n, final String name)
//    {
//    }
//
//    // Unique streams checks
//
//    public static <T> void checkUnique(Stream<T> stream, String format, BiFunction<Long, T, Object[]> getArgs)
//    {
//
//    }
//
//    public static void checkDuplicateIds(Stream<? extends LayoutComponent> layouts)
//    {
//    }
//
//    public static void checkComponents(String errorMessage, Collection<? extends Component> components, Predicate<Component> predicate)
//    {
//
//    }
//
//    public static void checkComponents(String errorMessage, Component[] components, Predicate<Component> predicate)
//    {
//
//    }
//
//    private static void handleComponent(Component component, Predicate<Component> predicate, StringBuilder sb, String path)
//    {
//    }
//
//    // Permission checks
//
//    public static void checkAccess(IPermissionHolder issuer, GuildChannel channel)
//    {
//
//    }
//
//    // Type checks
//
//    public static void checkSupportedChannelTypes(EnumSet<ChannelType> supported, ChannelType type, String what)
//    {
//
//    }
//}
