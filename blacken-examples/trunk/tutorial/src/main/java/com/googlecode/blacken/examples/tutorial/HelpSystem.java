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
import com.googlecode.blacken.core.Obligations;
import com.googlecode.blacken.exceptions.InvalidStringFormatException;
import com.googlecode.blacken.resources.ResourceIdentifier;
import com.googlecode.blacken.resources.ResourceMissingException;
import com.googlecode.blacken.resources.ResourceUtils;
import com.googlecode.blacken.terminal.BlackenKeys;
import com.googlecode.blacken.terminal.BlackenModifier;
import com.googlecode.blacken.terminal.BlackenMouseEvent;
import com.googlecode.blacken.terminal.BlackenWindowEvent;
import com.googlecode.blacken.terminal.TerminalCellTemplate;
import com.googlecode.blacken.terminal.TerminalInterface;
import com.googlecode.blacken.terminal.editing.CodepointCallbackInterface;
import com.googlecode.blacken.terminal.widgets.ModifiedKey;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steven Black
 */
public class HelpSystem implements CodepointCallbackInterface {
    private final static Logger LOGGER = LoggerFactory.getLogger(HelpSystem.class);
    public final static String LEGAL_NOTICES = "Legal Notices";
    public final static String FONT_LICENSE = Obligations.getFontName() + " Font License";
    public final static String BLACKEN_LICENSE = "Blacken License";
    public final static String HELP_HELP = "The Help System";
    private static final String baseHelpMessage =
"Basic Help Commands\n" +
"===================\n" +
"\n" +
"Ctrl+L : recenter and redisplay the screen\n" +
"Down : move one line down            | Up : move one line up\n" +
"PgDown, Space : move one page down   | PgUp, BackSpace : move one page up" +
"End : jump to end of the file        | Home : jump to start of the file" +
"\n" +
"Ctrl+L; Ctrl+l (ell) : redraw the screen" +
"q, Q, Escape : exit viewer" +
"\n" +
"Switch to other help documents\n" +
"------------------------------\n" +
"\n";

    private static HelpSystem instance = null;
    private final ViewerHelper vh;
    private final List<Integer> gradientNormal;
    private final List<Integer> gradientMessage;
    private Map<String, ResourceIdentifier> messages = new HashMap<>();
    private LinkedHashMap<ModifiedKey, String> keymap = new LinkedHashMap<>();

    public static HelpSystem getInstance() {
        if (instance == null) {
            instance = new HelpSystem();
        }
        return instance;
    }

    public static void blackenLicense(TerminalInterface term) {
        HelpSystem that = HelpSystem.getInstance();
        that.switchHelpText(BLACKEN_LICENSE);
        that.run(term);
    }

    public static void legalNotices(TerminalInterface term) {
        HelpSystem that = HelpSystem.getInstance();
        that.switchHelpText(LEGAL_NOTICES);
        that.run(term);
    }

    static void fontLicense(TerminalInterface term) {
        HelpSystem that = HelpSystem.getInstance();
        that.switchHelpText(FONT_LICENSE);
        that.run(term);
    }

    static void help(TerminalInterface term) {
        HelpSystem that = HelpSystem.getInstance();
        that.switchHelpText(HELP_HELP);
        that.run(term);
    }
    private EnumSet<BlackenModifier> lastModifiers;

