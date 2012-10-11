/*
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.bigdata.processor;

import org.openspaces.bigdata.common.counters.GlobalCounter;
import org.openspaces.bigdata.processor.events.TokenCounter;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.TransactionalEvent;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.openspaces.events.polling.ReceiveHandler;
import org.openspaces.events.polling.receive.MultiTakeReceiveOperationHandler;
import org.openspaces.events.polling.receive.ReceiveOperationHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Polling event container polling for {@link TokenCounter} instances and updating atomic counters accordingly.
 * 
 * @author dotan
 */
@EventDriven
@Polling(gigaSpace = "gigaSpace", concurrentConsumers = 1, maxConcurrentConsumers = 1, receiveTimeout = 1000)
@TransactionalEvent
public class GlobalTokenCounter {
    private static final Logger log = Logger.getLogger(GlobalTokenCounter.class.getName());
    private static final int BATCH_SIZE = 100;


    @ReceiveHandler
    ReceiveOperationHandler receiveHandler() {
        MultiTakeReceiveOperationHandler receiveHandler = new MultiTakeReceiveOperationHandler();
        receiveHandler.setMaxEntries(BATCH_SIZE);
        receiveHandler.setNonBlocking(true);
        receiveHandler.setNonBlockingFactor(1);
        return receiveHandler;
    }

    @EventTemplate
    TokenCounter tokenCounter() {
        return new TokenCounter();
    }

    @SpaceDataEvent
    public void eventListener(TokenCounter counter,GigaSpace gigaSpace) {
    	log.info("Increment  local token " +counter.getToken() + " by " + counter.getCount());
        GlobalCounter globalCount =  gigaSpace.readById(GlobalCounter.class,counter.getToken());
        if ( globalCount == null)
        	globalCount = new GlobalCounter(counter.getToken(),counter.getCount());
        else
        	globalCount.incrementCountBy(counter.getCount());
        
        gigaSpace.write(globalCount);
        if (log.isLoggable(Level.FINE)) {
            log.fine("+++ token=" + globalCount.getToken() + " count=" + globalCount);
        }
    }


}