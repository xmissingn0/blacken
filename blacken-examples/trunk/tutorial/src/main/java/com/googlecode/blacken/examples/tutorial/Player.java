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

import com.googlecode.blacken.core.ListMap;
import com.googlecode.blacken.dungeon.Containerlike;
import com.googlecode.blacken.dungeon.SimpleContainer;
import com.googlecode.blacken.dungeon.TIMTypes.Attribute;
import com.googlecode.blacken.dungeon.TIMTypes.Itemlike;
import com.googlecode.blacken.dungeon.TIMTypes.MotileDriver;
import com.googlecode.blacken.dungeon.TIMTypes.Skill;
import com.googlecode.blacken.dungeon.TIMTypes.TimedAttribute;
import com.googlecode.blacken.dungeon.TIMTypes.TimedFlag;
import com.googlecode.blacken.dungeon.TIMTypes.TimedPlayerlike;
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.grid.Positionable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Steven Black
 */
public class Player implements TimedPlayerlike {
    private static final long serialVersionUID = 7704307859898283031L;
    private Positionable pos = new Point(-1, -1);
    private Containerlike<Itemlike> inventory = new SimpleContainer<>();
    private ListMap<String, TimedAttribute> attributes = new ListMap<>();
    private ListMap<String, Skill> skills = new ListMap<>();
    private ListMap<String, TimedFlag> flags = new ListMap<>();
    private ListMap<String, Goal> goals = new ListMap<>();
    private Memorylike memory;

    @Override
    public int getX() {
        return pos.getX();
    }

    @Override
    public int getY() {
        return pos.getY();
    }

    @Override
    public Positionable getPosition() {
        return pos;
    }

    @Override
    public void setX(int x) {
        pos.setX(x);
    }

    @Override
    public void setY(int y) {
        pos.setY(y);
    }

    @Override
    public void setPosition(int y, int x) {
        pos.setPosition(y, x);
    }

    @Override
    public void setPosition(Positionable point) {
        pos.setPosition(point);
    }

    @Override
    public TimedAttribute getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Containerlike<Itemlike> getInventory() {
        return this.inventory;
    }

    @Override
    public MotileDriver getDriver() {
        return null;
    }

    @Override
    public void setDriver(MotileDriver driver) {
    }

    @Override
    public ListMap<String, TimedAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public ListMap<String, Skill> getSkills() {
        return skills;
    }

    @Override
    public ListMap<String, TimedFlag> getFlags() {
        return flags;
    }

    @Override
    public Skill getSkill(String name) {
        return skills.get(name);
    }

    @Override
    public TimedFlag getFlag(String name) {
        return flags.get(name);
    }

    @Override
    public void decreaseTime(int duration) {
        Set<String> drop = new HashSet<>();
        for (String name : attributes.keySet()) {
            TimedAttribute a = attributes.get(name);
            int d = a.getDuration();
            if (d >= 0 && d - duration < 0) {
                drop.add(name);
            } else {
                a.setDuration(d - duration);
            }
        }
        attributes.removeAll(drop);
        drop.clear();
        for (String name : flags.keySet()) {
            TimedFlag f = flags.get(name);
            int d = f.getDuration();
            if (d >= 0 && d - duration < 0) {
                drop.add(name);
            } else {
                f.setDuration(d - duration);
            }
        }
        flags.removeAll(drop);
    }

    public ListMap<String, Goal> getGoals() {
        return goals;
    }

    public void setGoals(ListMap<String, Goal> goals) {
        this.goals = goals;
    }

    public Goal getGoal(String key) {
        return goals.get(key);
    }

