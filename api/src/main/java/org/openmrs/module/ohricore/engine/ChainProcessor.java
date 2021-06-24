package org.openmrs.module.ohricore.engine;

import javax.mail.MethodNotSupportedException;
import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * @author smallGod
 * date: 24/06/2021
 */
public interface ChainProcessor extends Callable<String> {

    /**
     * Compute if necessary and Call next ComputedConcept processor service in chain
     *
     * @param processorIndex - index of the service in the service publisher
     * @param returnValue    - the value that was returned by the method, if any
     * @param method         - the method that was invoked
     * @param args           - the arguments to the method
     * @param target         - the target of the method invocation. May be null.
     * @param trigger        - the trigger for this event
     * @throws RejectedExecutionException
     * @throws CancellationException
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    void moveToNext(int processorIndex,
                    Object returnValue,
                    Method method,
                    Object[] args,
                    Object target,
                    EventTrigger trigger) throws
            RejectedExecutionException,
            CancellationException,
            InterruptedException,
            TimeoutException,
            ExecutionException;

    @Override
    default String call() throws Exception, MethodNotSupportedException {
        return null;
    }
}
