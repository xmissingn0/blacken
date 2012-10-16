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

import com.googlecode.blacken.colors.ColorPalette;
import com.googlecode.blacken.grid.BoxRegion;
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.grid.Regionlike;
import com.googlecode.blacken.terminal.BlackenKeys;
import com.googlecode.blacken.terminal.BlackenModifier;
import com.googlecode.blacken.terminal.BlackenMouseEvent;
import com.googlecode.blacken.terminal.BlackenWindowEvent;
import com.googlecode.blacken.terminal.TerminalCellLike;
import com.googlecode.blacken.terminal.TerminalCellTemplate;
import com.googlecode.blacken.terminal.TerminalView;
import com.googlecode.blacken.terminal.TerminalViewInterface;
import com.googlecode.blacken.terminal.editing.Alignment;
import com.googlecode.blacken.terminal.editing.BreakableLoop;
import com.googlecode.blacken.terminal.editing.CodepointCallbackInterface;
import com.googlecode.blacken.terminal.editing.SingleLine;
import com.googlecode.blacken.terminal.editing.Steppable;
import com.googlecode.blacken.terminal.editing.StringViewer;
import com.googlecode.blacken.terminal.utils.TerminalUtils;
import com.googlecode.blacken.terminal.widgets.Box;
import java.util.EnumSet;
import javax.swing.GroupLayout;

/**
 *
 * @author Steven Black
 */
public class ConfirmationDialog implements Steppable, CodepointCallbackInterface {
    private String[] options;
    private String[] message;
    private CodepointCallbackInterface backer;
    private int maxLength;
    private TerminalView view;
    private TerminalView messageView;
    private TerminalViewInterface term;
    private TerminalView buttonView;
    private TerminalCellTemplate template;
    private int currentOption = -1;
    private StringViewer messageViewer;
    private boolean complete;
    private int lastModifier = BlackenKeys.NO_KEY;
    private Integer cancelIndex = null;
    private boolean squelchBox;

    ConfirmationDialog(TerminalViewInterface term, 
            CodepointCallbackInterface backer, String message, 
            String... options) {
        internalSetAll(term, backer, message, options,
                0xFFaaaaaa, 0xFF000000);
    }
    private void internalSetAll(TerminalViewInterface term,
            CodepointCallbackInterface backer, String message,
            String[] options, TerminalCellTemplate template) {
        this.options = options;
        this.backer = backer;
        this.term = term;
        this.view = new TerminalView(term);
        this.messageView = new TerminalView(term);
        this.buttonView = new TerminalView(term);
        this.message = message.split("(\n|\r|\r\n)");
        this.maxLength = 0;
        for (String m : this.message) {
            if (m.length() > maxLength) {
                maxLength = m.length();
            }
        }
        if (template.isCellWallsUnset()) {
            template.clearCellWalls();
        }
        if (template.isStyleUnset()) {
            template.clearStyle();
        }
        if (options.length > 0) {
            this.currentOption = 0;
        }
        this.messageViewer = new StringViewer(messageView, message);
        reposition();
    }
    private void internalSetAll(TerminalViewInterface term,
            CodepointCallbackInterface backer, String message,
            String[] options, int foreground, int background) {
        template = new TerminalCellTemplate();
        template.setBackground(background);
        template.setForeground(foreground);
        internalSetAll(term, backer, message, options, template);
    }

    public static ConfirmationDialog YesNoDialog(TerminalViewInterface term, String message) {
        return new ConfirmationDialog(term, null, message, "Yes", "No");
    }

    public static ConfirmationDialog YesNoCancelDialog(TerminalViewInterface term, String message) {
        return new ConfirmationDialog(term, null, message, "Yes", "No", "Cancel");
    }

    public int getCurrentOption() {
        return currentOption;
    }
    public String getCurrentOptionText() {
        if (currentOption < 0 || currentOption >= options.length) {
            return null;
        }
        return this.options[currentOption];
    }

    /**
     * Set the currently selected option.
     * @param curOption option to select
     * @return true if selected; false option does not exist.
     */
    public boolean setCurrentOption(int curOption) {
        this.complete = false;
        if (curOption < this.options.length) {
            this.currentOption = curOption;
            this.redrawButtons();
            return true;
        }
        return false;
    }

