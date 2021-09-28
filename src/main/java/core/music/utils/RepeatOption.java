/*
 * MIT License
 *
 * Copyright (c) 2020 Melms Media LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 From Octave bot https://github.com/Stardust-Discord/Octave/ Modified for integrating with JAVA and the current bot
 */
package core.music.utils;

import core.otherlisteners.Reactions;

public enum RepeatOption {
    QUEUE("🔁"),
    SONG("🔂"),
    NONE(Reactions.REJECT);

    private final String emoji;

    RepeatOption(String s) {
        emoji = s;
    }

    public String getEmoji() {
        return emoji;
    }
}
