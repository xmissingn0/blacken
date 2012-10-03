/* blacken - a library for Roguelike games
 * Copyright © 2012 Steven Black <yam655@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.googlecode.blacken.examples.tutorial;

import com.googlecode.blacken.colors.ColorHelper;
import com.googlecode.blacken.colors.ColorPalette;
import com.googlecode.blacken.exceptions.InvalidStringFormatException;
import com.googlecode.blacken.grid.Grid;
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.grid.Sizable;
import com.googlecode.blacken.resources.ResourceMissingException;
import com.googlecode.blacken.terminal.BlackenEventType;
import com.googlecode.blacken.terminal.BlackenImageLoader;
import com.googlecode.blacken.terminal.BlackenKeys;
import com.googlecode.blacken.terminal.BlackenMouseEvent;
import com.googlecode.blacken.terminal.BlackenWindowEvent;
import com.googlecode.blacken.terminal.TerminalCellTemplate;
import com.googlecode.blacken.terminal.TerminalInterface;
import com.googlecode.blacken.terminal.editing.Alignment;
import com.googlecode.blacken.terminal.editing.BreakableLoop;
import com.googlecode.blacken.terminal.editing.CodepointCallbackInterface;
import com.googlecode.blacken.terminal.editing.Images;
import com.googlecode.blacken.terminal.editing.SingleLine;
import com.googlecode.blacken.terminal.editing.Steppable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 *
 * @author Steven Black
 */
public class SplashScreen implements Steppable, CodepointCallbackInterface {
    private final TerminalInterface term;
    private List<Integer> gradientColors;
    private List<Integer> gradient = new ArrayList<>();
    private Grid<Integer> image;
    private Grid<Integer> imageColors;
    private Grid<Integer> orcImage;
    private Sizable base;
    private boolean quit;
    private int startIndex;
    private int modifier = BlackenKeys.NO_KEY;

    public SplashScreen(TerminalInterface term, Sizable base) {
        this.base = base;
        this.term = term;
        setup();
    }
    public void run() {
        this.quit = false;
        EnumSet<BlackenEventType> oldNotices = term.getEventNotices();
        term.setEventNotices(EnumSet.of(BlackenEventType.MOUSE_CLICKED));
        ColorPalette oldPalette = term.getPalette();
        ColorPalette newPalette = new ColorPalette(oldPalette);
        startIndex = newPalette.size();
        newPalette.addAll(gradientColors);
        term.setPalette(newPalette);
        gradient.clear();
        for (int i = 0; i < gradientColors.size(); i++) {
            gradient.add(i + startIndex);
        }
        redisplay();
        loop();
        term.setEventNotices(oldNotices);
        term.setPalette(oldPalette);
    }

    @Override
    public int handleCodepoint(int codepoint) {
        if (BlackenKeys.isModifier(codepoint)) {
            this.modifier = codepoint;
            return codepoint;
        }
        switch(codepoint) {
        case BlackenKeys.NO_KEY:
        case BlackenKeys.RESIZE_EVENT:
            // should be safe
            break;
        case 'l':
        case 'L':
            HelpSystem.myLicense(term);
            break;
        case 'n':
        case 'N':
            HelpSystem.legalNotices(term);
            break;
        case 'f':
        case 'F':
            HelpSystem.fontLicense(term);
            break;
        case '?':
            HelpSystem.help(term);
            break;
        default:
            this.quit = true;
            codepoint = BlackenKeys.CMD_END_LOOP;
            break;
        }
        this.modifier = BlackenKeys.NO_KEY;
        return codepoint;
    }

    @Override
    public boolean handleMouseEvent(BlackenMouseEvent mouse) {
        this.quit = true;
        return true;
    }

    @Override
    public boolean handleWindowEvent(BlackenWindowEvent window) {
        return false;
    }

    @Override
    public void handleResizeEvent() {
        this.redisplay();
    }

    private void setup() {
        /*
        try {
            // gradient = ColorHelper.createGradient(null, 12, ColorHelper.lookup(null, "#271f0f", "#863"));
        } catch (InvalidStringFormatException ex) {
            throw new RuntimeException(ex);
        }
        */
        this.gradientColors = ColorHelper.createGradient(null, 12,
                ColorHelper.lookup(null, 0xfff0f0e0, 0xffffffef));
        BlackenImageLoader imageLoader = term.getImageLoader();
        try {
            this.image = imageLoader.loadImage(this.getClass(), "Splash.txt");
        } catch (ResourceMissingException ex) {
            throw new RuntimeException(ex);
        }
        try {
            this.imageColors = imageLoader.loadImage(this.getClass(), "SplashColor.txt");
        } catch (ResourceMissingException ex) {
            throw new RuntimeException(ex);
        }
        try {
            this.orcImage = imageLoader.loadImage(this.getClass(), "orc_priest.bmp");
        } catch (ResourceMissingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void redisplay() {
        SingleLine.applyTemplate(term, -1, -1, 0, 0,
                new TerminalCellTemplate(new WoodGrain(gradient), " ", 
                0xFF000000, null));
        Images.imageToSequence(term, 0,
                (term.getWidth() - imageColors.getWidth()) / 2, image, null);
        Images.imageToBackground(term, 0,
                (term.getWidth() - imageColors.getWidth()) / 2, imageColors, 0);
        Images.imageToBackground(term, imageColors.getHeight()-1,
                (orcImage.getWidth() / -4) 
                + (term.getWidth() - base.getWidth()) / 2, orcImage, 0);
        TerminalCellTemplate template = new TerminalCellTemplate(null, 0xFF000000, null);
        SingleLine.putString(term, 
                new Point(image.getHeight() - 1, term.getWidth()/2),
                null, "Copyright (C) 2012 Steven Black", template,
                Alignment.CENTER);
        int postOrc = orcImage.getWidth()
                + ((orcImage.getWidth() / -4)
                    + (term.getWidth() - base.getWidth()) / 2);
        int postOrcSize = term.getWidth() - postOrc;
        Point last = new Point(term.getHeight() - 2, postOrc + postOrcSize / 2);
        SingleLine.putString(term, last, null,
                "Press '?' for Help.", template, Alignment.CENTER);
        last.setY(last.getY() + 1);
        SingleLine.putString(term, last, null,
                "Press any other key to continue.", template, Alignment.CENTER);
    }

    private void loop() {
        BreakableLoop looper = new BreakableLoop(term,
                BreakableLoop.DEFAULT_FREQUENCY_MILLIS, this, this);
        looper.run();
    }

    @Override
    public int getStepCount() {
        return -1;
    }

    @Override
    public int getCurrentStep() {
        return -1;
    }

    @Override
    public void step() {
        term.getPalette().rotate(startIndex, gradient.size(), +1);
        Images.refreshForColors(gradient, term);
        term.doUpdate();
    }

    @Override
    public boolean isComplete() {
        return this.quit;
    }

}
