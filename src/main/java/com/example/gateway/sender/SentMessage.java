package com.example.gateway.sender;

/** Immutable record of a successfully delivered simulated message. */
public record SentMessage(String recipient, String title, String body, String payload) {}
