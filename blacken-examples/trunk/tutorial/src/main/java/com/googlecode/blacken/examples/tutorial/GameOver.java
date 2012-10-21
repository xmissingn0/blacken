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
import com.googlecode.blacken.grid.BoxRegion;
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.grid.Regionlike;
import com.googlecode.blacken.grid.Sizable;
import com.googlecode.blacken.terminal.BlackenEventType;
import com.googlecode.blacken.terminal.BlackenKeys;
import com.googlecode.blacken.terminal.BlackenModifier;
import com.googlecode.blacken.terminal.BlackenMouseEvent;
import com.googlecode.blacken.terminal.BlackenWindowEvent;
import com.googlecode.blacken.terminal.CellWalls;
import com.googlecode.blacken.terminal.TerminalCellTemplate;
import com.googlecode.blacken.terminal.TerminalInterface;
import com.googlecode.blacken.terminal.TerminalStyle;
import com.googlecode.blacken.terminal.TerminalView;
import com.googlecode.blacken.terminal.editing.Alignment;
import com.googlecode.blacken.terminal.editing.CodepointCallbackInterface;
import com.googlecode.blacken.terminal.editing.Images;
import com.googlecode.blacken.terminal.editing.SingleLine;
import com.googlecode.blacken.terminal.editing.StringViewer;
import com.googlecode.blacken.terminal.utils.TerminalUtils;
import com.googlecode.blacken.terminal.widgets.Box;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steven Black
 */
