/*
 * Copyright (c) 2007 Mockito contributors 
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.verification;

import java.util.LinkedList;
import java.util.List;

import org.mockito.exceptions.Reporter;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.InvocationsCalculator;
import org.mockito.internal.invocation.InvocationsChunker;
import org.mockito.internal.invocation.InvocationsMarker;
import org.mockito.internal.progress.VerificationMode;

public class VerifyingRecorder {

    private final LinkedList<Invocation> registeredInvocations = new LinkedList<Invocation>();
    private final Reporter reporter = new Reporter();
    
    private final InvocationsMarker marker;
    private final List<Verifier> verifiers;
    private final InvocationsChunker chunker;
    
    public VerifyingRecorder(InvocationsChunker chunker, InvocationsMarker marker, List<Verifier> verifiers) {
        this.chunker = chunker;
        this.marker = marker;  
        this.verifiers = verifiers;
    }
    
    public void recordInvocation(Invocation invocation) {
        this.registeredInvocations.add(invocation);
    }
    
    public void eraseLastInvocation() {
        registeredInvocations.removeLast();
    }

    public List<Invocation> getRegisteredInvocations() {
        return registeredInvocations;
    }
    
    public void verify(InvocationMatcher wanted, VerificationMode mode) {
        List<Invocation> invocations = getInvocationsForEvaluation(mode);
        //TODO should not construct calculator
        InvocationsCalculator calculator = new InvocationsCalculator(invocations);
        
        for (Verifier verifier : verifiers) {
            verifier.verify(calculator, wanted, mode);
        }
        
        marker.markInvocationsAsVerified(invocations, wanted, mode);
    }
    
    private List<Invocation> getInvocationsForEvaluation(VerificationMode mode) {
        if (mode.isStrict()) {
            return chunker.getFirstUnverifiedInvocationChunk(mode.getAllMocksToBeVerifiedInSequence());
        } else {
            return registeredInvocations;
        }
    }
    
    public void verifyNoMoreInteractions() {
        InvocationsCalculator calculator = new InvocationsCalculator(registeredInvocations);
        Invocation unverified = calculator.getFirstUnverified();
        if (unverified != null) {
            reporter.noMoreInteractionsWanted(unverified.toString(), unverified.getStackTrace());
        }
    }
}