    private String divider(char what, int howMany) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < howMany; i++) {
            buf.append(what);
        }
        return buf.toString();
    }
    private String rewrap(String what, int lineLength) {
        what = what.replaceFirst("^\n+","").replaceAll("\t","    ");
        what = what.replaceAll("([^\\s])\n\n+", "$1 ");
        String[] lines = what.split("\n");
        StringBuilder buf = new StringBuilder();
        for (String line : lines) {
            int len = line.length();
            if (len < lineLength) {
                buf.append(line);
                buf.append("\n");
            } else {
                int col = 0;
                for (String word : line.split(" ")) {
                    if (word.isEmpty()) {
                        if (col+1 < lineLength){
                            buf.append(" ");
                            col++;
                        } else {
                            buf.append("\n");
                            col = 0;
                        }
                    } else if (col + word.length() < lineLength) {
                        if (col > 0) {
                            buf.append(" ");
                        }
                        buf.append(word);
                        col += word.length();
                    } else if (word.length() < lineLength) {
                        buf.append("\n");
                        buf.append(word);
                        col = word.length();
                    } else {
                        String w = word;
                        if (col + 10 < lineLength) {
                            int splitPoint = lineLength - col;
                            if (w.codePointAt(splitPoint - 1) > 0xffff) {
                                splitPoint--;
                            }
                            buf.append(w.substring(0, splitPoint));
                            buf.append("\n");
                            w = w.substring(splitPoint);
                        }
                        while(!w.isEmpty()) {
                            if (w.length() < lineLength) {
                                buf.append("\n");
                                buf.append(w);
                                col = w.length();
                                w = "";
                            } else {
                                int splitPoint = lineLength;
                                if (w.codePointAt(splitPoint - 1) > 0xffff) {
                                    splitPoint--;
                                }
                                buf.append(w.substring(0, splitPoint));
                                buf.append("\n");
                                w = w.substring(splitPoint);
                            }
                        }
                    }
                }
            }
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (!attributes.isEmpty()) {
            buf.append("Attributes\n==========\n\n");
        }
        for (Attribute a : attributes) {
            buf.append(a.getTitle());
            buf.append(": ");
            buf.append(a.getCurrent());
            buf.append("\n");
        }

        List<Skill> sortSkills = new ArrayList<>(skills);
        Collections.sort(sortSkills);
        String lastName = null;
        if (!skills.isEmpty()) {
            buf.append("Skills\n======\n\n");
        }
        for (Skill s : sortSkills) {
            if (Objects.equals(lastName, s.getGroupName())) {
                lastName = s.getGroupName();
                buf.append(s.getGroupName());
                buf.append("\n");
                buf.append(divider('-', s.getGroupName().length()));
                buf.append("\n");
            }
            buf.append("* ");
            buf.append(s.getTitle());
            buf.append(": ");
            buf.append(String.format("%d", s.getSkill()));
            buf.append("\n");
        }

        boolean goodContents = false;
        List<TimedFlag> sortFlags = new ArrayList<>(flags);
        Collections.sort(sortFlags);
        lastName = null;
        for (TimedFlag f : sortFlags) {
            if (f.getDuration() != -1) {
                continue;
            }
            if (!goodContents) {
                buf.append("Flags\n=====\n\n");
                goodContents = true;
            }
            if (Objects.equals(lastName, f.getGroupName())) {
                lastName = f.getGroupName();
                buf.append(f.getGroupName());
                buf.append("\n");
                buf.append(divider('-', f.getGroupName().length()));
                buf.append("\n");
            }
            buf.append("* ");
            buf.append(f.getTitle());
            buf.append("\n");
        }
        if (!goals.isEmpty()) {
            buf.append("Goals\n=====\n\n");
        }
        for (Goal g : goals) {
            buf.append(g.getTitle());
            buf.append("\n");
            buf.append(divider('-', g.getTitle().length()));
            buf.append("\n");
            buf.append(String.format("%3.4f%% finished\n", g.getSuccess() * 100f));
            if (g.getDescription() != null) {
                buf.append("\n");
                buf.append(rewrap(g.getDescription(), 77));
                buf.append("\n");
            }
        }
        return buf.toString();
    }

    Memorylike getMemory() {
        return this.memory;
    }

    @Override
    public String getName() {
        return "Player";
    }
}
