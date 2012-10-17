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
    private List<Integer> gradientNormalColors = new ArrayList<>();
    private List<Integer> gradientNormal = new ArrayList<>();
    private ViewerHelper vh;
    private List<Integer> gradientMessageColors = new ArrayList<>();
    private List<Integer> gradientMessage = new ArrayList<>();
    private List<Integer> allGradients = new ArrayList<>();
    private Sizable base;
    private boolean complete;
    private int startIndex;
    private List<String> gameTitles = new ArrayList<>();
    private Map<String, String> gameText = new HashMap<>();
    private int maxLength;
    private TerminalView messageView;
    private TerminalView buttonView;
    private int currentMessage = 0;
    private StringViewer messageViewer;
    private EnumSet<BlackenModifier> lastModifiers = EnumSet.noneOf(BlackenModifier.class);
    private TerminalCellTemplate clearCellNormal;
    private TerminalCellTemplate clearCellMessage;

    public GameOver(TerminalInterface term, Sizable base) {
        this.base = base;
        this.term = term;
        setup();
    }
    private ColorPalette switchPalettes() {
        ColorPalette newPalette = new ColorPalette(term.getPalette());
        startIndex = newPalette.size();
        int offset = newPalette.size();
        newPalette.addAll(gradientNormalColors);
        gradientNormal.clear();
        if (gradientNormalColors.isEmpty()) {
            throw new RuntimeException("Need normal gradient colors defined.");
        }
        for (int i = 0; i < gradientNormalColors.size(); i++) {
            gradientNormal.add(i + offset);
        }
        offset = newPalette.size();
        newPalette.addAll(gradientMessageColors);
        gradientMessage.clear();
        if (gradientMessageColors.isEmpty()) {
            throw new RuntimeException("Need message gradient colors defined.");
        }
        for (int i = 0; i < gradientMessageColors.size(); i++) {
            gradientMessage.add(i + offset);
        }
        this.allGradients.clear();
        this.allGradients.addAll(gradientNormal);
        this.allGradients.addAll(gradientMessage);
        return term.setPalette(newPalette);
    }
    public void run() {
        this.complete = false;
        EnumSet<BlackenEventType> oldNotices = term.getEventNotices();
        term.setEventNotices(EnumSet.of(BlackenEventType.MOUSE_CLICKED));
        ColorPalette oldPalette = switchPalettes();
        enableGradient(!complete);
        switchMessage(currentMessage);
        redrawButtons();
        loop();
        term.setEventNotices(oldNotices);
        enableGradient(!complete);
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
        this.gradientNormalColors = ColorHelper.createGradient(3, 0xffaa0800, 0xff880100);
        ColorHelper.extendGradient(gradientNormalColors, 6, 0xff330000);
        ColorHelper.extendGradient(gradientNormalColors, 30, 0xff000000);
        this.gradientMessageColors = new ArrayList<>();
        for (Integer c : gradientNormalColors) {
            gradientMessageColors.add(ColorHelper.lerp(c, 0xFFcccccc, 0.3f));
        }
        vh = new ViewerHelper(term, null, null);
        vh.setSecondaryCallback(this);
        vh.setMenuSpace(true);
        Regionlike b = term.getBounds();
        b.setHeight(1);
        buttonView = new TerminalView(term, b);
    }

    private void enableGradient(boolean state) {
        DripTexture grainNormal = null;
        DripTexture grainMessage = null;
        //LOGGER.debug("gradientNormal:{}", gradientNormal);
        //LOGGER.debug("gradientMessage:{}", gradientMessage);
        if (state) {
            grainNormal = new DripTexture(gradientNormal);
            grainMessage = new DripTexture(gradientMessage);
        }
        clearCellNormal = new TerminalCellTemplate(
                grainNormal, "", 0xffaaaaaa, 0xFF000000,
                EnumSet.noneOf(TerminalStyle.class), EnumSet.noneOf(CellWalls.class));
        clearCellMessage = new TerminalCellTemplate(
                grainMessage, "", 0xffaaaaaa, 0xFF000000,
                EnumSet.noneOf(TerminalStyle.class), EnumSet.noneOf(CellWalls.class));
        vh.setColor(this.clearCellNormal);
        vh.setMessageColor(this.clearCellMessage);
    }

    private void loop() {
        vh.run();
    }

    private void animate() {
        if (!this.complete) {
            LOGGER.debug("Starting color: {}", term.getBackingTerminal().getPalette().get(gradientNormal.get(0)));
            term.getPalette().rotate(gradientNormal.get(0),
                                     gradientNormal.size(), +1);
            term.getPalette().rotate(gradientMessage.get(0),
                                     gradientMessage.size(), +1);
            Images.refreshForColors(allGradients, term.getBackingTerminal());
        }
        term.doUpdate();
    }

    @Override
    public void handleResizeEvent() {
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
