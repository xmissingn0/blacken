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

/**
 *
 * @author yam655
 */
interface Goal {
    public String getTitle();
    public void setTitle(String title);
    public boolean isComplete();
    public void setComplete(boolean state);
    public String getDescription();
    public void setDescription(String description);
    public float getSuccess();
    public void setSuccess(float state);
    public float addSuccess(float state);
    public void setRequired(boolean state);
    public boolean isRequired();
}
