/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.integration.adapter.mail;

import org.springframework.integration.message.Message;

/**
 * The default implementation of {@link MailHeaderGenerator}. Configures the
 * {@link org.springframework.mail.MailMessage} properties based on attributes
 * provided with known attribute keys as defined in {@link MailAttributeKeys}.
 * 
 * @author Mark Fisher
 */
public class DefaultMailHeaderGenerator extends AbstractMailHeaderGenerator {

	@Override
	protected String getSubject(Message<?> message) {
		return this.retrieveAsString(message, MailAttributeKeys.SUBJECT);
	}

	@Override
	protected String[] getTo(Message<?> message) {
		return this.retrieveAsStringArray(message, MailAttributeKeys.TO);
	}

	@Override
	protected String[] getCc(Message<?> message) {
		return this.retrieveAsStringArray(message, MailAttributeKeys.CC);
	}

	@Override
	protected String[] getBcc(Message<?> message) {
		return this.retrieveAsStringArray(message, MailAttributeKeys.BCC);
	}

	@Override
	protected String getFrom(Message<?> message) {
		return this.retrieveAsString(message, MailAttributeKeys.FROM);
	}

	@Override
	protected String getReplyTo(Message<?> message) {
		return this.retrieveAsString(message, MailAttributeKeys.REPLY_TO);
	}


	private String retrieveAsString(Message<?> message, String key) {
		Object value = message.getHeader().getAttribute(key);
		return (value instanceof String) ? (String) value : null;
	}

	private String[] retrieveAsStringArray(Message<?> message, String key) {
		Object value = message.getHeader().getAttribute(key);
		if (value instanceof String[]) {
			return (String[]) value;
		}
		if (value instanceof String) {
			return new String[] { (String) value };
		}
		return null;
	}

}
