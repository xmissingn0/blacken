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
import com.googlecode.blacken.extras.PerlinNoise;
import com.googlecode.blacken.grid.Regionlike;
import com.googlecode.blacken.terminal.TerminalCellLike;
import com.googlecode.blacken.terminal.TerminalCellTransformer;
import java.util.List;

/**
 *
 * @author Steven Black
 */
public class DripTexture implements TerminalCellTransformer {
    private final List<Integer> palette;
    private final int maxIndex;
    static final float z = Random.getInstance().nextFloat();

    public DripTexture(List<Integer> palette) {
        this.palette = palette;
        maxIndex = palette.size();
    }

    @Override
    public boolean transform(TerminalCellLike cell, Regionlike bounds, int y, int x) {
        if (palette.isEmpty()) {
            return false;
        }
        double n = PerlinNoise.fbmNoise(5, x, 1, z);

        int index = ((int)(n * maxIndex) - y) % palette.size();
        if (index < 0) {
            index += palette.size();
        }
        cell.setBackground(palette.get(index));
        return true;
    }

}
