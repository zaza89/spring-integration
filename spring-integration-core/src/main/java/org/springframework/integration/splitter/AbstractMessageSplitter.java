/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.integration.splitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.support.MessageBuilder;

/**
 * Base class for Message-splitting handlers.
 * 
 * @author Mark Fisher
 * @author Dave Syer
 */
public abstract class AbstractMessageSplitter extends AbstractReplyProducingMessageHandler {

	public static final String SEQUENCE_DETAILS = MessageHeaders.PREFIX + "sequenceDetails";

	@Override
	@SuppressWarnings("unchecked")
	protected final Object handleRequestMessage(Message<?> message) {
		Object result = this.splitMessage(message);
		if (result == null) {
			return null;
		}
		MessageHeaders headers = message.getHeaders();
		Object incomingCorrelationId = headers.getCorrelationId();
		List<Object[]> incomingSequenceDetails = headers.get(SEQUENCE_DETAILS, List.class);
		if (incomingCorrelationId != null) {
			if (incomingSequenceDetails == null) {
				incomingSequenceDetails = new ArrayList<Object[]>();
			}
			else {
				incomingSequenceDetails = new ArrayList<Object[]>(incomingSequenceDetails);
			}
			incomingSequenceDetails.add(new Object[] {
					incomingCorrelationId, headers.getSequenceNumber(), headers.getSequenceSize() });
			incomingSequenceDetails = Collections.unmodifiableList(incomingSequenceDetails);
		}
		Object correlationId = headers.getId();
		List<MessageBuilder<?>> messageBuilders = new ArrayList<MessageBuilder<?>>();
		if (result instanceof Collection) {
			Collection<?> items = (Collection<?>) result;
			int sequenceNumber = 0;
			int sequenceSize = items.size();
			for (Object item : items) {
				messageBuilders.add(this.createBuilder(
						item, incomingSequenceDetails, correlationId, ++sequenceNumber, sequenceSize));
			}
		}
		else if (result.getClass().isArray()) {
			Object[] items = (Object[]) result;
			int sequenceNumber = 0;
			int sequenceSize = items.length;
			for (Object item : items) {
				messageBuilders.add(this.createBuilder(
						item, incomingSequenceDetails, correlationId, ++sequenceNumber, sequenceSize));
			}
		}
		else {
			messageBuilders.add(this.createBuilder(result, incomingSequenceDetails, correlationId, 1, 1));
		}
		return messageBuilders;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private MessageBuilder createBuilder(Object item, List<Object[]> incomingSequenceDetails, Object correlationId,
			int sequenceNumber, int sequenceSize) {
		MessageBuilder builder = (item instanceof Message) ? MessageBuilder.fromMessage((Message) item)
				: MessageBuilder.withPayload(item);
		builder.setCorrelationId(correlationId).setSequenceNumber(sequenceNumber).setSequenceSize(sequenceSize)
				.setHeader(MessageHeaders.ID, UUID.randomUUID());
		if (incomingSequenceDetails != null) {
			builder.setHeader(SEQUENCE_DETAILS, incomingSequenceDetails);
		}
		return builder;
	}

	@Override
	public String getComponentType() {
		return "splitter";
	}

	/**
	 * Subclasses must override this method to split the received Message. The return value may be a Collection or
	 * Array. The individual elements may be Messages, but it is not necessary. If the elements are not Messages, each
	 * will be provided as the payload of a Message. It is also acceptable to return a single Object or Message. In that
	 * case, a single reply Message will be produced.
	 */
	protected abstract Object splitMessage(Message<?> message);

}