public class GameOver implements CodepointCallbackInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameOver.class);
    private final TerminalInterface term;
    private List<Integer> gradientNormalLostColors = new ArrayList<>();
    private List<Integer> gradientNormal = new ArrayList<>();
    private ViewerHelper vh;
    private List<Integer> gradientMessageLostColors = new ArrayList<>();
    private List<Integer> gradientMessage = new ArrayList<>();
    private List<Integer> allGradients = new ArrayList<>();
    private boolean complete;
    private List<String> gameTitles = new ArrayList<>();
    private Map<String, String> gameText = new HashMap<>();
    private TerminalView buttonView;
    private int currentMessage = 0;
    private EnumSet<BlackenModifier> lastModifiers = EnumSet.noneOf(BlackenModifier.class);
    private TerminalCellTemplate clearCellNormal;
    private TerminalCellTemplate clearCellMessage;
    private boolean ascension;
    private List<Integer> gradientNormalWonColors;
    private ArrayList<Integer> gradientMessageWonColors;
    private ColorPalette oldPalette;

    public GameOver(TerminalInterface term) {
        this.term = term;
        setup();
    }

    public void setAscension(boolean state) {
        this.ascension = state;
    }
    public boolean getAscension() {
        return ascension;
    }
    private void tweakPalettes() {
        if (oldPalette == null) {
            oldPalette = term.getPalette();
        }
        fixGradientColors();
        ColorPalette newPalette = new ColorPalette(oldPalette);
        int offset = newPalette.size();
        if (ascension) {
            newPalette.addAll(gradientNormalWonColors);
        } else {
            newPalette.addAll(gradientNormalLostColors);
        }
        gradientNormal.clear();
        if (gradientNormalLostColors.isEmpty()) {
            throw new RuntimeException("Need normal gradient colors defined.");
        }
        for (int i = 0; i < gradientNormalLostColors.size(); i++) {
            gradientNormal.add(i + offset);
        }
        offset = newPalette.size();
        if (ascension) {
            newPalette.addAll(gradientMessageWonColors);
        } else {
            newPalette.addAll(gradientMessageLostColors);
        }
        gradientMessage.clear();
        if (gradientMessageLostColors.isEmpty()) {
            throw new RuntimeException("Need message gradient colors defined.");
        }
        for (int i = 0; i < gradientMessageLostColors.size(); i++) {
            gradientMessage.add(i + offset);
        }
        this.allGradients.clear();
        this.allGradients.addAll(gradientNormal);
        this.allGradients.addAll(gradientMessage);
        term.setPalette(newPalette);
        enableGradient();
    }
    public void run() {
        this.complete = false;
        this.oldPalette = term.getPalette();
        EnumSet<BlackenEventType> oldNotices = term.getEventNotices();
        term.setEventNotices(EnumSet.of(BlackenEventType.MOUSE_CLICKED));
        tweakPalettes();
        switchMessage(currentMessage);
        redrawButtons();
        loop();
        term.setEventNotices(oldNotices);
        tweakPalettes();
        term.setPalette(oldPalette);
    }

    @Override
    public int handleCodepoint(int codepoint) {
        if (codepoint == BlackenKeys.NO_KEY) {
            lastModifiers = null;
            animate();
            return codepoint;
        }
        if (BlackenKeys.isModifier(codepoint)) {
            this.lastModifiers = BlackenModifier.getAsSet(codepoint);
            return codepoint;
        }
        switch (codepoint) {
            case BlackenKeys.KEY_LEFT:
            case BlackenKeys.KEY_KP_LEFT:
                this.switchMessage(currentMessage - 1);
                break;
            case BlackenKeys.KEY_RIGHT:
            case BlackenKeys.KEY_KP_RIGHT:
                this.switchMessage(currentMessage + 1);
                break;
        }
        lastModifiers = EnumSet.noneOf(BlackenModifier.class);
        return codepoint;
    }

    @Override
    public boolean handleMouseEvent(BlackenMouseEvent mouse) {
        this.complete = true;
        return true;
    }

    @Override
    public boolean handleWindowEvent(BlackenWindowEvent window) {
        return false;
    }

    private void reposition() {
        buttonView.setBounds(1, term.getWidth(), 0, 0);
    }

    private void setup() {
        vh = new ViewerHelper(term, null, null);
        vh.setSecondaryCallback(this);
        vh.setMenuSpace(true);
        Regionlike b = term.getBounds();
        b.setHeight(1);
        buttonView = new TerminalView(term, b);
    }
    private void fixGradientColors() {
        this.gradientNormalLostColors = null;
        this.gradientMessageLostColors = null;
        this.gradientNormalWonColors = null;
        this.gradientMessageWonColors = null;
        if (!complete) {
            this.gradientNormalLostColors = ColorHelper.createGradient(3, 0xffaa0800, 0xff880100);
            ColorHelper.extendGradient(gradientNormalLostColors, 6, 0xff330000);
            ColorHelper.extendGradient(gradientNormalLostColors, term.getHeight()+5, 0xff000000);
            this.gradientMessageLostColors = new ArrayList<>();
            for (Integer c : gradientNormalLostColors) {
                gradientMessageLostColors.add(ColorHelper.lerp(c, 0xFFcccccc, 0.3f));
            }
            // The "won" and "lost" colors need to be exactly the same length
            this.gradientNormalWonColors = new ArrayList<>(gradientMessageLostColors.size());
            for (int i = 0; i < gradientMessageLostColors.size() -1; i++) {
                this.gradientNormalWonColors.add(0xffFFFFFF);
            }
            this.gradientNormalWonColors.add(0xffFDD017);
            this.gradientMessageWonColors = new ArrayList<>();
            for (Integer c : gradientNormalWonColors) {
                gradientMessageWonColors.add(ColorHelper.lerp(c, 0xFFcccccc, 0.3f));
            }
        }
    }

    private void enableGradient() {
        DripTexture dripNormal = null;
        DripTexture dripMessage = null;
        if (!complete) {
            dripNormal = new DripTexture(gradientNormal);
            dripMessage = new DripTexture(gradientMessage);
        }
        clearCellNormal = new TerminalCellTemplate(
                dripNormal, "",
                ascension ? 0xff000000 : 0xffaaaaaa,
                ascension ? 0xFFffffff : 0xFF000000,
                EnumSet.noneOf(TerminalStyle.class),
                EnumSet.noneOf(CellWalls.class));
        clearCellMessage = new TerminalCellTemplate(
                dripMessage, "",
                ascension ? 0xff000000 : 0xffaaaaaa,
                ascension ? 0xFFffffff : 0xFF000000,
                EnumSet.noneOf(TerminalStyle.class),
                EnumSet.noneOf(CellWalls.class));
        vh.setColor(this.clearCellNormal);
        vh.setMessageColor(this.clearCellMessage);
    }

    private void loop() {
        vh.run();
    }

    private void animate() {
        if (!this.complete) {
            LOGGER.debug("Gradient size? {}", gradientNormal.size());
            // LOGGER.debug("Starting color: {}", term.getBackingTerminal().getPalette().get(gradientNormal.get(0)));
            term.getPalette().rotate(gradientNormal.get(0),
                                     gradientNormal.size(), ascension ? -1 : +1);
            term.getPalette().rotate(gradientMessage.get(0),
                                     gradientMessage.size(), ascension ? -1 : +1);
            Images.refreshForColors(allGradients, term.getBackingTerminal());
            term.doUpdate();
        }
    }

    @Override
    public void handleResizeEvent() {
        tweakPalettes();
        reposition();
        redrawButtons();
    }
    private void redrawButtons() {
        TerminalUtils.applyTemplate(buttonView, clearCellNormal);
        int textSize = (this.buttonView.getWidth() - 8) / 3;
        Box.box(this.buttonView, Box.BoxMethod.OUTSIDE_WALL,
                        new BoxRegion(1, textSize, buttonView.getY(),
                                      buttonView.getX() + textSize + 4));
        String s = "";
        if (gameText.size() > 3 && currentMessage > 2) {
            s = "<";
        }
        buttonView.set(buttonView.getY(), buttonView.getX(), s, null, null);
        buttonView.set(buttonView.getY(), buttonView.getX()+1, s, null, null);
        s = "";
        if (gameText.size() > 3 && currentMessage < gameText.size() - 2) {
            s = ">";
        }
        buttonView.set(buttonView.getY(),
                buttonView.getX()+buttonView.getWidth()-1, s, null, null);
        buttonView.set(buttonView.getY(),
                buttonView.getX()+buttonView.getWidth()-2, s, null, null);
        if (currentMessage > 0) {
            String opt = gameTitles.get(currentMessage - 1);
            if (opt.length() >= textSize) {
                opt = opt.substring(0, textSize);
            }
            SingleLine.putString(buttonView,
                    new Point(buttonView.getY(),
                              buttonView.getX() + 3 + textSize / 2),
                    null, opt, null, Alignment.CENTER);
        }
        if (currentMessage < gameTitles.size() - 1) {
            String opt = gameTitles.get(currentMessage + 1);
            if (opt.length() >= textSize) {
                opt = opt.substring(0, textSize);
            }
            SingleLine.putString(buttonView,
                    new Point(buttonView.getY(),
                              buttonView.getX() + textSize * 2
                              + 5 + textSize / 2),
                    null, opt, null, Alignment.CENTER);
        }
        String msg = "";
        if (currentMessage >= 0 && currentMessage < gameText.size()) {
            msg = gameTitles.get(currentMessage);
        }
        if (msg.length() >= textSize) {
            msg = msg.substring(0, textSize);
        }
        SingleLine.putString(buttonView,
                new Point(buttonView.getY(),
                          buttonView.getX() + textSize + 4 + textSize / 2),
                null, msg, null, Alignment.CENTER);
    }

    public void switchMessage(int index) {
        if (index < 0) {
            index = gameTitles.size() -1;
        }

        if (index >= gameTitles.size()) {
            index = 0;
        }
        this.currentMessage = index;
        redrawButtons();
        String message = gameText.get(gameTitles.get(index));
        vh.setMessage(message);
    }

    void addText(String title, String text) {
        this.gameTitles.add(title);
        this.gameText.put(title, text);
    }

}
