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
import com.googlecode.blacken.dungeon.TIMTypes.Flag;
import com.googlecode.blacken.dungeon.TIMTypes.Monsterlike;
import com.googlecode.blacken.dungeon.TIMTypes.MotileDriver;
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.grid.Positionable;

/**
 *
 * @author yam655
 */
public class AbstractHorror implements Monsterlike {
    private static final long serialVersionUID = 1534903125975203358L;
    private String name;
    Point location = new Point();

    public AbstractHorror(String name) {
        this.name = name;
    }

    @Override
    public MotileDriver getDriver() {
        return null;
    }

    @Override
    public Flag getFlag(String name) {
        return null;
    }

    @Override
    public ListMap<String, ? extends Flag> getFlags() {
        return null;
    }

    @Override
    public void setDriver(MotileDriver driver) {
        // do nothing
    }

    @Override
    public int getX() {
        return location.getX();
    }

    @Override
    public int getY() {
        return location.getY();
    }

    @Override
    public Positionable getPosition() {
        return location.getPosition();
    }

    @Override
    public void setX(int x) {
        location.setX(x);
    }

    @Override
    public void setY(int y) {
        location.setY(y);
    }

    @Override
    public void setPosition(int y, int x) {
        location.setPosition(y, x);
    }

    @Override
    public void setPosition(Positionable point) {
        location.setPosition(point);
    }

    @Override
    public String getName() {
        return name;
    }

}
