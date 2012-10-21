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
import com.googlecode.blacken.dungeon.TIMTypes.Itemlike;
import com.googlecode.blacken.dungeon.TIMTypes.MotileDriver;
import com.googlecode.blacken.dungeon.TIMTypes.Skill;
import com.googlecode.blacken.dungeon.TIMTypes.TimedAttribute;
import com.googlecode.blacken.dungeon.TIMTypes.TimedFlag;
import com.googlecode.blacken.dungeon.TIMTypes.TimedPlayerlike;
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.grid.Positionable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author yam655
 */
public class Player implements TimedPlayerlike {
    private static final long serialVersionUID = 7704307859898283031L;
    private Positionable pos = new Point(-1, -1);
    private Containerlike<Itemlike> inventory = new SimpleContainer<>();
    private ListMap<String, TimedAttribute> attributes = new ListMap<>();
    private ListMap<String, Skill> skills = new ListMap<>();
    private ListMap<String, TimedFlag> flags = new ListMap<>();

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


}
