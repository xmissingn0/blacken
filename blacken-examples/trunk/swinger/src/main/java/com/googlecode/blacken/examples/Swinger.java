/* blacken - a library for Roguelike games
 * Copyright © 2010, 2011 Steven Black <yam655@gmail.com>
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
package com.googlecode.blacken.examples;

import com.googlecode.blacken.colors.ColorNames;
import com.googlecode.blacken.colors.ColorPalette;
import com.googlecode.blacken.swing.SwingTerminal;
import com.googlecode.blacken.terminal.BlackenEventType;
import com.googlecode.blacken.terminal.BlackenKeys;
import com.googlecode.blacken.terminal.BlackenModifier;
import com.googlecode.blacken.terminal.CursesLikeAPI;
import com.googlecode.blacken.terminal.TerminalInterface;
import java.util.EnumSet;

/**
 * Generic SwingTerminal test.
 * 
 * @author Steven Black
 */
public class Swinger {
    /**
     * TerminalInterface used by the example
     */
    protected CursesLikeAPI term;
    /**
     * ColorPalette used by the example
     */
    protected ColorPalette palette;
    /**
     * Whether to quit the loop or not
     */
    protected boolean quit;

    Swinger() {
        // do nothing
    }

    /**
     * Tell the loop to quit.
     *
     * @param quit new quit status
     */
    public void setQuit(boolean quit) {
        this.quit = quit;
    }
    /**
     * Get the quit status.
     *
     * @return whether we should quit
     */
    public boolean getQuit() {
        return quit;
    }

    /**
     * Initialize the example
     *
     * @param term alternate TerminalInterface to use
     * @param palette alternate ColorPalette to use
     */
    public void init(TerminalInterface term, ColorPalette palette) {
        if (term == null) {
            term = new SwingTerminal();
            term.init("Example Program", 25, 80);
        }
        this.term = new CursesLikeAPI(term);
        if (palette == null) {
            palette = new ColorPalette();
            palette.addAll(ColorNames.XTERM_256_COLORS, false);
            palette.putMapping(ColorNames.SVG_COLORS);
        }
        this.palette = palette;
        this.term.setPalette(palette);
    }
    /**
     * Quit the application.
     *
     * <p>This calls quit on the underlying TerminalInterface.</p>
     */
    public void quit() {
        term.quit();
    }

    /**
     * @param args command-line arguments
     * @param that example instance
     */
    public static void main(String[] args) {
        Swinger that = new Swinger();
        that.init(null, null);
        that.loop();
        that.quit();
    }

    /**
     * Show the help message
     */
    public void help() {
        // XXX do this
    }
    
    public boolean loop() {
        int ch = BlackenKeys.NO_KEY;
        term.setEventNotices(EnumSet.allOf(BlackenEventType.class));
        term.puts("Terminal Interface\n");
        term.puts("Press F10 to quit.\n");
        term.puts(">");
        while (ch != BlackenKeys.KEY_F10) {
            ch = term.getch();
            if (ch == BlackenKeys.MOUSE_EVENT) {
                term.puts("\n");
                term.puts(term.getmouse().toString());
                term.puts("\n>");
            } else if (ch == BlackenKeys.WINDOW_EVENT) {
                term.puts("\n");
                term.puts(term.getwindow().toString());
                term.puts("\n>");
            } else if (BlackenKeys.isModifier(ch)) {
                term.puts(BlackenModifier.getModifierString(ch).toString());
            } else if (BlackenKeys.isKeyCode(ch)) {
                term.puts(BlackenKeys.getKeyName(ch));
                if (ch == BlackenKeys.RESIZE_EVENT) {
                    term.puts("\nYummy window resize!");
                }
                term.puts("\n>");
                
            } else {
                term.puts(BlackenKeys.toString(ch));
                term.puts("\n>");
            }
        }
        return this.quit;
    }
    
}
