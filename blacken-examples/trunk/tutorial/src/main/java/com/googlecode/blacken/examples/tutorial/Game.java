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

import com.googlecode.blacken.core.Random;
import com.googlecode.blacken.dungeon.TIMTypes.Itemlike;
import com.googlecode.blacken.dungeon.TIMTypes.Monsterlike;
import com.googlecode.blacken.terminal.TerminalInterface;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steven Black
 */
public class Game {
    private static Logger LOGGER = LoggerFactory.getLogger(Game.class);
    private GameOver gameOver = null;
    static private Game instance = new Game();
    private Player player = new Player();
    private static TerminalInterface term = null;
    private Game() {
        SimpleGoal goal = new SimpleGoal();
        String relative = Random.getInstance().choice("mother",
                "grandmother", "father", "grandfather");
        goal.setDescription(
            "Your " + relative + " always told you that you should never "
            + "be a quitter.");
        goal.setSuccess(1f);
        goal.setRequired(true);
        goal.setComplete(false);
        goal.setTitle("Don't be a quitter.");
        player.getGoals().add("quitter", goal);

        goal = new SimpleGoal();
        relative = Random.getInstance().choice("mother",
                "grandmother", "father", "grandfather");
        goal.setDescription(
            "Your " + relative + " requested that you survive your adventure.");
        goal.setSuccess(1f);
        goal.setRequired(true);
        goal.setComplete(false);
        goal.setTitle("Survive your adventure.");
        player.getGoals().add("survive", goal);

    }
    static public void setTerminal(TerminalInterface view) {
        term = view.getBackingTerminal();
    }
    static public TerminalInterface getTerminal() {
        return term;
    }
    static public Game getInstance() {
        return instance;
    }
    public void identifyAll(Collection<Itemlike> bunch) {
        for (Itemlike item : bunch) {
            if (!item.isKnown()) {
                identify(player, item);
            }
        }
    }
    public void quit() {
        player.getGoal("quitter").setSuccess(0f);
        this.prepareGameOver();
    }
    public void prepareGameOver() {
        if (term == null) {
            throw new NullPointerException("Set TerminalInterface before preparing GameOver");
        }
        player.getGoal("quitter").setComplete(true);
        player.getGoal("survive").setComplete(true);
        gameOver = new GameOver(term);
        String playerText = null;
        if (player == null) {
            return;
        }

        StringBuilder buf = new StringBuilder();
        boolean failure = false;
        buf.append("Required Goal Overview:\n\n");
        if (player.getGoals().isEmpty()) {
            buf.append("* None.\n\nYou have evaded an interesting life.");
        }
        boolean specialNoteA = false;
        for (Goal g : player.getGoals()) {
            if (!g.isRequired()) {
                continue;
            }
            buf.append("* ");
            buf.append(g.getTitle());
            if (!g.isComplete()) {
                failure = true;
                buf.append(" - Incomplete");
                if (g.getSuccess() >= 1f) {
                    specialNoteA = true;
                } else {
                    buf.append(String.format(": %3.4f%% finished", g.getSuccess() * 100f));
                }
            } else if (g.getSuccess() >= 1f) {
                buf.append(" - Success");
                buf.append(String.format(": %3.4f%% finished", g.getSuccess() * 100f));
            } else {
                failure = true;
                buf.append(" - Failed");
                buf.append(String.format(": %3.4f%% finished", g.getSuccess() * 100f));
            }
            buf.append("\n");
        }
        if (specialNoteA) {
            buf.append("\n" +
                "Note A. Once a goal reaches 100% it is ellegible for completion but does\n" +
                "        not automatically self-complete. The difference is, for example,\n" +
                "        a goal of gathering X things and returning them to a person. You\n" +
                "        reach 100% when you gather X things, but the goal is not complete\n" +
                "        until you deliver these things to the person.\n");
        }
        gameOver.setAscension(!failure);
        gameOver.addText(failure ? "You Lost" : "You Won", buf.toString());

        playerText = player.toString();
        gameOver.addText("Player", playerText);

        if (player != null && player.getInventory() != null) {
            identifyAll(player.getInventory());
            buf = new StringBuilder();
            int c = 1;
            for (Itemlike item : player.getInventory()) {
                buf.append(String.format("%u. ", c++));
                buf.append(item.toString());
                buf.append("\n");
            }
            if (player.getInventory().isEmpty()) {
                buf.append("Inventory is empty.\n");
            }
            gameOver.addText("Inventory", buf.toString());
        }
    }
    public GameOver getGameOver() {
        if (gameOver == null) {
            this.prepareGameOver();
        }
        return gameOver;
    }

    public Player getPlayer() {
        return player;
    }

    private void identify(Player player, Itemlike item) {
        item.setKnown(true);
        player.getMemory().addKnowledge(item);
    }

    void killPlayer(Monsterlike killer) {
        Goal goal = player.getGoal("survive");
        goal.setSuccess(0f);
        // goal.setTitle("You were " + killer.getName() + ".");
        goal.setDescription(goal.getDescription() + "\n\nYou were " + killer.getName() + ".");
    }

}
