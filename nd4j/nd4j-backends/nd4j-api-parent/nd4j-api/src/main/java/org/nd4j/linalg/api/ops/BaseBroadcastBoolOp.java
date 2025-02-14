/*******************************************************************************
 * Copyright (c) 2015-2018 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.nd4j.linalg.api.ops;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import onnx.Onnx;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.base.Preconditions;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.shape.LongShapeDescriptor;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.factory.Broadcast;
import org.nd4j.linalg.util.ArrayUtil;
import org.tensorflow.framework.AttrValue;
import org.tensorflow.framework.GraphDef;
import org.tensorflow.framework.NodeDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Slf4j
public abstract class BaseBroadcastBoolOp extends BaseOp implements BroadcastOp {
    protected int[] dimension;


    public BaseBroadcastBoolOp(SameDiff sameDiff,
                               SDVariable i_v1,
                               SDVariable i_v2,
                               int[] dimension) {
        this(sameDiff, i_v1, i_v2, false, dimension);
    }

    public BaseBroadcastBoolOp(SameDiff sameDiff,
                               SDVariable i_v1,
                               SDVariable i_v2,
                               boolean inPlace,
                               int[] dimension) {
        super(sameDiff, inPlace, new Object[]{i_v2});
        if (i_v1 != null && i_v2 != null) {
            f().validateDifferentialFunctionsameDiff(i_v1);
            f().validateDifferentialFunctionsameDiff(i_v2);
            this.sameDiff = sameDiff;
            this.inPlace = inPlace;
            this.dimension = dimension;
            if(Shape.isPlaceholderShape(i_v1.getShape())) {
                sameDiff.addPropertyToResolve(this,i_v1.getVarName());
            }

            if(Shape.isPlaceholderShape(i_v2.getShape())) {
                sameDiff.addPropertyToResolve(this,i_v2.getVarName());
            }
            sameDiff.addArgsFor(new SDVariable[]{i_v1,i_v2},this);

        } else {
            throw new IllegalArgumentException("Input not null variables.");
        }

    }

    public BaseBroadcastBoolOp(SameDiff sameDiff) {
        this.sameDiff = sameDiff;
    }

    public BaseBroadcastBoolOp(SameDiff sameDiff,
                               SDVariable i_v1,
                               SDVariable i_v2,
                               int[] dimension,
                               Object[] extraArgs) {
        super(sameDiff, extraArgs);
        this.dimension = dimension;
        if (i_v1 != null && i_v2 != null) {
            f().validateDifferentialFunctionsameDiff(i_v1);
            f().validateDifferentialFunctionsameDiff(i_v2);

            this.sameDiff = sameDiff;
            sameDiff.addArgsFor(new SDVariable[]{i_v1,i_v2},this);

        } else {
            throw new IllegalArgumentException("Input not null variables.");
        }


    }


    public BaseBroadcastBoolOp(SameDiff sameDiff, SDVariable i_v, int[] dimension, boolean inPlace) {
        this(sameDiff, i_v, i_v.getShape(), inPlace, dimension, null);
    }

    public BaseBroadcastBoolOp(SameDiff sameDiff,
                               SDVariable i_v,
                               int[] shape,
                               boolean inPlace,
                               int[] dimension,
                               Object[] extraArgs) {
        // FIXME: int cast
        this(sameDiff, i_v, ArrayUtil.toLongArray(shape), inPlace, dimension, extraArgs);
    }

    public BaseBroadcastBoolOp(SameDiff sameDiff,
                               SDVariable i_v,
                               long[] shape,
                               boolean inPlace,
                               int[] dimension,
                               Object[] extraArgs) {
        super(sameDiff, inPlace, extraArgs);
        this.dimension = dimension;
        if (i_v != null) {
            f().validateDifferentialFunctionsameDiff(i_v);
            sameDiff.addArgsFor(new SDVariable[]{i_v},this);


        } else {
            throw new IllegalArgumentException("Input not null variable.");
        }


    }


    public BaseBroadcastBoolOp(SameDiff sameDiff,
                               SDVariable i_v,
                               int[] dimension,
                               Object[] extraArgs) {
        this(sameDiff, i_v, i_v.getShape(), false, dimension, extraArgs);
    }

    public BaseBroadcastBoolOp(INDArray x, INDArray y, INDArray z, int... dimension) {
        super(x, y, z);
        Broadcast.validateBroadcastDims(x,y,z, dimension);

        this.dimension = dimension;
        for (int i = 0; i < dimension.length; i++)
            if (dimension[i] < 0)
                dimension[i] += x.rank();

        defineDimensions(dimension);
    }

    @Override
    public Type opType() {
        return Type.BROADCAST;
    }

    /**
     * Calculate the output shape for this op
     *
     * @return
     */
    public List<LongShapeDescriptor> calculateOutputShape() {
        if(x == null || y == null)
            return Collections.emptyList();

        long[] shapeX = x.shape();
        long[] shapeY = y.shape();

        return Collections.singletonList(LongShapeDescriptor.fromShape(Shape.broadcastOutputShape(shapeX, shapeY), DataType.BOOL));
    }


    @Override
    public int[] getDimension() {
        if (dimension == null) {
            if(x != null && y != null){
                dimension = Shape.getBroadcastDimensions(x.shape(), y.shape());
            } else {
                dimension = Shape.getBroadcastDimensions(larg().getShape(), rarg().getShape());
            }
        }
        return dimension;
    }


    @Override
    public void setDimension(int... dimension) {
        this.dimension = dimension;
    }


    @Override
    public void initFromTensorFlow(NodeDef nodeDef, SameDiff initWith, Map<String, AttrValue> attributesForNode, GraphDef graph) {
    }



    @Override
    public void initFromOnnx(Onnx.NodeProto node, SameDiff initWith, Map<String, Onnx.AttributeProto> attributesForNode, Onnx.GraphProto graph) {

    }

    @Override
    public boolean validateDataTypes(boolean experimentalMode) {

        val op = opNum();

        Preconditions.checkArgument(x().dataType() == y().dataType(), "Op.X and Op.Y must have the same data type: x.dataType=%s, y.dataType=%s, op=%s",
                x.dataType(), y.dataType(), getClass().getName());

        Preconditions.checkArgument(z().isB(), "Op.Z must have bool type: z has type %s for op %s", z().dataType(), getClass());

        return true;
    }

    @Override
    public Type getOpType() {
        return Type.BROADCAST_BOOL;
    }
}
