/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fasttext.utils;

public enum loss_name {
        hs(1), ns(2), softmax(3);
        public int value;

        private loss_name(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static loss_name fromValue(int value) throws IllegalArgumentException {
            try {
                value -= 1;
                return loss_name.values()[value];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Unknown loss_name enum value :" + value);
            }
        }
    }