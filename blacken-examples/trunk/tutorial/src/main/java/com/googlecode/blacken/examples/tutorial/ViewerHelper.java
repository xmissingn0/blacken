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
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.terminal.BlackenEventType;
import com.googlecode.blacken.terminal.BlackenMouseEvent;
import com.googlecode.blacken.terminal.BlackenWindowEvent;
import com.googlecode.blacken.terminal.CellWalls;
import com.googlecode.blacken.terminal.TerminalCellTemplate;
import com.googlecode.blacken.terminal.TerminalInterface;
import com.googlecode.blacken.terminal.TerminalView;
import com.googlecode.blacken.terminal.TerminalViewInterface;
import com.googlecode.blacken.terminal.editing.CodepointCallbackInterface;
import com.googlecode.blacken.terminal.editing.SingleLine;
import com.googlecode.blacken.terminal.editing.StringViewer;
import java.util.EnumSet;

/**
 *
 * @author Steven Black
 */
public class ViewerHelper implements CodepointCallbackInterface {
    private TerminalInterface term;
    private TerminalViewInterface view;
    private StringViewer viewer;
    private TerminalViewInterface helpView;
    private StringViewer helpViewer;
    private String title;
    String helpMessage =
            "Q / q : quit this viewer; " +
            "PageUp / PageDown : Next or previous page";
    private TerminalCellTemplate template;
    private TerminalCellTemplate messageTemplate;

    public ViewerHelper(TerminalInterface term, String title, String message) {
        this.title = title;
        this.term = term;
        view = new TerminalView(term);
        viewer = new StringViewer(view, message, this);
        helpView = new TerminalView(term);
        helpViewer = new StringViewer(helpView, helpMessage, null);
        int background = term.getEmpty().getBackground();
        int foreground = ColorHelper.makeVisible(background);
        template = new TerminalCellTemplate();
        template.setBackground(background);
        template.setForeground(foreground);
    }
    public void setColor(TerminalCellTemplate template) {
        if (template == null) {
            throw new NullPointerException("template cannot be null");
        }
        template = new TerminalCellTemplate(template);
        this.template = template;
    }
    public void setColor(int foreground, int background) {
        template = new TerminalCellTemplate();
        template.setBackground(background);
        template.setForeground(foreground);
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
        try {
            if (template.getSequence() == null) {
                template.setSequence(" ");
            }
        } catch(NullPointerException ex) {
            template.setSequence(" ");
        }
        this.messageTemplate = template;
    }
    public void setMessageColor(String foreground, String background) {
        ColorPalette palette = term.getBackingTerminal().getPalette();
        int fg = palette.getColorOrIndex(foreground);
        int bg = palette.getColorOrIndex(background);
        setMessageColor(fg, bg);
    }

    public void run() {
        EnumSet<BlackenEventType> oldNotices = term.getEventNotices();
        term.setEventNotices(EnumSet.of(BlackenEventType.MOUSE_WHEEL));
        displayFrame();
        viewer.setColor(messageTemplate);
        viewer.run();
        term.setEventNotices(oldNotices);
    }

    @Override
    public int handleCodepoint(int codepoint) {
        return codepoint;
    }

    @Override
    public boolean handleMouseEvent(BlackenMouseEvent mouse) {
        return false;
    }

    @Override
    public boolean handleWindowEvent(BlackenWindowEvent window) {
        return false;
    }

    @Override
    public void handleResizeEvent() {
        term.clear();
        displayFrame();
    }

    public void centerOnLine(int y, String string, TerminalCellTemplate tmplate) {
        int offset = term.getWidth() / 2 - string.length() / 2;
        SingleLine.putString(term, new Point(y, offset), null, string, tmplate);
    }

    private void displayFrame() {
        centerOnLine(0, title, template);
        view.setBounds(term.getHeight()-1-helpViewer.getLines(), term.getWidth()-2, 1, 1);
        helpView.setBounds(helpViewer.getLines(), term.getWidth(), term.getHeight()-helpViewer.getLines(), 0);

        SingleLine.applyTemplate(term, template);
        SingleLine.applyTemplate(view, messageTemplate);

        helpViewer.setColor(template);
        helpViewer.step();

        for (int x = 1; x < term.getWidth()-1; x++) {
            term.set(0, x, null, null, null, null,
                    EnumSet.of(CellWalls.BOTTOM));
            term.set(term.getHeight()-helpViewer.getLines(), x, 
                    null, null, null, null, EnumSet.of(CellWalls.TOP));
        }
        for (int y = 1; y < term.getHeight()-helpViewer.getLines(); y++) {
            term.set(y, 0, null, null, null, null, EnumSet.of(CellWalls.RIGHT));
            term.set(y, term.getWidth()-1, null, null, null, null, EnumSet.of(CellWalls.LEFT));
        }
    }

}
