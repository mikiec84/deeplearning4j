package org.nd4j.linalg.jcublas.ops.executioner.kernels.factory.impl;

import org.nd4j.linalg.api.ops.Op;
import org.nd4j.linalg.jcublas.context.CudaContext;
import org.nd4j.linalg.jcublas.gpumetrics.GpuMetrics;
import org.nd4j.linalg.jcublas.ops.executioner.kernels.GpuKernelCall;
import org.nd4j.linalg.jcublas.ops.executioner.kernels.GpuKernelCallFactory;
import org.nd4j.linalg.jcublas.ops.executioner.kernels.impl.PostProcessKernelCall;

/**
 * Creates post process kernel calls
 *
 * @author Adam Gibson
 */
public class PostProcessKernelCallFactory implements GpuKernelCallFactory {
    @Override
    public GpuKernelCall create(Op op, Object... otherArgs) {
        return new PostProcessKernelCall(op,(int[]) otherArgs[0],(int) otherArgs[1],(int) otherArgs[2],(Object[]) otherArgs[3],(CudaContext) otherArgs[4],(GpuMetrics) otherArgs[5]);
    }
}
