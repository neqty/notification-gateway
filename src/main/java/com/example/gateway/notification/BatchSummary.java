package com.example.gateway.notification;

public record BatchSummary(int total, int successful, int failed) {
    public BatchSummary {
        if (total < 0 || successful < 0 || failed < 0 || total != successful + failed)
            throw new IllegalArgumentException("Inconsistent batch counts");
    }
    @Override public String toString() {
        return "Total: " + total + "\nSuccessful: " + successful + "\nFailed: " + failed;
    }
}
