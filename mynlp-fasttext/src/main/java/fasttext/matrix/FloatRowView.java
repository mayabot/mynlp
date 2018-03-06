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

package fasttext.matrix;

import com.google.common.base.Preconditions;

public class FloatRowView extends Vector {

    private int row_view ;

    public FloatRowView(Matrix matrix, int row) {
        super(matrix.getData(), row * matrix.cols(), row * matrix.cols() + matrix.cols(), matrix.cols());
        Preconditions.checkArgument(row >= 0 && row < matrix.rows());
        row_view = row;

    }

    public int rowNum(){
        return row_view;
    }
}
