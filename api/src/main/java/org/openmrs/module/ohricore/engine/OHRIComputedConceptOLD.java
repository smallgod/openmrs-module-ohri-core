package org.openmrs.module.ohricore.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.TaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author smallGod date: 24/06/2021
 */
public abstract class OHRIComputedConceptOLD implements ChainProcessor {
	
	private final Log logger = LogFactory.getLog(this.getClass());
	
	private final TaskExecutor taskExecutor = TaskExecutor.getInstance();
	
	protected int processorIndex;
	
	protected Object methodReturnValue;
	
	protected Method methodInvoked;
	
	protected Object[] methodArgs;
	
	protected Object target;
	
	protected EventTrigger trigger;
	
	public OHRIComputedConceptOLD() {
	}
	
	/**
	 * Compute if necessary and Call next ComputedConcept processor service in chain
	 * 
	 * @param processorIndex - index of the service in the service publisher
	 * @param returnValue - the value that was returned by the method, if any
	 * @param method - the method that was invoked
	 * @param args - the arguments to the method
	 * @param target - the target of the method invocation. May be null.
	 * @param trigger - the trigger for this event
	 * @throws RejectedExecutionException
	 * @throws CancellationException
	 */
	@Override
    public void moveToNext(int processorIndex,
                           Object returnValue,
                           Method method,
                           Object[] args,
                           Object target,
                           EventTrigger trigger) throws
            RejectedExecutionException,
            CancellationException {

        processorIndex += 1; // move to next index

        this.processorIndex = processorIndex;
        this.methodReturnValue = returnValue;
        this.methodInvoked = method;
        this.methodArgs = args;
        this.target = target;
        this.trigger = trigger;

        if (processorIndex < OHRIComputedConceptPublisher.NUM_OF_SERVICES) {

            OHRIComputedConceptOLD processor = OHRIComputedConceptPublisher.SERVICES[processorIndex];

            processor.methodReturnValue = returnValue;
            processor.methodInvoked = method;
            processor.methodArgs = args;
            processor.target = target;
            processor.trigger = trigger;
            processor.processorIndex = processorIndex;

            try {
                Context.openSessionWithCurrentUser();
                taskExecutor.submitTask(processor);
            } catch (InterruptedException | TimeoutException | ExecutionException exception) {
                logger.error("Processor at index - " + processorIndex + " : " + exception.getMessage());
                exception.printStackTrace();
            } finally {
                Context.closeSessionWithCurrentUser();
            }
        }
    }
}
