package com.epam.gym_crm.domain.exception;

public class ErrorMessage {

	private MessageType messageType;

	private String details;

	public ErrorMessage(MessageType messageType, String details) {
		this.messageType = messageType;
		this.details = details;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String prepareErrorMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append(messageType.getMessage());
		if (this.details != null) {
			builder.append(" : " + this.details);
		}

		return builder.toString();

	}
}
