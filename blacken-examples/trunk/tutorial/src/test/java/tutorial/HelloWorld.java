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

package tutorial;

import com.googlecode.blacken.swing.SwingTerminal;
import com.googlecode.blacken.terminal.TerminalInterface;

/**
 * The simplest "Hello, World" in Blacken.
 *
 * @author Steven Black
 */
public class HelloWorld {

    /**
     * Start the application.
     *
     * @param args all ignored
     */
    public static void main(String[] args) {
        TerminalInterface term = new SwingTerminal();
        term.init("Hello", 25, 80);
        term.putString(0, 0, "Hello, World!");
        while(term.isRunning()) {
            term.getch();
        }
        term.quit();
    }
}