    private String generateHelpMessage() {
        StringBuilder buf = new StringBuilder();
        buf.append(HelpSystem.baseHelpMessage);
        for (ModifiedKey key : this.keymap.keySet()) {
            buf.append(key.toString());
            buf.append(" : ");
            buf.append(keymap.get(key));
            buf.append("\n");
        }
        return buf.toString();
    }
    private HelpSystem() {
        try {
            gradientNormal = ColorHelper.createGradient(null, 12, ColorHelper.lookup(null, "#271f0f", "#863"));
        } catch (InvalidStringFormatException ex) {
            throw new RuntimeException(ex);
        }
        gradientMessage = new ArrayList<>();
        for (int c : gradientNormal) {
            gradientMessage.add(ColorHelper.lerp(c, 0xFF333333, 0.4f));
        }
        vh = new ViewerHelper();
        vh.setSecondaryCallback(this);
        vh.setColor(new TerminalCellTemplate(new WoodGrain(gradientNormal),
                " ", 0xFFe0e0e0, null));
        vh.setMessageColor(new TerminalCellTemplate(new WoodGrain(gradientMessage),
                " ", 0xFFe0e0e0, null));
        messages = new HashMap<>();
        messages.put(LEGAL_NOTICES, Obligations.getBlackenNotice(null));
        messages.put(BLACKEN_LICENSE, Obligations.getBlackenLicense(null));
        messages.put(FONT_LICENSE, Obligations.getFontLicense(null));
        messages.put(HELP_HELP, null);
        keymap.put(new ModifiedKey(null, '?'), HELP_HELP);
        keymap.put(new ModifiedKey(null, BlackenKeys.KEY_HELP), HELP_HELP);
        keymap.put(new ModifiedKey(null, BlackenKeys.KEY_F01), HELP_HELP);
        keymap.put(new ModifiedKey(null, 'n'), LEGAL_NOTICES);
        keymap.put(new ModifiedKey(null, 'N'), LEGAL_NOTICES);
        keymap.put(new ModifiedKey(null, 'l'), BLACKEN_LICENSE);
        keymap.put(new ModifiedKey(null, 'L'), BLACKEN_LICENSE);
        keymap.put(new ModifiedKey(null, 'f'), FONT_LICENSE);
    }

    public String removeBinding(ModifiedKey mk) {
        return keymap.remove(mk);
    }
    public void removeBinding(String title) {
        messages.remove(title);
        Set<ModifiedKey> keys = new HashSet<>();
        for (Entry<ModifiedKey, String> entry : keymap.entrySet()) {
            if (title.equals(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        for (ModifiedKey k : keys) {
            keymap.remove(k);
        }
    }

    public void changeBinding(ModifiedKey mk, ModifiedKey newk) {
        if (mk.equals(newk)) {
            return;
        }
        if (mk == null || newk == null) {
            throw new NullPointerException("neither argument may be null");
        }
        if (!keymap.containsKey(mk)) {
            throw new NullPointerException("original key not found");
        }
        keymap.put(newk, keymap.remove(mk));
    }

    public void moveBindingToEnd(ModifiedKey mk) {
        String title = keymap.get(mk);
        if (title == null) {
            throw new NullPointerException("Failed to locate key:" + mk.toString());
        }
        keymap.remove(mk);
        keymap.put(mk, title);
    }

    public void addBinding(String title, ModifiedKey... mks) {
        if (title == null) {
            throw new NullPointerException("title may not be null");
        }
        for (ModifiedKey mk : mks) {
            keymap.put(mk, title);
        }
    }
    public void addBinding(String title, ResourceIdentifier resid, ModifiedKey... mks) {
        if (title == null || resid == null) {
            throw new NullPointerException("neither title nor resid may not be null");
        }
        for (ModifiedKey mk : mks) {
            keymap.put(mk, title);
        }
        messages.put(title, resid);
    }

    public void switchHelpText(String title) {
        vh.setTitle(title);
        ResourceIdentifier resid = messages.get(title);
        String message;
        if (resid == null) {
            message = this.generateHelpMessage();
        } else {
            try {
                message = ResourceUtils.getResourceAsString(resid);
            } catch (ResourceMissingException ex) {
                message = "Unable to load: " + title;
            }
        }
        vh.setMessage(message);
    }
    public void run(TerminalInterface term) {
        vh.setTerm(term);
        vh.run();
        vh.setTerm(null);
    }

    @Override
    public int handleCodepoint(int codepoint) {
        if (codepoint == BlackenKeys.NO_KEY) {
            lastModifiers = null;
            return codepoint;
        }
        if (BlackenKeys.isModifier(codepoint)) {
            this.lastModifiers = BlackenModifier.getAsSet(codepoint);
            return codepoint;
        }
        ModifiedKey mk = new ModifiedKey(lastModifiers, codepoint);
        String titleCheck = keymap.get(mk);
        if (titleCheck != null) {
            LOGGER.debug("Checking {} found {}", mk, titleCheck);
            this.switchHelpText(titleCheck);
            codepoint = BlackenKeys.NO_KEY;
            vh.redraw();
        }
        lastModifiers = null;
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
        // do nothing
    }

}
