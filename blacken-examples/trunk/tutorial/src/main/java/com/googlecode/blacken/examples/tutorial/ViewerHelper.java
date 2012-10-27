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
import com.googlecode.blacken.terminal.BlackenEventType;
import com.googlecode.blacken.terminal.BlackenKeys;
import com.googlecode.blacken.terminal.BlackenModifier;
import com.googlecode.blacken.terminal.BlackenMouseEvent;
import com.googlecode.blacken.terminal.BlackenWindowEvent;
import com.googlecode.blacken.terminal.TerminalCellTemplate;
import com.googlecode.blacken.terminal.TerminalInterface;
import com.googlecode.blacken.terminal.TerminalView;
import com.googlecode.blacken.terminal.TerminalViewInterface;
import com.googlecode.blacken.terminal.editing.CodepointCallbackInterface;
import com.googlecode.blacken.terminal.editing.SingleLine;
import com.googlecode.blacken.terminal.editing.StringViewer;
import com.googlecode.blacken.terminal.utils.TerminalUtils;
import com.googlecode.blacken.terminal.widgets.Box;
import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steven Black
 */
public class ViewerHelper implements CodepointCallbackInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewerHelper.class);
    private TerminalViewInterface term = null;
    private TerminalViewInterface view = null;
    private StringViewer viewer = null;
    private TerminalViewInterface helpView = null;
    private StringViewer helpViewer = null;
    private String title = null;
    private String helpMessage =
            "Q / q : quit this viewer; " +
            "PageUp / PageDown : Next or previous page";
    private TerminalCellTemplate template = null;
    private TerminalCellTemplate messageTemplate = null;
    private String message = null;
    private EnumSet<BlackenModifier> lastModifiers;
    private CodepointCallbackInterface secondaryCallback = null;
    private boolean useDefaultTemplate = true;
    private boolean saveMenuSpace = false;

    public ViewerHelper() {
    }

    public ViewerHelper(TerminalViewInterface term, String title, String message) {
        internalSetup(term, title, message);
    }

    public void setup(TerminalViewInterface term, String title, String message) {
        internalSetup(term, title, message);
    }

    private void internalSetup(TerminalViewInterface term, String title, String message) {
        this.title = title;
        if (term != null) {
            internalSetTerm(term);
            internalDefaultTemplate();
        }
        this.message = message;
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
    private void internalSetTerm(TerminalViewInterface term) {
        if (term == null) {
            this.term = null;
            this.view = null;
            this.viewer = null;
            this.helpView = null;
            this.helpViewer = null;
            if (useDefaultTemplate) {
                this.template = null;
            }
            return;
        }
        this.term = term;
        if (useDefaultTemplate) {
            internalDefaultTemplate();
        }
        view = new TerminalView(term);
        if (this.message == null) {
            this.message = "";
        }
        viewer = new StringViewer(view, this.message, this);
        helpView = new TerminalView(term);
        helpViewer = new StringViewer(helpView, helpMessage, this);
    }

    public void setMessage(String message) {
        this.message = message;
        if (this.term != null) {
            viewer.setMessage(this.message);
            viewer.handleResizeEvent();
            viewer.step();
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

    public void run() {
        if (this.term == null) {
            throw new NullPointerException("Cannot run with a null terminal");
        }
        TerminalInterface realTerm = term.getBackingTerminal();
        EnumSet<BlackenEventType> oldNotices = realTerm.getEventNotices();
        realTerm.setEventNotices(EnumSet.of(BlackenEventType.MOUSE_WHEEL));
        viewer.setColor(messageTemplate);
        redraw();
        viewer.run();
        realTerm.setEventNotices(oldNotices);
    }
    public void redraw() {
        reposition();
        term.clear();
        displayFrame();
        viewer.step();
    }

    @Override
    public int handleCodepoint(int codepoint) {
        //LOGGER.debug("Found codepoint: {}", BlackenCodePoints.getCodepointName(codepoint));
        if (this.secondaryCallback != null) {
            codepoint = secondaryCallback.handleCodepoint(codepoint);
        }
        if (BlackenKeys.isModifier(codepoint)) {
            this.lastModifiers = BlackenModifier.getAsSet(codepoint);
            return codepoint;
        }
        switch (codepoint) {
            case 'l':
            case 'L':
                if (lastModifiers != null && lastModifiers.contains(BlackenModifier.MODIFIER_KEY_CTRL)) {
                    redraw();
                }
                break;
        }
        lastModifiers = null;
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

    @Override
    public void handleResizeEvent() {
        if (this.secondaryCallback != null) {
            secondaryCallback.handleResizeEvent();
        }
        redraw();
    }

    public void centerOnLine(int y, String string, TerminalCellTemplate tmplate) {
        int offset = term.getWidth() / 2 - string.length() / 2;
        SingleLine.putString(term, new Point(y, offset), null, string, tmplate);
    }

    private void reposition() {
        if (this.saveMenuSpace) {
            Regionlike realBounds = term.getBackingTerminal().getBounds();
            if (term.getBackingTerminal() == term) {
                term = new TerminalView(term);
            }
            term.setBounds(realBounds.getHeight()-1, realBounds.getWidth(), 1, 0);
        } else {
            term = term.getBackingTerminal();
        }
        view.setBounds(term.getHeight()-1-helpViewer.getLines(), term.getWidth()-2, 1+term.getY(), 1);
        helpView.setBounds(helpViewer.getLines(), term.getWidth() - 2, term.getHeight()-helpViewer.getLines()+term.getY(), 1);
    }

    private void displayFrame() {
        if (title != null) {
            centerOnLine(0, title, template);
        }

        TerminalUtils.applyTemplate(term, template);
        TerminalUtils.applyTemplate(view, messageTemplate);

        helpViewer.setColor(template);
        helpViewer.step();

        Box.box(term, Box.BoxMethod.INSIDE_WALL, BoxRegion.inset(view.getBounds(), -1, -1));
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

    public boolean hasMenuSpace() {
        return saveMenuSpace;
    }

    public void setMenuSpace(boolean saveMenuSpace) {
        this.saveMenuSpace = saveMenuSpace;
    }

}
