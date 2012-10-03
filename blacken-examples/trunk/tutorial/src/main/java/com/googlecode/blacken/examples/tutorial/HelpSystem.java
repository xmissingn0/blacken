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

import com.googlecode.blacken.core.Obligations;
import com.googlecode.blacken.terminal.TerminalInterface;

/**
 *
 * @author Steven Black
 */
public class HelpSystem {

    public static void myLicense(TerminalInterface term) {
        HelpSystem that = new HelpSystem(term);
        that.myLicense();
    }

    public static void legalNotices(TerminalInterface term) {
        HelpSystem that = new HelpSystem(term);
        that.legalNotices();
    }

    static void fontLicense(TerminalInterface term) {
        HelpSystem that = new HelpSystem(term);
        that.fontLicense();
    }

    static void help(TerminalInterface term) {
        HelpSystem that = new HelpSystem(term);
        that.help();
    }
    private final TerminalInterface term;
    private String helpMessage =
"Tutorial Example Commands\n" +
"============================================================================\n" +
"Ctrl+L : recenter and redisplay the screen\n" +
"j, Down : move down                  | k, Up : move up\n" +
"h, Left : move left                  | l (ell), Right: move right\n" +
"\n" +
"Space : next representation set      | Backspace : previous representations\n" +
"\n" +
"Q, q, Escape : quit\n" +
"\n" +
"L : show my license                  | N : show legal notices\n" +
"\n" +
"? : this help screen\n";

    public HelpSystem(TerminalInterface term) {
        this.term = term;
    }
    public void legalNotices() {
        // show Notices file
        // This is the only one that needs to be shown for normal games.
        ViewerHelper vh;
        vh = new ViewerHelper(term, "Legal Notices", Obligations.getBlackenNotice());
        vh.setColor(7, 0);
        vh.run();
    }

    public void fontLicense() {
        // show the font license
        ViewerHelper vh;
        new ViewerHelper(term,
                Obligations.getFontName() + " Font License",
                Obligations.getFontLicense()).run();
    }

    public void help() {
        ViewerHelper vh;
        vh = new ViewerHelper(term, "Help", helpMessage);
        vh.setColor(7, 0);
        vh.run();
    }

    public void myLicense() {
        // show Apache 2.0 License
        ViewerHelper vh;
        vh = new ViewerHelper(term, "License", Obligations.getBlackenLicense());
        vh.setColor(7, 0);
        vh.run();
    }

}