    public void setCurrentOption(String curOption) {
        if (curOption == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i < this.options.length; i++) {
            if (curOption.equals(options[i])) {
                setCurrentOption(i);
                return;
            }
        }
        throw new IllegalArgumentException("Unable to find argument");
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
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public int handleCodepoint(int codepoint) {
        if (BlackenKeys.isModifier(codepoint)) {
            this.lastModifier = codepoint;
            return codepoint;
        }
        EnumSet<BlackenModifier> modStates;
        modStates = BlackenModifier.getAsSet(lastModifier);
        int modifierSelection = 0;
        if (modStates.contains(BlackenModifier.MODIFIER_KEY_SHIFT)) {
            modifierSelection += 10;
        }
        if (modStates.contains(BlackenModifier.MODIFIER_KEY_CTRL)) {
            modifierSelection += 20;
        }
        if (modStates.contains(BlackenModifier.MODIFIER_KEY_ALT)) {
            modifierSelection += 40;
        }
        switch (codepoint) {
        case BlackenKeys.KEY_ESCAPE:
        case BlackenKeys.KEY_CANCEL:
            if (this.cancelIndex != null) {
                this.currentOption = this.cancelIndex;
                this.complete = true;
                return BlackenKeys.CMD_END_LOOP;
            }
            break;
        case BlackenKeys.KEY_NP_1:
        case '1':
            this.setCurrentOption(0 + modifierSelection);
            break;
        case BlackenKeys.KEY_NP_2:
        case '2':
            this.setCurrentOption(1 + modifierSelection);
            break;
        case BlackenKeys.KEY_NP_3:
        case '3':
            this.setCurrentOption(2 + modifierSelection);
            break;
        case BlackenKeys.KEY_NP_4:
        case '4':
            this.setCurrentOption(3 + modifierSelection);
            break;
        case BlackenKeys.KEY_NP_5:
        case '5':
            this.setCurrentOption(4 + modifierSelection);
            break;
        case BlackenKeys.KEY_NP_6:
        case '6':
            this.setCurrentOption(5 + modifierSelection);
            break;
        case BlackenKeys.KEY_NP_7:
        case '7':
            this.setCurrentOption(6 + modifierSelection);
            break;
        case BlackenKeys.KEY_NP_8:
        case '8':
            this.setCurrentOption(7 + modifierSelection);
            break;
        case BlackenKeys.KEY_NP_9:
        case '9':
            this.setCurrentOption(8 + modifierSelection);
            break;
        case BlackenKeys.KEY_NP_0:
        case '0':
            this.setCurrentOption(9 + modifierSelection);
            break;
        case BlackenKeys.KEY_LEFT:
        case BlackenKeys.KEY_KP_LEFT:
            if (currentOption > 0) {
                this.setCurrentOption(currentOption - 1);
            } else {
                this.setCurrentOption(options.length -1);
            }
            break;
        case BlackenKeys.KEY_RIGHT:
        case BlackenKeys.KEY_KP_RIGHT:
            if (currentOption < options.length - 1) {
                this.setCurrentOption(currentOption + 1);
            } else {
                this.setCurrentOption(0);
            }
            break;
        case BlackenKeys.KEY_ENTER:
        case BlackenKeys.KEY_NP_ENTER:
            this.complete = true;
            codepoint = BlackenKeys.CMD_END_LOOP;
            break;
        default:
            lastModifier = BlackenKeys.NO_KEY;
            return codepoint;
        }
        lastModifier = BlackenKeys.NO_KEY;
        return BlackenKeys.NO_KEY;
    }

    public void clearButtonBoxes() {
        int textSize = (this.buttonView.getWidth() - 8) / 3;
        Box.box(this.buttonView, Box.BoxMethod.REMOVE_WALL,
                new BoxRegion(1, 2, buttonView.getY(), buttonView.getX()));
        Box.box(this.buttonView, Box.BoxMethod.REMOVE_WALL,
                new BoxRegion(1, 2, buttonView.getY(),
                            buttonView.getX() + buttonView.getWidth() - 3));
        Box.box(this.buttonView, Box.BoxMethod.REMOVE_WALL,
                new BoxRegion(1, textSize, buttonView.getY(),
                            buttonView.getX() + 3));
        Box.box(this.buttonView, Box.BoxMethod.REMOVE_WALL,
                new BoxRegion(1, textSize, buttonView.getY(),
                            buttonView.getX() + textSize + 4));
        Box.box(this.buttonView, Box.BoxMethod.REMOVE_WALL,
                new BoxRegion(1, textSize, buttonView.getY(),
                            buttonView.getX() + textSize * 2 + 5));
    }

    @Override
    public boolean handleMouseEvent(BlackenMouseEvent mouse) {
        if (this.buttonView.getBounds().contains(mouse.getPosition())) {
            clearButtonBoxes();
            int textSize = (this.buttonView.getWidth() - 8) / 3;
            Box.box(this.buttonView, Box.BoxMethod.REMOVE_WALL,
                    new BoxRegion(1, textSize, buttonView.getY(),
                            buttonView.getX() + textSize + 4));
        }
        return false;
    }

    @Override
    public boolean handleWindowEvent(BlackenWindowEvent window) {
        return false;
    }

    private void redraw() {
        TerminalCellTemplate clearTemplate = new TerminalCellTemplate(template);
        view.clear(clearTemplate.makeSafe(null));
        if (!squelchBox) {
            Box.box(view, Box.BoxMethod.INSIDE_WALL, BoxRegion.inset(view.getBounds(), 0, 1));
        }
        this.messageViewer.setColor(template);
        this.messageViewer.step();
        this.redrawButtons();
    }

    private void reposition() {
        int ys = this.message.length + 5;
        int yo = term.getHeight() - ys;
        int xs = maxLength + 6;
        int xo = term.getWidth() - xs;
        if (this.message.length + 5 >= term.getHeight()) {
            this.squelchBox = true;
            if (this.maxLength + 6 >= term.getWidth()) {
                this.view.setBounds(term.getBounds());
            } else {
                this.view.setBounds(term.getHeight(), xs, 0, xo / 2);
            }
        } else {
            if (this.maxLength + 6 >= term.getWidth()) {
                this.squelchBox = true;
                this.view.setBounds(ys, term.getWidth(), yo / 2, 0);
            } else {
                this.view.setBounds(ys, xs, yo / 2, xo / 2);
                this.squelchBox = false;
            }
        }
        Regionlike b = this.view.getBounds();
        b.setWidth(b.getWidth() - 6);
        b.setX(b.getX() + 3);
        b.setHeight(b.getHeight() - 5);
        b.setY(b.getY() + 1);
        this.messageView.setBounds(b);
        b.setY(b.getY() + b.getHeight() + 1);
        b.setHeight(1);
        this.buttonView.setBounds(b);
    }
    public void setColor(TerminalCellTemplate template) {
        if (template == null) {
            throw new NullPointerException("template cannot be null");
        }
        template = new TerminalCellTemplate(template);
        this.template = template;
        if (template.isCellWallsUnset()) {
            template.clearCellWalls();
        }
        if (template.isStyleUnset()) {
            template.clearStyle();
        }
    }
    public void setColor(int foreground, int background) {
        template = new TerminalCellTemplate();
        template.setBackground(background);
        template.setForeground(foreground);
        template.clearCellWalls();
        template.clearStyle();
    }
    public void setColor(String foreground, String background) {
        ColorPalette palette = term.getBackingTerminal().getPalette();
        int fg = palette.getColorOrIndex(foreground);
        int bg = palette.getColorOrIndex(background);
        setColor(fg, bg);
    }

    private void redrawButtons() {
        TerminalUtils.applyTemplate(this.buttonView, template);
        buttonView.clear(template.makeSafe(null));
        int textSize = (this.buttonView.getWidth() - 8) / 3;
        Box.box(this.buttonView, Box.BoxMethod.OUTSIDE_WALL,
                        new BoxRegion(1, textSize, buttonView.getY(),
                                      buttonView.getX() + textSize + 4));
        String s = "";
        if (options.length > 3 && currentOption > 2) {
            s = "<";
        }
        buttonView.set(buttonView.getY(), buttonView.getX(), s, null, null);
        buttonView.set(buttonView.getY(), buttonView.getX()+1, s, null, null);
        s = "";
        if (options.length > 3 && currentOption < options.length - 2) {
            s = ">";
        }
        buttonView.set(buttonView.getY(),
                buttonView.getX()+buttonView.getWidth()-1, s, null, null);
        buttonView.set(buttonView.getY(),
                buttonView.getX()+buttonView.getWidth()-2, s, null, null);
        if (currentOption > 0) {
            String opt = options[currentOption - 1];
            if (opt.length() >= textSize) {
                opt = opt.substring(0, textSize);
            }
            SingleLine.putString(buttonView,
                    new Point(buttonView.getY(), 
                              buttonView.getX() + 3 + textSize / 2),
                    null, opt, null, Alignment.CENTER);
        }
        if (currentOption < options.length - 1) {
            String opt = options[currentOption + 1];
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
        if (currentOption >= 0 && currentOption < options.length) {
            msg = options[currentOption];
        }
        if (msg.length() >= textSize) {
            msg = msg.substring(0, textSize);
        }
        SingleLine.putString(buttonView,
                new Point(buttonView.getY(), 
                          buttonView.getX() + textSize + 4 + textSize / 2),
                null, msg, null, Alignment.CENTER);
    }

    @Override
    public void handleResizeEvent() {
        reposition();
        if (this.backer != null) {
            backer.handleResizeEvent();
        } else {
            this.term.clear();
        }
        redraw();
    }

    public void run() {
        this.complete = false;
        redraw();
        BreakableLoop looper = new BreakableLoop(term,
                BreakableLoop.DEFAULT_FREQUENCY_MILLIS, this, this);
        looper.run();
    }

}
