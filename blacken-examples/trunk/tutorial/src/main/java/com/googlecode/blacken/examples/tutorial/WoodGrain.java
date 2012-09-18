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

import com.googlecode.blacken.extras.PerlinNoise;
import com.googlecode.blacken.grid.Regionlike;
import com.googlecode.blacken.terminal.TerminalCellLike;
import com.googlecode.blacken.terminal.TerminalCellTransformer;
import java.util.List;

/**
 *
 * @author Steven Black
 */
public class WoodGrain implements TerminalCellTransformer {
    private final List<Integer> palette;

    public WoodGrain(List<Integer> palette) {
        this.palette = palette;
    }

    @Override
    public boolean transform(TerminalCellLike cell, Regionlike bounds, int y, int x) {
        double n = PerlinNoise.noise(x, y / (palette.size() * 1.5));
        n *= palette.size();
        if (n < 0) {
            n += palette.size();
        }
        cell.setBackground(palette.get((int)n));
        return true;
    }

}
