package dev.t1deedyn;

/** Immutable module-setting descriptor. */
public record Setting(String id,String label,double min,double max,double defaultValue) {}
