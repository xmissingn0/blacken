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
import com.googlecode.blacken.resources.ResourceIdentifier;
import com.googlecode.blacken.resources.ResourceMissingException;
import com.googlecode.blacken.resources.ResourceUtils;
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
import com.googlecode.blacken.terminal.TerminalViewInterface;
import com.googlecode.blacken.terminal.editing.Alignment;
import com.googlecode.blacken.terminal.editing.CodepointCallbackInterface;
import com.googlecode.blacken.terminal.editing.SingleLine;
import com.googlecode.blacken.terminal.editing.StringViewer;
import com.googlecode.blacken.terminal.utils.TerminalUtils;
import com.googlecode.blacken.terminal.widgets.Box;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author yam655
 */
public class MultiPick implements CodepointCallbackInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPick.class);
    private TerminalInterface term;
    private List<Integer> gradientNormalColors = new ArrayList<>();
    private List<Integer> gradientNormal = new ArrayList<>();
    private List<Integer> gradientMessageColors = new ArrayList<>();
    private List<Integer> gradientMessage = new ArrayList<>();
    private List<Integer> allGradients = new ArrayList<>();
    private boolean complete;
    private List<String> choiceTitles = new ArrayList<>();
    private Map<String, String> choiceText = new HashMap<>();
    private TerminalView selectorView;
    private int currentOption = 0;
    private EnumSet<BlackenModifier> lastModifiers = EnumSet.noneOf(BlackenModifier.class);
    private TerminalCellTemplate clearCellNormal;
    private TerminalCellTemplate clearCellMessage;
    private ColorPalette oldPalette;
    private boolean staticOnTop = true;
    private TerminalViewInterface topView = null;
    private StringViewer topViewer = null;
    private TerminalViewInterface bottomView = null;
    private StringViewer bottomViewer = null;
    private String title = null;
    private final String helpMessageDefault =
            "Q / q : quit; PageUp / PageDown : Next or previous page;\n"
            + "left/right select choice; Enter to pick";
    private String bottomMessage = helpMessageDefault;
    private TerminalCellTemplate template = null;
    private TerminalCellTemplate messageTemplate = null;
    private String topMessage = null;
    private CodepointCallbackInterface secondaryCallback = null;
    private boolean useDefaultTemplate = true;
    private Integer cancelIndex = null;

    public MultiPick(TerminalInterface term, String title) {
        this.term = term;
        internalSetup(term, title);
    }

    private void tweakPalettes() {
        if (oldPalette == null) {
            oldPalette = term.getPalette();
        }
        fixGradientColors();
        gradientNormal.clear();
        gradientMessage.clear();
        if (!complete) {
            ColorPalette newPalette = new ColorPalette(oldPalette);
            int offset = newPalette.size();
            newPalette.addAll(gradientNormalColors);
            if (gradientNormalColors.isEmpty()) {
                throw new RuntimeException("Need normal gradient colors defined.");
            }
            for (int i = 0; i < gradientNormalColors.size(); i++) {
                gradientNormal.add(i + offset);
            }
            offset = newPalette.size();
            newPalette.addAll(gradientMessageColors);
            if (gradientMessageColors.isEmpty()) {
                throw new RuntimeException("Need message gradient colors defined.");
            }
            for (int i = 0; i < gradientMessageColors.size(); i++) {
                gradientMessage.add(i + offset);
            }
            this.allGradients.clear();
            this.allGradients.addAll(gradientNormal);
            this.allGradients.addAll(gradientMessage);
            term.setPalette(newPalette);
        }
        enableGradient();
    }
    public void run() {
        if (this.term == null) {
            throw new NullPointerException("Cannot run with a null terminal");
        }
        this.complete = false;
        this.oldPalette = term.getPalette();
        EnumSet<BlackenEventType> oldNotices = term.getEventNotices();
        term.setEventNotices(EnumSet.of(BlackenEventType.MOUSE_CLICKED, BlackenEventType.MOUSE_WHEEL));
        tweakPalettes();
        switchMessage(currentOption);
        topViewer.setColor(messageTemplate);
        bottomViewer.setColor(messageTemplate);
        redraw();
        topViewer.run();
        term.setEventNotices(oldNotices);
        tweakPalettes();
        term.setPalette(oldPalette);
    }

    @Override
    public int handleCodepoint(int codepoint) {
        if (this.secondaryCallback != null) {
            codepoint = secondaryCallback.handleCodepoint(codepoint);
        }
        if (codepoint == BlackenKeys.NO_KEY) {
            lastModifiers = EnumSet.noneOf(BlackenModifier.class);
            return codepoint;
        }
        if (BlackenKeys.isModifier(codepoint)) {
            this.lastModifiers = BlackenModifier.getAsSet(codepoint);
            return codepoint;
        }
        int modifierSelection = 0;
        if (lastModifiers.contains(BlackenModifier.MODIFIER_KEY_SHIFT)) {
            modifierSelection += 10;
        }
        if (lastModifiers.contains(BlackenModifier.MODIFIER_KEY_CTRL)) {
            modifierSelection += 20;
        }
        if (lastModifiers.contains(BlackenModifier.MODIFIER_KEY_ALT)) {
            modifierSelection += 40;
        }
        switch (codepoint) {
            case BlackenKeys.KEY_ESCAPE:
            case BlackenKeys.KEY_CANCEL:
            case 'q':
            case 'Q':
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
                this.switchMessage(currentOption - 1);
                break;
            case BlackenKeys.KEY_RIGHT:
            case BlackenKeys.KEY_KP_RIGHT:
                this.switchMessage(currentOption + 1);
                break;
            case 'l':
            case 'L':
                if (lastModifiers.contains(BlackenModifier.MODIFIER_KEY_CTRL)) {
                    redraw();
                }
                break;
            case BlackenKeys.KEY_ENTER:
            case BlackenKeys.KEY_NP_ENTER:
                ConfirmationDialog dialog = new ConfirmationDialog(
                        term.getBackingTerminal(), this,
                        String.format("Do you want to select \"%s\"?",
                        this.getCurrentOptionText()), "Yes", "No");
                dialog.setColor(this.template);
                dialog.run();
                codepoint = BlackenKeys.NO_KEY;
                if (Objects.equals(dialog.getCurrentOptionText(),"Yes")) {
                    this.complete = true;
                    codepoint = BlackenKeys.CMD_END_LOOP;
                }
                redraw();
                break;
        }
        lastModifiers = EnumSet.noneOf(BlackenModifier.class);
        return codepoint;
    }

    @Override
    public boolean handleMouseEvent(BlackenMouseEvent mouse) {
        boolean ret = false;
        if (this.secondaryCallback != null) {
            ret = secondaryCallback.handleMouseEvent(mouse);
        }
        return ret;
    }

    @Override
    public boolean handleWindowEvent(BlackenWindowEvent window) {
        boolean ret = false;
        if (this.secondaryCallback != null) {
            ret = secondaryCallback.handleWindowEvent(window);
        }
        return ret;
    }

    private void fixGradientColors() {
        this.gradientNormalColors = null;
        this.gradientMessageColors = null;
        if (!complete) {
            gradientNormalColors = ColorHelper.createGradient(12, 0xFF271f0f, 0xFF886633);
            this.gradientMessageColors = new ArrayList<>();
            for (Integer c : gradientNormalColors) {
                gradientMessageColors.add(ColorHelper.lerp(c, 0xFF333333, 0.4f));
            }
        }
    }

    private void enableGradient() {
        WoodGrain dripNormal = null;
        WoodGrain dripMessage = null;
        if (!complete) {
            dripNormal = new WoodGrain(gradientNormal);
            dripMessage = new WoodGrain(gradientMessage);
        }
        clearCellNormal = new TerminalCellTemplate(
                dripNormal, "",
                0xFFe0e0e0,
                0xFF000000,
                EnumSet.noneOf(TerminalStyle.class),
                EnumSet.noneOf(CellWalls.class));
        clearCellMessage = new TerminalCellTemplate(
                dripMessage, "",
                0xFFe0e0e0,
                0xFF000000,
                EnumSet.noneOf(TerminalStyle.class),
                EnumSet.noneOf(CellWalls.class));
        setColor(this.clearCellNormal);
        setMessageColor(this.clearCellMessage);
    }

    @Override
    public void handleResizeEvent() {
        if (this.secondaryCallback != null) {
            secondaryCallback.handleResizeEvent();
        }
        tweakPalettes();
        redraw();
    }

    public void setStaticMessage(String message) {
        if (staticOnTop) {
            setTopMessage(message);
        } else {
            setBottomMessage(message);
        }
        redraw();
    }

    public void switchMessage(int index) {
        this.complete = false;
        if (index < 0) {
            index = choiceTitles.size() -1;
        }

        if (index >= choiceTitles.size()) {
            index = 0;
        }
        this.currentOption = index;
        String message = choiceText.get(choiceTitles.get(index));
        if (!staticOnTop) {
            setTopMessage(message);
        } else {
            setBottomMessage(message);
        }
        redraw();
    }

    void addChoice(String title, String text) {
        this.choiceTitles.add(title);
        this.choiceText.put(title, text);
    }

    void addChoice(String title, ResourceIdentifier resid) throws ResourceMissingException {
        this.choiceTitles.add(title);
        String text = ResourceUtils.getResourceAsString(resid);
        this.choiceText.put(title, text);
    }

    public boolean isStaticOnTop() {
        return staticOnTop;
    }

    public void setStaticOnTop(boolean staticOnTop) {
        this.staticOnTop = staticOnTop;
    }

    /**
     * Set the currently selected option.
     * @param curOption option to select
     * @return true if selected; false option does not exist.
     */
    public void setCurrentOption(int curOption) {
        this.switchMessage(curOption);
    }

    public void setCurrentOption(String curOption) {
        if (curOption == null) {
            throw new NullPointerException();
        }
        int i = choiceTitles.indexOf(curOption);
        if (i < 0) {
            throw new IllegalArgumentException("Unable to find argument");
        }
        setCurrentOption(i);
    }

    public int getCurrentOption() {
        return currentOption;
    }
    public String getCurrentOptionText() {
        if (currentOption < 0 || currentOption >= this.choiceTitles.size()) {
            return null;
        }
        return this.choiceTitles.get(currentOption);
    }


    public void setup(TerminalInterface term, String title) {
        internalSetup(term, title);
    }

    private void internalSetup(TerminalInterface term, String title) {
        this.title = title;
        if (term != null) {
            internalSetTerm(term);
            internalDefaultTemplate();
        }
        this.topMessage = "";
    }

    private void internalDefaultTemplate() {
        int background = term.getEmpty().getBackground();
        int foreground = ColorHelper.makeVisible(background);
        template = new TerminalCellTemplate();
        template.setBackground(background);
        template.setForeground(foreground);
        template.clearCellWalls();
        template.clearStyle();
    }
    private void internalSetTerm(TerminalInterface term) {
        if (term == null) {
            this.term = null;
            this.topView = null;
            this.topViewer = null;
            this.bottomView = null;
            this.bottomViewer = null;
            if (useDefaultTemplate) {
                this.template = null;
            }
            return;
        }
        this.term = term;
        if (useDefaultTemplate) {
            internalDefaultTemplate();
        }
        topView = new TerminalView(term);
        if (this.topMessage == null) {
            this.topMessage = "";
        }
        topViewer = new StringViewer(topView, this.topMessage, this);
        bottomView = new TerminalView(term);
        bottomViewer = new StringViewer(bottomView, bottomMessage, this);
        selectorView = new TerminalView(term);
    }

    public void setTopMessage(String message) {
        this.topMessage = message;
        if (this.term != null) {
            topViewer.setMessage(this.topMessage);
            topViewer.handleResizeEvent();
            topViewer.step();
        }
    }

    public void setColor(TerminalCellTemplate template) {
        if (template == null) {
            this.useDefaultTemplate = true;
            return;
        }
        this.useDefaultTemplate = false;
        template = new TerminalCellTemplate(template);
        template.setSequence("");
        this.template = template;
        try {
            if (template.getCellWalls() == null) {
                template.clearCellWalls();
            }
        } catch(NullPointerException ex) {
            template.clearCellWalls();
        }
        try {
            if (template.getStyle() == null) {
                template.clearStyle();
            }
        } catch(NullPointerException ex) {
            template.clearStyle();
        }
    }
    public void setColor(int foreground, int background) {
        template = new TerminalCellTemplate();
        template.setBackground(background);
        template.setForeground(foreground);
        template.clearCellWalls();
        template.clearStyle();
        this.useDefaultTemplate = false;
    }
    public void setColor(String foreground, String background) {
        ColorPalette palette = term.getBackingTerminal().getPalette();
        int fg = palette.getColorOrIndex(foreground);
        int bg = palette.getColorOrIndex(background);
        setColor(fg, bg);
    }
    public void setMessageColor(int foreground, int background) {
        messageTemplate = new TerminalCellTemplate();
        messageTemplate.setBackground(background);
        messageTemplate.setForeground(foreground);
    }
    public void setMessageColor(TerminalCellTemplate template) {
        if (template == null) {
            throw new NullPointerException("template cannot be null");
        }
        template = new TerminalCellTemplate(template);
        if (template.isSequenceUnset()) {
            template.setSequence("");
        }
        this.messageTemplate = template;
    }
    public void setMessageColor(String foreground, String background) {
        ColorPalette palette = term.getBackingTerminal().getPalette();
        int fg = palette.getColorOrIndex(foreground);
        int bg = palette.getColorOrIndex(background);
        setMessageColor(fg, bg);
    }

    public void setTerm(TerminalInterface term) {
        internalSetTerm(term);
    }

    private void reposition() {
        topView.setBounds(term.getHeight() - 3 - bottomViewer.getLines(),
                term.getWidth() - 2, 1 + term.getY(), 1 + term.getX());
        selectorView.setBounds(1, term.getWidth() - 2,
                topView.getY() + topView.getHeight(), 1 + term.getX());
        bottomView.setBounds(bottomViewer.getLines(), term.getWidth() - 2, 
                term.getHeight() - bottomViewer.getLines()+term.getY() - 1, 1);
    }

    public void redraw() {
        reposition();

        term.clear();
        displayFrame();
        topViewer.step();

        TerminalCellTemplate clearCell = new TerminalCellTemplate(clearCellNormal);
        clearCell.setCellWalls((Set)null);
        TerminalUtils.applyTemplate(selectorView, clearCell);
        int textSize = (this.selectorView.getWidth() - 8) / 3;
        Box.box(this.selectorView, Box.BoxMethod.OUTSIDE_WALL,
                        new BoxRegion(1, textSize, selectorView.getY(),
                                      selectorView.getX() + textSize + 4));
        String s = "";
        if (choiceText.size() > 3 && currentOption > 2) {
            s = "<";
        }
        selectorView.set(selectorView.getY(), selectorView.getX(), s, null, null);
        selectorView.set(selectorView.getY(), selectorView.getX()+1, s, null, null);
        s = "";
        if (choiceText.size() > 3 && currentOption < choiceText.size() - 2) {
            s = ">";
        }
        selectorView.set(selectorView.getY(),
                selectorView.getX()+selectorView.getWidth()-1, s, null, null);
        selectorView.set(selectorView.getY(),
                selectorView.getX()+selectorView.getWidth()-2, s, null, null);
        if (currentOption > 0) {
            String opt = choiceTitles.get(currentOption - 1);
            if (opt.length() >= textSize) {
                opt = opt.substring(0, textSize);
            }
            SingleLine.putString(selectorView,
                    new Point(selectorView.getY(),
                              selectorView.getX() + 3 + textSize / 2),
                    null, opt, null, Alignment.CENTER);
        }
        if (currentOption < choiceTitles.size() - 1) {
            String opt = choiceTitles.get(currentOption + 1);
            if (opt.length() >= textSize) {
                opt = opt.substring(0, textSize);
            }
            SingleLine.putString(selectorView,
                    new Point(selectorView.getY(),
                              selectorView.getX() + textSize * 2
                              + 5 + textSize / 2),
                    null, opt, null, Alignment.CENTER);
        }
        String msg = "";
        if (currentOption >= 0 && currentOption < choiceText.size()) {
            msg = choiceTitles.get(currentOption);
        }
        if (msg.length() >= textSize) {
            msg = msg.substring(0, textSize);
        }
        SingleLine.putString(selectorView,
                new Point(selectorView.getY(),
                          selectorView.getX() + textSize + 4 + textSize / 2),
                null, msg, null, Alignment.CENTER);
    }

    public void centerOnLine(int y, String string, TerminalCellTemplate tmplate) {
        int offset = term.getWidth() / 2 - string.length() / 2;
        SingleLine.putString(term, new Point(y, offset), null, string, tmplate);
    }

    private void displayFrame() {
        TerminalUtils.applyTemplate(term, template);
        TerminalUtils.applyTemplate(topView, messageTemplate);
        TerminalUtils.applyTemplate(bottomView, messageTemplate);

        bottomViewer.step();

        Box.box(term, Box.BoxMethod.INSIDE_WALL,
                BoxRegion.inset(topView.getBounds(), -1, -1));
        Box.box(term, Box.BoxMethod.INSIDE_WALL,
                BoxRegion.inset(bottomView.getBounds(), -1, -1));

        if (title != null) {
            TerminalCellTemplate t = new TerminalCellTemplate(template);
            t.setSequence(null);
            t.setCellWalls((Set)null);
            centerOnLine(0, title, t);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CodepointCallbackInterface getSecondaryCallback() {
        return secondaryCallback;
    }

    public void setSecondaryCallback(CodepointCallbackInterface secondaryCallback) {
        this.secondaryCallback = secondaryCallback;
    }

    public void setBottomMessage(String message) {
        this.bottomMessage = message;
        if (this.term != null) {
            bottomViewer.setMessage(message);
            redraw();
        }
    }

    public Integer getCancelIndex() {
        return cancelIndex;
    }

    public void setCancelIndex(Integer cancelIndex) {
        this.cancelIndex = cancelIndex;
    }
}
